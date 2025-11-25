(ns f.core)

;; more functions in this section:
;; - `dosync` (starts a transaction)
;; - `sync`   (dosync is a wrapper around sync; sync was designed to accept options but as of now, there is no option available)
;; - `alter`
;; - `commute`
;; - `ensure`
;; - `ref-set`
;; - `ref-history-count`
;; - `ref-min-history`
;; - `ref-max-history`
;; - `io!`

;; `ref`, `dosync` and the other functions in this section are the main entry point into Clojure implementation of Software Transactional Memory (or STM for short). 

;; Clojure however takes a lock-free approach to thread coordination with the STM.

;; The STM applies a 100 milliseconds wait period between transactions retries (is not user configurable).
;; There is a hard limit of 10,000 retries before the STM gives up and throws exception.

;; Canonical example of reference coordination in a transaction is modeling the transfer of a sum from a bank account to another.

(def account-1 (ref 1000))
(def account-2 (ref  500))

(type account-1) ; clojure.lang.Ref

;; transfer from account-1 to account-2 (if possible)
(defn transfer [amount]
  (dosync                                           ; start transaction
   (when (pos? (- @account-1 amount))
     (alter account-1 - amount)                     ; multiple refs can coordinate inside the same transaction
     (alter account-2 + amount))
   {:account-1 @account-1,
    :account-2 @account-2}))

(transfer 300)
; {:account-1 700, 
;  :account-2 800)

@account-1 ; 700
@account-2 ; 800

(transfer 1000)
; {:account-1 700,
;  :account-2 800)

@account-1 ; 700
@account-2 ; 800

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ref-history-count, ref-min-history, ref-max-history
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ref instances contain a queue-like storage space that can be used during transactions

(def r (ref "start value"))

(ref-min-history r) ; 0  (default value)
(ref-max-history r) ; 10 (default value)

(let [r  (ref 0)
      T1 (future     ; slow reader
           (dosync
            (println "T1 starts")
            (Thread/sleep 1000)
            (println "T1 history-count" (ref-history-count r))
            @r))
      T2 (future     ; fast writer
           (dosync
            (println "T2 starts")
            (alter r inc)))]
  [@T1 @T2 @r])

; (out) T1 starts
; (out) T2 starts
; (out) T1 history-count 0
; (out) T1 starts             (transaction was restarted)
; (out) T1 history-count 0
; [1 1 1]

;; :min-history determines how many in-transaction values the ref tolerates before producing a read fault
;; :max-history determines the maximum number of values the reference can store before a read fault occurs
;; :min-history = 1 => at least 1 thread is allowed to read a stale value of the ref in case of concurrent access
(let [r  (ref 0 :min-history 1)
      T1 (future     ; slow reader
           (dosync
            (println "T1 starts")
            (Thread/sleep 1000)
            (println "T1 history-count" (ref-history-count r))
            @r))
      T2 (future     ; fast writer
           (dosync
            (println "T2 starts")
            (alter r inc)))]
  [@T1 @T2 @r])

; (out) T1 starts
; (out) T2 starts
; (out) T1 history-count 1
; [0 1 1]

;; ;;;;;;;;;;;;;;;;;
;; alter and ref-set
;; ;;;;;;;;;;;;;;;;;

;; `alter` and `ref-set` mutate a reference inside a transaction
(def r1 (ref 0))
(dosync (alter r1 inc)) ; 1
(dosync (ref-set r1 2)) ; 2

(def op1 (ref 0))
(def op2 (ref 1))
(def result (ref []))

(defn perform []
  (dosync             ; the dosync transaction boundary marks an area of atomic computation that either completes as a whole or not
   (dotimes [i 3]
     (print (format "###-%s-###\n" (hash (Thread/currentThread))))
     (alter op1 inc)
     (alter op2 inc)
     (alter result conj (+ @op1 @op2))
     (print (format "%s + %s = %s (i=%s)\n" @op1 @op2 (+ @op1 @op2) i))
     (Thread/sleep 100))
   @result))

;; without concurrency
(perform)
; (out) ###-1039673971-###
; (out) 1 + 2 = 3 (i=0)
; (out) ###-1039673971-###
; (out) 2 + 3 = 5 (i=1)
; (out) ###-1039673971-###
; (out) 3 + 4 = 7 (i=2)
; [3 5 7]

;; without concurrency
(perform)
; (out) ###-1039673971-###
; (out) 4 + 5 = 9 (i=0)
; (out) ###-1039673971-###
; (out) 5 + 6 = 11 (i=1)
; (out) ###-1039673971-###
; (out) 6 + 7 = 13 (i=2)
; [3 5 7 9 11 13]

;; reset refs
(dosync
 (ref-set op1 0)
 (ref-set op2 1)
 (ref-set result []))

#_{:clj-kondo/ignore [:unused-value]}
;; with concurrency
(let [p1 (future (perform))
      p2 (future (perform))]
  [@p1 @p2] ; makes sure all futures have finished before reading the results
  @result)
; (out) ###-1734726004-###
; (out) ###-1832176266-### (fails; will be restarted)
; (out) 1 + 2 = 3 (i=0)
; (out) ###-1734726004-###
; (out) ###-1832176266-### (fails; will be restarted)
; (out) 2 + 3 = 5 (i=1)
; (out) ###-1734726004-###
; (out) 3 + 4 = 7 (i=2)
; (out) ###-1832176266-### (fails; will be restarted)
; (out) ###-1832176266-###
; (out) 4 + 5 = 9 (i=0)
; (out) ###-1832176266-###
; (out) 5 + 6 = 11 (i=1)
; (out) ###-1832176266-###
; (out) 6 + 7 = 13 (i=2)
; [3 5 7 9 11 13]

;; ;;;;;;;
;; commute
;; ;;;;;;;

;; `commute` is a relaxed form of `alter` that signals the STM that writes operation using 
;; this function can execute in any order (assuming the write operations are commutative).

;; When `commute` is used instead of `alter`, transactions don’t need to restart waiting on 
;; each other results because the computation does not dependent on read order.

(def votes (ref {}))

(defn count-votes [poll votes]
  (future
    (dosync
     (doseq [pref poll]
       (commute votes update pref (fnil inc 0))))))

(defn generate-poll [& preferences]
  (eduction
   (map-indexed #(repeat %2 (str "candidate-" %1)))
   cat
   preferences))

(comment
  (generate-poll 1 2 3))
;; ("candidate-0"
;;  "candidate-1"
;;  "candidate-1"
;;  "candidate-2"
;;  "candidate-2"
;;  "candidate-2")

#_{:clj-kondo/ignore [:unused-value]}
(let [c1 (count-votes (generate-poll 40 64 19 82 11) votes)
      c2 (count-votes (generate-poll 60 36 81 18 89) votes)]
  [@c1 @c2] ; makes sure all futures have finished before reading the results
  @votes)
;; {"candidate-0" 100,
;;  "candidate-1" 100,
;;  "candidate-2" 100,
;;  "candidate-3" 100,
;;  "candidate-4" 100}

;; ;;;;;;
;; ensure
;; ;;;;;;

;; - must be called in a transaction
;; - protects the ref from modification by other transactions
;; - returns the in-transaction-value of ref

(def honeypot-votes
  {"honeypot"    (ref 0)
   "candidate-0" (ref 0)
   "candidate-1" (ref 0)
   "candidate-2" (ref 0)
   "candidate-3" (ref 0)
   "candidate-4" (ref 0)})

#_{:clj-kondo/ignore [:unused-value]}
(defn batch [poll]
  (future
    (dosync
     (ensure (honeypot-votes "honeypot")) ; protects the ref from modification by other transactions
     (doseq [candidate poll
             :while (< @(honeypot-votes "honeypot") 5)]
       (update honeypot-votes candidate commute inc)))))

#_{:clj-kondo/ignore [:redefined-var]}
(defn generate-poll [honeypot & preferences]
  (shuffle
   (concat
    (repeat honeypot "honeypot")
    (eduction
     (map-indexed #(repeat %2 (str "candidate-" %1)))
     cat
     preferences))))

(comment
  (generate-poll 3 1 2 3 4 5))
  ; ["candidate-3"
  ;  "candidate-2"
  ;  "candidate-4"
  ;  "candidate-3"
  ;  "honeypot"
  ;  "candidate-1"
  ;  "candidate-1"
  ;  "candidate-2"
  ;  "candidate-4"
  ;  "candidate-2"
  ;  "honeypot"
  ;  "candidate-4"
  ;  "candidate-0"
  ;  "candidate-4"
  ;  "candidate-3"
  ;  "candidate-4"
  ;  "candidate-3"
  ;  "honeypot"]

#_{:clj-kondo/ignore [:unused-value]}
(let [c1 (batch (generate-poll 3 10 30 20 30 20))
      c2 (batch (generate-poll 5 20 10 10 30 20))]
  [@c1 @c2] ; makes sure all futures have finished before reading the results
  {:total-votes     (reduce + (map deref (vals honeypot-votes)))
   :winner          (ffirst (sort-by (comp deref second) > honeypot-votes))
   :possible-fraud? (= @(honeypot-votes "honeypot") 5)})
;; {:total-votes 91, :winner "candidate-3", :possible-fraud? true}

;; ;;;
;; io!
;; ;;;

;; - arbitrary code needs to signal unsuitability for transactions
;; - we can signal this fact using `io!`
;; - throws an IllegalStateException

#_{:clj-kondo/ignore [:redefined-var]}
(def count-votes (ref 0))

(defn f2 [value]
  (io! (inc value))) ;; this function is unsuitable for transactions

(defn f1 []
  (dosync
   (f2 ; calls f2 which is unsuitable for transactions
    (commute count-votes f2))))

;; not in a transaction
(f2 2) ; 3

(comment
  (f1))
  ; (err) Execution error (IllegalStateException)
  ; (err) I/O in transaction

;; Metadata and Refs

;; functions like `with-meta` don’t work with reference types. But `ref` offers the :meta option.
(def r2 (ref 0 :meta {:create-at :now}))
(meta r2) ; {:create-at :now}

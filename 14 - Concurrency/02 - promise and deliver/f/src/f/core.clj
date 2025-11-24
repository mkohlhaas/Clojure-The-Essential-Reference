(ns f.core)

;; A promise creates a "gate" around an initially empty memory location.
;; The gate protects the content from threads trying to access it.
;; The function deliver atomically writes a value to the location and opens the gate.
(def p1 (promise))

(future (println "Thread 1 got access to" @p1))
(future (println "Thread 2 got access to" @p1))
(future (println "Thread 3 got access to" @p1))

;; delivers the supplied value to the promise
(deliver p1 :location)
; (out) Thread 1 got access to :location
; (out) Thread 2 got access to :location
; (out) Thread 3 got access to :location

;; `promise` creates a callable object (of one argument)
(def p2 (promise))
(future (println "Delivered" @p2))
(ifn? p2)       ; true
(realized? p2)  ; false
(p2 :value)     ; (out) Delivered :value (#<Promise@565064d: :value>)
(realized? p2)  ; true
(p2 :value)     ; nil

;; ;;;;;;;;;;;;;;;;;;;;;;;;;
;; Cigarette Smokers Problem
;; ;;;;;;;;;;;;;;;;;;;;;;;;;

;; `promise` and `future` can be useful for threads coordination

;; we model the problem by using a promise for each ingredient and a future for each player

(def msgs (atom []))

(defn smoke [smoker ingr1 ingr2]
  (swap! msgs conj (str smoker " attempts"))
  (deref ingr1)
  (deref ingr2)
  (swap! msgs conj (str smoker " successful!")))

(defn pick-two [tobacco paper matches]
  (rest
   (shuffle
    [#(deliver tobacco :tobacco)
     #(deliver paper   :paper)
     #(deliver matches :matches)])))

(defn run []
  (dotimes [i 5]
    (swap! msgs conj (str "Round " i))
    (let [tobacco (promise)
          paper   (promise)
          matches (promise)]
      (future (smoke "tobacco holder" paper   matches))
      (future (smoke "paper   holder" tobacco matches))
      (future (smoke "matches holder" tobacco paper))
      (doseq [add (pick-two tobacco paper matches)]
        (add))
      (Thread/sleep 10)))
  @msgs)

;; Only one future/thread succeeds, the other ones keep on running forever. The simulation leaks threads each round.
(partition 5 (run))
;; (("Round 0"
;;   "tobacco holder attempts"
;;   "paper   holder attempts"
;;   "matches holder attempts"
;;   "tobacco holder successful!")
;;  ("Round 1"
;;   "tobacco holder attempts"
;;   "paper   holder attempts"
;;   "matches holder attempts"
;;   "paper   holder successful!")
;;  ("Round 2"
;;   "tobacco holder attempts"
;;   "paper   holder attempts"
;;   "matches holder attempts"
;;   "paper   holder successful!")
;;  ("Round 3"
;;   "tobacco holder attempts"
;;   "paper   holder attempts"
;;   "paper   holder successful!"
;;   "matches holder attempts")
;;  ("Round 4"
;;   "tobacco holder attempts"
;;   "paper   holder attempts"
;;   "tobacco holder successful!"
;;   "matches holder attempts"))

#_{:clj-kondo/ignore [:redefined-var]}
(def msgs (atom []))

#_{:clj-kondo/ignore [:redefined-var]}
;; Using `deref` with a time-out is the solution!
(defn smoke [smoker ingr1 ingr2]
  (let [i1 (deref ingr1 100 "fail!")
        i2 (deref ingr2 100 "fail!")]
    (swap! msgs conj (str smoker " " i1 " " i2))))

(run)
;; ["Round 0"
;;  "paper   holder :tobacco :matches"
;;  "Round 1"
;;  "matches holder :tobacco :paper"
;;  "Round 2"
;;  "matches holder :tobacco :paper"
;;  "Round 3"
;;  "tobacco holder :paper :matches"
;;  "Round 4"
;;  "tobacco holder :paper :matches"]

@msgs
;; ["Round 0"
;;  "paper   holder :tobacco :matches"
;;  "Round 1"
;;  "matches holder :tobacco :paper"
;;  "Round 2"
;;  "matches holder :tobacco :paper"
;;  "Round 3"
;;  "tobacco holder :paper :matches"
;;  "Round 4"
;;  "tobacco holder :paper :matches"
;;  "tobacco holder fail! :matches"
;;  "matches holder :tobacco fail!"
;;  "tobacco holder :paper fail!"
;;  "paper   holder :tobacco fail!"
;;  "paper   holder :tobacco fail!"
;;  "tobacco holder :paper fail!"
;;  "paper   holder fail! :matches"
;;  "matches holder fail! :paper"
;;  "paper   holder fail! :matches"
;;  "matches holder fail! :paper"]

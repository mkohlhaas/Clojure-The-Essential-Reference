(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(distinct  [1 2 1 1 3 2 4 1]) ; (1 2 3 4)
(distinct?  1 2 3 2 4 1)      ; false
(dedupe    [1 2 1 1 3 2 4 1]) ; (1 2 1 3 2 4 1)

(comment
  (apply distinct? []))
  ; (err) Execution error (ArityException) at f.core/eval5166 (form-init1746846153886981767.clj:10).
  ; (err) Wrong number of args (0) passed to: clojure.core/distinct?

(and (seq []) (apply distinct? [])) ; nil

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; Voting System

(def votes
  [{:id 14637 :vote 3 :secs 5}
   {:id 39212 :vote 4 :secs 9}
   {:id 39212 :vote 4 :secs 9}
   {:id 14637 :vote 2 :secs 43}
   {:id 39212 :vote 4 :secs 121}
   {:id 39212 :vote 4 :secs 121}
   {:id 45678 :vote 1 :secs 19}])

(->> votes
     (group-by :id)
     (reduce-kv
      (fn [m user votes]
        (assoc m user (distinct (map :vote votes))))
      {}))
; {14637 (3 2), 39212 (4), 45678 (1)}

; after (group-by :id)
; {14637 [{:id 14637, :vote 3, :secs 5} {:id 14637, :vote 2, :secs 43}],
;  39212 [{:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 121} {:id 39212, :vote 4, :secs 121}],
;  45678 [{:id 45678, :vote 1, :secs 19}]}

(comment
  ;; for user 39212 
  (map :vote [{:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 121} {:id 39212, :vote 4, :secs 121}])              ; (4 4 4 4)
  (distinct (map :vote [{:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 121} {:id 39212, :vote 4, :secs 121}])))  ; (4)

(->> votes
     dedupe ; remove keyboard bounces
     (group-by :id)
     (reduce-kv
      (fn [m user votes]
        (assoc m user (distinct (map :vote votes))))
      {}))
; {14637 (3 2), 39212 (4), 45678 (1)}

(def max-mask-bits 13)

(defn- shift-mask [shift mask hash]
  (-> hash
      (bit-shift-right shift)
      (bit-and mask)))

(defn- maybe-min-hash [hashes]
  (let [mask-bits   (range 1 (inc max-mask-bits))
        shift-bits  (range 0 31)
        masks       (map #(dec (bit-shift-left 1 %)) mask-bits)
        shift-masks (for [mask masks shift shift-bits]
                      [shift mask])]
    (first
     (filter
      (fn [[s m]]
        (apply distinct?
               (map #(shift-mask s m %) hashes)))
      shift-masks))))

(maybe-min-hash
 (map (memfn hashCode) [:a :b :c :d]))
; [1 3]

;; (case op :a "a" :b "b" :c "c" :d "d")

(map #(shift-mask 1 3 %)
     (map (memfn hashCode) [:a :b :c :d]))
; (0 2 1 3)

;; transducers

;; `distinct` as a transducer requires wrapping inside parenthesis
(sequence
 (comp
  (map range) ; (() (0) (0 1) (0 1 2) (0 1 2 3) (0 1 2 3 4) (0 1 2 3 4 5) (0 1 2 3 4 5 6) (0 1 2 3 4 5 6 7) (0 1 2 3 4 5 6 7 8))
  cat         ; (0 0 1 0 1 2 0 1 2 3 0 1 2 3 4 0 1 2 3 4 5 0 1 2 3 4 5 6 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 8)
  (distinct)) ; (0 1 2 3 4 5 6 7 8)
 (range 10))
; (0 1 2 3 4 5 6 7 8)

;; `dedupe` as a transducer requires wrapping inside parenthesis
(sequence
 (dedupe)
 [1 1 1 2 1 1 1 3 1 1])
; (1 2 1 3 1)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Duplicates, Sorting and Sets
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def duplicates [8 1 2 1 1 7 3 3])

(distinct duplicates)      ; (8 1 2 7 3)
(dedupe (sort duplicates)) ; (1 2 3 7 8)

(set duplicates) ; #{7 1 3 2 8}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `distinct` is lazy
(first (distinct (map #(do (print % ",") %) (range)))) ; 0
; (out) 0 ,

;; `dedupe` is semi-lazy and uses chunks of 32 elements
(first (dedupe (map #(do (print % ",") %) (range))))   ; 0
; (out) 0 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,10 ,11 ,12 ,13 ,14 ,15 ,16 ,17 ,18 ,19 ,20 ,21 ,22 ,23 ,24 ,25 ,26 ,27 ,28 ,29 ,30 ,31 ,32 ,

;; n * n elements
(defn with-dupes [n]
  (shuffle
   (into [] (apply concat (take n (repeat (range n)))))))

(comment
  (repeat (range 10))
  ; ((0 1 2 3 4 5 6 7 8 9)
  ;  (0 1 2 3 4 5 6 7 8 9)
  ;  (0 1 2 3 4 5 6 7 8 9)
  ;  (0 1 2 3 4 5 6 7 8 9)
  ;  (0 1 2 3 4 5 6 7 8 9)
  ;  …
  ; )
  (with-dupes 10)) ; 100 elements
  ; [7 2 9 3 6 4 3 6 9 5 
  ;  4 7 3 8 0 9 7 1 7 2 
  ;  7 6 6 0 4 7 2 2 1 9 
  ;  6 2 2 8 9 5 0 5 1 4 
  ;  3 4 3 1 0 1 1 3 3 8 
  ;  9 6 6 0 1 3 9 9 4 5 
  ;  2 4 8 6 5 7 0 1 5 3 
  ;  8 8 4 0 8 7 9 1 1 3 
  ;  2 6 0 0 8 5 0 6 2 5 
  ;  9 5 2 7 7 8 4 5 8 4]

(comment
  (let [c (with-dupes 1000)] ; 1000000 elements
    (quick-bench (doall (distinct c)))            ; (out) Execution time mean : 264.313883 ms
    (quick-bench (doall (dedupe c)))              ; (out) Execution time mean : 235.069753 ms
    (quick-bench (doall (sequence (distinct) c))) ; (out) Execution time mean : 182.916073 ms
    (quick-bench (doall (sequence (dedupe) c))))) ; (out) Execution time mean : 240.849406 ms

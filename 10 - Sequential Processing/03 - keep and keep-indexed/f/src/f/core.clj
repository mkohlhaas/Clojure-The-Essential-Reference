(ns f.core)

;; also removes nil's
(keep first [[1 2] [2] [] nil [0] [2]]) ; (1 2 0 2)

(keep-indexed #(nthnext (repeat %2 %1) %1) [1 3 8 3 4 5 6])  ; ((0) (1 1) (2 2 2 2 2 2))

(comment
  (nthnext (range 10) 2)) ; (2 3 4 5 6 7 8 9)

;; `keep` doesn’t accept multiple collections arguments as `map` does

;; nil's are removed
(sequence (keep #(when (> 0.5 (rand)) %)) (range 20))
; (1 15 18 19)
; (0 5 8 9 10 12 14 15 16 17 19)
; (1 2 5 7 8 9 10 11 13 14 16 17 18 19)
; (0 1 4 8 9 11 12 14 15)
; (1 2 3 5 6 15 19)

;; idiomatically, `keep` is known as a more concise way to express `(remove nil? (map f coll))`

(def dict {1 "one", 2 "two", 3 "three"})

(remove nil? (map dict [5 3 2])) ; ("three" "two")
(keep dict [5 3 2])              ; ("three" "two")

;; Position of an Element in a Sequence using `keep-indexed`

(defn first-index-of [x coll]
  (first (keep-indexed #(when (= %2 x) %1) coll)))

(comment
  (keep-indexed #(when (= %2  2) %1) (list 3 9 1 0 2 3 2))  ; (4 6)
  (keep-indexed #(when (= %2 11) %1) (list 3 9 1 0 2 3 2))) ; ()

(first-index-of  2 (list 3 9 1 0 2 3 2)) ; 4
(first-index-of 11 (list 3 9 1 0 2 3 2)) ; nil

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Extending keep to Multiple Collections
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn keep+ [f & colls]
  (lazy-seq
   (let [ss (map seq colls)]
     (when (every? identity ss)
       (let [x  (apply f (map first ss))
             rs (map rest ss)]
         (if (nil? x)
           (apply keep+ f rs)
           (cons x (apply keep+ f rs))))))))

(keep+ #(and %1 %2 %3)
       [1 2 nil 4]
       [5 nil 7 8]
       (range))
; (0 3)

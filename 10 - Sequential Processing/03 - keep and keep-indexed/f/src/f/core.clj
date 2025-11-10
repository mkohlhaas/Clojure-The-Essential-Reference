(ns f.core)

(keep first [[1 2] [2] [] nil [0] [2]]) ; (1 2 0 2)

(keep-indexed #(nthnext (repeat %2 %1) %1) [1 3 8 3 4 5 6])  ; ((0) (1 1) (2 2 2 2 2 2))

(sequence (keep #(when (> 0.5 (rand)) %)) (range 20)) ; (0 1 2 3 5 8 10 11 17 18)

(def dict {1 "one" 2 "two" 3 "three"})

(remove nil? (map dict [5 3 2])) ; ("three" "two")

(keep dict [5 3 2]) ; ("three" "two")

(defn first-index-of [x coll]
  (first (keep-indexed #(when (= %2 x) %1) coll)))

(first-index-of  2 (list 3 9 1 0 2 3 2)) ; 4
(first-index-of 11 (list 3 9 1 0 2 3 2)) ; nil

(defn keep+ [f & colls]
  (lazy-seq
   (let [ss (map seq colls)]
     (when (every? identity ss)
       (let [x (apply f (map first ss))
             rs (map rest ss)]
         (if (nil? x)
           (apply keep+ f rs)
           (cons x (apply keep+ f rs))))))))

(keep+ #(and %1 %2 %3)
       [1 2 nil 4]
       [5 nil 7 8]
       (range))
; (0 3)

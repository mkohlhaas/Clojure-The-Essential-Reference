(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

(take-nth 3 [0 1 2 3 4 5 6 7 8 9]) ; (0 3 6 9)

(into [] (take-nth 2) (range 10))  ; [0 2 4 6 8]

(defn mult-n [n]
  (rest (take-nth n (range))))

(take 10 (mult-n 11)) ; (11 22 33 44 55 66 77 88 99 110)
(take 10 (mult-n 42)) ; (42 84 126 168 210 252 294 336 378 420)

(defn sparsev [& kv]
  (let [idx   (take-nth 2 kv)
        xs    (take-nth 2 (next kv))
        items (zipmap idx xs)]
    (reduce
     #(conj %1 (items %2 0))
     []
     (range 0 (inc (apply max idx))))))

(sparsev 1 4 3 7 21 8) ; [0 4 0 7 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 8]

(defn drop-nth [n coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (concat (take (dec n) (rest s))
             (drop-nth n (drop n s))))))

(drop-nth 3 (range 10)) ; (1 2 4 5 7 8)

(defn drop-nth2 [n coll]
  (keep-indexed
   #(when-not (zero? (rem %1 n)) %2)
   coll))

(drop-nth2 3 (range 10)) ; (1 2 4 5 7 8)

(defn xdrop-nth [n]
  (keep-indexed
   #(when-not (zero? (rem %1 n)) %2)))

(sequence (xdrop-nth 3) (range 10)) ; (1 2 4 5 7 8)

(comment
  (let [xs (range 1e5)] (quick-bench (last (drop-nth 3 xs))))
  ;; 13.511636 ms

  (let [xs (range 1e5)] (quick-bench (last (drop-nth2 3 xs))))
  ;; 4.586312 ms

  (let [xs (range 1000000)] (quick-bench (last (take-nth 2 xs))))
  ;; Execution time mean : 75.020203 ms

  (let [xs (range 1000000)] (quick-bench (last (sequence (take-nth 2) xs))))
  ;; Execution time mean : 69.482801 ms

  (let [xs (range 1000000)] (quick-bench (reduce + (take-nth 2 xs))))
  ;; Execution time mean : 70.808658 ms

  (let [xs (range 1000000)] (quick-bench (transduce (take-nth 2) + xs))))
  ;; Execution time mean : 45.558123 ms

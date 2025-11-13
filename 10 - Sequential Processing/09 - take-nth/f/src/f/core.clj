(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

;; take every nth element

(take-nth 3 [0 1 2 3 4 5 6 7 8 9]) ; (0 3 6 9)

;; as a transducer
(into [] (take-nth 2) (range 10))  ; [0 2 4 6 8]

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn mult-n [n]
  (rest (take-nth n (range))))

(mult-n 10)           ; (10 20 30 40 50 60 …)
(take 10 (mult-n 11)) ; (11 22 33 44 55 66 77 88 99 110)
(take 10 (mult-n 42)) ; (42 84 126 168 210 252 294 336 378 420)

;; Sparse Vector

;; create a sparse vector that has zeros at every index except for those indicated by the arguments
(defn sparsev [& kv]
  (let [idx   (take-nth 2 kv)
        xs    (take-nth 2 (next kv))
        items (zipmap idx xs)]
    (reduce
     #(conj %1 (items %2 0)) ; default value is 0
     []
     (range 0 (inc (apply max idx))))))

(comment
  ;; generate list of indices
  (range 0 (inc (apply max '(1 4 3 7 21 8))))) ; (0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21)

;; value at index  1 is 4
;; value at index  3 is 7
;; value at index 21 is 8
(sparsev 1 4 3 7 21 8) ; [0 4 0 7 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 8]

;; ;;;;;;;;;;;;;;;;;;;;;;;
;; Implementing `drop-nth`
;; ;;;;;;;;;;;;;;;;;;;;;;;

;; drop every nth element
(defn drop-nth [n coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (concat (take (dec n) (rest s))
             (drop-nth n (drop n s))))))

(drop-nth 3 (range 10)) ; (1 2 4 5 7 8)

;; alternative implementation with `rem`
(defn drop-nth2 [n coll]
  (keep-indexed
   #(when-not (zero? (rem %1 n)) %2)
   coll))

(drop-nth2 3 (range 10)) ; (1 2 4 5 7 8)

;; transducer version of `drop-nth2` (just coll removed)
(defn xdrop-nth [n]
  (keep-indexed
   #(when-not (zero? (rem %1 n)) %2)))

(sequence (xdrop-nth 3) (range 10)) ; (1 2 4 5 7 8)

(comment
  ;; `drop-nth2` is faster
  (let [xs (range 1e5)] (quick-bench (last (drop-nth  3 xs))))               ; (out) Execution time mean : 39.701424 ms
  (let [xs (range 1e5)] (quick-bench (last (drop-nth2 3 xs))))               ; (out) Execution time mean : 15.026512 ms

  ;; transducer version of `take-nth` is (only) slightly faster
  (let [xs (range 1000000)] (quick-bench (last (take-nth 2 xs))))            ; (out) Execution time mean : 176.686150 ms
  (let [xs (range 1000000)] (quick-bench (last (sequence (take-nth 2) xs)))) ; (out) Execution time mean : 150.388611 ms

  ;; in a reducing context the transducer version is much faster
  (let [xs (range 1000000)] (quick-bench (reduce + (take-nth 2 xs))))        ; (out) Execution time mean : 204.270551 ms
  (let [xs (range 1000000)] (quick-bench (transduce (take-nth 2) + xs))))    ; (out) Execution time mean : 100.241353 ms

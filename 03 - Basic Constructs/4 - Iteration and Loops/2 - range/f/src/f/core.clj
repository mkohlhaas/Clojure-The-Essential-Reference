(ns f.core
  (:require
   [clojure.string :as s]
   [criterium.core :refer [quick-bench]]))

(range 10)   ; (0 1 2 3 4 5 6 7 8 9)
(range 1 10) ; (1 2 3 4 5 6 7 8 9)
(range 10 1) ; ()

(range (dec Long/MAX_VALUE) (+' Long/MAX_VALUE 3))
; (9223372036854775806
;  9223372036854775807
;  9223372036854775808N   ; NOTE: auto-promoting longs into bigint
;  9223372036854775809N)

(range 0 20 2)
; (0 2 4 6 8 10 12 14 16 18)

(range -1 -20 -2)
; (-1 -3 -5 -7 -9 -11 -13 -15 -17 -19)

;; range works with any kind of number
(class 0.5) ; java.lang.Double
(range 0.5 5 0.5)
; (0.5 1.0 1.5 2.0 2.5 3.0 3.5 4.0 4.5)

;; Ratios
(class 1/10) ; clojure.lang.Ratio
(range 1 0 -1/10)
; (1 9/10 4/5 7/10 3/5 1/2 2/5 3/10 1/5 1/10)

(map range (range 10))
; (()
;  (0)
;  (0 1)
;  (0 1 2)
;  (0 1 2 3)
;  (0 1 2 3 4)
;  (0 1 2 3 4 5)
;  (0 1 2 3 4 5 6)
;  (0 1 2 3 4 5 6 7)
;  (0 1 2 3 4 5 6 7 8))

;; ;;;;;;;;;;;;;;;;;;;;
;; Pascal-like Triangle
;; ;;;;;;;;;;;;;;;;;;;;

(->> (reverse (range 10))   ; (9 8 7 6 5 4 3 2 1 0)
     (map range (range 10)) ; (0 1 2 3 4 5 6 7 8 9) = (range 10)
     (remove empty?))       ; ((0 1 2 3 4 5 6 7 8) (1 2 3 4 5 6 7) (2 3 4 5 6) (3 4 5) (4))

;; ((0 1 2 3 4 5 6 7 8)
;;    (1 2 3 4 5 6 7)
;;      (2 3 4 5 6)
;;        (3 4 5)
;;          (4))

;; ;;;;;;;;;;
;; Palindrome
;; ;;;;;;;;;;

(defn palindrome? [xs cnt]
  (let [idx (range (quot cnt 2) -1 -1)]
    (every? #(= (nth xs %) (nth xs (- cnt % 1))) idx)))

(defn string-palindrome? [s]
  (let [chars (some->> s
                       s/lower-case
                       (remove (comp s/blank? str)))]
    (palindrome? chars (count chars))))

(string-palindrome? "Was it a car or a cat I saw")
; true

;; ;;;;;;;;;;;;;;;;;;
;; Garbage Collection
;; ;;;;;;;;;;;;;;;;;;

;; `last` is also the final result of evaluating the entire form, the rest of 
;; the sequence can be safely garbage collected as the sequence is processed.

#_{:clj-kondo/ignore [:unused-value]}
(let [r (range 1e7)] (first r) (last r))
; 9999999

(let [r (range 1e7)] (last r))
; 9999999

(comment
  ;; The last operation appears before another operation to access the large 
  ;; sequence. As a result the sequence produces by range needs to remain in 
  ;; memory in full.

  #_{:clj-kondo/ignore [:unused-value]}
  (let [r (range 1e7)] (last r) (first r)))
  ; (err) Execution error (OutOfMemoryError)

;; ;;;;;;;;;;;;
;; Benchmarking
;; ;;;;;;;;;;;;

(comment
  ;; sequential path for `reduce` is selected
  (let [xs (range 1000000)]
    (quick-bench (reduce + (map inc xs))))
  ; (out) Execution time mean : 91.399026 ms

  ;; `transduce` can activate the fast path
  (let [xs (range 1000000)]
    (quick-bench (transduce (map inc) + xs)))
  ; (out) Execution time mean : 75.736634 ms

  ;; k-th coefficient of (x - 1)^n
  ;; using `apply`
  (defn kth1 [n k]
    (/ (apply *' (range n (- n k) -1))
       (apply *' (range k 0 -1))
       (if (and (even? k) (< k n)) -1 1)))

  (quick-bench (kth1 820 6))
  ; (out) Execution time mean : 1.729769 µs

  ;; using `reduce`
  (defn kth2 [n k]
    (/ (reduce *' (range n (- n k) -1))
       (reduce *' (range k 0 -1))
       (if (and (even? k) (< k n)) -1 1)))

  (quick-bench (kth2 820 6)))
  ; (out) Execution time mean : 1.129454 µs

(ns f.core
  (:require
   [criterium.core :refer [bench]]))

;;`to-array` is very similar to `object-array`, but uses a (presumably) faster implementation to transform the input collection.

;; The speed-up is not guaranteed!

;; Prefer `to-array` to create an object array out of a collection,
;; while `object-array` offers the option to create an object array without any input.

(comment
  ;; same performance

  ;; to-array
  (let [v (vec (range 100))] (bench (to-array v)))      ; (out) Execution time mean : 1.405576 µs

  ;; object-array
  (let [v (vec (range 100))] (bench (object-array v)))) ; (out) Execution time mean : 1.575978 µs

;; `into-array` is similar to `to-array`, but will try to guess or force a specific type for the output array
(type (to-array   [1 2 3])) ; java.lang.Object/1
(type (into-array [1 2 3])) ; java.lang.Long/1

(comment
  ;; `into-array` uses the type of the first element to guess an appropriate type for the output array.
  (into-array [1 2 (short 3)]))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) array element type mismatch

;; `into-array` accepts an explicit type parameter
(def a (into-array Short/TYPE [1. 2 (short 3)])) ; [1, 2, 3]

(type a)     ; short/1
(map type a) ; (java.lang.Short java.lang.Short java.lang.Short)

(comment
  (into-array Short/TYPE [Integer/MAX_VALUE]))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Value out of range for short: 2147483647

;; `to-array-2d` is a shortcut to create a two-dimensional object array
(def b (to-array-2d [[1 2] [3 4]])) ; [[1, 2], [3, 4]]

(type b)      ; java.lang.Object/2
(map  type b) ; (java.lang.Object/1 java.lang.Object/1)
(mapv vec  b) ; [[1 2] [3 4]]

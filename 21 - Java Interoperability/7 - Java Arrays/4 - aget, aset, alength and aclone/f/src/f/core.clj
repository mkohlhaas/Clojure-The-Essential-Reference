(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(def a (into-array [:a :b :c])) ; [:a, :b, :c]

(aget a 0)          ; :a
(aset a 0 :changed) ; :changed
a                   ; [:changed :b :c]

;; multi-dimensional arrays ;;

(def matrix (to-array-2d [[0 1 2] [3 4 5] [6 7 8]])) ; [[0, 1, 2], [3, 4, 5], [6, 7, 8]]
(aget matrix 1 1) ; 4

(aset matrix 1 1 42) ; 42
(mapv vec matrix)    ; [[0 1 2] [3 42 5] [6 7 8]]

;; Transpose of a Square Matrix ;;

;; in-place
(defn transpose! [matrix]
  (dotimes [i (alength matrix)]
    (doseq [j (range (inc i) (alength matrix))]
      (let [temp (aget matrix i j)]
        (aset matrix i j (aget matrix j i))
        (aset matrix j i temp)))))

(def matrix1                    ; [[1.0, 2.0, 3.0], [4.0, 5.0, 6.0], [7.0, 8.0, 9.0]]
  (into-array
   (map double-array
        [[1.0  2.0  3.0]
         [4.0  5.0  6.0]
         [7.0  8.0  9.0]])))

(transpose! matrix1) ; nil
(mapv vec matrix1)   ; [[1.0 4.0 7.0]    [2.0 5.0 8.0]    [3.0 6.0 9.0]]
matrix1              ; [[1.0, 4.0, 7.0], [2.0, 5.0, 8.0], [3.0, 6.0, 9.0]]

;; without side-effects
(defn transpose [matrix]
  (let [size (alength matrix)
        output (into-array (map aclone matrix))]
    (dotimes [i size]
      (dotimes [j size]
        (aset output j i (aget matrix i j))))
    output))

(def transposed (transpose matrix1))

(mapv vec transposed) ; [[1.0 2.0 3.0]    [4.0 5.0 6.0]    [7.0 8.0 9.0]]
transposed            ; [[1.0, 2.0, 3.0], [4.0, 5.0, 6.0], [7.0, 8.0, 9.0]]
matrix1               ; [[1.0, 4.0, 7.0], [2.0, 5.0, 8.0], [3.0, 6.0, 9.0]] (still the same)

(comment
  ;; transposing a matrix requires the length of the array

  ;; `alength` is much faster than `count`

  ;; count
  (let [a (int-array (range 1000))] (quick-bench (count   a)))  ; (out) Execution time mean : 49.856526 ns

  ;; alength
  (let [a (int-array (range 1000))] (quick-bench (alength a)))) ; (out) Execution time mean :  1.157053 ns

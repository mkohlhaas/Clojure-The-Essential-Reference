(ns f.core
  (:require
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint       :refer [cl-format]]
   [clojure.set          :refer [union]]
   [criterium.core       :refer [quick-bench]]))

;; arguments have to be of type byte, short, int or long and cannot be nil
;; Clojure always returns 64 bits numbers of type long

;; (bit-not [x])
;;
;; (bit-and [x y & more])
;; (bit-or  [x y & more])
;; (bit-xor [x y & more])
;; (bit-and-not [x y & more])
;;
;; n is the index of a bit in the set (starting from the least significant!)
;; (bit-clear [x n])
;; (bit-set   [x n])
;; (bit-flip  [x n])
;; (bit-test  [x n])
;; (bit-shift-left  [x n])
;; (bit-shift-right [x n])
;; (unsigned-bit-shift-right [x n]) ; there is no unsigned-bit-shift-left because the effect would be exactly the same as bit-shift-left

(comment
  (javadoc 1))

(Long/toBinaryString 201) ; "11001001"
(Long/toBinaryString 198) ; "11000110"
(Long/toBinaryString 192) ; "11000000"

(bit-and 201 198)               ; 192
(bit-and 2r11001001 2r11000110) ; 192

(defn bin [n]
  (cl-format nil "~2,8,'0r" n))

(bin (bit-and 2r11001001
              2r11000110
              2r01011110))
;              "01000000"

(def fourth-bit-set-mask 2r00001000)

(bin (bit-and 2r11001001 fourth-bit-set-mask))
; "00001000"

(bit-test 2r11001001 3)
; true

(def turn-4th-bit-to-zero-mask 2r11110111)

(bin (bit-and 2r11001001 turn-4th-bit-to-zero-mask))
; "11000001"

(bin (bit-clear 2r11001001 3))
; "11000001"

(map bin ((juxt bit-or bit-xor) 2r1 2r1))
; ("00000001" "00000000")

(bin (bit-xor 2r11001001
              2r11001000))
;              "00000001"

(bin (bit-xor 2r11001001
              2r00010001))
;              "11011000"

;; In Java all numerical types are signed so the most significant bit represents the sign.
;; arithmetic shifting => keep the sign
(Integer/toBinaryString +147)                     ; "10010011"
(Integer/toBinaryString (bit-shift-right +147 1)) ;  "1001001"
(Integer/toBinaryString (bit-shift-right +147 2)) ;   "100100"

(Integer/toBinaryString -147)                     ; "11111111111111111111111101101101"
(Integer/toBinaryString (bit-shift-right -147 1)) ; "11111111111111111111111110110110"
(Integer/toBinaryString (bit-shift-right -147 2)) ; "11111111111111111111111111011011"

;; divide by 2 (right shift)
(bit-shift-right -144 1) ; -72
(bit-shift-right -144 2) ; -36

;; multiply by 2 (left shift)
;; sign bit is preserved
(dotimes [i 5]
  (println [(int (* -92337811 (Math/pow 2 i)))
            (Integer/toBinaryString (bit-shift-left -92337811 i))]))
; (out) [-92337811   11111010011111110000100101101101]
; (out) [-184675622  11110100111111100001001011011010]
; (out) [-369351244  11101001111111000010010110110100]
; (out) [-738702488  11010011111110000100101101101000]
; (out) [-1477404976 10100111111100001001011011010000]

(defn right-pad [n]
  (cl-format nil "~64,'0d" n))

;; ignoring the sign bit with unsigned-bit-shift-right
;; called logical shifting
(dotimes [i 5]
  (->> i
       (unsigned-bit-shift-right -22)
       Long/toBinaryString
       right-pad
       println))
; (out) 1111111111111111111111111111111111111111111111111111111111101010
; (out) 0111111111111111111111111111111111111111111111111111111111110101
; (out) 0011111111111111111111111111111111111111111111111111111111111010
; (out) 0001111111111111111111111111111111111111111111111111111111111101
; (out) 0000111111111111111111111111111111111111111111111111111111111110

;; output adjusted with padding zeros
(dotimes [i 8]
  (println (Integer/toBinaryString i)))
; (out) 000
; (out) 001
; (out) 010
; (out) 011
; (out) 100
; (out) 101
; (out) 110
; (out) 111

(defn bit-powerset [coll]
  (let [cnt  (count coll)
        bits (Math/pow 2 cnt)]
    (for [i (range bits)]
      (for [j (range cnt)
            :when (bit-test i j)]
        (nth coll j)))))

(bit-powerset [1 2 3])
; (() (1) (2) (1 2) (3) (1 3) (2 3) (1 2 3))

#_{:clj-kondo/ignore [:type-mismatch]}
(defn powerset [items]
  (reduce
   (fn [s x] (union s (map #(conj % x) s)))
   (hash-set #{})
   items))

(powerset [1 2 3])
; #{#{} #{3} #{2} #{1} #{1 3 2} #{1 3} #{1 2} #{3 2}}

(comment
  (let [s (vec (range 10))] (quick-bench (powerset s)))
  ; (out) Execution time mean : 1.678344 ms

  (let [s (vec (range 10))] (quick-bench (doall (bit-powerset s)))))
  ; (out) Execution time mean : 226.597715 µs

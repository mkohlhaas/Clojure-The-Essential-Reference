(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

(quot 38 4) ; 9
(rem  38 4) ; 2

(mod -38 4) ;  2
(rem -38 4) ; -2

(quot 38. 4) ; 9.0

(defn optimal-size [n m]
  (let [size  (quot n m)
        left? (zero? (rem n m))]
    (if left?
      size
      (inc size))))

(optimal-size 900 22) ; 41

(partition-all (optimal-size 900 22) (range 900))

(def alpha
  ["a" "b" "c" "d"
   "e" "f" "g" "h"
   "i" "j" "k" "l"
   "m" "n" "o" "p"
   "q" "r" "s" "t"
   "u" "v" "w" "x"
   "y" "z"])

(defn ++ [c]
  (-> (.indexOf alpha c)
      inc
      (mod 26)
      alpha))

(++ "a") ; "b"
(++ "z") ; "a"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (let [num 100 div 10] (quick-bench (rem num div)))
  ; (out) Execution time mean : 2.901960 ns

  (let [num (int 100) div (int 10)] (quick-bench (unchecked-remainder-int num div))))
  ; (out) Execution time mean : 0.086392 ns

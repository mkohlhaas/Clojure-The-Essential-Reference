(ns f.core)

(unchecked-add 1 2)       ; 3
(unchecked-subtract 2 38) ; -36
(unchecked-multiply 10 3) ; 30
(unchecked-inc 100)       ; 101
(unchecked-dec 12)        ; 11
(unchecked-negate 1)      ; -1

[(long (- (Math/pow 2 63))) (long (Math/pow 2 63))]  ; [-9223372036854775808 9223372036854775807]

(unchecked-inc (long (Math/pow 2 63)))      ; -9223372036854775808
(unchecked-dec (long (- (Math/pow 2 63))))  ;  9223372036854775807

;; ;;;;;;;
;; Hashing
;; ;;;;;;;

(defn scramble [^long x ^long y]
  (unchecked-add (unchecked-multiply 31 x) y))

(defn hash-str [s]
  (let [large-prime 1125899906842597]
    (reduce scramble large-prime (map int s))))

(hash-str "couple words")          ; 1664082230529263278
(hash-str "even longer sentences") ; -7674745620208396614

(double (- 10 1/3))  ; 9.666666666666668

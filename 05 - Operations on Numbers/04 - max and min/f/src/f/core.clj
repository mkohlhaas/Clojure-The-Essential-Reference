(ns f.core)

(max 5 7 3 7)   ;  7
(min -18 4 -12) ; -18

(max Long/MAX_VALUE 5 (/  1.0 0)) ; ##Inf
(min 1 Long/MIN_VALUE (/ -1.0 0)) ; ##-Inf
(max (/ 1.0 0) (/ 0.0 0))         ; ##NaN

(apply (juxt min max) (range 20)) ; [0 19]

(defn tracker []
  (let [times (atom [])]
    (fn [t]
      (swap! times conj t)
      (apply min @times))))

(def timer (tracker))

(timer 37.21) ; 37.21
(timer 38.34) ; 37.21
(timer 36.44) ; 36.44
(timer 37.21) ; 36.44

(def ∞ (/ 1. 0))

(/ 0. 0)       ; ##NaN
(/ ∞ ∞)        ; ##NaN
(* 0 ∞)        ; ##NaN
(- ∞ ∞)        ; ##NaN
(Math/pow 1 ∞) ; ##NaN
(Math/sqrt -1) ; ##NaN

;; NaN is a number! Hihi!
(number? Double/NaN)          ; true
(inc Double/NaN)              ; ##NaN
(instance? Double Double/NaN) ; true

;; NaN is not equal to itself
(== Double/NaN Double/NaN)    ; false

(comment
  (let [s (apply sorted-set (range 100000))] (time (apply min s)))
  ; (out) "Elapsed time: 27.149727 msecs"

  (let [s (apply sorted-set (range 1000000))] (time (apply min s)))
  ; (out) "Elapsed time: 267.636806 msecs"

  (let [s (apply sorted-set (range 10000000))] (time (apply min s))))
  ; (out) "Elapsed time: 2526.019319 msecs"

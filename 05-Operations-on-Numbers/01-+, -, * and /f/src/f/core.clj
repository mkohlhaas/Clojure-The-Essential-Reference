(ns f.core)

(def empty-coll [])

(+)   ; 0
(*)   ; 1
(- 2) ; -2
(/ 5) ; 1/5

(apply + empty-coll) ; 0
(apply * empty-coll) ; 1

;; ;;;;;;;;;;;;
;; Riemann Zeta
;; ;;;;;;;;;;;;

(defn x-power-of-y [x y] (reduce * (repeat y x)))

(def square #(x-power-of-y % 2))
(def cube   #(x-power-of-y % 3))

(defn reciprocal-of [f]
  (->> (range)     ; (0 1 2 3 4 …)
       (map f)     ; (0 1 4 9 16 25 36 49 64 81 100 121 144 …)
       rest        ; (1 4 9 16 25 36 49 64 81 100 121 144 …)
       (map /)))   ; (1 1/4 1/9 1/16 1/25 1/36 1/49 1/64 1/81 1/100 1/121)

(comment
  (reciprocal-of square)) ; (1 1/4 1/9 1/16 1/25 1/36 1/49 1/64 1/81 1/100 1/121 …)

(defn riemann-zeta [f n]
  (->> f
       reciprocal-of
       (take n)
       (reduce +)
       float))

(/ (* Math/PI Math/PI) 6)  ; 1.6449340668482264
(riemann-zeta square 1000) ; 1.6439346
(riemann-zeta cube   100)  ; 1.2020074

;; ;;;;;;;;;;;;;;;;;;;;
;; Annual Interest Rate
;; ;;;;;;;;;;;;;;;;;;;;

(defn powers-of [n]
  (iterate (partial * n) 1))

(comment
  (take 10 (powers-of 2))  ; (1 2 4 8 16 32 64 128 256 512)
  (take 10 (powers-of 3))) ; (1 3 9 27 81 243 729 2187 6561 19683)

(defn interest-at [rate initial year]
  (->> (powers-of (inc rate))
       (map (partial * initial))
       (take year)
       last))

(interest-at 0.2 1000 4) ; 1728.0

(comment
  (take 7 (iterate #(* % %) 2)))
  ; (err) Error printing return value (ArithmeticException)
  ; (err) long overflow

;; auto-promoting (to Big Integer/N)
(take 7 (iterate #(*' % %) 2)) ; (2 4 16 256 65536 4294967296 18446744073709551616N)

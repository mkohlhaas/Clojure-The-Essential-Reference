(ns f.core)

(def empty-coll [])

(+) ; 0
(*) ; 1

(apply + empty-coll) ; 0
(apply * empty-coll) ; 1

(defn x-power-of-y [x y] (reduce * (repeat y x)))

(def square #(x-power-of-y % 2))
(def cube   #(x-power-of-y % 3))

(defn reciprocal-of [f]
  (->> (range)
       (map f)
       rest
       (map /)))

(defn riemann-zeta [f n]
  (->> f
       reciprocal-of
       (take n)
       (reduce +)
       float))

(/ (* Math/PI Math/PI) 6)  ; 1.6449340668482264
(riemann-zeta square 1000) ; 1.6439346
(riemann-zeta cube 100)    ; 1.2020074

(defn powers-of [n]
  (iterate (partial * n) 1))

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

(take 7 (iterate #(*' % %) 2)) ; (2 4 16 256 65536 4294967296 18446744073709551616N)

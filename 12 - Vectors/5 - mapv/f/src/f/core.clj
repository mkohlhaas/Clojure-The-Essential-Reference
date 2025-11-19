(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(mapv inc [0 1 2 3]) ; [1 2 3 4]

(mapv hash-map [:a :b :c] (range)) ; [{:a 0} {:b 1} {:c 2}]

(vec (remove odd? (range 10)))  ; [0 2 4 6 8]

(comment
  (let [r (range 100)]
    (quick-bench (vec (remove odd? r)))))
  ; (out) Execution time mean : 7.568251 µs

(into [] (remove odd?) (range 10)) ; [0 2 4 6 8]

(comment
  (let [r (range 100)]
    (quick-bench (into [] (remove odd?) r))))
  ; (out) Execution time mean : 5.400792 µs

(defn create-vector-fn [f]
  (fn [a & b] (apply mapv f a b)))

(def add (create-vector-fn +))

(add [1 2] [3 4]) ; [4 6]

(def subtract (create-vector-fn -))

(subtract [2 7 3] [5 4 1]) ; [-3 3 2]

(defn scalar-multiply [c a]
  (mapv (partial * c) a))

(scalar-multiply 3 [1 2 3]) ; [3 6 9]

(defn dot-product [a b]
  (reduce + (map * a b)))

(dot-product [1 1 0] [0 0 1]) ; 0

(comment
  (let [r (range 10000)] (quick-bench (into [] (map inc r))))
  ; (out) Execution time mean : 598.160247 µs

  (let [r (range 10000)] (quick-bench (mapv inc r)))
  ; (out) Execution time mean : 387.356748 µs

  (let [r (range 10000)] (quick-bench (into [] (map inc) r)))
  ; (out) Execution time mean : 439.725299 µs

  (let [r (range 10000)] (quick-bench (subvec (mapv inc r) 0 10)))
  ; (out) Execution time mean : 381.264943 µs

  (let [r (range 10000)] (quick-bench (vec (take 10 (map inc r)))))
  ; (out) Execution time mean : 3.625005 µs

  (let [r (range 10000)] (quick-bench (into [] (map + r r))))
  ; (out) Execution time mean : 3.527497 ms

  (let [r (range 10000)] (quick-bench (mapv + r r)))
  ; (out) Execution time mean : 3.566834 ms

  (defn mapv+ [f c1 c2]
    (let [cnt (dec (min (count c1) (count c2)))]
      (loop [idx 0
             res (transient [])]
        (if (< cnt idx)
          (persistent! res)
          (recur (+ 1 idx) (conj! res (f (nth c1 idx) (nth c2 idx))))))))

  (let [r (vec (range 10000))] (quick-bench (mapv+ + r r))))
  ; (out) Execution time mean : 1.035004 ms

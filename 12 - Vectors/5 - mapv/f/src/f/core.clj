(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

;; `mapv` offers same interface as `map` but returs a vector
;; `mapv` doesn't produce a transducer

(mapv inc  [0 1 2 3])              ; [1 2 3 4]
(mapv inc '(0 1 2 3))              ; [1 2 3 4]
(mapv hash-map [:a :b :c] (range)) ; [{:a 0} {:b 1} {:c 2}]

(type (map  inc [0 1 2 3])) ; clojure.lang.LazySeq
(type (mapv inc [0 1 2 3])) ; clojure.lang.PersistentVector

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Where is my "removev" function?
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; the only sequence functions with vectors versions are `mapv` and `filterv`

(comment
  ;; instead of `removev` write a transducer version

  ;; sequence version
  (vec (remove odd? (range 10)))               ; [0 2 4 6 8]
  (let [r (range 100)]                         ; (out) Execution time mean : 7.568251 µs
    (quick-bench (vec (remove odd? r))))

  ;; transducer version
  (into [] (remove odd?) (range 10))           ; [0 2 4 6 8]
  (let [r (range 100)]                         ; (out) Execution time mean : 5.400792 µs
    (quick-bench (into [] (remove odd?) r))))

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn vector-fn [f]
  (fn [a & b] (apply mapv f a b)))

(def add-vectors      (vector-fn +))
(def subtract-vectors (vector-fn -))

(add-vectors      [1 2] [3 4])     ; [4 6]
(subtract-vectors [1 2] [3 4])     ; [-2 -2]
(subtract-vectors [2 7 3] [5 4 1]) ; [-3 3 2]

(defn scalar-multiply [c a]
  (mapv (partial * c) a))

(scalar-multiply 3 [1 2 3]) ; [3 6 9]

;; returns a scalar (no `mapv`)
(defn dot-product [a b]
  (reduce + (map * a b)))

(dot-product [1 1 0] [0 0 1]) ; 0 (perpendicular)
(dot-product [1 2 3] [4 5 6]) ; 32 (+ 4 10 18)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; transient mapv
(defn mapv+ [f c1 c2]
  (let [cnt (dec (min (count c1) (count c2)))]
    (loop [idx 0
           res (transient [])]
      (if (< cnt idx)
        (persistent! res)
        (recur (+ 1 idx)
               (conj! res (f (nth c1 idx) (nth c2 idx))))))))

(comment
  ;; inc with map
  (let [r (range 10000)] (quick-bench (into [] (map inc r))))
  ; (out) Execution time mean : 598.160247 µs

  ;; inc with mapv
  (let [r (range 10000)] (quick-bench (mapv inc r)))
  ; (out) Execution time mean : 387.356748 µs

  ;; inc with map and into
  (let [r (range 10000)] (quick-bench (into [] (map inc) r)))
  ; (out) Execution time mean : 439.725299 µs

  ;; subtract with mapv
  (let [r (range 10000)] (quick-bench (subvec (mapv inc r) 0 10)))
  ; (out) Execution time mean : 381.264943 µs

  ;; map
  (let [r (range 10000)] (quick-bench (vec (take 10 (map inc r)))))
  ; (out) Execution time mean : 3.625005 µs

  ;; map and into
  (let [r (range 10000)] (quick-bench (into [] (map + r r))))
  ; (out) Execution time mean : 3.527497 ms

  ;; mapv
  (let [r (range 10000)] (quick-bench (mapv + r r)))
  ; (out) Execution time mean : 3.566834 ms

  ;; transient mapv
  (let [r (vec (range 10000))] (quick-bench (mapv+ + r r))))
  ; (out) Execution time mean : 1.035004 ms

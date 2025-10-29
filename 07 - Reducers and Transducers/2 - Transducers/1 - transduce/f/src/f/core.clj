(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

(reduce + (map inc (filter odd? (range 10))))           ; 30
(transduce (comp (filter odd?) (map inc)) + (range 10)) ; 30

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn egypt-mult [x y]
  (->> (map vector
            (iterate #(quot % 2) x)
            (iterate #(* % 2) y))
       (take-while #(pos? (first %))) ; ([640 10] [320 20] [160 40] [80 80] [40 160] [20 320] [10 640] [5 1280] [2 2560] [1 5120])
       (filter #(odd? (first %)))     ; ([5 1280] [1 5120])
       (map second)                   ; (1280 5120)
       (reduce +)))                   ; 6400

(egypt-mult 640 10)
; 6400

#_{:clj-kondo/ignore [:redefined-var]}
;; transduce doesn't support multiple input collections
(defn egypt-mult [x y]
  (transduce
   (comp
    (take-while #(pos? (first %)))
    (filter #(odd? (first %)))
    (map second))
   +
   (map vector
        (iterate #(quot % 2) x)
        (iterate #(* % 2) y))))

(egypt-mult 640 10)
; 6400

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Create a Custom Transducer
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

#_{:clj-kondo/ignore [:redefined-var]}
;; now completely transducible
(defn egypt-mult [x y]
  (->> (interleave
        (iterate #(quot % 2) x)
        (iterate #(* % 2) y))
       (partition-all 2)
       (take-while #(pos? (first %))) ; ((640 10) (320 20) (160 40) (80 80) (40 160) (20 320) (10 640) (5 1280) (2 2560) (1 5120))
       (filter #(odd? (first %)))     ; ((5 1280) (1 5120))
       (map second)                   ; (1280 5120)
       (reduce +)))                   ; 6400

(egypt-mult 640 10)
; 6400

(defn interleave-xform [coll]
  (fn [rf]
    (let [fillers (volatile! (seq coll))]
      (fn
        ([] (rf))
        ([result] (rf result))
        ([result input]
         (if-let [[filler] @fillers]
           (let [step (rf result input)]
             (if (reduced? step)
               step
               (do
                 (vswap! fillers next)
                 (rf step filler))))
           (reduced result)))))))

#_{:clj-kondo/ignore [:redefined-var]}
(defn egypt-mult [x y]
  (transduce
   (comp
    (interleave-xform (iterate #(* % 2) y))
    (partition-all 2)
    (take-while #(pos? (first %)))
    (filter #(odd? (first %)))
    (map second))
   +
   (iterate #(quot % 2) x)))

(egypt-mult 640 10) ; 6400
(egypt-mult 4 5)    ; 20

(comment
  (let [coll (into [] (range 1000))] ; <1>
    (quick-bench (reduce + (filter odd? (map inc coll)))))
  ; (out) Execution time mean : 113.780227 µs

  (let [coll (into [] (range 1000))] ; <2>
    (quick-bench (transduce (comp (map inc) (filter odd?)) + coll))))
  ; (out) Execution time mean : 79.708522 µs

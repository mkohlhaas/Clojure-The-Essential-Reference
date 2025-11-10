(ns f.core
  (:require [criterium.core :refer [bench]]))

(map    range [1 5 10]) ; ((0) (0 1 2 3 4) (0 1 2 3 4 5 6 7 8 9))
(mapcat range [1 5 10]) ; (0 0 1 2 3 4 0 1 2 3 4 5 6 7 8 9)

(sequence (mapcat repeat) [1 2 3] ["!" "?" "$"])  ; ("!" "?" "?" "$" "$" "$")

(def hex?
  (set (sequence
        (comp
         (mapcat range)
         (map char))
        [48 65]
        [58 71])))

(every? hex? "CAFEBABE") ; true

(def libs {:async        [:analyzer.jvm]
           :analyzer.jvm [:memoize :analyzer :reader :asm]
           :memoize      [:cache]
           :cache        [:priority-map]
           :priority-map []
           :asm          []})

(mapcat libs (:analyzer.jvm libs)) ; (:cache)

(defn tp-sort [deps k]
  (loop [res () ks [k]]
    (if (empty? ks)
      (butlast res)
      (recur (apply conj res ks)
             (mapcat deps ks)))))

(tp-sort libs :async) ; (:priority-map :cache :asm :reader :analyzer :memoize :analyzer.jvm)

(def a-1 (mapcat range (map #(do (print ".") %) (into () (range 10)))))
; (out) ....

a-1 ; (0 1 2 3 4 5 6 7 8 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6. 0 1 2 3 4 5. 0 1 2 3 4. 0 1 2 3. 0 1 2. 0 1. 0)

(defn mapcat* [f & colls]
  (letfn [(step [colls]
            (lazy-seq
             (when-first [c colls]
               (concat c (step (rest colls))))))]
    (step (apply map f colls))))

(def a-2 (mapcat* range (map #(do (print ".") %) (into () (range 10)))))

a-2 ; (0 1 2 3 4 5 6 7 8 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 0 1 2 3 4 5 0 1 2 3 4 0 1 2 3 0 1 2 0 1 0)

(comment
  (let [xs (range 1000)] (bench (last (mapcat range xs))))            ; 18ms

  (let [xs (range 1000)] (bench (last (sequence (mapcat range) xs)))) ; 48ms
  (let [xs (range 1000)] (bench (last (eduction (mapcat range) xs)))) ; 48ms

  (let [xs (range 1000)] (bench (reduce + (mapcat range xs))))        ; 8.5ms
  (let [xs (range 1000)] (bench (transduce (mapcat range) + xs)))     ; 6.9ms

  (let [xs (range 1000)] (bench (into [] (mapcat range) xs))))        ; 10.4ms

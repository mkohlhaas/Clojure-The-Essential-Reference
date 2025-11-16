(ns f.core
  (:require
   [criterium.core :refer [quick-bench]])
  (:import
   [clojure.lang PersistentTreeMap]
   [java.util    TreeMap]))

(sorted-map :c 3 :b 2 :a 1)        ; {:a 1, :b 2, :c 3}
(type (sorted-map :c 3 :b 2 :a 1)) ; clojure.lang.PersistentTreeMap

(sorted-map-by
 #(< (:age %1) (:age %2))
 {:age 35} ["J" "K"]
 {:age 13} ["Q" "R"]
 {:age 14} ["T" "V"])
; {{:age 13} ["Q" "R"], 
;  {:age 14} ["T" "V"], 
;  {:age 35} ["J" "K"]

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn timed [s]
  (let [t (System/nanoTime)]
    (println "key" s "created at time" t)
    (with-meta s {:created-at t})))

(def m
  (sorted-map (timed 'a) 1 (timed 'a) 2))
; (out) key a created at time 11024338970002
; (out) key a created at time 11024339568799

m  ; {a 2}

(meta (ffirst m)) ; {:created-at 11024338970002}

;; Priority Queues & A* Algorithm

(sorted-map-by
 compare
 [5.5 "point 3"] [["origin"] 2.5]
 [4.5 "point 2"] [["origin"] 1.5])
; {[4.5 "point 2"] [["origin"] 1.5], 
;  [5.5 "point 3"] [["origin"] 2.5])

(def my-graph
  {:orig [{:a 1.5 :d 2}      0]
   :a    [{:orig 1.5 :b 2}   4]
   :b    [{:a 2 :c 3}        2]
   :c    [{:b 3 :dest 4}     4]
   :d    [{:orig 2 :e 3}   4.5]
   :e    [{:dest 2 :d 3}     2]
   :dest [{:c 4 :e 2}        0]})

(defn discover [graph node path visited]
  (let [walkable (first (graph node))
        seen     (map last (keys visited))]
    (reduce dissoc walkable (conj seen (last path)))))

(defn walk [graph visited dest]
  (loop [visited visited]
    (let [[[_score node :as current] [path total-distance]] (first #p visited)]
      (if (= dest node)
        (conj path dest)
        (recur
         (reduce-kv
          (fn [m neighbour partial-distance]
            (let [d     (+ total-distance partial-distance)
                  score (+ d (last (graph neighbour)))]
              (assoc m [score neighbour] [(conj path node) d])))
          (dissoc visited current)
          (discover graph node path visited)))))))

(defn a* [graph orig dest]
  (walk graph (sorted-map-by compare [0 orig] [[] 0]) dest))

(comment
  (sorted-map-by compare [0 :orig] [[] 0]))

(a* my-graph :orig :dest) ; [:orig :d :e :dest]
(a* my-graph :d    :c)    ; [:d :e :dest :c]

;; Comparators and Uniqueness of Elements

;; flawed custom comparator(!)
(sorted-map-by
 #(compare (count %2) (count %1))
 [:a :b] 1 [:a] 2 [:b] 3)
; {[:a :b] 1, [:a] 3}

(def ordered-by-count
  (sorted-map-by
   #(compare (count %2) (count %1))
   [:a :b] 1 [:a] 2 [:b] 3))
; {[:a :b] 1, [:a] 3}

(assoc ordered-by-count [:x] 4) ; {[:a :b] 1, [:a] 4}

(dissoc ordered-by-count [:x])  ; {[:a :b] 1}

(def flawed-comparator
  #(compare (count %2) (count %1)))

;; The comparator does not work properly because it is used to 
;; 1. check if a key is in the map 
;; 2. decide in which relative order it should appear in the resulting sorted-map
(flawed-comparator [:a] [:x]) ; 0

(def good-comparator
  #(compare [(count %2) %1] [(count %1) %2]))

(good-comparator [:a] [:x]) ; -23

(def ordered-by-count-correct
  (sorted-map-by
   good-comparator
   [:a :b] 1 [:a] 2 [:b] 3))

(assoc  ordered-by-count-correct [:x] 4)              ; {[:a :b] 1, [:a] 2, [:b] 3, [:x] 4}
(dissoc ordered-by-count-correct [:x])                ; {[:a :b] 1, [:a] 2, [:b] 3}
(dissoc (assoc ordered-by-count-correct [:x] 4) [:x]) ; {[:a :b] 1, [:a] 2, [:b] 3}

(comment
  ;; different ways to create a sorted-map with 1000 keys

  (let [pairs (into [] (range 2e3))]                        ; (out) Execution time mean : 1.279825 ms
    (quick-bench (apply sorted-map pairs)))

  (let [pairs (into [] (map-indexed vector (range 1e3)))]   ; (out) Execution time mean : 1.164903 ms
    (quick-bench (into (sorted-map) pairs)))

  ;; mutable Java version
  (let [m (apply hash-map (into [] (range 2e3)))]           ; (out) Execution time mean : 253.548921 µs
    (quick-bench (TreeMap. m)))

  (let [m (apply sorted-map (range 10))]                    ; (out) Execution time mean : 97.977706 ns
    (quick-bench (first m)))

  ;; using Java interop
  (let [m (apply sorted-map (range 10))]                    ; (out) Execution time mean : 10.391083 ns
    (quick-bench (.min ^PersistentTreeMap m))))

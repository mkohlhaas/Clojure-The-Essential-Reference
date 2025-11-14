(ns f.core
  (:require
   [criterium.core :refer [quick-bench]])
  (:import
   [java.util    TreeMap]
   [clojure.lang PersistentTreeMap]))

(sorted-map :c 3 :b 2 :a 1)  ; {:a 1, :b 2, :c 3}

(sorted-map-by
 #(< (:age %1) (:age %2))
 {:age 35} ["J" "K"]
 {:age 13} ["Q" "R"]
 {:age 14} ["T" "V"])
; {{:age 13} ["Q" "R"], {:age 14} ["T" "V"], {:age 35} ["J" "K"]}

(defn timed [s]
  (let [t (System/nanoTime)]
    (println "key" s "created at" t)
    (with-meta s {:created-at t})))

(def m (sorted-map (timed 'a) 1 (timed 'a) 2))
; (out) key a created at 8071209171573
; (out) key a created at 8071209753639

m  ; {a 2}

(meta (ffirst m)) ; {:created-at 8071209171573}

(sorted-map-by
 compare
 [4.5 "point 2"] [["origin"] 1.5]
 [5.5 "point 3"] [["origin"] 2.5])
; {[4.5 "point 2"] [["origin"] 1.5], [5.5 "point 3"] [["origin"] 2.5]}

(def graph
  {:orig [{:a 1.5 :d 2} 0]
   :a    [{:orig 1.5 :b 2} 4]
   :b    [{:a 2 :c 3} 2]
   :c    [{:b 3 :dest 4} 4]
   :dest [{:c 4 :e 2} 0]
   :e    [{:dest 2 :d 3} 2]
   :d    [{:orig 2 :e 3} 4.5]})

(defn discover [node path visited]
  (let [walkable (first (graph node))
        seen (map last (keys visited))]
    (reduce dissoc walkable (conj seen (last path)))))

(defn walk [visited dest]
  (loop [visited visited]
    (let [[[_score node :as current] [path total-distance]] (first visited)]
      (if (= dest node)
        (conj path dest)
        (recur
         (reduce-kv
          (fn [m neighbour partial-distance]
            (let [d (+ total-distance partial-distance)
                  score (+ d (last (graph neighbour)))]
              (assoc m [score neighbour] [(conj path node) d])))
          (dissoc visited current)
          (discover node path visited)))))))

(defn a* [_graph orig dest]
  (walk (sorted-map-by compare [0 orig] [[] 0]) dest))

(a* graph :orig :dest) ; [:orig :d :e :dest]
(a* graph :d    :c)    ; [:d :e :dest :c]

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

(flawed-comparator [:a] [:x]) ; 0

(def good-comparator
  #(compare [(count %2) %1] [(count %1) %2]))

(good-comparator [:a] [:x]) ; -23

#_{:clj-kondo/ignore [:redefined-var]}
(def ordered-by-count
  (sorted-map-by
   #(compare [(count %2) %1] [(count %1) %2])
   [:a :b] 1 [:a] 2 [:b] 3))

(assoc ordered-by-count [:x] 4) ; {[:a :b] 1, [:a] 2, [:b] 3, [:x] 4}

(dissoc ordered-by-count [:x])  ; {[:a :b] 1, [:a] 2, [:b] 3}

(comment
  (let [pairs (into [] (range 2e3))]                        ; (out) Execution time mean : 1.279825 ms
    (quick-bench (apply sorted-map pairs)))

  (let [pairs (into [] (map-indexed vector (range 1e3)))]   ; (out) Execution time mean : 1.164903 ms
    (quick-bench (into (sorted-map) pairs)))

  (let [m (apply hash-map (into [] (range 2e3)))]           ; (out) Execution time mean : 253.548921 µs
    (quick-bench (TreeMap. m)))

  (let [m (apply sorted-map (range 10))]                    ; (out) Execution time mean : 97.977706 ns
    (quick-bench (first m)))

  (let [m (apply sorted-map (range 10))]                    ; (out) Execution time mean : 10.391083 ns
    (quick-bench (.min ^PersistentTreeMap m))))


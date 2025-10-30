(ns f.core
  (:require [criterium.core :refer [quick-benchmark quick-bench]]
            [com.hypirion.clj-xchart :as c])
  (:import clojure.lang.PersistentQueue))

;; `into` conjoins all items of a source collection "from" into a destination collection "to".

(into #{} (range 10)) ; #{0 7 1 4 6 3 2 9 5 8}

(into [:g :x :d] [1 5 9]) ; [:g :x :d 1 5 9]

;; "to" can also be nil and in this case a default list is used as the target collection.
#_{:clj-kondo/ignore [:type-mismatch]}
(into nil (range 10)) ; (9 8 7 6 5 4 3 2 1 0)

 ;; with a transducer chain
(into (vector-of :int)
      (comp (map inc) (filter even?))
      (range 10)) ; [2 4 6 8 10]

;; target is a map, input is some sort of pairs
(into {} [[:a "1"] [:b "2"]]) ; {:a "1", :b "2"}
(into {} [{:a "1"} {:b "2"}]) ; {:a "1", :b "2"}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; output remains the same type of collection as the input (with `empty`)
(defn maintain [fx f coll]
  (into
   (empty coll)  ; create an empty collection of the same type as the input collection
   (fx f coll)))

;; set as input, set as output
(->> #{1 2 3 4 5}
     (maintain map inc)
     (maintain filter odd?))
; #{3 5}

;; map as input, map as output
(->> {:a 1 :b 2 :c 5}
     (maintain filter (comp odd? last)))
; {:a 1, :c 5}

;; `maintain` with a transducer chain

;; simple transducer chain
(def xform
  (comp
   (map dec)
   (drop-while neg?)
   (filter even?)))

;; queue constructor
(defn queue [& items]
  (reduce conj PersistentQueue/EMPTY) items)

;; `maintain` must now provide additional arities for transducers
#_{:clj-kondo/ignore [:redefined-var]}
(defn maintain
  ([fx f coll]
   (into (empty coll) (fx f coll)))
  ([xform coll]
   (into (empty coll) xform coll)))

(def input-queue (queue -10 -9 -8 -5 -2 0 1 3 4 6 8 9))
; (-10 -9 -8 -5 -2 0 1 3 4 6 8 9)

(type input-queue) ; clojure.lang.PersistentVector$ChunkedSeq

;; `maintain` using a transducer
(def transformed-queue
  (maintain xform input-queue))
; (8 2 0)

(type transformed-queue) ; clojure.lang.PersistentList

(peek transformed-queue) ; 8

(into [] transformed-queue) ; [8 2 0]

;; ;;;;;;;;;;;;;;;;;;;
;; `into` and metadata
;; ;;;;;;;;;;;;;;;;;;;

;; add a time-stamp value to a collection 
(def v (with-meta [1 2 3] {:ts (System/currentTimeMillis)}))

(meta v) ; {:ts 1761833155625}

;; `into` keeps metadata of "to" and discards "from" meta data.
(defn sign [c]
  (with-meta c {:signature (apply str c)}))

;; meta keeps from target collection
(meta (into (sign [1 2 3]) (sign (range 10))))
; {:signature "123"}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; extracts the relevant mean execution time from the map of metrics returned by Criterium
  (defmacro b [expr]
    `(first (:mean (quick-benchmark ~expr {}))))

  (defn sample [c]
    (for [n (range 100000 1e6 100000)]
      (b (into c (range n)))))

  ;; list doesn't support transients (therefore much slower)
  ;; x-axis is execution time
  (c/view
   (c/xy-chart
    {"(list)"   [(sample '()) (range 100000 1e6 100000)]
     "(vector)" [(sample [])  (range 100000 1e6 100000)]}))

  ;; merging maps
  (let [maps (map #(apply hash-map  %) (partition-all 10 (range 100)))]
    (quick-bench (apply merge maps)))
  ; (out) Execution time mean : 19.444496 µs

  ;; using `into`
  (let [maps (map #(apply hash-map  %) (partition-all 10 (range 100)))]
    (quick-bench (into {} maps))))
  ; (out) Execution time mean : 18.256418 µs

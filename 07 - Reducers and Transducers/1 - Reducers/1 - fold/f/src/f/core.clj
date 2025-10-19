(ns f.core
  (:require
   [clojure.core.protocols]
   [clojure.core.reducers :as r]
   [criterium.core :refer [quick-bench]])
  (:import
   [java.util.concurrent ConcurrentHashMap]))

(reduce + (into [] (range 1000000))) ; 499999500000
(r/fold + (into [] (range 1000000))) ; 499999500000

;; ;;;;;;;;;;;;;;;;
;; Word Frequencies
;; ;;;;;;;;;;;;;;;;

(defn count-occurrences [coll]
  (r/fold
   (r/monoid #(merge-with + %1 %2) (constantly {}))
   (fn [m [k cnt]] (assoc m k (+ cnt (get m k 0))))
   (r/map #(vector % 1) (into [] coll))))

(defn word-count [s]
  (count-occurrences (.split #"\s+" s)))

(def war-and-peace-book (slurp "https://tinyurl.com/wandpeace"))

(def freqs (word-count war-and-peace-book))

(freqs "Andrew") ; 700

;; ;;;;;;;;;;;
;; By Initials
;; ;;;;;;;;;;;

(defn group-by-initial [freqs]
  (r/fold
   (r/monoid #(merge-with into %1 %2) (constantly {}))
   (fn [m k v]
     (let [c (Character/toLowerCase (first k))]
       (assoc m c (conj (get m c []) v))))
   freqs))

(defn update-values [m f]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(defn avg-by-initial [by-initial]
  (update-values by-initial #(/ (reduce + 0. %) (count %))))

(defn most-frequent-by-initial [freqs]
  (->> freqs
       group-by-initial
       avg-by-initial
       (sort-by second >)
       (take 5)))

(most-frequent-by-initial freqs)
; ([\t 41.06891634980989]
;  [\o 33.68537074148296]
;  [\h 28.92705882352941]
;  [\w 26.61111111111111]
;  [\a 26.54355400696864])

;; ;;;;;;;;;;
;; Leibniz Pi
;; ;;;;;;;;;;

(defn pi "Pi Leibniz formula approx." [n]
  (->> (range)
       (filter odd?)
       (take n)
       (map / (cycle [1 -1]))
       (reduce +)
       (* 4.0)))

(defn large-map [i j]
  (into {} (map vector (range i) (repeat j))))

(defn combinef [init]
  (fn
    ([] init)
    ([m _] m)))

(defn reducef [^java.util.Map m k]
  (doto m
    (.put k (pi (.get m k)))))

(def a-large-map (ConcurrentHashMap. (large-map 100000 100)))

(comment
  (dorun
   (r/fold
    (combinef a-large-map)
    reducef
    a-large-map)))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Don't know how to create ISeq from: clojure.lang.Var$Unbound

;; ;;;;;;;;;;;;;;;;;;
;; IKVReduce Protocol
;; ;;;;;;;;;;;;;;;;;;

(extend-protocol
 clojure.core.protocols/IKVReduce
  java.util.concurrent.ConcurrentHashMap
  (kv-reduce [m f _]
    (reduce (fn [amap [k _v]] (f amap k)) m m)))

(comment
  (time
   (dorun
    (r/fold
     (combinef a-large-map)
     reducef
     a-large-map))))
  ; "Elapsed time: 41113.49182 msecs"

(.get a-large-map 8190)
; 3.131592903558553

(defn foldmap [m n combinef reducef]
  (#'r/foldvec
   (into [] (keys m))
   n
   combinef
   reducef))

(extend-protocol r/CollFold
  java.util.concurrent.ConcurrentHashMap
  (coll-fold
    [m n combinef reducef]
    (foldmap m n combinef reducef)))

(comment
  (time
   (dorun
    (into {}
          (r/fold
           (combinef a-large-map)
           reducef
           a-large-map)))))
  ; "Elapsed time: 430.96208 msecs"

#_{:clj-kondo/ignore [:redefined-var]}
(def a-large-map (large-map 100000 100))

(comment
  (time
   (dorun
    (r/fold
     (r/monoid merge (constantly {}))
     (fn [m k v] (assoc m k (pi v)))
     a-large-map))))
  ; (out) "Elapsed time: 50000.731331 msecs"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (let [not-so-big-data (into [] (range 1000))]
    (quick-bench (reduce + not-so-big-data)))
  ; (out) Execution time mean : 36.608276 µs

  (let [not-so-big-data (into [] (range 1000))]
    (quick-bench (r/fold + not-so-big-data))))
  ; (out) Execution time mean : 80.787929 µs

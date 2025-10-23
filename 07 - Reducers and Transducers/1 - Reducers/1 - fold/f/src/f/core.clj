(ns f.core
  (:require
   [clojure.core.protocols]
   [clojure.core.reducers :as r]
   [criterium.core :refer [quick-bench]])
  (:import
   [java.util.concurrent ConcurrentHashMap]))

(reduce + (into [] (range 1000000))) ; 499999500000
(r/fold + (into [] (range 1000000))) ; 499999500000

;; non-vectors also work
;; But `fold` falls back on sequential reduce.
(reduce + (range 1000000)) ; 499999500000
(r/fold + (range 1000000)) ; 499999500000

;; ;;;;;;;;;;;;;;;;
;; Word Frequencies
;; ;;;;;;;;;;;;;;;;

(defn count-occurrences [words]
  (r/fold
   (r/monoid #(merge-with + %1 %2) (constantly {}))
   (fn [m [word cnt]] (assoc m word (+ cnt (get m word 0))))
   (r/map #(vector % 1) (into [] words))))

(comment
  (.split #"\s+" "The Project Gutenberg EBook of War and Peace, by Leo Tolstoy\r\n\r\nThis eBook is for the use of anyone anywhere at no cost and with")
  ; ["The", "Project", "Gutenberg", "EBook", "of", "War", "and", "Peace,",
  ;  "by", "Leo", "Tolstoy", "This", "eBook", "is", "for", "the", "use",
  ;  "of", "anyone", "anywhere", "at", "no", "cost", "and", "with"]

  (map #(vector % 1) (into [] (.split #"\s+" "The Project Gutenberg EBook of War and Peace, by Leo Tolstoy\r\n\r\nThis eBook is for the use of anyone anywhere at no cost and with")))) ; #object[clojure.core.reducers$folder$reify__12275 0xf63b906 "clojure.core.reducers$folder$reify__12275@f63b906"]
  ; (["The" 1]
  ;  ["Project" 1]
  ;  ["Gutenberg" 1]
  ;  ["EBook" 1]
  ;  ["of" 1]
  ;  ["War" 1]
  ;  ["and" 1]
  ;  ["Peace," 1]
  ;  ["by" 1]
  ;  ["Leo" 1]
  ;  ["Tolstoy" 1]
  ;  ["This" 1]
  ;  ["eBook" 1]
  ;  ["is" 1]
  ;  ["for" 1]
  ;  ["the" 1]
  ;  ["use" 1]
  ;  ["of" 1]
  ;  ["anyone" 1]
  ;  ["anywhere" 1]
  ;  ["at" 1]
  ;  ["no" 1]
  ;  ["cost" 1]
  ;  ["and" 1]
  ;  ["with" 1])

(defn word-count [s]
  (count-occurrences (.split #"\s+" s)))

(def war-and-peace-book (slurp "https://tinyurl.com/wandpeace"))

(def freqs (word-count war-and-peace-book))
;; {"refraining" 3,
;;  "account.”" 1,
;;  "sacrifice," 1,
;;  "merry." 2,
;;  "shouted," 31,
;;  "smartly" 5,
;;  "state.”" 1,
;;  "convince" 10,
;;  "weary," 5,
;;  "“Stretchers!”" 2,
;;  "mounting" 3,
;;  "ashes." 1,
;;  "restatement" 1,
;;  "plows," 1,
;;  …}

(freqs "Andrew") ; 700

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Average Frequency by Initials
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn group-by-initial [freqs]
  (r/fold
   (r/monoid #(merge-with into %1 %2) (constantly {}))
   (fn [m word cnt]
     (let [c (Character/toLowerCase (first word))]
       (assoc m c (conj (get m c []) cnt))))
   freqs))

(comment
  (group-by-initial
   {"refraining" 3
    "account.”" 1,
    "sacrifice," 1,
    "merry." 2,
    "shouted," 31,
    "smartly" 5,
    "state.”" 1,
    "convince" 10,
    "weary," 5,
    "“Stretchers!”" 2,
    "mounting" 3,
    "ashes." 1,
    "restatement" 1,
    "plows," 1,
    "trousseau." 1,
    "crusty" 1,
    "uphold" 1,
    "pledge!”" 1,
    "brigand," 1,
    "gallant..." 1,
    "brother...”" 1,
    "“Forever?”" 2,
    "prince”—he" 1,
    "new," 16,
    "faut," 1,
    "reverberation" 1,
    "transitions" 1}))
; {\a [1 1],
;  \b [1 1],
;  \c [10 1],
;  \f [1],
;  \g [1],
;  \m [2 3],
;  \n [16],
;  \p [1 1 1],
;  \r [3 1 1],
;  \s [1 31 5 1],
;  \t [1 1],
;  \u [1],
;  \w [5],
;  \“ [2 2]}

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

;; ;;;;;;;;;;;;;;;;;;;;
;; Create your own fold
;; ;;;;;;;;;;;;;;;;;;;;

;; for ConcurrentHashMap

(defn pi "Pi Leibniz formula approx." [n]
  (->> (range)
       (filter odd?)           ; (1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33 35 …)
       (take n)
       (map / (cycle [1 -1]))  ; (1 -1/3 1/5 -1/7 1/9 -1/11 1/13 -1/15 1/17 -1/19 1/21 -1/23 1/25 -1/27 1/29 -1/31 1/33 -1/35 …)
       (reduce +)
       (* 4.0)))               ; 3.131592903558553

(comment
  (pi 100)   ; 3.131592903558553
  (pi 500)   ; 3.1395926555897833
  (pi 1000)) ; 3.140592653839793

(defn large-map [i j]
  (into {} (map vector (range i) (repeat j))))

(comment
  (large-map 10 100))
; {0 100, 
;  7 100, 
;  1 100, 
;  4 100, 
;  6 100, 
;  3 100, 
;  2 100, 
;  9 100, 
;  5 100, 
;  8 100))

(def a-large-map (ConcurrentHashMap. (large-map 100000 100)))

(defn combinef [init]
  (fn
    ([] init)
    ([m _] m)))

(defn reducef [^java.util.Map m k]
  (doto m
    (.put k (pi (.get m k)))))

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

;; ;;;;;;;;;;;;;;;;;
;; CollFold Protocol
;; ;;;;;;;;;;;;;;;;;

(defn foldmap [m n combinef reducef]
  (#'r/foldvec
   (into [] (keys m))
   n
   combinef
   reducef))

(extend-protocol r/CollFold
  java.util.concurrent.ConcurrentHashMap
  (coll-fold [m n combinef reducef]
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

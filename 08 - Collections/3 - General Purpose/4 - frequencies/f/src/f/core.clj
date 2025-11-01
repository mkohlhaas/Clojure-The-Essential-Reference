(ns f.core
  (:require
   [clojure.core.reducers :refer [fold]]
   [clojure.string :refer [blank? lower-case lower-case split split split-lines]]))

(frequencies [:a :b :b :c :c :d])
; {:a 1, :b 2, :c 2, :d 1}

(frequencies {:a 1, :b 2, :c 3, :d 4})
; {[:a 1] 1, [:b 2] 1, [:c 3] 1, [:d 4] 1}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(frequencies ['() [] clojure.lang.PersistentQueue/EMPTY])
; {() 3}

(frequencies [(byte 1) (short 1) (int 1) (long 1) 1N])
; {1 5}

(frequencies nil)
; {}

;; ;;;;;;;;;;
;; Word Count
;; ;;;;;;;;;;

(defn freq-used-words [s]
  (->> (split (lower-case s) #"\s+")
       frequencies
       (sort-by last >)
       (take 5)))

(def war-and-peace-book (slurp "https://tinyurl.com/wandpeace"))

(time (freq-used-words war-and-peace-book))
; (out) "Elapsed time: 670.719806 msecs"
; (["the" 34258] ["and" 21396] ["to" 16500] ["of" 14904] ["a" 10388])

;; ;;;;;;;;;;;;;;;;;;;;
;; Parallel Frequencies
;; ;;;;;;;;;;;;;;;;;;;;

(defn reducef [freqs line]
  (if (blank? line)
    freqs
    (let [words (split (lower-case line) #"\s+")]
      (reduce #(update %1 %2 (fnil inc 0)) freqs words))))

(defn combinef
  ([] {})
  ([m1 m2] (merge-with + m1 m2)))

;; word count with reducers
(defn freq-used-words-reducer [s]
  (->> (split-lines s)
       (fold 512 combinef reducef)
       (sort-by last >)
       (take 5)))

;; … and it's slower! Oops!
(time (freq-used-words-reducer war-and-peace-book))
; (out) "Elapsed time: 1001.33593 msecs"
; (["the" 34258] ["and" 21396] ["to" 16500] ["of" 14904] ["a" 10388])

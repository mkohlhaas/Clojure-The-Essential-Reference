(ns f.core
  (:require
   [clojure.java.io :as io])
  (:import
   [java.io File]
   [java.util Calendar]))

(def day-of-year (.get (Calendar/getInstance) (Calendar/DAY_OF_YEAR)))

(drop day-of-year (range 1 366)) ;; (315 316 317 320 … 361 362 363 364 365)

(def hub-sample
  [[401 7  :mar "-0800" :GET 1.1 12846]
   [200 9  :mar "-0800" :GET 1.1 4523]
   [200 2  :mar "-0800" :GET 1.1 6291]
   [401 17 :mar "-0800" :GET 1.1 7352]
   [200 23 :mar "-0800" :GET 1.1 5253]
   [200 7  :mar "-0800" :GET 1.1 11382]
   [400 27 :mar "-0800" :GET 1.1 4924]
   [200 27 :mar "-0800" :GET 1.1 12851]])

(defn error-in-month? [code month]
  (= (>= code 400) (= :mar month)))

(defn process-errors [hub-messages]
  (filter #(let [[code _ month] (take 3 %)]
             (error-in-month? code month))
          hub-messages))

(process-errors hub-sample)
; ([401 7  :mar "-0800" :GET 1.1 12846]
;  [401 17 :mar "-0800" :GET 1.1 7352]
;  [400 27 :mar "-0800" :GET 1.1 4924])

(defn tokenize [pred xs]
  (lazy-seq
   (when-let [ys (seq (drop-while (complement pred) xs))]
     (cons (take-while pred ys)
           (tokenize pred (drop-while pred ys))))))

(def digits '(1 4 1 5 9 2 6 4 3 5 8 9 3 2 6))

(tokenize odd? digits) ; ((1) (1 5 9) (3 5) (9 3))

(transduce (comp (drop 3) (map inc))
           +
           (range 10))
; 49

(defn xs []
  (map
   #(do (print ".") %)
   (iterate inc 0)))

(def take-test (take 10000000 (xs)))
(def time-bomb (drop 10000000 (xs)))

(def eager (take-last 1 (take 10 (xs))))
; (out) ..........

(def lazy-bomb (drop-last (xs)))

(def lazier (nthrest (xs) 3))
; (out) ...

(defn generate-file [id]
  (let [file (File/createTempFile (str "temp" id "-") ".tmp")]
    (with-open [fw (io/writer file)]
      (binding [*out* fw]
        (pr id)
        file))))

(defn fetch-clean [f]
  (let [content (slurp f)]
    (println "Deleting file" (.getName f))
    (io/delete-file f)
    content))

(defn service []
  (let [data (map #(generate-file %) (list 1 2 3 4 5))]
    (nthrest (map fetch-clean data) 2)))

(def consumer (service))
; (out) Deleting file temp1-6837490957826528953.tmp
; (out) Deleting file temp2-12358182479219111105.tmp

consumer ; ("3" "4" "5")

(defn dropv [n v] (subvec v n (count v)))

(dropv 5 (vec (range 10))) ; [5 6 7 8 9]

(defn takev [n v] (subvec v 0 n))

(takev 5 (vec (range 10))) ; [0 1 2 3 4]

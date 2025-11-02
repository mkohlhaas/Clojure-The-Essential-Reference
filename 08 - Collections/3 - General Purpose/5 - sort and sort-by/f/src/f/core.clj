(ns f.core
  (:require
   [clojure.core.reducers :as r]
   [clojure.java.io :as io]))

(sort   [:a :z :h :e :w]) ; (:a :e :h :w :z)
(sort > [18 43 3 0 9])    ; (43 18 9 3 0)

;; sort-by allows to pre-process items or to transform their types before comparison
(sort-by :age [{:age 65} {:age 13} {:age 8}]) ; ({:age 8} {:age 13} {:age 65})
(sort-by  str [:f "s" \c 'u])                 ; (:f \c "s" u)

(comment
  (sort [:f "s" \c 'u]))
  ; (err) Execution error (ClassCastException)

;; sorting happens in-place(!) for native arrays
(let [a (to-array [3 2 1])]
  (sort a)
  (seq a))
; (1 2 3)

;; all functions are also comparators(!!!)
(map #(instance? java.util.Comparator %) [< > <= >= =]) ; (true true true true true)
(map #(instance? java.util.Comparator %) [+ - str prn]) ; (true true true true)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(sort > (range 10)) ; (9 8 7 6 5 4 3 2 1 0)

(sort-by last >= [[1 2] [5 4] [3 4]]) ; ([3 4] [5 4] [1 2])
(sort-by last >  [[1 2] [5 4] [3 4]]) ; ([5 4] [3 4] [1 2])

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Parallel and Lazy Merge-Sort
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `sort` and `sort-by` are eager and load the whole dataset into memory

;; saves sorted collcection (data) to disk and returns file handle
(defn- save-chunk! [data]
  (let [file (java.io.File/createTempFile "mergesort-" ".tmp")]
    (with-open [fw (io/writer file)]
      (binding [*out* fw]
        (pr data)
        file))))

;; protocol for fetching data from some external source
(defprotocol DataProvider
  (fetch-ids [id-range]))

;; Fetch IDs, sorting the resulting data, save the results.
;; Will happen on each thread in parallel.
(defn- process-leaf [id-range sort-fn]
  (-> (fetch-ids id-range)
      sort-fn
      save-chunk!
      vector))

;; from reducer library:
;; (defprotocol CollFold
;;   (coll-fold [coll n combinef reducef]))

;; By extending `CollFold` we can use an `IdRange` type as the last parameter of a `fold` call 
;; and have the call to be routed to our custom implementation.
(defrecord IdRange [from to]
  r/CollFold
  (coll-fold [{:keys [from to] :as id-range} n combinef reducef]
    (if (<= (- to from) n)
      (process-leaf id-range reducef)
      (let [half   (+ from (quot (- to from) 2))
            range1 (IdRange. from half)
            range2 (IdRange. half to)
            fc     (fn [id-range] #(r/fold n combinef reducef id-range))]
        (#'r/fjinvoke
         #(let [f1 (fc range1)
                t2 (#'r/fjtask (fc range2))]
            (#'r/fjfork t2)
            (combinef (f1) (#'r/fjjoin t2)))))))
  DataProvider
  (fetch-ids [id-range]
    (shuffle (range (:from id-range) (:to id-range)))))

;; (extend-type IdRange
;;   DataProvider
;;   (fetch-ids [id-range]
;;     (shuffle (range (:from id-range) (:to id-range)))))

(map (memfn getName) (r/fold concat sort (IdRange. 0 2000)))
; ("mergesort-8993937192991874425.tmp"
;  "mergesort-1866861984855104579.tmp"
;  "mergesort-15610728592950955780.tmp"
;  "mergesort-9189662597637299744.tmp")

;; (defn sort-all
;;   ([colls]
;;    (sort-all compare colls))
;;   ([cmp colls]
;;    (lazy-seq
;;     (when (some identity (map first colls))
;;       (let [[[win & lose] & xs] (sort-by first cmp colls)]
;;         (cons win (sort-all cmp (if lose (conj xs lose) xs))))))))

(defn- load-chunk [fname]
  (read-string (slurp fname)))

;; Too complicated! Filenames are already sorted by content (increasing numbers).
;; (defn psort
;;   ([id-range]
;;    (psort compare id-range))
;;   ([cmp id-range]
;;    (->> (r/fold 10000 concat (partial sort cmp) id-range)
;;         (map load-chunk)
;;         (sort-all cmp))))

(defn psort
  ([id-range]
   (psort compare id-range))
  ([cmp id-range]
   (->> (r/fold 10000 concat (partial sort cmp) id-range)
        (map load-chunk)
        concat
        first)))

(take 20 (psort (IdRange. 0 10000)))
; (0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19)

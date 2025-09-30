(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

;; ;;;;;;;;;;;;;;
;; Sum of Squares
;; ;;;;;;;;;;;;;;

(defn sum-of-squares [n]
  (->> (range n)
       (map #(* % %))
       (reduce +)))

(defn average-of-squares [n]
  (/ (sum-of-squares n) (double n)))

(average-of-squares 10)
; 28.5

(reductions + (map #(* % %) (range 5)))
; (0 1 5 14 30)

(reduce + nil)
; 0

(comment
  (+) ; 0
  ;; (/) ; / is called with 0 args but expects 1, 2 or more [invalid-arity]
  (reduce / [])   ; (err) clojure.lang.ArityException: Wrong number of args (0)
  (reduce / nil)) ; (err) clojure.lang.ArityException: Wrong number of args (0)

;; ;;;;;;;;;;
;; Word Count
;; ;;;;;;;;;;

(defn count-occurrences [coll]
  (->> coll
       (map #(vector % 1))
       (reduce (fn [m [k cnt]]
                 (assoc m k (+ cnt (get m k 0))))
               {})))

(defn word-count1 [s]
  (count-occurrences (.split #"\s+" s)))

(word-count1 "To all things, all men, all of the women and children")
; {"women"    1,
;  "of"       1,
;  "children" 1,
;  "To"       1,
;  "things,"  1,
;  "and"      1,
;  "all"      3,
;  "the"      1,
;  "men,"     1}

(defn word-count2 [s]
  (frequencies (.split #"\s+" s)))

(word-count2 "To all things, all men, all of the women and children")
; {"women"    1,
;  "of"       1,
;  "children" 1,
;  "To"       1,
;  "things,"  1,
;  "and"      1,
;  "all"      3,
;  "the"      1,
;  "men,"     1}

;; ;;;;;;;;
;; Averages
;; ;;;;;;;;

(defn next-average [[cnt sum _avg] x]
  (let [new-cnt (inc cnt)
        new-sum (+ sum x)
        new-avg (/ new-sum (double new-cnt))]
    [new-cnt new-sum new-avg]))

(defn stock-prices [values]
  (reductions next-average [0 0 0] values))

(stock-prices [5.4 3.4 7 8.2 11])
; ([0 0 0]
;  [1 5.4 5.4]
;  [2 8.8 4.4]
;  [3 15.8 5.266666666666667]
;  [4 24.0 6.0]
;  [5 35.0 7.0])

;; ;;;;
;; Fold
;; ;;;;

;; Do you need a right-fold or a left-fold?

(def numbers (cons 1 (cons 2 (cons 3 (cons 4 (list)))))) ; (1 2 3 4)

(defn foldl [f init xs]
  (if (empty? xs)
    init
    (foldl f (f init (first xs)) (rest xs))))

(foldl + 0 numbers)
; 10

(defn foldr [f init xs]
  (if-let [x (first xs)]
    (f x (foldr f init (rest xs)))
    init))

(foldr + 0 numbers)
; 10

(foldl / 1. numbers) ; 0.041666666666666664
(foldr / 1. numbers) ; 0.375

(defn foldr2 [f init xs]
  (reduce (fn [x y] (f y x)) init (reverse xs)))

(foldr2 / 1. numbers)
; 0.375

;; ;;;;;;;
;; reduced
;; ;;;;;;;

; Wraps a value x in a way such that a reduce will terminate with the value x

(reductions
 (fn [acc itm]
   (if (> itm 5)
     (reduced (+ itm acc))
     (+ itm acc)))
 (range 10))
; (0 1 3 6 10 15 21)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (let [xs (range 1e8)] (reduce + xs))      ; 4999999950000000

  (take 10 (reduce merge '() (range 1e8))) ; java.lang.OutOfMemoryError: GC overhead limit exceeded
  ; (99999999
  ;  99999998
  ;  99999997
  ;  99999996
  ;  99999995
  ;  99999994
  ;  99999993
  ;  99999992
  ;  99999991
  ;  99999990)

  #_{:clj-kondo/ignore [:unused-value]}
  (let [xs (range 1e8)] (last xs) (reduce + xs)) ; 4999999950000000
  ; (err) Execution error (OutOfMemoryError) at f.core/eval4137 (form-init16992514233485024568.clj:144).
  ; (err) Java heap space

  (let [xs (range 1000)]
    (quick-bench (last (reductions unchecked-add-int xs))))
  ; (out) Execution time mean : 220.245738 µs

  (let [xs (range 1000)]
    (quick-bench (reduce unchecked-add-int xs))))
  ; (out) Execution time mean : 28.012165 µs

(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

;; ;;;;;;;;;
;; Example 1
;; ;;;;;;;;;

(defn rand-b [n]
  (->> #(rand-int 2)
       (repeatedly n)
       (apply str)))

(rand-b 10)
; "0101010111"

;; ;;;;;;;;;
;; Example 2
;; ;;;;;;;;;

(defn event-stream []
  (interleave (repeatedly (fn [] (System/nanoTime))) (range)))

(apply hash-map (take 8 (event-stream)))
; {394045793253 1, 394045843085 3, 394045819479 2, 394045759686 0}

;; ;;;;;;;;;
;; Example 3
;; ;;;;;;;;;

(def header [:sold :sigma :end])
(def table [[120 3 399] [100 2 242] [130 6 3002]])

(defn totals [table]
  (->> table
       (apply map +)
       (interleave header)))

(totals table)
; (:sold 350 :sigma 11 :end 3643)

;; ;;;;;;;;;;;;
;; Benchmarking
;; ;;;;;;;;;;;;

(comment
  (defn noop [& _args])

  (quick-bench (apply noop 1 2 []))                    ; (out) Execution time mean : 133.007091 ns
  (quick-bench (apply noop 1 2 3 []))                  ; (out) Execution time mean : 158.429014 ns
  (quick-bench (apply noop 1 2 3 4 []))                ; (out) Execution time mean : 285.596583 ns
  (quick-bench (apply noop 1 2 3 4 5 6 []))            ; (out) Execution time mean : 531.457310 ns
  (quick-bench (apply noop 1 2 3 4 5 6 7 8 []))        ; (out) Execution time mean : 764.440759 ns
  (quick-bench (apply noop 1 2 3 4 5 6 7 8 9 10 [])))  ; (out) Execution time mean : 854.096869 ns

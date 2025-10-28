(ns f.core
  (:require [clojure.core.reducers :as r]))

; (monoid [op ctor])

;; `op` must be a function accepting two arguments and is required argument.
;; `ctor` must be a function accepting a zero arguments call and is required argument.

;; Differently from normal reduce, r/fold calls the zero-arguments arity of the reducing function if no initial value is provided.

(r/fold
 (r/monoid str (constantly "Concatenate ")) ; used as reducing function
 ["th" "is " "str" "ing"])
; "Concatenate this string"

(comment
  ;; ArityException or ClassCastException are typically seen when "ctor" is given as a value (e.g. a number, or empty vector) 
  ;; instead of a function of no arguments:
  (r/fold
   (r/monoid + 0)
   (range 10)))
  ; (err) Execution error (ClassCastException)

(r/fold
 (r/monoid + (constantly 0))
 (range 10))
; 45

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; `monoid` is mainly used to build the "reducef" or "combinef" argument for fold. 
(r/fold
 (r/monoid merge (constantly {}))
 (fn [m k v] (assoc m k (str v)))
 (zipmap (range 10) (range 10))) ; {0 0, 7 7, 1 1, 4 4, 6 6, 3 3, 2 2, 9 9, 5 5, 8 8}
; {0 "0", 7 "7", 1 "1", 4 "4", 6 "6", 3 "3", 2 "2", 9 "9", 5 "5", 8 "8"}

;; A monoid in abstract algebra has a neutral element and is associative.

;; neutral/identity element (0)
(+ 99 0)
; 99

#_{:clj-kondo/ignore [:redundant-nested-call]}
;; associative
(= (+ (+ 1 2) 3)
   (+ 1 (+ 2 3)))
; true

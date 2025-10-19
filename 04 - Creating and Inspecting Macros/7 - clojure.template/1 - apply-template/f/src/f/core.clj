(ns f.core
  (:require
   [clojure.template :refer [apply-template]]))

;; The original author of `apply-template` has stated that its inclusion in the Clojure standard library might have been a bad idea.

(apply-template '[x y] '(+ x y x) [1 2])
; (+ 1 2 1)

(apply-template '[x] '(let [x x] x) [1])
; (let [1 1] 1)

(apply-template '[x] '(P (x) ∧ (∃ x Q (x))) '[y])
; (P (y) ∧ (∃ y Q (y)))

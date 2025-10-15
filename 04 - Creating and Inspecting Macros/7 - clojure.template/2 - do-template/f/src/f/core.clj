(ns f.core
  (:require [clojure.template :refer [do-template]]))

;; Same shortcomings as `apply-template`.
;; Better alternative might be [core.unify](https://github.com/clojure/core.unify).

(do-template [x] (println x) 1 2 3)
; (out) 1
; (out) 2
; (out) 3
; nil

(do-template [x] (println '(P (x) ∧ (∃ x Q (x)))) y z)
; (out) (P (y) ∧ (∃ y Q (y)))
; (out) (P (z) ∧ (∃ z Q (z)))
; nil

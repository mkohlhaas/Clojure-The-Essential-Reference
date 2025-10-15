(ns f.prepost
  (:require [clojure.test :refer [is are]]))

(defn save! [item]
  {:pre [(are [x] x
           (map? item)
           (integer? (:mult item))
           (#{:double :triple} (:width item)))]
   :post [(is (= 10 (:id %)))]} ; % is the result of applying `save!`
  (assoc item :id (* (:mult item) 2)))

;; wrong pre-condition
;; no `:single`
(save! {:mult "4" :width :single})
; (out) 
; (out) FAIL in () (form-init8042715344632943510.clj:5)
; (out) expected: (integer? (:mult item))
; (out)   actual: (not (integer? "4"))
; (out) 
; (out) FAIL in () (form-init8042715344632943510.clj:5)
; (err) Execution error (AssertionError) at f.prepost/save! (form-init8042715344632943510.clj:4).
; (err) Assert failed: (are [x] x (map? item) (integer? (:mult item)) (#{:double :triple} (:width item)))
; (out) expected: (#{:double :triple} (:width item))
; (out)   actual: nil

;; wrong post-condition
;; result != 10
(save! {:mult 4 :width :double})
; (out) 
; (out) FAIL in () (form-init8042715344632943510.clj:9)
; (out) expected: (= 10 (:id %))
; (out)   actual: (not (= 10 8))
; (out) -  10
; (out) +  8
; (err) Execution error (AssertionError) at f.prepost/save! (form-init8042715344632943510.clj:4).
; (err) Assert failed: (clojure.test/is (= 10 (:id %)))

;; ✓ pre- and post-conditions
(save! {:mult 5 :width :double}) ; {:mult 5, :width :double, :id 10}

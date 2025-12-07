(ns f.core
  (:require
   [clojure.java.javadoc :refer [javadoc]]))

(def sb (new StringBuffer "init")) ; #object[java.lang.StringBuffer 0x4fc0b37d "init"]

(.append sb " item")

(str sb) ; "init item"

(comment
  (javadoc BigDecimal)

  ;; possible constructors for int and long type (given a Clojure Long)
  ;; as both can be casted without loss of information
  (let [l (Long. 1)]
    (new BigDecimal l))
  ; (err) Syntax error (IllegalArgumentException)
  ; (err) More than one matching method found: java.math.BigDecimal

  (let [l (Long. 1)]
    (type l))      ; java.lang.Long

  (let [l 1]
    (type l)))     ; java.lang.Long

;; passing a primitive long solves the problem(???)
(let [l 1]
  (new BigDecimal l))
; 1M

;; `new` has a recommended short form (`.` after class name)
(StringBuffer. "init") ; #object[java.lang.StringBuffer 0x73b51fff "init"]

(macroexpand '(StringBuffer. "init"))
; (new StringBuffer "init")

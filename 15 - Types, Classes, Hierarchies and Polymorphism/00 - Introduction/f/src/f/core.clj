(ns f.core)

;; Clojure's object oriented programming:
;; - no concrete inheritance
;; - separates inheritance from interface declaration ("a-la carte polymorphism") 

;; ;;;;;;;;;;;;;;;
;; Accessing Types
;; ;;;;;;;;;;;;;;;

(class [])     ; clojure.lang.PersistentVector
(class "")     ; java.lang.String
(class #())    ; f.core$eval4108$fn__4109
(class nil)    ; nil (no class; nil is by definition the absence of an object)
(class {:a 1}) ; clojure.lang.PersistentArrayMap

;; `type` is the same as `class` (except it can read the metadata :type key if available)
(type            {:a 1})                  ; clojure.lang.PersistentArrayMap
(type (with-meta {:a 1} {:type :custom})) ; :custom

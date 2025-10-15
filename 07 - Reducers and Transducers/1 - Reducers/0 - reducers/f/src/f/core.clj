(ns f.core
  (:require [clojure.core.reducers :as r]))

;; To avoid confusion with the same functions in clojure.core, functions are often prefixed with `r/` (which is a conventional alias for clojure.core.reducers).

(def map-inc        (r/map inc))
(def filter-odd     (r/filter odd?))
(def compose-all    (comp map-inc filter-odd))
(def apply-to-input (compose-all (range 10)))

(reduce + apply-to-input) ; 30
(reduce + '(2 4 6 8 10))  ; 30

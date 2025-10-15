(ns f.05
  (:require [f.04 :as m]))

(defn vsum [x xs]
  (map #(m/plus x %) xs))

(vsum 3 [1 2 3]) ; (4 5 6)
;; int plus(Object Object)
;; int plus(Object Object)
;; int plus(Object Object)

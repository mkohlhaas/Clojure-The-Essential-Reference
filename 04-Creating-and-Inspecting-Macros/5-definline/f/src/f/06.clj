(ns f.06
  (:require [f.04 :as m]))

;; with casts
(defn vsum [x xs]
  (map #(m/plus (int x) (int %)) xs))

;; but didn't help
(vsum 3 [1 2 3]) ; (4 5 6)
;; int plus(Object Object)
;; int plus(Object Object)
;; int plus(Object Object)

(ns f.08
  (:require [f.07 :as m]))

;; Now plus is expanded in the place of the invocation, where
;; information about the types are still available for use.
(defn vsum [x xs]
  (map #(m/plus (int x) (int %)) xs))

;; now using the specialized versions
(vsum 3 [1 2 3]) ; (4 5 6)
;; int plus(int int)
;; int plus(int int)
;; int plus(int int)

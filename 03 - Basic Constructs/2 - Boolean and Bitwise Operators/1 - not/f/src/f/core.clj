(ns f.core
  (:require [clojure.string]))

;; In Clojure, the only `false` and `nil` are falsy!

(not true) ; false

;; ;;;;;;;;;
;; Pluralize
;; ;;;;;;;;;

(defn pluralize [s]
  (if (not (clojure.string/blank? s))
    (str s "s")
    s))

(pluralize "flower") ; flowers
(pluralize "")       ; ""
(pluralize "     ")  ; "     "

;; ;;;;;;;
;; Weekend
;; ;;;;;;;

(defn weekend? [day]
  (contains? #{"saturday" "sunday"} day))

(defn weekday? [day]
  (not (weekend? day)))

(weekday? "monday") ; true
(weekend? "sunday") ; true
(weekend? "monday") ; false

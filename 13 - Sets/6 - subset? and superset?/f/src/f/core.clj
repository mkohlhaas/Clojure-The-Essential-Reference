(ns f.core
  (:require
   [clojure.set :as s]))

(s/subset?   #{1 2} #{1 2 3})            ; true
(s/superset? #{:a :b :c} #{:a :c})       ; true
(s/superset? nil #{})                    ; true
(s/superset? #{} nil)                    ; true
(s/subset?   #{0 3} (set [:a :b :c :d])) ; true (using indices)

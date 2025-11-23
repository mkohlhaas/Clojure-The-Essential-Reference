(ns f.core
  (:require
   [clojure.set :as s]))

(s/union        #{1 2 3} #{4 2 6}) ; #{1 4 6 3 2}
(s/difference   #{1 2 3} #{4 2 6}) ; #{1 3}
(s/intersection #{1 2 3} #{4 2 6}) ; #{2}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(s/intersection #{1 2 3} #{} #{4 2 6})     ; #{}
(s/intersection #{1 2 3} nil #{4 2 6})     ; nil
(s/intersection #{1 2 3} nil #{4 2 6} #{}) ; #{}
(s/intersection #{1 2 3} nil #{} #{4 2 6}) ; #{}
(s/intersection #{1 2 3} #{} nil #{4 2 6}) ; nil
(s/intersection #{1 2 3} nil #{})          ; #{}
(s/intersection #{1 2 3} #{} nil)          ; nil

(apply s/intersection (remove nil? [#{1 2 3} nil #{4 2 6}])) ; #{2}

(comment
  (remove nil? [#{1 2 3} nil #{4 2 6}])) ; (#{1 3 2} #{4 6 2})

(defn symmetric-difference [s1 s2]
  (s/difference (s/union s1 s2) (s/intersection s1 s2)))

(symmetric-difference           #{1 2 4} #{1 6 8}) ; #{4 6 2 8}
(symmetric-difference (sorted-set 1 2 4) #{1 6 8}) ; #{2 4 6 8}

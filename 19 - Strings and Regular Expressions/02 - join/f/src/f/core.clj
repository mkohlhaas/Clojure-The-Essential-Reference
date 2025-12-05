(ns f.core
  (:require
   [clojure.string :refer [join]]
   [criterium.core :refer [quick-bench]]))

(join (list "Should " "this " "be " 1 \space 'sentence?)) ; "Should this be 1 sentence?"

(join "," (range 10))                                     ; "0,1,2,3,4,5,6,7,8,9"

(apply str (interpose "," (range 10)))                    ; "0,1,2,3,4,5,6,7,8,9"

(comment
  (interpose "," (range 10)))                             ; (0 "," 1 "," 2 "," 3 "," 4 "," 5 "," 6 "," 7 "," 8 "," 9)

(comment
  ;; `join` is faster

  ;; interpose
  (let [xs (interpose "," (range 10000))] ; (out) Execution time mean : 2.043777 ms
    (quick-bench (apply str xs)))

  ;; join
  (let [xs (range 10000)]                 ; (out) Execution time mean : 1.201130 ms
    (quick-bench (join "," xs))))

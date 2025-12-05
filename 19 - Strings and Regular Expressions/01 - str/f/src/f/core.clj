(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(str "This " 'is \space 1 " sentence")  ; "This is 1 sentence"

(comment
  ;; the default string conversion contains the class of the object and a hexadecimal id
  (str (Object.)))                      ; "java.lang.Object@72890b69"

(str :a 'b 1e8 (Object.) [1 2] {:a 1})  ; ":ab1.0E8java.lang.Object@255a4182[1 2]{:a 1}"

(str    (map inc (range 10)))           ; "clojure.lang.LazySeq@c5d38b66"

(pr-str (map inc (range 10)))           ; "(1 2 3 4 5 6 7 8 9 10)"

;; work-around with `apply` or `reduce`
(apply  str (range 10))                 ; "0123456789"
(reduce str (interpose "," (range 10))) ; "0,1,2,3,4,5,6,7,8,9"

(comment
  (interpose "," (range 10)))           ; (0 "," 1 "," 2 "," 3 "," 4 "," 5 "," 6 "," 7 "," 8 "," 9)

(comment
  ;; `apply` much faster than `reduce` in this case

  ;; `apply` is building up a StringBuilder
  (let [v (vec (range 1000))]
    (quick-bench (apply str v)))   ; (out) Execution time mean :  99.972382 µs

  ;; `reduce` is creating a new StringBuilder every time
  (let [v (vec (range 1000))]
    (quick-bench (reduce str v)))) ; (out) Execution time mean : 677.687133 µs

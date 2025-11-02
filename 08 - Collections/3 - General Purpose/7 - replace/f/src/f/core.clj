(ns f.core
  (:require
   [clojure.string]
   [criterium.core :refer [quick-bench]]))

(replace {:a "a" :b "b"} [:a 1 2 :b 3 4])
; ["a" 1 2 "b" 3 4]

(transduce
 (comp (replace {"0" 0})
       (map inc))
 +
 ["0" 1 2 "0" 10 11])
; 30

(replace [:a :b :c] (range 10))
; (:a :b :c 3 4 5 6 7 8 9)

(defn entry [k v] (clojure.lang.MapEntry/create k v))

(def user {:name "jack" :city "London" :id 123})
(def sub  {(entry :city "London") [:postcode "WD12"]})

(into {} (replace sub user))
; {:name "jack", :postcode "WD12", :id 123}

(def text "You provided the following: user {usr} password {pwd}")
(def sub1 {"{usr}" "'rb075'" "{pwd}" "'xfrDDjsk'"})

(transduce
 (comp
  (replace sub1)
  (interpose " "))
 str
 (clojure.string/split text #"\s"))
; "You provided the following: user 'rb075' password 'xfrDDjsk'"

(comment
  (defn large-map [i] (into {} (map vector (range i) (range i))))

  (def big-map (large-map 2e6))

  (let [v (into [] (range 1e6))]
    (quick-bench (replace {:small "map"} v)) ; (out) Execution time mean : 159.857240 ms
    (quick-bench (replace big-map v)))       ; (out) Execution time mean : 1.125796 sec

  (let [s (range 1e6)
        v (into [] s)]
    (quick-bench (doall (replace {:small "map"} s))) ; (out) Execution time mean : 126.977798 ms
    (quick-bench (replace {:small "map"} v)))        ; (out) Execution time mean : 162.569045 ms

  (let [s (range 1000000)]
    (quick-bench (doall (replace {:small "map"} s)))             ; (out) Execution time mean : 149.604271 ms
    (quick-bench (doall (sequence (replace {:small "map"}) s)))) ; (out) Execution time mean : 232.493099 ms

  (let [s (range 1000000)]
    (quick-bench (doall (map inc s)))              ; (out) Execution time mean : 110.243014 ms
    (quick-bench (doall (sequence (map inc) s))))) ; (out) Execution time mean : 219.240957 ms

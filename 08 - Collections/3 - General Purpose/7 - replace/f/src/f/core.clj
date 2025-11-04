(ns f.core
  (:require
   [clojure.string]
   [criterium.core :refer [quick-bench]]))

;; uses a substitution map (any associative data structure - vectors, maps, …)
(replace {:a "a" :b "b"} [:a 1 2 :b 3 4])
; ["a" 1 2 "b" 3 4]

(transduce
 (comp (replace {"0" 0})
       (map inc))
 +
 ["0" 1 2 "0" 10 11])
; 30

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; vector as substitution map uses the index of the vector to match the element to replace
;; creates this subsitution map: {0 :a 1 :b 2 :c}
(replace [:a :b :c] (range 10))
; (:a :b :c 3 4 5 6 7 8 9)

;; replace key-value pairs in maps (in practice a less common operation)

;; when a map is iterated sequentially, it returns a list of MapEntry objects, which is what we need to match against
(defn entry [k v]
  (clojure.lang.MapEntry/create k v))

(comment
  (entry :city "London")         ; [:city "London"]
  (type (entry :city "London"))) ; clojure.lang.MapEntry

;; replace :city with :postcode (!!!)
(def sub  {(entry :city "London") [:postcode "WD12"]}) ; {[:city "London"] [:postcode "WD12"]}    
(def user {:name "jack" :city "London" :id 123})       ; {:name "jack", :city "London", :id 123}

;; city has been replaced by its postcode
(into {} (replace sub user))
; {:name "jack", :postcode "WD12", :id 123}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Textual Substitution System (Templates)
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def text "You provided the following: user {usr} password {pwd}")
(def sub1 {"{usr}" "'rb075'" "{pwd}" "'xfrDDjsk'"})

(transduce
 (comp
  (replace sub1)
  (interpose " "))
 str
 (clojure.string/split text #"\s")) ; ["You" "provided" "the" "following:" "user" "{usr}" "password" "{pwd}"]
; "You provided the following: user 'rb075' password 'xfrDDjsk'"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (defn large-map [i] (into {} (map vector (range i) (range i))))

  (large-map 10) ; {0 0, 7 7, 1 1, 4 4, 6 6, 3 3, 2 2, 9 9, 5 5, 8 8}

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

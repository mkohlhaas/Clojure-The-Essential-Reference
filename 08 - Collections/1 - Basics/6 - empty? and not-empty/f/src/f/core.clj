(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

(empty? [])      ; true
(empty? [1 2 3]) ; false
(empty? [nil])   ; false

;; `not-empty` is not strictly the opposite of `empty?` as the 
;; missing question mark indicates that `not-empty` returns nil 
;; or "coll" itself to indicate logical true or false.
(not-empty [])      ; nil
(not-empty [1 2 3]) ; [1 2 3]

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; nil, empty string are empty
(remove empty? [nil "a" "" "nil" "" "b"])  ; ("a" "nil" "b")

(defn digit? [s]
  (every? #(Character/isDigit %) s))

(defn to-num [s]
  (and
   (not-empty s) ; filter out empty strings and nil
   (digit? s)
   (Long/valueOf s)))

;; result is nil, false, or number
(to-num nil)   ; nil
(to-num "")    ; nil
(to-num "a")   ; false
(to-num "12A") ; false
(to-num "12")  ; 12

;; in the body of `when-let` n is a number
(when-let [n (to-num "12A")] (* 2 n)) ; nil
(when-let [n (to-num "12")]  (* 2 n)) ; 24

;; `not-empty` returns the collection unaltered
(let [coll [1 2 3]]
  (when-let [c (not-empty coll)] (pop c))) ; [1 2]

(comment
  ;; `seq` is roughly equivalent to `not-empty` with an important caveat:
  ;; `seq` transforms the collection into a sequential iterator which is 
  ;; not compatible with the following `pop` invocation.
  (let [coll [1 2 3]]
    (when-let [c (seq coll)] (pop c))))
  ; (err) Execution error (ClassCastException)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (let [v (vec (range 1000))] (quick-bench (empty? v)))
  ; (out) Execution time mean : 58.241693 ns

  (let [v (vec (range 1000))] (quick-bench (zero? (count v))))
  ; (out) Execution time mean : 13.270393 ns

  (let [v (vec (range 1000))]
    (quick-bench (.isEmpty ^java.util.Collection v))))
  ; (out) Execution time mean : 12.679978 ns

(comment
  ;; `empty?` and `not-empty` implementations are based on `seq`

  ;; `empty?` realizes only the first chunk of a lazy (chunked) sequence (32 elements)
  (empty? (map #(do (println "realizing" %) %) (range 100)))
  ; (out) realizing 0
  ; (out) realizing 1
  ; (out) realizing 2
  ; (out) realizing 3
  ; (out) realizing 4
  ; (out) realizing 5
  ; (out) realizing 6
  ; (out) realizing 7
  ; (out) realizing 8
  ; (out) realizing 9
  ; (out) realizing 10
  ; (out) …
  ; (out) realizing 29
  ; (out) realizing 30
  ; (out) realizing 31
  ; false

  ;; when `count` is used the entire lazy sequence is realized
  (zero? (count (map #(do (println "realizing" %) %) (range 100)))))
  ; (out) realizing 0
  ; (out) realizing 1
  ; (out) realizing 2
  ; (out) realizing 3
  ; (out) realizing 4
  ; (out) realizing 5
  ; (out) realizing 6
  ; (out) realizing 7
  ; (out) realizing 8
  ; (out) realizing 9
  ; (out) realizing 10
  ; (out) …
  ; (out) realizing 93
  ; (out) realizing 94
  ; (out) realizing 95
  ; (out) realizing 96
  ; (out) realizing 97
  ; (out) realizing 98
  ; (out) realizing 99
  ; false

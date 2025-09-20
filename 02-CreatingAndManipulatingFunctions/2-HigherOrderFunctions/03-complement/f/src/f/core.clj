(ns f.core)

;; everything in Clojure has an extended boolean meaning and always returns either true or false

((complement true?) (integer? 1))                  ; false

;; item that doesn't belong to given map
((complement {:a 1 :b 2}) :c)                      ; true

;; watch out for nil values (maybe not what you expected)
((complement {:a 1 :b nil}) :b)                    ; true

;; items that don't belong to a set
(filter (complement #{:a :b :c}) [:d 2 :a 4 5 :c]) ; (:d 2 4 5)

(filter (complement #{nil :a 2}) [:a 2 nil nil])   ; (nil nil)

;; ;;;;;;;;;;;;;
;; wheel example
;; ;;;;;;;;;;;;;

(def wheel {:turn :left}) ; #'f.core/wheel

(defn turning-left? [wheel]
  (= :left (:turn wheel)))

;; we can't use `not` as it takes a boolean value not a function
(def turning-right?
  (complement turning-left?))

(defn turn-left [wheel]
  (if (turning-left? wheel)
    (println "already turning left")
    (println "turning left")))

(defn turn-right [wheel]
  (if (turning-right? wheel)
    (println "already turning right")
    (println "turning right")))

(turn-left wheel)
; (out) already turning left

(turn-right wheel)
; (out) turning right

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; `remove` from the standard
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn my-remove [pred coll]
  (filter (complement pred) coll))

(my-remove neg-int? (range -10 10))
; (0 1 2 3 4 5 6 7 8 9)

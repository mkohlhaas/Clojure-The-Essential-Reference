(ns f.core)

;; `contains?` returns true or false (therefore the `?`)

(contains? {:a "a" :b "b"} :b)  ; true
(contains? #{:x :y :z}     :z)  ; true
(contains? [:a :b :c]       1)  ; true

;; key is truncated to an integer
(long (Math/pow 2 32))             ; 4294967296
(.intValue (long (Math/pow 2 32))) ; 0

(let [power-2-32 (long (Math/pow 2 32))]
  (contains? [1 2 3] power-2-32))
; true

(let [power-2-32 (long (Math/pow 2 32))]
  (get [1 2 3] power-2-32))
; 1

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; Oops!
(let [m {:a 1 :b nil :c 3}]
  (if (get m :b) "Key found" "Key not found"))
; "Key not found"

;; Okay, but complicated due to a sentinel value
(let [m {:a 1 :b nil :c 3}]
  (if-not (= ::none (get m :b ::none)) "Key found" "Key not found"))
; "Key found"

;; idiomatic
(let [m {:a 1 :b nil :c 3}]
  (if (contains? m :b) "Key found" "Key not found"))
; "Key found"

;; ;;;;;;;
;; Sensors
;; ;;;;;;;

(def sensor-read
  [{:id "AR2" :location 2 :status "ok"}
   {:id "EF8" :location 2 :status "ok"}
   nil ; sensor did not send a response when it was requested
   {:id "RR2" :location 1 :status "ok"}
   nil ; sensor did not send a response when it was requested
   {:id "GT4" :location 1 :status "ok"}
   {:id "YR3" :location 4 :status "ok"}])

(defn problems? [sensors]
  (contains? (into #{} sensors) nil))

(comment
  (into #{} sensor-read)
  ; #{nil
  ;   {:id "RR2", :location 1, :status "ok"}
  ;   {:id "AR2", :location 2, :status "ok"}
  ;   {:id "YR3", :location 4, :status "ok"}
  ;   {:id "EF8", :location 2, :status "ok"}
  ;   {:id "GT4", :location 1, :status "ok"}}

  (problems? (into #{} sensor-read))) ; true

(defn raise-on-error [sensors]
  (if (problems? sensors)
    (throw (RuntimeException. "At least one sensor is malfunctioning"))
    :ok))

(comment
  (raise-on-error sensor-read))
  ; (err) At least one sensor is malfunctioning

;; Oops! This wouldn't work.
((into #{} sensor-read) nil) ; nil

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The many meanings of `contains?`
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `contains?` on vectors only works in the presence of an integer as the second argument
(contains? [1 2 3 4]      3) ; true
(contains? [1 2 3 4]      4) ; false
(contains? [:a :b :c :d] :a) ; false
(contains? [:a :b :c :d] :z) ; false

;; `.contains` works on vectors, strings, and hash-sets
(.contains [:a :b :c :d] :a)        ; true
(.contains "somelongstring" "long") ; true
(.contains #{:a 1} :a)              ; true

(comment
  ;; doesn't work on hash-maps
  (.contains {:a 1} :a))
  ; (err) Execution error (IllegalArgumentException)


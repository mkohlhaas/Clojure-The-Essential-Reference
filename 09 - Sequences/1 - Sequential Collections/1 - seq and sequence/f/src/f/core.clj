(ns f.core
  (:require
   [criterium.core :refer [bench]])
  (:import
   [java.util ArrayList]))

(def coll [])

(if (seq       coll) :full  :empty) ; :empty
(if (empty?    coll) :empty :full)  ; :empty
(if (not-empty coll) :full  :empty) ; :empty

(seq      nil)  ; nil
(sequence nil)  ; ()

(seq      [])   ; nil
(sequence [])   ; ()

(sequence (map str) [1 2 3] [:a :b :c])
; ("1:a" "2:b" "3:c")

(sequence (map *) (range 10) (range 10))
; (0 1 4 9 16 25 36 49 64 81)

(defn rev [coll]
  (loop [xs (seq coll) done ()]
    (if (seq xs)
      (recur
       (rest xs)
       (cons (first xs) done))
      done)))

(rev [8 9 10 3 7 2 0 0])
; (0 0 2 7 3 10 9 8)

(every? seq [#{} [:a] "hey" nil {:a 1}])
; false

(defn measure? [measure]
  (and
   measure
   (re-matches #"[0-9\.]*" measure)))

(defn metric? [[name & measures]]
  (and
   name
   (re-matches #"Class\D" name)
   (every? measure? measures)))

(defn header? [[kind & [code]]]
  (and
   (#{"Wireless" "Wired"} kind)
   (#{"MXD" "QXD" "CXP"} code)))

(defn pattern? [[header & metrics]]
  (and
   (header? header)
   (every? metric? metrics)))

(pattern? [["Wireless" "MXD" ""]
           ["ClassA" "34.97" "" "34.5"]
           ["ClassB" "11.7" "11.4"]])
; true

(defn all-except-first [lines]
  #(nthrest lines %))

(def if-header-or-metric
  #(take-while (some-fn header? metric?) %))

(defn filter-pattern [lines]
  (sequence
   (comp
    (map (all-except-first lines))
    (keep if-header-or-metric)
    (filter pattern?))
   (range (count lines))))

;; == example data pattern == ; <1>
;; Wireless MXD CXP  ; header: kind & codes
;; ClassA 34.97 34.5 ; metric: name & measures
;; ClassT 11.7 11.4  ; metric: name & measures
;; ClassH 0.7 0.4    ; metric: name & measures

(def device-output
  [["Communication services version 2"]
   ["Radio controlled:" "Enabled"]
   ["Ack on transmission" "Enabled" ""]
   ["TypeA"]
   ["East" "North" "South" "West"]
   ["10.0" "11.0" "12.0" "13.0"]
   ["Wireless" "MXD" ""]
   ["ClassA" "34.97" "" "34.5"]
   ["ClassB" "11.7" "11.4"]
   ["Unreadable line"]
   ["North" "South" "East" "West"]
   ["10.0" "11.0" "12.0" "13.0"]
   ["Wired" "QXD"]
   ["ClassA" "34.97" "33.6" "34.5"]
   ["ClassC" "11.0" "11.4"]])

(filter-pattern device-output)
; ((["Wireless" "MXD" ""]
;   ["ClassA" "34.97" "" "34.5"]
;   ["ClassB" "11.7" "11.4"])
;  (["Wired" "QXD"]
;   ["ClassA" "34.97" "33.6" "34.5"]
;   ["ClassC" "11.0" "11.4"]))

#_{:clj-kondo/ignore [:redefined-var]}
(defn filter-pattern [lines]
  (sequence
   (comp
    (map (all-except-first lines))
    (keep if-header-or-metric)
    (filter pattern?)
    (map #(do (println "executing xducers") %)))
   (range (count lines))))

(let [groups (filter-pattern device-output)]
  [(dorun (seq groups))
   (dorun (first groups))
   (dorun (last groups))])
; (out) executing xducers
; (out) executing xducers
; [nil nil nil]

(let [a (ArrayList. [:o :o :o]) s (seq a)]
  [(.set a 0 :x) (first s) (.get a 0)])
; [:o :x :x]

(let [a (ArrayList. [:o :o :o]) s (seq a)]
  [(first s) (.set a 0 :x) (first s) (.get a 0)])
; [:o :o :o :x]

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (let [xs (range 500000)]
    (bench (last (filter odd? (map inc xs)))))
;; Execution time mean : 26.944707 ms

  (let [xs (range 500000)]
    (bench (last (sequence (comp (map inc) (filter odd?)) xs)))))
;; Execution time mean : 37.773642 ms

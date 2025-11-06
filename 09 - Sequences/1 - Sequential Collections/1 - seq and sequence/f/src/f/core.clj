(ns f.core
  (:require
   [criterium.core :refer [bench]])
  (:import
   [java.util ArrayList]))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; What is the role of a sequence in Clojure?
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; 1. A sequence is basically a collection.
;; 2. A sequence does not implement a container for its data, but receives data from some source. 
;; 3. A sequence is lazy by construction.
;; 4. A sequence offers a basic caching mechanism. The sequence can be assigned to a let-block and accessed multiple times.

;; `seq` is of huge importance for Clojure internals (all sequence functions call `seq` in one way or another)

(def coll [])

(if (seq       coll) :full  :empty) ; :empty
(if (not-empty coll) :full  :empty) ; :empty
(if (empty?    coll) :empty :full)  ; :empty

;;`seq` and `sequence` are almost the same, except:
(seq      nil)  ; nil
(sequence nil)  ; ()

(seq      [])   ; nil
(sequence [])   ; ()

;; `sequence` also allows application of a transducer
(sequence (map str) [1 2 3] [:a :b :c])
; ("1:a" "2:b" "3:c")

(comment
  (str 1 :a)) ; "1:a"

;; `sequence` is the only transducer function supporting multiple collection inputs 
(sequence (map *) (range 10) (range 10))
; (0 1 4 9 16 25 36 49 64 81)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn rev [coll]
  (loop [xs   (seq coll)
         done ()]
    (if (seq xs) ; idiomatic use of `seq`
      (recur (rest xs) (cons (first xs) done))
      done)))

;; vectors are not native sequences
(rev [8 9 10 3 7 2 0 0])
; (0 0 2 7 3 10 9 8)

(map seq [#{} [:a] "hey" nil {:a 1}])
; (nil (:a) (\h \e \y) nil ([:a 1]))

(every? seq [#{} [:a] "hey" nil {:a 1}])
; false

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; Parse Unstructured Input
;; ;;;;;;;;;;;;;;;;;;;;;;;;

;; == example data pattern ==
;; Wireless MXD CXP  ; header: kind & codes
;; ClassA 34.97 34.5 ; metric: name & measures
;; ClassT 11.7 11.4  ; metric: name & measures
;; ClassH 0.7 0.4    ; metric: name & measures

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

(pattern? [["Wireless" "MXD"   ""]
           ["ClassA"   "34.97" "" "34.5"]
           ["ClassB"   "11.7"  "11.4"]])
; true

;; all lines except the first "n" ("n" comes from the transducer '= %')
(defn all-except-first [lines]
  #(nthrest lines %))

;; device output with a lot to be filtered noise
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

(comment
  (nthrest (range 10) 2) ; (2 3 4 5 6 7 8 9)
  (nthrest device-output (count (first device-output))))
  ; (["Radio controlled:" "Enabled"]
  ;  ["Ack on transmission" "Enabled" ""]
  ;  ["TypeA"]
  ;  ["East" "North" "South" "West"]
  ;  ["10.0" "11.0" "12.0" "13.0"]
  ;  ["Wireless" "MXD" ""]
  ;  ["ClassA" "34.97" "" "34.5"]
  ;  ["ClassB" "11.7" "11.4"]
  ;  ["Unreadable line"]
  ;  ["North" "South" "East" "West"]
  ;  ["10.0" "11.0" "12.0" "13.0"]
  ;  ["Wired" "QXD"]
  ;  ["ClassA" "34.97" "33.6" "34.5"]
  ;  ["ClassC" "11.0" "11.4"])

(map (all-except-first device-output) (range (count device-output)))
; ([["Communication services version 2"]
;   ["Radio controlled:" "Enabled"]
;   …
;   ["ClassC" "11.0" "11.4"]]
;  (["Radio controlled:" "Enabled"]
;   ["Ack on transmission" "Enabled" ""]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["Ack on transmission" "Enabled" ""]
;   ["TypeA"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["TypeA"]
;   ["East" "North" "South" "West"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["East" "North" "South" "West"]
;   ["10.0" "11.0" "12.0" "13.0"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["10.0" "11.0" "12.0" "13.0"]
;   ["Wireless" "MXD" ""]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["Wireless" "MXD" ""]
;   ["ClassA" "34.97" "" "34.5"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["ClassA" "34.97" "" "34.5"]
;   ["ClassB" "11.7" "11.4"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["ClassB" "11.7" "11.4"]
;   ["Unreadable line"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["Unreadable line"]
;   ["North" "South" "East" "West"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["North" "South" "East" "West"]
;   ["10.0" "11.0" "12.0" "13.0"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["10.0" "11.0" "12.0" "13.0"]
;   ["Wired" "QXD"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["Wired" "QXD"]
;   ["ClassA" "34.97" "33.6" "34.5"]
;   …
;   ["ClassC" "11.0" "11.4"])
;  (["ClassA" "34.97" "33.6" "34.5"]
;   ["ClassC" "11.0" "11.4"])
;  (["ClassC" "11.0" "11.4"]))

(def if-header-or-metric
  #(take-while (some-fn header? metric?) %))

(defn filter-pattern [lines]
  (sequence
   (comp
    (map (all-except-first lines))
    (keep if-header-or-metric)
    (filter pattern?))
   (range (count lines))))

(filter-pattern device-output)
; ((["Wireless" "MXD" ""]
;   ["ClassA" "34.97" "" "34.5"]
;   ["ClassB" "11.7" "11.4"])
;  (["Wired" "QXD"]
;   ["ClassA" "34.97" "33.6" "34.5"]
;   ["ClassC" "11.0" "11.4"]))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (defn filter-pattern [lines]
    (sequence
     (comp
      (map (all-except-first lines))
      (keep if-header-or-metric)
      (filter pattern?)
      (map #(do (println "executing xducers") %)))
     (range (count lines))))

  (let [groups (filter-pattern device-output)]
    [(dorun (seq   groups))
     (dorun (first groups))
     (dorun (last  groups))])
  ; (out) executing xducers
  ; (out) executing xducers
  ; [nil nil nil]

  (let [a (ArrayList. [:o :o :o]) s (seq a)]
    [(.set a 0 :x) (first s) (.get a 0)])
  ; [:o :x :x]

  (let [a (ArrayList. [:o :o :o]) s (seq a)]
    [(first s) (.set a 0 :x) (first s) (.get a 0)])
  ; [:o :o :o :x]

  (let [xs (range 500000)]
    (bench (last (filter odd? (map inc xs)))))
  ;; Execution time mean : 26.944707 ms

  (let [xs (range 500000)]
    (bench (last (sequence (comp (map inc) (filter odd?)) xs)))))
  ;; Execution time mean : 37.773642 ms

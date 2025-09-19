(ns f.core
  (:require [clojure.string :refer [starts-with? split-lines]]))

;; ;;;;;;;;;;;;;;;;;;;;;;
;; introduction to `comp`
;; ;;;;;;;;;;;;;;;;;;;;;;

;; `((comp f1 f2 f3) x)` is equivalent to `(f1 (f2 (f3 x)))`
;; => right to left
((comp inc +) 2 2)
; 5

;; acts like the `identity` function
((comp) 42)
; 42

;; `(comp f g h)`
;; `h` can accept any number of args
;; `f`, `g` can accept only one arg
;; `comp` returns a function of the same number of arguments of the rightmost input parameter

(def mailing
  [{:name "Mark", :label "12 High St\nAnchorage\n99501"}
   {:name "John", :label "1 Low ln\nWales\n99783"}
   {:name "Jack", :label "4 The Plaza\nAntioch\n43793"}
   {:name "Mike", :label "30 Garden pl\nDallas\n75395"}
   {:name "Anna", :label "1 Blind Alley\nDallas\n75395"}])

;; ;;;;;;;;;;;;;;
;; without `comp`
;; ;;;;;;;;;;;;;;

(defn postcodes-1 [mailing]
  (map #(last (split-lines (:label %))) mailing))

(postcodes-1 mailing)
; ("99501" "99783" "43793" "75395" "75395")

(frequencies (postcodes-1 mailing))
; {"99501" 1, "99783" 1, "43793" 1, "75395" 2}

;; ;;;;;;;;;;;
;; with `comp`
;; ;;;;;;;;;;;

(defn postcodes-2 [mailing]
  (map
   (comp
    last
    split-lines
    :label)
   mailing))

(postcodes-2 mailing)
; ("99501" "99783" "43793" "75395" "75395")

(frequencies (postcodes-2 mailing))
; {"99501" 1, "99783" 1, "43793" 1, "75395" 2}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; with `sequence` (using transducers)
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `map` is used to generate transducers and we need to use `sequence` to apply them
;; `sequence` accepts composition of transducers
;; reverse order
(defn postcodes-3 [mailing]
  (sequence (comp
             (map :label)
             (map split-lines)
             (map last))
            mailing))

(postcodes-3 mailing)
; ("99501" "99783" "43793" "75395" "75395")

(frequencies (postcodes-3 mailing)) ; {"99501" 1, "99783" 1, "43793" 1, "75395" 2}
; {"99501" 1, "99783" 1, "43793" 1, "75395" 2}

;; ;;;;;;;;;;;;;;;;
;; unique postcodes
;; ;;;;;;;;;;;;;;;;

(defn alaska? [postcode]
  (starts-with? postcode "99"))

(defn unique-postcodes [mailing]
  (sequence (comp
             (map :label)
             (map split-lines)
             (map last)
             (remove alaska?)
             (distinct))
            mailing))

(unique-postcodes mailing)
; ("43793" "75395")

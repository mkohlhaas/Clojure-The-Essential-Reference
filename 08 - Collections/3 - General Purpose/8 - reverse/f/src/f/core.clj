(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(reverse [0 1 2 3 4 5 6 7 8 9]) ; (9 8 7 6 5 4 3 2 1 0)

;; doesn't produce a lazy sequence like most other sequential functions
(type (reverse [1 2 3]))  ; clojure.lang.PersistentList

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; sort then reverse is very inefficient
(reverse (sort (shuffle (range 10))))  ; (9 8 7 6 5 4 3 2 1 0)

;; just use sort
(sort > (shuffle (range 10)))          ; (9 8 7 6 5 4 3 2 1 0)

;; if input isn't numeric use a (custom) comparator
(sort #(compare %2 %1) (shuffle (map str (range 10))))  ; ("9" "8" "7" "6" "5" "4" "3" "2" "1" "0")

;; ;;;;;;;;;;;;;;;;;
;; DNA Transcription
;; ;;;;;;;;;;;;;;;;;

(def DNA "CTATCTTTTAATCGGTTCTTGCAGTGAGATACATTCCACATGCCCGACTT")

(->> DNA
     reverse                              ; (\T \T \C \A \G \C \C \C \G \T \A … \T \C \T \A \T \C)
     (replace {\A \T \T \A \C \G \G \C})  ; (\A \A \G \T \C \G \G … \T \A \A \A \A \G \A \T \A \G)
     (apply str))                         ; "AAGTCGGGCATGTGGAATGTATCTCACTGCAAGAACCGATTAAAAGATAG"
; "AAGTCGGGCATGTGGAATGTATCTCACTGCAAGAACCGATTAAAAGATAG"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `reverse` is not lazy
(first (reverse (map #(do (print % "") %) (range 100)))) ; 99
; (out) 0 1 2 3 4 5 6 7 8 9 10 11 … 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 

(comment
  (let [s (range 1e6)
        v (into [] s)]
    (quick-bench (reverse s))        ; (out) Execution time mean : 57.924460 ms
    (quick-bench (reverse v))        ; (out) Execution time mean : 76.897953 ms
    (quick-bench (doall (rseq v))))) ; (out) Execution time mean : 34.306364 ms

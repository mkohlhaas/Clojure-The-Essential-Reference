(ns f.core
  (:require
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [cl-format]]))

;; `format`: wrapper around Java’s `String::format` method
;; `cl-format`: port of Common Lisp’s format function
;;              see chapter 18 of "Practical Common Lisp" by Peter Siebel for examples

(comment
  ;; for `format`
  (javadoc java.util.Formatter))

;; some `format` examples
(format "%3d" 1)                                ; "  1"
(format "%03d" 1)                               ; "001"
(format "%.2f" 10.3456)                         ; "10.35"
(format "%10s", "Clojure")                      ; "   Clojure"
(format "%-10s", "Clojure")                     ; "Clojure   "
(format "%-11.11s" "truncatefixedsize")         ; "truncatefix" (truncating a string if too long, right padding if too short)
(format "%tT" (java.util.Calendar/getInstance)) ; "22:15:11"

;; some `cl-format` examples
(cl-format nil "~:d" 1000000)        ; "1,000,000"               (large numbers)
(cl-format nil "~b" 10)              ; "1010"                    (binary, also octal and hex)
(cl-format nil "Anno Domini ~@r" 25) ; "Anno Domini XXV"         (Roman numerals)
(cl-format nil "~r" 158)             ; "one hundred fifty-eight" (words)
(cl-format nil "~:r and ~:r" 1 2)    ; "first and second"        (ordinals)
(cl-format nil "~r banana~:p" 1)     ; "one banana"              (pluralize)
(cl-format nil "~r banana~:p" 2)     ; "two bananas"             (pluralize)

;; conditional formatting (output depends on the size of the input) ;;
;; a way to produce grammatically correct english ;;

(def num-sentence "~#[nope~;~a~;~a and ~a~:;~a, ~a~]~#[~; and ~a~:;, ~a, etc~].")

(cl-format nil num-sentence 1 2)     ; "1 and 2."
(cl-format nil num-sentence 1 2 3)   ; "1, 2 and 3."
(cl-format nil num-sentence 1 2 3 4) ; "1, 2, 3, etc."

(def pluralize "I see ~[no~:;~:*~r~] fish~:*~[es~;~:;es~].")

(cl-format nil pluralize 0)   ; "I see no fishes."
(cl-format nil pluralize 1)   ; "I see one fish."
(cl-format nil pluralize 100) ; "I see one hundred fishes."

;; wrap text to a specific line size ;;

(def paragraph
  ["This" "sentence" "is" "too" "long" "for" "a" "small" "screen"
   "and" "should" "appear" "in" "multiple" "lines" "no" "longer"
   "than" "20" "characters" "each" "."])

;; max line size = 20
(println (cl-format nil "~{~<~%~1,20:;~A~> ~}" paragraph))
; (out) This sentence is too 
; (out) long for a small 
; (out) screen and should 
; (out) appear in multiple 
; (out) lines no longer than 
; (out) 20 characters each . 

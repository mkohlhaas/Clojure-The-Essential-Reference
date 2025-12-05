(ns f.core
  (:require
   [clojure.string :as s]))

;; for regexs in general visit https://www.regular-expressions.info/

(def s "Chat-room messages are up-to-date")
(s/replace s \- \space) ; "Chat room messages are up to date"

;; replacing string by string ✓
(s/replace "Closure is a Lisp" "Closure" "Clojure") ; "Clojure is a Lisp"

(comment
  ;; replacing string by char ✗
  (s/replace "I'm unjure" "j" \s)
  ; (err) Execution error (ClassCastException)
  ; (err) class java.lang.Character cannot be cast to class java.lang.CharSequence

  ;; replacing char by char ✓
  (s/replace "I'm unjure" \u \s)) ; "I'm snjsre"

(def s1 "Why was 12 afraid of 14? Because 14 ate 18.")

;; regex + replacement function
(s/replace s1 #"\d+" #(str (/ (Integer/valueOf %) 2))) ; "Why was 6 afraid of 7? Because 7 ate 9."

(comment
  (Integer/valueOf "12")) ; 12

(def s2 "Easter in 2038: 04/25/2038, Easter in 2285: 03/22/2285")

;; group capturing patterns
;; swap day and month
(s/replace s2 #"(\d{2})/(\d{2})/(\d{4})" "$2/$1/$3") ; "Easter in 2038: 25/04/2038, Easter in 2285: 22/03/2285"

(def s3 "May 2018, June 2019")

(comment
  (s/replace s3 #"May|June" "10$ in"))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Illegal group reference

(s/replace s3 #"May|June" (s/re-quote-replacement "10$ in")) ; "10$ in 2018, 10$ in 2019"

(def s4 "I could have a drink here and wine home.")

(s/replace s4 #"a drink|beer|wine" "water")       ; "I could have water here and water home."

(s/replace-first s4 #"a drink|beer|wine" "water") ; "I could have water here and wine home."

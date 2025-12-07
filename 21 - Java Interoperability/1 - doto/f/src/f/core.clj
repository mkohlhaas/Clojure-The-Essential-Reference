(ns f.core
  (:require
   [clojure.java.javadoc :refer [javadoc]])
  (:import
   [java.util Calendar Calendar$Builder]
   [java.util ArrayList]))

;; `.`, forward form, inverted form, slashed form:
;;
;; `.` (dot) is a multipurpose special form to access methods and attributes of Java objects.
;; Several variants exist, with the target object appearing as the first argument ("forward form"), 
;; or as the second ("inverted form"),
;; or abbreviated with a slash "/" ("slashed" form). 

(. Thread sleep 100)    ; Access static method of 1 arg.
(. Math random)         ; Access static field first (if any) or static method of no args.
(. Math (random))       ; Access static method of no args (unambiguously).
(. Math -PI)            ; Access static field access (unambiguously).
(. Math  PI)
(. Thread$State NEW)    ; Access inner class static method.

(Thread/sleep 100)      ; Access static method of 1 arg.
(Math/random)           ; Access static field first (if any) or static method of no args.

(comment
  Math/PI ; 3.141592653589793

  Math/-PI)
  ; (err) Syntax error (IllegalArgumentException)
  ; (err) No matching method _PI found taking 0 args for class java.lang.Math

(def point (java.awt.Point. 1 2))
(. point x)      ; Access instance field first (if any) or method of no args.
(.x point)       ; Same as above.
(. point (getX)) ; Access instance method (unambiguously).
(.-x point)      ; Access instance field  (unambiguously).

(defmacro getter [object field]
  (let [getName# (symbol (str "get" field))]
    `(. ~object ~getName#)))

(defmacro setter [object field & values]
  (let [setName# (symbol (str "set" field))]
    `(. ~object ~setName# ~@values)))

(getter (java.awt.Point. 2 2) "X") ; 2.0

(comment
  (symbol (str "get" "X")) ; getX

  ;; awt points don't have setX method (it's called setLocation)
  (setter point "X" 42)
  ; (err) Execution error (IllegalArgumentException)
  ; (err) No matching method setX found taking 1 args for class java.awt.Point

  (javadoc point)

  (doto point
    (setter "Location" 4 5)))
  ; #object[java.awt.Point 0x46f020d4 "java.awt.Point[x=4,y=5]"]

;; Double dot ".." uses the result of invoking the first two arguments as the input for the next pair, and so on, until there are no more arguments.
;; ".." is useful to connect a chain of state-mutating calls so they always operate on the initial object.
;; A common case, is building instances using the Java builder pattern.

;; builder pattern
(.. (Calendar$Builder.)
    (setCalendarType "iso8601")
    (setWeekDate 2019 4 (Calendar/SATURDAY))
    (build))
; #inst "2019-01-26T00:00:00.000+01:00"

(macroexpand
 '(.. (Calendar$Builder.)
      (setCalendarType "iso8601")
      (setWeekDate 2019 4 (Calendar/SATURDAY))
      (build)))
; (.
;  (.
;   (. (Calendar$Builder.) (setCalendarType "iso8601"))
;   (setWeekDate 2019 4 (Calendar/SATURDAY)))
;  (build))

;; the target object remains the same through all the steps of the chain
(let [l (ArrayList.)]
  (doto l
    (.add "fee")
    (.add "fi")
    (.add "fo")
    (.add "fum")))
; ["fee" "fi" "fo" "fum"]

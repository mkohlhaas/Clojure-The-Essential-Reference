(ns f.core
  (:require
   [clojure.java.javadoc :refer [javadoc]]))

;; `defrecord` generates a deftype-based class that additionally implements Clojure map semantic on declared attributes.
;; Differently from `deftype`, objects produced by `defrecord` are immutable

(defrecord Point [x y])

;; Java interoperability
(def p (Point. 1 2))
(.x p)  ; 1
(.-x p) ; 1
(. p x) ; 1

;; map semantics
(def p1 (map->Point {:x 1 :y 2})) ; map of keywords to field values
(:x p1) ; 1
(:y p1) ; 2

(comment
  (type p1) ; f.core.Point
  (ifn? p1) ; false

  (p1 :x))
  ; (err) Execution error (ClassCastException)
  ; (err) class f.core.Point cannot be cast to class clojure.lang.IFn

(def p2 (map->Point {:x 1})) ; {:x 1, :y nil}
(def p3 (assoc p2 :y 2))     ; {:x 1, :y 2}
(type p3)                    ; f.core.Point
(def p4 (dissoc p3 :y))      ; {:x 1}
(type p4)                    ; clojure.lang.PersistentArrayMap (Oops!)

;; Like `deftype`, `defrecord` can implement any number of interfaces.
;; A record extends java.lang.Object by default.

(defn- euclidean-distance [x1 y1 x2 y2]
  (Math/sqrt
   (+ (Math/pow (- x1 x2) 2)
      (Math/pow (- y1 y2) 2))))

(comment
  (javadoc Comparable))

(defrecord Point1 [x y]
  Comparable
  (compareTo [p1 p2] ; p1 = this
    (compare (euclidean-distance (.x p1) (.y p1) 0 0)    ; distance from origin
             (euclidean-distance (.x p2) (.y p2) 0 0))))

;; sort by distance from origin
(sort [(->Point1 5 2)
       (->Point1 2 4)
       (->Point1 3 1)])
; ({:x 3, :y 1} 
;  {:x 2, :y 4} 
;  {:x 5, :y 2})

(defrecord Point2 [x y])

(->Point2 1 2)        ; {:x 1, :y 2}             (`print` has been overridden …)
(str (->Point2 1 2))  ; "f.core.Point2@78de238e" (… but not `toString`)

;; override `toString` in Object
(defrecord Point3 [x y]
  Object
  (toString [_this]
    (format "[%s, %s]" x y)))

(str (->Point3 1 2)) ; "[1, 2]"

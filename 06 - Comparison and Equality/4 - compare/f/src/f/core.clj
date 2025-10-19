(ns f.core
  (:import
   java.util.GregorianCalendar))

(let [c (compare 1 2)]
  (cond
    (neg? c)  "less than"
    (zero? c) "equal"
    (pos? c)  "greater than"))
; "less than"

;; ;;;;;;;;;;;;;;;;;;
;; Gregorian Calendar
;; ;;;;;;;;;;;;;;;;;;

(def t1 (GregorianCalendar/getInstance))
(def t2 (GregorianCalendar/getInstance))

;; GregorianCalendar implements java.lang.Comparable interface
(compare t1 t2) ; -1

;; ;;;;
;; Refs
;; ;;;;

;; Refs are compared based on their creation order!
(compare (ref :a) (ref :a))  ; -1

;; ;;;
;; Nil
;; ;;;

;; nil is accepted as possible argument and is always considered the "smallest" value
(compare nil (/ -1. 0)) ; -1

(def -∞ (/ -1. 0)) ; ##-Inf

(map compare [nil nil "a"] [-∞ nil nil]) ; (-1 0 1)

;; ;;;
;; NaN
;; ;;;

(def nan Double/NaN) ; ##NaN

;; NaN is always the same as any other number or itself
(map compare [nan nan 1] [1 nan nan]) ; (0 0 0)

;; ;;;;;;
;; Ranges
;; ;;;;;;

;; A clojure.lang.LongRange object is not Comparable.
(instance? java.lang.Comparable (range 10))  ; false

(comment
  ;; if we try to compare ranges, we get the expected exception
  (compare (range 10) (range 10)))
  ; (err) Execution error (ClassCastException)
  ; (err) class clojure.lang.LongRange cannot be cast to class java.lang.Comparable

;; However, we can apparently compare identical ranges when instead we are just in 
;; front of the obvious result of equal objects when they are the same instance.
;; When the two arguments are the same (as in identical?, which means they are the same Java object) compare returns 0.
(let [x (range 10) y x] (compare x y)) ; 0

;; ;;;;;;;
;; Vectors
;; ;;;;;;;

;; The first vector contains 4 elements, while the second only 3.
;; This is equivalent to compare (compare 4 3) ignoring the content altogether.
(compare [1 1 1 1] [2 2 2])  ; 1

;; Provided the size is the same, the first pair is compared.
;; If they are equal, the second pair is compared and so on, 
;; until the first pair that is not equal or the end of the vector.
;; The last pair [4 3] is the one producing the result.
(compare [1 2 4]   [1 2 3])  ; 1

;; ;;;;;;;
;; Strings
;; ;;;;;;;

(compare "a" "z")       ; -25
;; The two strings are of different sizes with "abc" substring of "abcz". Their length is compared.
(compare "abcz" "abc")  ;  1

;; ;;;;;;;;
;; Keywords
;; ;;;;;;;;

;; Clojure keywords and symbols behave like strings with the addition that if they are namespace qualified,
;; then the namespace string comparison takes precedence.
;; If the first keyword is not qualified but the second is, the result is always -1.
;; If the second keyword is namespace qualified but the first is not, then the result is always +1.
(map compare
     [:a :my/a :a    :my/a :abc123/a]
     [:z :my/z :my/a :a    :abc/a])
; (-25 -25 -1 1 3)

;; ;;;;;;;
;; Symbols
;; ;;;;;;;

;; Exactly the same applies to symbols, which are internally stored as keywords.
(map compare
     ['a 'my/a 'a    'my/a 'abc123/a]
     ['z 'my/z 'my/a 'a    'abc/a])
; (-25 -25 -1 1 3)

;; ;;;;;;;;;;;;
;; Gas Stations
;; ;;;;;;;;;;;;

(defn- sq [x] (* x x))

(defn- distance [x1 y1 x2 y2]
  (Math/sqrt (+ (sq (- x1 x2)) (sq (- y1 y2)))))

;; `compareTo` is the method required by the java.util.Comparable interface
;; distance-origin-fn = function that calculates the distance to our location (the origin)
(defrecord Point [x y distance-origin-fn]
  Comparable
  (compareTo [this other]
    (compare (distance-origin-fn (:x this)  (:y this))
             (distance-origin-fn (:x other) (:y other)))))

;; [x2 y2] is the origin (our location)
(defn relative-point [x1 y1 x2 y2]
  (->Point x1 y1 (partial distance x2 y2)))

(defrecord GasStation [brand location]
  Comparable
  (compareTo [this other]
    (compare (:location this) (:location other))))

(def gas-stations
  (let [x 3 y 5] ; our location (the origin)
    [(->GasStation "Shell"    (relative-point 3.4  5.1 x y))
     (->GasStation "Gulf"     (relative-point 1      1 x y))
     (->GasStation "Exxon"    (relative-point -5     8 x y))
     (->GasStation "Speedway" (relative-point 10    -1 x y))
     (->GasStation "Mobil"    (relative-point 2    2.7 x y))
     (->GasStation "Texaco"   (relative-point -4.4  11 x y))
     (->GasStation "76"       (relative-point 3     -3 x y))
     (->GasStation "Chevron"  (relative-point -2   5.3 x y))
     (->GasStation "Amoco"    (relative-point 8     -1 x y))]))

(map :brand (sort gas-stations))
; ("Shell"
;  "Mobil"
;  "Gulf"
;  "Chevron"
;  "Amoco"
;  "76"
;  "Exxon"
;  "Speedway"
;  "Texaco")

;; ;;;;;;;;;;;;;;;;;
;; Beware of the NaN
;; ;;;;;;;;;;;;;;;;;

;; `compare` always returns 0 when Double/NaN is present
(compare Double/NaN 1)          ; 0
(compare 1 Double/NaN)          ; 0
(compare Double/NaN Double/NaN) ; 0

;; `sort` by default uses `compare` as a comparator and since compare of NaN is always 0,
;; different results are produced based on the relative ordering of the elements appearing before NaN in the vector.
(sort [3 2 Double/NaN 0]) ; (0 2 3 ##NaN)
(sort [2 3 Double/NaN 0]) ; (2 3 ##NaN 0)

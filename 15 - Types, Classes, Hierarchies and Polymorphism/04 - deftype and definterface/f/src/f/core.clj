(ns f.core)

;; `deftype` and `definterface` are low level constructs to generate Java classes or interfaces

(deftype Point [x y])

;; several ways to instantiate a Point
(def p1 (new Point 1 2))
(def p2 (Point.    1 2))
(def p3 (->Point   1 2))

;; several ways to access attributes
(.x   p1) ; 1
(.-x  p1) ; 1
(. p1  y) ; 2

(.x   p2) ; 1
(.-x  p2) ; 1
(. p2  y) ; 2

(.x   p3) ; 1
(.-x  p3) ; 1
(. p3  y) ; 2

;; Euclidean distance
(defn- distance [x1 y1 x2 y2]
  (Math/sqrt
   (+ (Math/pow (- x1 x2) 2)
      (Math/pow (- y1 y2) 2))))

;; `deftype` accepts interface declarations
(deftype Point1 [x y]
  Comparable
  (compareTo [p1 p2]
    (compare (distance (.x p1) (.y p1) 0 0)
             (distance (.x p2) (.y p2) 0 0))))

;; sort by distance to origin
(sort [(->Point1 5 2)
       (->Point1 2 4)
       (->Point1 3 1)])
; output is kind of useless:
; (#object[f.core.Point1 0x53b796b3 "f.core.Point1@53b796b3"]
;  #object[f.core.Point1 0x5ed38e83 "f.core.Point1@5ed38e83"]
;  #object[f.core.Point1 0x570040d  "f.core.Point1@570040d"])

;; teach Point how to print itself
(deftype Point2 [x y]
  Object ; java.lang.Object is the only class accepted by `deftype` that is not an interface
  (toString [_this]
    (format "[%s,%s]" x y)))

(Point2. 1 2) ; #object[f.core.Point2 0x5d73ea20 "[1,2]"]

;; several interfaces
(deftype Point3 [x y]
  Object
  (toString [_this]
    (format "[%s,%s]" x y))
  Comparable
  (compareTo [p1 p2]
    (compare (distance (.x p1) (.y p1) 0 0)
             (distance (.x p2) (.y p2) 0 0))))

;; sort by distance to origin
(sort [(->Point3 5 2)
       (->Point3 2 4)
       (->Point3 3 1)])
; (#object[f.core.Point3 0x39c13bd  "[3,1]"]
;  #object[f.core.Point3 0x2302c408 "[2,4]"]
;  #object[f.core.Point3 0x7db566f2 "[5,2]"])

;; `deftype` is one of the fewest options to create truly mutable objects in Clojure. 
;; `deftype` attributes are normally declared public and final.
;; We can force deftype to remove the final keyword in 2 ways:
;; - using the ^:unsynchronized-mutable metadata, attributes are declared not public and not final.
;;   Since attributes stop being public, they need to be exposed with getter/setter functions.
;; - using the ^:volatile-mutable metadata, we make an attribute not public, not final and volatile.

;; bean-like deftype definition ;;

(definterface IPerson
  (getName [])   ; no explicit `this` needed
  (setName [s])
  (getAge  [])
  (setAge  [n]))

(deftype Person [^:unsynchronized-mutable name ^:unsynchronized-mutable age]
  IPerson
  (getName [_this]    name)
  (setName [_this s] (set! name s))
  (getAge  [_this]    age)
  (setAge  [_this n] (set! age n)))

(def p (->Person "Natasha" "823")) ; #object[f.core.Person 0x312367eb "f.core.Person@312367eb"]

;; fixing the wrong age attribute
(.getAge p)    ; "823"
(.setAge p 23) ; 23
(.getAge p)    ; 23

(spit (str *compile-path* "/bookdeftype.clj")
      "(ns bookdeftype)
   (defn bar [] \"bar\")
   (defprotocol P (foo [p]))
   (deftype Foo [] P (foo [this] (bar)))")

(comment
  ;; deftype implements a protocol
  (println (slurp (str *compile-path* "/bookdeftype.clj")))) ; nil
  ; (out) (ns bookdeftype)
  ; (out) (defn bar [] "bar")
  ; (out) (defprotocol P (foo [p]))
  ; (out) (deftype Foo [] P (foo [this] (bar)))

(compile 'bookdeftype) ; bookdeftype

;; After restarting the REPL
(import 'bookdeftype.Foo)  ; bookdeftype.Foo

;; functions inside the same namespace but outside the deftype definition might not load automatically
;; bar has not been loaded
(def p4 (Foo.))
(.foo p4)
;; IllegalStateException Attempting to call unbound fn: #'bookdeftype/bar

;; use `:load-ns` to fix the issue
(spit (str *compile-path* "/bookdeftype.clj")
      "(ns bookdeftype)
   (defn bar [] \"bar\")
   (defprotocol P (foo [p]))
   (deftype Foo [] :load-ns true P
     (foo [this] (bar)))")

(comment
  ;; with `:load-ns true`
  (println (slurp (str *compile-path* "/bookdeftype.clj")))) ; nil
  ; (out) (ns bookdeftype)
  ; (out)    (defn bar [] "bar")
  ; (out)    (defprotocol P (foo [p]))
  ; (out)    (deftype Foo [] :load-ns true P
  ; (out)      (foo [this] (bar)))

(compile 'bookdeftype) ; bookdeftype

(import 'bookdeftype.Foo) ; bookdeftype.Foo

;; now it works
(def p5 (Foo.))
(.foo p5) ; "bar"

(ns f.core
  (:import
   [java.awt Point]))

;; `set!` is quite rare function to see in production applications.
;;  The presence of `set!` could signal the presence of unsafe mutable objects for which Clojure has plently of effective alternatives.

;; `set!` can write into static or instance attributes of Java classes ;;

(def p (Point.)) ; #object[java.awt.Point 0x9f9525b "java.awt.Point[x=0,y=0]"]

[(. p x) (. p y)] ; [0 0]

(set! (. p -x) 1) ; 1  (NOTE: the dash sign `-` tells Clojure that `-x` is the instance attribute `x` and not the getter method)
(set! (. p -y) 2) ; 2
[(. p x) (. p y)] ; [1 2]

;; `set!` can mutate thread-bound var objects

(def non-dynamic 1)         ; 1
(def ^:dynamic *dynamic* 1) ; 1

(comment
  ;; `set!` cannot change the root binding of the var in both cases and throws exceptions

  (set! non-dynamic 2)
  ; (err) Execution error (IllegalStateException)
  ; (err) Can't change/establish root binding of: non-dynamic with set

  (set! *dynamic* 2))
  ; (err) Execution error (IllegalStateException)
  ; (err) Can't change/establish root binding of: *dynamic* with set

;; but can change the locally bound value of the dynamic var
(binding [*dynamic* 1]
  (set! *dynamic* 2))
; 2

;; `set!` is commonly seen to set locally bound variables such as *warn-on-reflection*,
;;  a special dynamic var that is implicitly thread-bound by the Clojure compiler.

;; Since it is already thread-bound, *warn-on-reflection* doesn’t require to be surrounded by `binding`, 
;; effectively looking like a global var.

(set! *warn-on-reflection* false)

(fn [x] (.toString x)) ; #object[f.core$eval4143$fn__4144 0x393cd430 "f.core$eval4143$fn__4144@393cd430"]

(set! *warn-on-reflection* true)

(fn [x] (.toString x)) ; #object[f.core$eval4149$fn__4150 0x794f218d "f.core$eval4149$fn__4150@794f218d"]
; (err) Reflection warning … reference to field toString can't be resolved.

;; mutations inside `deftype` definitions ;;

(deftype Counter [^:unsynchronized-mutable cnt]
  clojure.lang.IFn
  (invoke [_this] (set! cnt (inc cnt))))

(def counter (->Counter 0))

(counter) ; 1
(counter) ; 2

;; 1.clj

(var a)
;; CompilerException java.lang.RuntimeException: Unable to resolve var: a [...]

(def a 1)
(var a)
;; #'user/a

(var test-var/a)
;; CompilerException java.lang.RuntimeException: Unable to resolve var: test-var/a [...]

(create-ns 'test-var)
(intern 'test-var 'a 1)
(var test-var/a)
;; #'test-var/a

(= (var a) (var test-var/a))
;; false

;; 2.clj

(var clojure.core/+)
;; #'clojure.core/+

#'clojure.core/+
;; #'clojure.core/+

(identical? (var clojure.core/+) #'clojure.core/+)
;; true

;; 3.clj

(find-var 'user/test-find-var)
;; nil

(find-var 'test-find-var)
;; IllegalArgumentException Symbol must be namespace-qualified

;; 4.clj

(resolve 'Exception)
;; java.lang.Exception

(resolve (symbol "[I"))
;; [I

;; 5.clj

(defn replace-var [name value]
  (let [protected #{'system}]
    (when (resolve protected name)
      (intern *ns* name value))))

(def mydef 1)
(def system :dont-change-me)

(replace-var 'x 2)
;; nil

(replace-var 'mydef 2)
mydef
;; 2

(replace-var 'system 2)
system
;; :dont-change-me

;; 6.clj

(require '[clojure.repl :refer [dir-fn dir]])
(require 'clojure.set)

(dir-fn 'clojure.set)
;; (difference index intersection
;;  join map-invert project rename
;;  rename-keys select subset?
;;  superset? union)

(dir clojure.set)
;; difference
;; index
;; intersection
;; [..]
;; union
;; nil

;; 7.clj

(def ^:dynamic *dvar*)

((ns-map 'user) '*dvar*)
;; #'user/*dvar1*

(bound? #'*dvar*)
;; false

(thread-bound? #'*dvar*)
;; false

(binding [*dvar* 1]
  [(bound? #'*dvar*)
   (thread-bound? #'*dvar*)])
;; [true true]

(def avar)

(bound? #'avar)
;; false

(thread-bound? #'avar)
;; false

(intern *ns* 'avar 1)
(bound? #'avar)
;; true

(thread-bound? #'avar)
;; false

;; 8.clj

(def a 1) (def b 2) (def c 3)
(bound? #'a #'b #'c)
;; true


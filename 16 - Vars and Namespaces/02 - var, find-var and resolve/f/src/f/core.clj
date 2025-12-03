(ns f.core
  (:require
   [clojure.repl :refer [dir dir-fn]]
   [clojure.set]))

;; ;;;
;; var
;; ;;;

(comment
  (var a))
  ; (err) Syntax error compiling var at (src/f/core.clj:6:3).
  ; (err) Unable to resolve var: a in this context

(def a 1) ; #'f.core/a
(var a)   ; #'f.core/a

(comment
  #_{:clj-kondo/ignore [:unresolved-namespace]}
  (var test-var/a))
  ;; CompilerException java.lang.RuntimeException: Unable to resolve var: test-var/a [...]

(create-ns 'test-var)      ; #object[clojure.lang.Namespace 0x63ef8416 "test-var"]
(intern    'test-var 'a 1) ; #'test-var/a

#_{:clj-kondo/ignore [:unresolved-namespace]}
(var test-var/a)           ; #'test-var/a

#_{:clj-kondo/ignore [:unresolved-namespace]}
(= (var a) (var test-var/a)) ; false

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; var and the syntax literal #'
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(var clojure.core/+) ; #'clojure.core/+

;; the Clojure reader interprets "#" as lookup into the reader syntax table for ' single quote that follows
;; #'clojure.core/+  ; #'clojure.core/+

(identical? (var clojure.core/+)
            #'clojure.core/+)     ; true

;; ;;;;;;;;
;; find-var
;; ;;;;;;;;

;; prefer `find-var` to `var` if you don’t want to use try-catch to deal with non-existing vars
(find-var 'f.core/test-find-var) ; nil

(comment
  (find-var 'test-find-var))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Symbol must be namespace-qualified

;; ;;;;;;;;;;;;;;;;;;;;;;
;; resolve and ns-resolve
;; ;;;;;;;;;;;;;;;;;;;;;;

;; Clojure imports most of the java.lang.* classes in namespaces automatically
(resolve 'Exception) ; java.lang.Exception

;; an array of integers has a class type in Java named using the open square bracket followed by the letter 'I'.
(resolve (symbol "[I")) ; int/1

;; 5.clj

(defn replace-var [name value]
  (let [protected-env #{'system}]
    (when (resolve protected-env name)
      (intern *ns* name value))))

(comment
  (type #{'system})                       ; clojure.lang.PersistentHashSet
  (ns-resolve *ns* #{'system} 'system)    ; nil
  (ns-resolve *ns* #{'system} 'whatever)) ; nil (???)

(def mydef  1)               ; #'f.core/mydef
(def system :dont-change-me) ; #'f.core/system

(replace-var 'x     2) ; nil
(replace-var 'mydef 2) ; #'f.core/mydef
mydef                  ; 2

(replace-var 'system 2) ; nil
system                  ; :dont-change-me

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; clojure.repl/dir-fn and clojure.repl/dir
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(dir-fn 'clojure.set)
; (difference
;  index
;  intersection
;  join
;  map-invert
;  project
;  rename
;  rename-keys
;  select
;  subset?
;  superset?
;  union)

(comment
  (dir clojure.set))
  ; (out) difference
  ; (out) index
  ; (out) intersection
  ; (out) join
  ; (out) map-invert
  ; (out) project
  ; (out) rename
  ; (out) rename-keys
  ; (out) select
  ; (out) subset?
  ; (out) superset?
  ; (out) union

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; bound? and thread-bound?
;; ;;;;;;;;;;;;;;;;;;;;;;;;

(declare ^:dynamic *dyn-var*)

((ns-map 'f.core) '*dvar*) ; #'f.core/*dvar*

(bound?        #'*dyn-var*) ; false
(thread-bound? #'*dyn-var*) ; false

;; thread-aware context
(binding [*dyn-var* 1]
  [(bound?        #'*dyn-var*)
   (thread-bound? #'*dyn-var*)])
; [true true]

(declare avar)

(bound?        #'avar) ; false
(thread-bound? #'avar) ; false

*ns* ; #object[clojure.lang.Namespace 0x1e8ef964 "f.core"]

(intern *ns*    'avar 1) ; #'f.core/avar
(bound?        #'avar)   ; true
(thread-bound? #'avar)   ; false

;; multiple params
(def d 1)
(def e 2)
(def f 3)
(bound? #'d #'e #'f) ; true

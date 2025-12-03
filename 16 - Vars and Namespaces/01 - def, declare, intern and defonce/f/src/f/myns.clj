#_{:clj-kondo/ignore [:unused-referred-var]}
(ns myns
  (:require
   [clojure.repl :refer [doc]]))

*ns* ; #object[clojure.lang.Namespace 0x444c8045 "f.myns"]

;; ;;;
;; def
;; ;;;

(type (def mydef "thedef")) ; clojure.lang.Var

mydef ; "thedef"

(identical? (var mydef) ((ns-map 'myns) 'mydef)) ; true

(meta (var mydef))
; {:line 9,
;  :column 7,
;  :file
;  "/home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/16 - Vars and Namespaces/01 - def, declare, intern and defonce/f/src/f/myns.clj",
;  :name mydef,
;  :ns #object[clojure.lang.Namespace 0x7e65cc4 "myns"]}

;; custom metadata and docstring ;;

(def ^{:created-at "date"}             ; custom metadata
  def-meta-doc                         ; name
  "A def with metadata and docstring." ; docstring
  1)                                   ; body

(clojure.repl/doc def-meta-doc)
; (out) -------------------------
; (out) myns/def-meta-doc
; (out)   A def with metadata and docstring.

(:created-at (meta (var def-meta-doc))) ; "date"

(comment
  (var def-meta-doc) ; #'myns/def-meta-doc

  (meta (var def-meta-doc)))
; {:created-at "date",
;  :line 27,
;  :column 1,
;  :file
;  "/home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/16 - Vars and Namespaces/01 - def, declare, intern and defonce/f/src/f/myns.clj",
;  :doc "A def with metadata and docstring.",
;  :name def-meta-doc,
;  :ns #object[clojure.lang.Namespace 0x7e65cc4 "myns"]}

;; ;;;;;;;
;; declare
;; ;;;;;;;

#_{:clj-kondo/ignore [:uninitialized-var]}
;; def without a body
(def unbound-var) ; #'myns/unbound-var

(type unbound-var) ; clojure.lang.Var$Unbound

;; state machine ;;

;; convey intention with `declare`
(declare state-one)

;; mutual recursive functions (checks for alternating 0s and 1s)
(def state-zero
  #(if (= \0 (first %))
     (state-one (next %))
     (if (nil? %) true false)))

(def state-one
  #(if (= \1 (first %))
     (state-zero (next %))
     (if (nil? %) true false)))

(state-zero "0100100001") ; false
(state-zero "0101010101") ; true

;; ;;;;;;
;; intern
;; ;;;;;;

;; `intern` works similarly to `def`, but offers the possibility to create definitions in other namespaces

(create-ns 'ext) ; #object[clojure.lang.Namespace 0x12f645f2 "ext"]

;; `create-ns` doesn't change namespace
*ns* ; #object[clojure.lang.Namespace 0x7e65cc4 "myns"]

(intern 'ext 'ext-var 1) ; #'ext/ext-var

((ns-map 'ext) 'ext-var) ; #'ext/ext-var

(comment
  ;; no automatic creation of the namespace
  (intern 'yet-to-exist 'a 1))
  ; (err) Execution error
  ; (err) No namespace: yet-to-exist found

;; `intern` is useful for all programmatic definitions of vars ;;

(def definitions
  {'ns1 [['a1 1] ['b1 2]]
   'ns2 [['a2 2] ['b2 2]]})

(defn defns [definitions]
  (for [[ns defs]   definitions
        [name body] defs]
    (do
      (create-ns ns)
      (intern ns name body))))

(defns definitions) ; (#'ns1/a1 #'ns1/b1 #'ns2/a2 #'ns2/b2)

#_{:clj-kondo/ignore [:unresolved-namespace]}
(comment
  ns1/a1  ; 1
  ns1/b1  ; 2
  ns2/a2  ; 2
  ns2/b2) ; 2

;; ;;;;;;;
;; defonce
;; ;;;;;;;

(def     redefine      "1")
(defonce dont-redefine "1")

#_{:clj-kondo/ignore [:redefined-var]}
(def     redefine      "2")
#_{:clj-kondo/ignore [:redefined-var]}
(defonce dont-redefine "2")

redefine      ; "2"
dont-redefine ; "1"


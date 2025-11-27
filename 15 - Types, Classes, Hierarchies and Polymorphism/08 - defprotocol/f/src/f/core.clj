(ns f.core
  (:require
   [criterium.core :refer [bench]]))

;; The `defprotocol` macro initializes a polymorphic dispatch mechanism for functions based on types.

;; generates a class with these functions
(defprotocol MyProtocol
  (method1 [this])
  (method2 [this]))

(type f.core.MyProtocol) ; java.lang.Class

(.getDeclaredMethods f.core.MyProtocol)
; [#object[java.lang.reflect.Method 0x19ea8a89 "public abstract java.lang.Object f.core.MyProtocol.method1()"],
;  #object[java.lang.reflect.Method 0x11e1eb24 "public abstract java.lang.Object f.core.MyProtocol.method2()"]]

MyProtocol ; a var holding data about the methods and their signatures
; {:on f.core.MyProtocol,
;  :on-interface f.core.MyProtocol,
;  :sigs
;  {:method1 {:tag nil, :name method1, :arglists ([this]), :doc nil},       (NOTE: generated dispatch functions)
;   :method2 {:tag nil, :name method2, :arglists ([this]), :doc nil}},      (NOTE: generated dispatch functions)
;  :var #'f.core/MyProtocol,
;  :method-map {:method1 :method1, :method2 :method2},
;  :method-builders                                                         (NOTE: method builders generate instances of the dispatching mechanism for that specific function)
;  {#'f.core/method2
;   #object[f.core$eval4209$fn__4210 0x430c228c "f.core$eval4209$fn__4210@430c228c"],
;   #'f.core/method1
;   #object[f.core$eval4209$fn__4221 0x78596fd5 "f.core$eval4209$fn__4221@78596fd5"]}}

;; generated functions
(fn? method1) ; true
(fn? method2) ; true

(comment
  ;; but no implementation
  ;; the generated functions contain the dispatch mechanism to find and invoke the right function based on the type of the first argument
  (method1 "arg"))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) No implementation of method: :method1 of protocol: #'f.core/MyProtocol found for class: java.lang.String

;; providing an implementation
(extend java.lang.String
  MyProtocol
  {:method1 #(.toUpperCase %)})

;; now it works
(method1 "arg") ; "ARG"

;; we cannot extend the protocol for a type that is already part of the protocol declaration ;;

(deftype MyProtocolImpl []
  MyProtocol
  (method1 [_this] "MyProtocolImpl::method1")
  (method2 [_this] "MyProtocolImpl::method2"))

(method1 (MyProtocolImpl.)) ; "MyProtocolImpl::method1"

(comment
  ;; extension not possible
  (extend MyProtocolImpl
    MyProtocol
    {:method1 (constantly "extend::method1")}))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) class f.core.MyProtocolImpl already directly implements interface f.core.MyProtocol for protocol:#'f.core/MyProtocol

;; ;;;;;;;;;;;;;;;;;;;;;;;
;; Instance-Based Dispatch
;; ;;;;;;;;;;;;;;;;;;;;;;;

;; not sure about possible applications for this feature

;; https://clojure.org/reference/protocols#_extend_via_metadata
;; https://medium.com/@flexianadevgroup/on-the-nature-of-clojure-protocols-74d6d15dc061 (Extend via metadata)

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defprotocol Foo
  :extend-via-metadata true ; must be explicitly enabled
  (foo [x]))
; Foo

;; instance based dispatch on the vector [42]
(foo (with-meta [42] {`foo (fn [x] (println x) :boo)})) ; :boo (NOTE: backticks for symbol `foo`)
; (out) [42]

(defprotocol Bench (m [this]))

(comment
  ;; direct type
  (deftype DirectType []
    Bench
    (m [_this]))

  ;; extended type
  (deftype     ExtendedType [])
  (extend-type ExtendedType
    Bench
    (m [_this]))

  ;; no performance difference at all
  (let [dt (DirectType.)]   (bench (m dt)))  ; (out) Execution time mean : 6.516205 ns
  (let [et (ExtendedType.)] (bench (m et)))) ; (out) Execution time mean : 6.672766 ns

(comment
  ;; no implementation for `m`
  (def dt (DirectType.))
  (m dt))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) No implementation of method: :m of protocol: #'f.core/Bench

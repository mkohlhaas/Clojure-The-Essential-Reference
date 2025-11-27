(ns f.core
  (:require
   [criterium.core :refer [bench]]))

(defprotocol MyProtocol
  (method1 [this])
  (method2 [this]))

(vec (.getDeclaredMethods f.core.MyProtocol))
; [#object[java.lang.reflect.Method 0x46dc1f73 "public abstract java.lang.Object f.core.MyProtocol.method1()"]
;  #object[java.lang.reflect.Method 0xcb3967d "public abstract java.lang.Object f.core.MyProtocol.method2()"]]

MyProtocol
; {:on f.core.MyProtocol,
;  :on-interface f.core.MyProtocol,
;  :sigs
;  {:method1 {:tag nil, :name method1, :arglists ([this]), :doc nil},
;   :method2 {:tag nil, :name method2, :arglists ([this]), :doc nil}},
;  :var #'f.core/MyProtocol,
;  :method-map {:method1 :method1, :method2 :method2},
;  :method-builders
;  {#'f.core/method2
;   #object[f.core$eval4209$fn__4210 0x430c228c "f.core$eval4209$fn__4210@430c228c"],
;   #'f.core/method1
;   #object[f.core$eval4209$fn__4221 0x78596fd5 "f.core$eval4209$fn__4221@78596fd5"]}}

(fn? method1) ; true
(fn? method2) ; true

(comment
  (method1 "arg"))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) No implementation of method: :method1 of protocol: #'f.core/MyProtocol found for class: java.lang.String

(extend java.lang.String
  MyProtocol
  {:method1 #(.toUpperCase %)})

(method1 "arg") ; "ARG"

(deftype MyProtocolImpl []
  MyProtocol
  (method1 [_this] "MyProtocolImpl::method1")
  (method2 [_this] "MyProtocolImpl::method2"))

(method1 (MyProtocolImpl.)) ; "MyProtocolImpl::method1"

(comment
  (extend MyProtocolImpl
    MyProtocol
    {:method1 (constantly "extend::method1")}))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) class f.core.MyProtocolImpl already directly implements interface f.core.MyProtocol for protocol:#'f.core/MyProtocol

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defprotocol Foo
  :extend-via-metadata true
  (foo [x]))
; Foo

(foo (with-meta [42] {`foo (fn [_x] :boo)})) ; :boo

(defprotocol Bench (m [this]))

(comment
  (deftype DirectBench [] Bench (m [_this]))

  (deftype LaterBench [])
  (extend-type LaterBench Bench (m [_this]))

  (let [db (DirectBench.)] (bench (m db)))  ; (out) Execution time mean : 6.516205 ns
  (let [lb (LaterBench.)]  (bench (m lb)))) ; (out) Execution time mean : 6.672766 ns

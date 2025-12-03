;; 1.clj

(def a 1)
(ns-name (.ns #'a))
;; user

(ns ns1)

(def b 1)
(ns-name (.ns #'b))
;; ns1

;; 2.clj

(create-ns 'a)

(ns my.ns
  (:refer a)
  (:refer-clojure :exclude [+ - * /])
  (:import java.util.Date)
  (:require (clojure.set))
  (:use (clojure.xml))
  (:gen-class))

;; 3.clj

new-ns/var-def
;; CompilerException java.lang.RuntimeException: No such namespace: new-ns

(contains? (set (map ns-name (all-ns))) 'new-ns)
;; false

(create-ns 'new-ns)
(intern 'new-ns 'var-def "now it's working")
(contains? (ns-map 'new-ns) 'var-def)
;; true

new-ns/var-def
;; "now it's working"

(contains? (set (map ns-name (all-ns))) 'new-ns)
;; true

('Integer (ns-map 'new-ns))
;; java.lang.Integer

;; 4.clj

(remove-ns 'new-ns)

new-ns/var-def
;; CompilerException java.lang.RuntimeException: No such namespace: new-ns

;; 5.clj

(create-ns 'disappear)
(intern 'disappear 'my-var 0)
(refer 'disappear :only ['my-var])
;; nil

my-var
;; 0

(remove-ns 'disappear)
;; #object[clojure.lang.Namespace 0x1f780201 "disappear"]

(.ns #'my-var)
;; #object[clojure.lang.Namespace 0x1f780201 "disappear"]

(create-ns 'disappear)
(intern 'disappear 'my-var 1)

my-var
;; 0

@#'disappear/my-var
;; 1


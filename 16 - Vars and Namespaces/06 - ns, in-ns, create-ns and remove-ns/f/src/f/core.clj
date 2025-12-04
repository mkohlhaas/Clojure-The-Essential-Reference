(ns f.core)

;; 1.clj

(def a 1)
(ns-name (.ns #'a)) ; f.core

;; (ns ns1)

;; this example doesn't translate in a leiningen project
(def b 1)
(ns-name (.ns #'b)) ; ns1

;; 2.clj

(create-ns 'a)

;; `ns` supports a large set of options to alter mappings and aliases at the time the namespace is created:
;; (ns my.ns
;;   (:refer a)
;;   (:refer-clojure :exclude [+ - * /])
;;   (:import java.util.Date)
;;   (:require (clojure.set))
;;   (:use (clojure.xml))
;;   (:gen-class))

(comment
  #_{:clj-kondo/ignore [:unresolved-namespace]}
  new-ns/var-def)
  ; (err) No such namespace: new-ns

(contains? (set (map ns-name (all-ns))) 'new-ns) ; false

(create-ns 'new-ns)                           ; #object[clojure.lang.Namespace 0x7b6d6310 "new-ns"]
(intern 'new-ns 'var-def "now it's working")
(contains? (ns-map 'new-ns) 'var-def)         ; true
#_{:clj-kondo/ignore [:unresolved-namespace]}
new-ns/var-def                                ; "now it's working"

(contains? (set (map ns-name (all-ns))) 'new-ns) ; true
('Integer (ns-map 'new-ns))                      ; java.lang.Integer 
                                                 ; Java classes belonging to the java.lang.* namespace are immediately available

(remove-ns 'new-ns)

(comment
  #_{:clj-kondo/ignore [:unresolved-namespace]}
  new-ns/var-def)
  ; (err) No such namespace: new-ns

;; `remove-ns` should be used sparingly, because it could be still referenced in other namespaces, generating all sort of problems.
;;  If a namespace is referenced by another namespace, the garbage collector would be unable to claim the corresponding object reference.
;;  Even more dangerously, if the same namespace is created again, there could be a mix of namespaces pointing at the old reference and the new.

(create-ns 'disappear)
(intern    'disappear 'my-var 0)
(refer     'disappear :only ['my-var])
;; nil

#_{:clj-kondo/ignore [:unresolved-symbol]}
my-var ; 0

(remove-ns 'disappear) ; but `my-var` keeps a reference to the namespace which can't be garbage collected

#_{:clj-kondo/ignore [:unresolved-symbol]}
(.ns #'my-var) ; #object[clojure.lang.Namespace 0x12271fdc "disappear"]

(create-ns 'disappear)        ; recreate
(intern 'disappear 'my-var 1) ; this time different value

#_{:clj-kondo/ignore [:unresolved-symbol]}
my-var              ; 0 (referencing the old value)

#_{:clj-kondo/ignore [:unresolved-namespace]}
@#'disappear/my-var ; 1

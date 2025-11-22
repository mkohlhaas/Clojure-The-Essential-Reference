(ns f.core)

;; Clojure offers two types of sets
;; 1. hash-sets (reader sytax `#{}`)
;; 2. sorted-sets

 ;; `disj`. `conj` and `into` also works on sets 

(type #{1 2 3})           ; clojure.lang.PersistentHashSet
(type (sorted-set 5 3 1)) ; clojure.lang.PersistentTreeSet

(ifn? #{1 2 3})           ; true
(ifn? (sorted-set 5 3 1)) ; true

(some #{1 2 3} [0 4 6 8 1]) ; 1
((sorted-set 5 3 1) 1)      ; 1

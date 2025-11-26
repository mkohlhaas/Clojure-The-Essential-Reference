(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(symbol  "s")  ;  s
(keyword "k") ; :k

(type (symbol  "s")) ; clojure.lang.Symbol
(type (keyword "k")) ; clojure.lang.Keyword

;; Clojure tries to lookup symbols in the current namespace or local context

;; `first` is in the clojure.core namespace
first  ; #object[clojure.core$first__5470 0x6979d53e "clojure.core$first__5470@6979d53e"]

;; `a` is in the local context
(let [a 1] (inc a)) ; 2

(def form (read-string "(a b)")) ; (a b)

(type form) ; clojure.lang.PersistentList

;; text becomes symbols 
(map type form) ; (clojure.lang.Symbol clojure.lang.Symbol)

;; a macro ransforms a string into Clojure data structures
(defmacro reading-symbols [& symbols]
  `(map type '~symbols))

#_{:clj-kondo/ignore [:unresolved-symbol]}
;; non-evaluated form appears to the macro as a list of symbols
(reading-symbols a b)  ; (clojure.lang.Symbol clojure.lang.Symbol)

;; using `/` for separating namespaces
(def ax (symbol "a/x")) ; a/x (x in the namespace a)
(def bx (symbol "b/x")) ; b/x (x in the namespace b)

;; they have the same name 
[(name ax) (name bx)] ; ["x" "x"]

;; they are different symbols
(= ax bx) ; false

;; they live in different namespaces
[(namespace ax) (namespace bx)] ; ["a" "b"]

;; two-argument constructor
(def ax1 (symbol  "a" "x")) ;  a/x
(def bx1 (keyword "b" "x")) ; :b/x

[(namespace ax1) (namespace bx1)]  ; ["a" "b"]

(identical? (symbol  "a") (symbol  "a")) ; false
(identical? (keyword "a") (keyword "a")) ; true  (keywords are cached, called "interning")

(comment
  ;; symbols are created more quickly than keywords that use a caching mechanism
  (quick-bench (symbol  "a"))  ; (out) Execution time mean : 33.254894 ns
  (quick-bench (keyword "a"))) ; (out) Execution time mean : 87.049318 ns

;; check whether a keyword has already been created
(find-keyword "never-created")  ; nil
(find-keyword "doc")            ; :doc (doc is used in the standard library to add documentation to functions)

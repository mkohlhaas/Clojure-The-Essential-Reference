(ns f.grammar)

;; (defn ^<metamap>? <name> fdecl)
;;
;; fdecl :=> <docstring>? <metamap>? arities <metamap>?
;;
;; arities :=> ^<metamap>? [arity] body
;; OR
;; (^<metamap>? [arity1] body)
;; (^<metamap>? [arity2] body)
;; ..
;; (^<metamap>? [arityN] body)
;;
;; arity :=> <ret-typehint>? [<arg1-typehint>? <arg1>
;;                            ..
;;                            <argN-typehint>? <argN>]
;;
;; body :=> <metamap> <forms>

#_{:clj-kondo/ignore [:unused-value]}
(defn ^{:t1 1} foo
  "docstring"
  {:t2 2} (^{:t3 3} [a b] {:t4 4} (+ a b))
  {:t5 5})

(meta #'foo)
; {:ns #object[clojure.lang.Namespace 0x6784f606 "f.grammar"],
;  :name foo,
;  :t2 2,
;  :file
;  "/home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/02-CreatingAndManipulatingFunctions/1-FunctionDefinition/1-defn-and-defn-/f/src/f/grammar.clj",
;  :column 1,
;  :line 21,
;  :arglists ([a b]),
;  :doc "docstring",
;  :t1 1,
;  :t5 5}

(meta (first (:arglists (meta #'foo)))) ; {:t3 3, :t4 4}

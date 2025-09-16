#_{:clj-kondo/ignore [:unused-value]}
(defn ^{:t1 1} foo
  "docstring"
  {:t2 2} (^{:t3 3} [a b] {:t4 4} (+ a b))
  {:t5 5})

(meta #'foo)
; {:ns #object[sci.lang.Namespace 0x117eb996 "user"],
;  :name foo,
;  :t2 2,
;  :file
;  "/home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/02-CreatingAndManipulatingFunctions/1-FunctionDefinition/1-defn-and-defn-/05.clj",
;  :column 1,
;  :line 1,
;  :arglists ([a b]),
;  :doc "docstring",
;  :t1 1,
;  :t5 5}

(meta (first (:arglists (meta #'foo))))
; {:t3 3, :t4 4}

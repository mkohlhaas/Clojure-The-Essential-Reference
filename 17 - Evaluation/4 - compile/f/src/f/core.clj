(ns f.core)

(spit (str *compile-path* "/source.clj")
      "(ns source)
   (defn plus [x y] (+ x y))")

(comment
  *compile-path*)
  ; "/home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/17 - Evaluation/4 - compile/f/target/classes"

;; `compile` returns the symbol of the class that was just created
(compile 'source) ; source

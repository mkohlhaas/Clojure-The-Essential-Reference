(ns f.core)

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn repeatedly* [f]
  (lazy-seq (cons (f) (repeatedly* f))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn iterate* [f x]
  (lazy-seq (cons x (iterate* f (f x)))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn repeat* [x]
  (lazy-seq (cons x (repeat* x))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn cycle* [coll]
  ((fn step [[x & xs]]
     (lazy-seq
      (if x
        (cons x (step xs))
        (cycle* coll)))) coll))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn range* [n]
  ((fn step [x]
     (lazy-seq
      (when (< x n)
        (cons x (step (inc x)))))) 0))

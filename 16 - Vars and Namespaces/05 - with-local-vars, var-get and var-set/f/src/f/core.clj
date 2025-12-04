(ns f.core)

;; - `with-local-vars` creates a thread local scope where vars can be created and mutated
;; -  along with `var-get` and `var-set`, `with-local-vars` allows an imperative, thread-safe style of programming
;; - `with-local-vars` is rarely seen in real-life Clojure code (because ot its imperative style)

(defn ++ [v]
  (var-set v (inc (var-get v))))

(defn count-even [xs]
  (with-local-vars [a 0]
    (doseq [x xs]
      (when (zero? (rem x 2))
        (++ a)))
    @a))

(count-even (range 10)) ; 5

(ns f.core)

;; ;;;;;;;;;;;;;;;;;;;;
;; Implementation Notes
;; ;;;;;;;;;;;;;;;;;;;;

;; with `cons` and `lazy-seq`

(defn repeatedly* [f]
  (lazy-seq (cons (f) (repeatedly* f))))

(defn iterate* [f x]
  (lazy-seq (cons x (iterate* f (f x)))))

(defn repeat* [x]
  (lazy-seq (cons x (repeat* x))))

(defn cycle* [coll]
  ((fn step [[x & xs]]
     (lazy-seq
      (if x
        (cons x (step xs))
        (cycle* coll)))) coll))

(defn range* [n]
  ((fn step [x]
     (lazy-seq
      (when (< x n)
        (cons x (step (inc x)))))) 0))

(take 10 (repeatedly* #(inc 0))) ; (1 1 1 1 1 1 1 1 1 1)
(take 10 (iterate* inc 0))       ; (0 1 2 3 4 5 6 7 8 9)
(take 10 (repeat* 1))            ; (1 1 1 1 1 1 1 1 1 1)
(take 10 (cycle* '(1 2 3)))      ; (1 2 3 1 2 3 1 2 3 1)
(range* 10)                      ; (0 1 2 3 4 5 6 7 8 9)

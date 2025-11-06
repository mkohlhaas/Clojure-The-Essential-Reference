(ns f.core)

(lazy-seq '(1 2 3))       ; (1 2 3)

(type (lazy-seq [1 2 3])) ; clojure.lang.LazySeq

(seq? (lazy-seq [1 2 3])) ; true

(lazy-seq 1 2 [3])        ; (3)

(def output (lazy-seq (println "evaluated") '(1 2 3)))
; evaluated
; (1 2 3)

(first output)  ; 1
; evaluated
; 1

(defn trace [x]
  (println "evaluating" x) x)

#_{:clj-kondo/ignore [:redefined-var]}
(def output (lazy-seq (list (trace 1) 2 3)))

(first output)
; (out) evaluating 1
; 1

(first output)
; 1

(comment
  (lazy-seq 1 2 3))
  ; (err) Don't know how to create ISeq from: java.lang.Long

(defn eager-map [f coll]
  (when-first [x coll]
    (println "iteration")
    (cons (f x)
          (eager-map f (rest coll)))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(def eager-out (eager-map str '(0 1 2)))
; (out) iteration
; (out) iteration
; (out) iteration

(defn lazy-map [f coll]
  (lazy-seq
   (when-first [x coll]
     (println "iteration")
     (cons (f x)
           (lazy-map f (rest coll))))))

(def lazy-out (lazy-map str '(0 1 2)))

(first lazy-out)
; (out) iteration
; "0"

(defn sieve [n]
  (letfn [(divisor-of? [m]
            #(zero? (rem % m)))
          (step [[x & xs]]
            (lazy-seq (cons x (step (remove (divisor-of? x) xs)))))]
    (take n (step (nnext (range))))))

(sieve 10) ; (2 3 5 7 11 13 17 19 23 29)

(macroexpand
 '(lazy-seq
   (when coll
     (cons
      (f (first coll))
      (lazy-map f (next coll))))))

;; (new clojure.lang.LazySeq
;;   (fn* []
;;     (when coll
;;       (cons
;;         (f (first coll))
;;         (lazy-map f (next coll))))))

(defn lazy-bomb [[x & xs]]
  (letfn [(step [[y & ys]]
            (lazy-seq
             (when y
               (cons y (step ys)))))]
    (lazy-seq
     (when x
       (cons x (lazy-bomb (step xs)))))))

(comment
  (last (lazy-bomb (range 10000))))
  ; (err) Execution error (StackOverflowError)

#_{:clj-kondo/ignore [:redefined-var]}
(defn sieve [n]
  (letfn [(remove-step [x [y & ys]]
            (lazy-seq
             (when y
               (if (zero? (rem y x))
                 (remove-step x ys)
                 (cons y (remove-step x ys))))))
          (sieve-step [[x & xs]]
            (lazy-seq
             (cons x (sieve-step (remove-step x xs)))))]
    (take n (sieve-step (nnext (range))))))

(sieve 10) ; (2 3 5 7 11 13 17 19 23 29)
;; (2 3 5 7 11 13 17 19 23 29)

(sieve 10000000)
;; StackOverflowError ???

#_{:clj-kondo/ignore [:redefined-var]}
(defn sieve [n]
  (letfn [(odds-from [n]
            (iterate #(+ 2 %) (if (odd? n) (+ 2 n) (+ 1 n))))
          (divisor? [p]
            #(zero? (rem p %)))
          (cross-upto [n primes]
            (take-while #(<= (* % %) n) primes))]
    (loop [cnt (dec n) primes [2]]
      (if (pos? cnt)
        (recur (dec cnt)
               (conj primes
                     (first (drop-while
                             #(some (divisor? %) (cross-upto % primes))
                             (odds-from (peek primes))))))
        primes))))

(peek (sieve 10000)) ; 104729

(defn squares [x]
  (cons (* x x) (lazy-seq (squares (* x x)))))

(def sq2 (squares 2))

(take 5 sq2) ; (4 16 256 65536 4294967296)

(comment
  (take 6 sq2)
  ; (err) long overflow

  (take 6 sq2))
  ; (err) Error printing return value (NullPointerException)

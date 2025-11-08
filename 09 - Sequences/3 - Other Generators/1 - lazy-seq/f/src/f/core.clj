(ns f.core)

(lazy-seq '(1 2 3))       ; (1 2 3)
(type (lazy-seq [1 2 3])) ; clojure.lang.LazySeq
(seq? (lazy-seq [1 2 3])) ; true
(lazy-seq 1 2 [3])        ; (3)

;; delayed evaluation

(def output (lazy-seq (println "evaluated") '(1 2 3)))

(first output) ; 1        (out) evaluated
output         ; (1 2 3)
output         ; (1 2 3)

;; caching 

(defn trace [x]
  (println "evaluating" x)
  x)

(def output-1 (lazy-seq (list (trace 1) 2 3)))

(first output-1) ; 1 (out) evaluating 1
(first output-1) ; 1

(comment

  ;; `lazy-seq` accepts any number of arguments that are treated as an 
  ;; implicit do block. The result of the evaluation of the do block 
  ;; needs to return a sequential collection.
  (lazy-seq 1 2 3))
  ; (err) Don't know how to create ISeq from: java.lang.Long

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn eager-map [f coll]
  (when-first [x coll]
    (println "iteration")
    (cons (f x) (eager-map f (rest coll)))))

(def eager-out (eager-map str '(0 1 2)))
; (out) iteration
; (out) iteration
; (out) iteration

eager-out ; ("0" "1" "2")

;; idiomatic/canonical usage of `lazy-seq` and `cons`
(defn lazy-map [f coll]
  (lazy-seq ; only difference from `eager-map`
   (when-first [x coll]
     (println "iteration")
     (cons (f x) (lazy-map f (rest coll))))))

(def lazy-out (lazy-map str '(0 1 2)))

(first lazy-out) ; "0"
; (out) iteration

;; The general recursive pattern to build lazy sequences

;; typical pattern for creating a lazy list
(defn myfn [xs]
  (lazy-seq
   (when xs
     (cons (first xs)
           (myfn (next xs))))))

(def my-list (myfn (range 10)))

(first my-list) ; 0
my-list ; (0 1 2 3 4 5 6 7 8 9)

;; Sieve of Eratosthenes

(defn sieve [n]
  (letfn [(divisor-of? [m]
            #(zero? (rem % m)))
          (primes [[x & xs]]
            (lazy-seq (cons x (primes (remove (divisor-of? x) xs)))))]
    (take n (primes (nnext (range))))))

(comment
  ;; starts at 2
  (take 20 (nnext (range)))) ; (2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21)

(sieve 20) ; (2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71)

(comment
  (last (sieve 1000000)))
  ; (err) Execution error (StackOverflowError)

;; Object allocation in Java happens on the heap.
;; `lazy-seq` transforms a stack-consuming into heap-consuming algorithm.
;; `lazy-seq` produces linear, heap-consuming lazy sequences.
(macroexpand
 '(lazy-seq
   (when coll
     (cons
      (f (first coll))
      (lazy-map f (next coll))))))
;; (new
;;  clojure.lang.LazySeq
;;  (fn* [] (when coll (cons (f (first coll)) (lazy-map f (next coll))))))

;; Nesting recursive lazy sequence generators can blow the stack.
(defn lazy-bomb [[x & xs]]
  (letfn [(step [[y & ys]]
            (lazy-seq ; first lazy sequence
             (when y
               (cons y (step ys)))))]
    (lazy-seq ; second lazy sequence (misplaced `lazy-seq`)
     (when x
       (cons x (lazy-bomb (step xs)))))))

(comment
  (last (lazy-bomb (range 10000))))
  ; (err) Execution error (StackOverflowError)

;; The `sieve` function presented before contains an accidental nesting disguised as a `remove` call.
(defn sieve-stackoverflow [n]
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

(sieve-stackoverflow 10) ; (2 3 5 7 11 13 17 19 23 29)

(comment
  ;; same as `sieve` before but making everything explicit
  (sieve-stackoverflow 1000000000000000000))
  ; (err) Execution error (StackOverflowError)

(defn sieve-tail-recursive [n]
  (letfn [(odds-from [n]
            (iterate #(+ 2 %) (if (odd? n) (+ 2 n) (+ 1 n))))
          (divisor? [p]
            #(zero? (rem p %)))
          (cross-upto [n primes]
            (take-while #(<= (* % %) n) primes))]
    (loop [cnt    (dec n)
           primes [2]]
      (if (pos? cnt)
        (recur (dec cnt)
               (conj primes
                     (first (drop-while
                             #(some (divisor? %) (cross-upto % primes))
                             (odds-from (peek primes))))))
        primes))))

(peek (sieve-tail-recursive 10000)) ; 104729

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn squares [x]
  (cons (* x x) (lazy-seq (squares (* x x)))))

(def sq2 (squares 2))

(take 5 sq2) ; (4 16 256 65536 4294967296)

(comment
  ;; ArithmeticException
  (take 6 sq2)
  ; (err) Error printing return value (ArithmeticException)
  ; (err) long overflow

  ;; NullPointerException
  (take 6 sq2))
  ; (err) Error printing return value (NullPointerException)

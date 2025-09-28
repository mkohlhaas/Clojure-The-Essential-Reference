(ns f.core
  (:require [criterium.core :refer [bench]]
            [no.disassemble :refer [disassemble]]))

;; idiomatic functional style
(loop [i 0
       s []]
  (if (< i 10)
    (recur (inc i) (conj s (* i i)))
    s))
; [0 1 4 9 16 25 36 49 64 81]

;; non-idiomatic imperative style
(let [i (atom 0)
      s (atom [])]
  (while (< @i 10)
    (swap! s #(conj % (* @i @i)))
    (swap! i inc))
  @s)
; [0 1 4 9 16 25 36 49 64 81]

;; ;;;;;;;;;
;; Fibonacci
;; ;;;;;;;;;

;; not tail-recursive
(defn fib1 [n]
  (if (<= 0 n 1)
    n
    (+ (fib1 (- n 1)) (fib1 (- n 2)))))

(map fib1 (range 10))
; (0 1 1 2 3 5 8 13 21 34)

;; tail-recursive
;; function definition is recursion target
(defn fib2 [a b cnt]
  (if (zero? cnt)
    b
    (recur (+ a b) a (dec cnt))))

(map (partial fib2 1 0) (range 10))
; (0 1 1 2 3 5 8 13 21 34)

;; tail-recursive
;; loop definition is recursion target
;; NOTE: possible target: loop | defn | defn- | fn | fn* | #()
(defn fib3 [n]
  (loop [a 1
         b 0
         cnt n]
    (if (zero? cnt)
      b
      (recur (+ a b) a (dec cnt)))))

(map fib3 (range 10))
; (0 1 1 2 3 5 8 13 21 34)

(map
 (partial
  (fn [a b cnt]
    (if (zero? cnt)
      b
      (recur (+ a b) a (dec cnt)))) 1 0)
 (range 10))
; (0 1 1 2 3 5 8 13 21 34)

;; also possible with anonymous functions/function literals but almost unreadable
(map
 (partial
  #(if (zero? %3)
     %2
     (recur (+ %1 %2) %1 (dec %3))) 1 0)
 (range 10))
;; (0 1 1 2 3 5 8 13 21 34)

;; ;;;;;;;;;;;
;; Square Root
;; ;;;;;;;;;;;

(set! *warn-on-reflection* true)

(defn lazy-root [^double x]
  (->> 1.
       (iterate #(/ (+ (/ x %) %) 2))
       (filter #(< (Math/abs (- (* % %) x)) 1e-8))
       first))

(defn sq-root [x]
  (cond
    (or (zero? x) (= 1 x)) x
    (neg? x) Double/NaN
    :else (lazy-root x)))

(sq-root 2)
; 1.4142135623746899

;; ;;;;;;;;;;;;
;; Benchmarking
;; ;;;;;;;;;;;;

;; Clojure doesn’t offer automatic tail-recursive optimization, but 
;; can optimize tail recursion with the loop-recur construct. It 
;; would be relatively simple to have an automatic way to detect 
;; tail-call optimizable code, but Clojure prefers to rely on Java 
;; semantic for method calls and Java doesn’t implement tail-call 
;; optimization.

(comment
  (bench (lazy-root 2.))
  ; (out) Execution time mean : 1.007627 µs

  (bench (Math/sqrt 2.)))
  ; (out) Execution time mean : 3.084960 ns

;; loop-recur for performance
(defn recursive-root [x]
  (loop [guess 1.] ; guess is a double (no type hinting necessary)
    (if (> (Math/abs (- (* guess guess) x)) 1e-8)
      (recur (/ (+ (/ x guess) guess) 2.)) ; guess is a double
      guess)))

(comment
  (bench (recursive-root 2.))
  ; (out) Execution time mean : 65.713904 ns

  ;; no type hint
  (println (disassemble (fn [n] (loop [i 0] (< i n) (inc i)))))

  ;; // Compiled from form-init72854.clj (version 1.5 : 49.0, super bit)
  ;; // some details removed for clarity
  ;; public final class user$eval444$fn__445 extends clojure.lang.AFunction {
  ;;
  ;; public java.lang.Object invoke(java.lang.Object n);
  ;;  0  lconst_0
  ;;  1  lstore_2 [i]
  ;;  2  lload_2 [i]
  ;;  3  aload_1 [n]
  ;;  4  invokestatic clojure.lang.Numbers.lt(long, java.lang.Object) ; <3>
  ;;  7  pop
  ;;  8  lload_2 [i]
  ;;  9  invokestatic clojure.lang.Numbers.inc(long) : long [21] ; <4>
  ;; 15  areturn
  ;; }

  ;; with type hint
  (println (disassemble (fn [^long n] (loop [i 0] (< i n) (inc i))))))

  ;; // Compiled from form-init789662854.clj (version 1.5 : 49.0, super bit)
  ;; // some details removed for clarity
  ;; public final class user$eval448$fn__449 extends clojure.lang.AFunction {
  ;;
  ;; public final java.lang.Object invokePrim(long n);
  ;;  0  lconst_0
  ;;  1  lstore_3 [i]
  ;;  2  lload_3 [i]
  ;;  3  lload_1 [n]
  ;;  4  invokestatic clojure.lang.Numbers.lt(long, long) : boolean [19]       ; <1>
  ;;  7  pop
  ;;  8  lload_3 [i]
  ;;  9  invokestatic clojure.lang.Numbers.inc(long) : long [23]
  ;; 15  areturn
  ;; }

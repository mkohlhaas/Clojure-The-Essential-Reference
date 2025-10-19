(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(dotimes [i 3] ; NOTE: bindings must be a vector of two elements only
  (println i))
; (out) 0
; (out) 1
; (out) 2

(comment
  ;; `dotimes` expands to a loop-recur form
  (macroexpand '(dotimes [i 3] (println i))))
  ; (let* [n (long 3)]
  ;   (loop [i 0]
  ;     (when (< i n)
  ;       (println i)
  ;       (recur (unchecked-inc i)))))

(time (dotimes [_ 1000000]
        (apply max (range 100)))) ; 99
; (out) "Elapsed time: 5568.569052 msecs"

(comment
  (defn fizz-buzz-for [n]
    (condp #(zero? (mod %2 %1)) n
      15 "fizzbuzz"
      3  "fizz"
      5  "buzz"
      n))

  (defn fizz-buzz-slow [n]
    (doall (map fizz-buzz-for (range n))))

  ;; alternative implementation with `dotimes`
  (defn fizz-buzz [n]
    (let [res (transient [])]
      (dotimes [i n]
        (assoc! res i (fizz-buzz-for i)))
      (persistent! res)))

  (quick-bench (fizz-buzz-slow 1000))
  ; (out) Execution time mean : 346.187848 µs

  (quick-bench (fizz-buzz 1000)))
  ; (out) Execution time mean : 327.471867 µs

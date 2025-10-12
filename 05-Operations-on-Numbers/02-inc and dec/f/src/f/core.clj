(ns f.core
  (:require [criterium.core :refer [bench]]))

(inc 1) ; 2
(dec 1) ; 0

(defn instrument [f]
  (let [calls (atom 0)]
    (fn [& args]
      (if (= "secret" (first args))
        @calls
        (do (swap! calls inc)
            (apply f args))))))

(def say-hello
  (instrument #(println "hello" %)))

(say-hello "john") ; nil
; (out) hello john

(say-hello "laura") ; nil
; (out) hello laura

(say-hello "secret")
; 2

(comment
  (inc Long/MAX_VALUE))
  ; (err) Execution error (ArithmeticException)
  ; (err) long overflow

Long/MAX_VALUE                 ;  9223372036854775807
(unchecked-inc Long/MAX_VALUE) ; -9223372036854775808

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn slower [n]
  (loop [n (int n) i 0]
    (if (< i n)
      (recur n (inc i))
      i)))

(defn faster [n]
  (loop [n (int n) i 0]
    (if (< i n)
      (recur n (unchecked-inc i))
      i)))

(comment
  (bench (slower 10000))
  ; (out) Execution time mean : 5.149268 µs

  (bench (faster 10000)))
  ; (out) Execution time mean : 3.181401 µs

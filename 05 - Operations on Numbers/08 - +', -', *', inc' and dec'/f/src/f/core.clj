(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(+' Double/MAX_VALUE Double/MAX_VALUE)                   ; ##Inf
(+' (bigdec Double/MAX_VALUE) (bigdec Double/MAX_VALUE)) ; 3.5953862697246314E+308M
(+  (bigdec Double/MAX_VALUE) (bigdec Double/MAX_VALUE)) ; 3.5953862697246314E+308M

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Diffie-Hellman Key-Exchange Algorithm
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn genbig [n]
  (->> #(rand-int 10)
       (repeatedly n)
       (apply str)
       (BigInteger.)
       bigint))

(comment
  (genbig 10)) ; 4758710651N

;; convert to Java BigInteger
(defn I [n] (.toBigInteger n))

(comment
  (I 16N)) ; 16

(defn prime?  [n accuracy]
  (.isProbablePrime (.toBigInteger (bigint n)) accuracy))

(defn next-prime [n]
  (loop [candidate n]
    (if (prime? candidate 10)
      candidate
      (recur (if (neg? candidate)
               (dec' candidate)
               (inc' candidate))))))

;; using Java's modPow
(defn- mod-pow [b e m]
  (bigint (.modPow (I b) (I e) (I (next-prime m)))))

(defn public-share [base secret modulo]
  (mod-pow base secret modulo))

(defn shared-secret [public secret modulo]
  (mod-pow public secret modulo))

(def modulo-pub (genbig 30))
(def base-pub   (genbig 30))

(def a-pub (public-share base-pub 123456789N modulo-pub))
(def b-pub (public-share base-pub 987654321N modulo-pub))

(def a-common-secret (shared-secret b-pub 123456789N modulo-pub))
(def b-common-secret (shared-secret a-pub 987654321N modulo-pub))

(= a-common-secret b-common-secret) ; true

(type 2M) ; java.math.BigDecimal
(type 2N) ; clojure.lang.BigInt

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (quick-bench (loop [i 0] (if (< i 10000) (recur (inc i)) i)))
  ; (out) Execution time mean : 7.885625 µs

  (quick-bench (loop [i 0] (if (< i 10000) (recur (inc' i)) i))))
  ; (out) Execution time mean : 461.362246 µs

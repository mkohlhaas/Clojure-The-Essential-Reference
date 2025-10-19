(ns f.core
  (:require
   [clojure.string :refer [split]]
   [criterium.core :refer [bench]]
   [no.disassemble :refer [disassemble]]))

;; You should take particular care using case with test expressions other than numbers, strings and keywords.

;; Compared to other conditional forms, case is specifically designed with performance in mind.
;; `case` implementation compiles into the optimized "tableswitch" JVM bytecode instruction.

(let [n 1]
  (case n
    0 "O"
    1 "l"
    4 "A"))
; "l"

(let [n 1]
  (case n
    (inc 0) "inc"
    (dec 1) "dec"
    :none))
; "dec"

(comment
  (case 'pi
    'alpha \α
    'beta  \β
    'pi    \π)
  ; (err) Duplicate case test constant: quote

  (macroexpand ''alpha)
  ; 'alpha
  ; (quote alpha)

  (case 'pi
    (quote alpha) \α
    (quote beta)  \β
    (quote pi)    \π))
  ; (err) Duplicate case test constant: quote

(case 'pi
  alpha \α
  beta  \β
  pi    \π) ; \π
; \π

;; ;;;;;;;;;;
;; Calculator
;; ;;;;;;;;;;

(defn error [& args]
  (println "Unrecognized operator for" args))

(defn operator [op]
  (case op
    ("+" "plus"  "sum")      + ; using lists for multiple options
    ("-" "minus" "subtract") -
    ("*" "x"     "times")    *
    ("/" "÷"     "divide")   /
    error))

(defn execute [arg1 op arg2]
  ((operator op) (Integer/valueOf arg1) (Integer/valueOf arg2)))

(defn calculator [s]
  (let [[arg1 op arg2] (split s #"\s+")]
    (execute arg1 op arg2)))

(calculator "10 ÷ 5") ; 2
(calculator "10 / 5") ; 2
(calculator "10 x 5") ; 50

;; ;;;;;;;;;;;;;;;;;;;;
;; Vim Cursor Movements
;; ;;;;;;;;;;;;;;;;;;;;

(defn score [ks]
  (case ks
    [\k \k \k \k \l \l \l \l] 5 ; we can't use lists here!
    [\4 \k \4 \l] 10
    0))

(defn check [s]
  (score (seq s)))

(check "kl")       ; 0
(check "kkkkllll") ; 5
(check "4k4l")     ; 10

;; ;;;;;;;;;;;;
;; Benchmarking
;; ;;;;;;;;;;;;

(comment
  (defn c1 [n]
    (cond
      (= n 0) "0" (= n 1) "1"
      (= n 2) "2" (= n 3) "3"
      (= n 4) "4" (= n 5) "5"
      (= n 6) "6" (= n 7) "7"
      (= n 8) "8" (= n 9) "9"
      :else :none))

  (bench (c1 9)) ; "9"
  ; (out) Execution time mean : 415.626572 ns

  (defn c2 [n]
    (case n
      0 "0" 1 "1"
      2 "2" 3 "3"
      4 "4" 5 "5"
      6 "6" 7 "7"
      8 "8" 9 "9"
      :default))

  (bench (c2 9)))
  ; (out) Execution time mean : 55.729286 ns

(defn c1 [n]
  (case n
    127 "127"
    128 "128"
    :none))

(c1 127) ; "127"
(c1 128) ; "128"

(defn c2 [n]
  (cond
    (identical? n 127) "127"
    (identical? n 128) "128"
    :else :none))

(c2 127) ; "127"
(c2 128) ; :none NOTE: internal JVM caching of boxed Integers only being available up to 127!!!

(macroexpand
 '(case a 0 "0" 1 "1" :default))

;; (let*
;;   [G__759 a]
;;     (case* G__759
;;       0 0 :default
;;       {0 [0 "0"], 1 [1 "1"]}
;;       :compact :int))

(comment
  (println
   (disassemble
    #(let [a 8] (case a 0 "0" 1 "1" :default)))))

;; [...] ; <3>
;;  0  ldc2_w <Long 8> [12]
;;  3  lstore_1 [a]
;;  4  lload_1 [a]
;;  5  lstore_3 [G__22423]
;;  6  lload_3 [G__22423]
;;  7  l2i
;;  8  tableswitch default: 54
;;       case 0: 32
;;       case 1: 43
;; ;; [...]

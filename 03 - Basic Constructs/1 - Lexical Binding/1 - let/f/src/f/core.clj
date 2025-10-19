(ns f.core
  (:require
   [no.disassemble :refer [disassemble]]))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; `let` as Syntactic Sugar for Lambda Function Invokation
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(let [a 1 b 2] (* (+ a b) b))  ; 6

((fn [a b] (* (+ a b) b)) 1 2) ; 6

;; type hints are not allowed
;; (let [^long i 0]
;;   (println i))
; (err) Can't type hint a local with a primitive initializer

;; ;;;;;;;
;; Example
;; ;;;;;;;

(let [x (rand-int 10)]
  (if (>= x 5)
    (str x " is above the average")
    (str x " is below the average")))
; "0 is below the average"
; "1 is below the average"
; "9 is above the average"
; …

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game Example - Rock, Paper, Scissor
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn rule [moves]
  (let [[p1 p2] moves]
    (cond
      (= p1 p2)                             "tie game"
      (every? #{"rock"    "paper"}   moves) "paper wins over rock"
      (every? #{"scissor" "rock"}    moves) "rock wins over scissor"
      (every? #{"paper"   "scissor"} moves) "scissor wins over paper"
      :else                                 "computer can't win that!")))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn game-loop []
  (println "Rock, paper or scissors?")
  (let [human (read-line)
        ai    (rand-nth ["rock" "paper" "scissor"])
        res   (rule [human ai])]
    (if (= "exit" human)
      "Game over"
      (do
        (println (format "Computer played %s: %s" ai res))
        (recur)))))

;; (game-loop)

;; Rock, paper or scissors?
;; Bang
;; Computer played scissor: computer can't win that!
;; Rock, paper or scissors?
;; paper
;; Computer played rock: paper wins over rock
;; Rock, paper or scissors?
;; exit
;; "Game over"

;; ;;;;;;;;;;;
;; Disassemble
;; ;;;;;;;;;;;

(defn- generate-symbol [n]
  (symbol (str "a" n)))

(comment
  (generate-symbol 10)) ; a10

(defn- generate [n]
  (->> (range n)
       (map (juxt generate-symbol identity))
       flatten
       vec))

(comment
  (generate 4)) ; [a0 0 a1 1 a2 2 a3 3]

(defmacro large-let [n]
  (let [bindings (generate n)]
    `(let ~bindings
       (reduce + [~@(map generate-symbol (range n))]))))

(macroexpand '(large-let 4)) ; 6
; (let* [a0 0 a1 1 a2 2 a3 3]
;    (clojure.core/reduce clojure.core/+ [a0 a1 a2 a3]))

;; (large-let 5000)
; CompilerException java.lang.RuntimeException: Method code too large!

(disassemble (fn [] (large-let 2))) ; ""

;; public final class LetPerf extends clojure.lang.AFunction {
;;
;;   // Omitted some static class attributes declaration.
;;
;;   // Method descriptor #11 ()Ljava/lang/Object;
;;   // Stack: 6, Locals: 5
;;   public java.lang.Object invoke() {        // <1>
;;      // 0  lconst_0
;;      // 1  lstore_1 [a0]
;;      // 2  lconst_1
;;      // 3  lstore_3 [a1]
;;      // Omitted bytecode related to loading reduce
;;     // 28  lload_1 [a0]
;;     // 29  invokestatic clojure.lang.Numbers.num(long) : java.lang.Number [34]
;;     // 32  lload_3 [a1]
;;     // 33  invokestatic clojure.lang.Numbers.num(long) : java.lang.Number [34]
;;     // 36  invokeinterface clojure.lang.IFn.invoke(Object, Object) : Object [37]
;;     // 41  invokeinterface clojure.lang.IFn.invoke(Object, Object) : Object [37]
;;     // 46  areturn
;;   }
;;   // Omitted static block initializer
;; }

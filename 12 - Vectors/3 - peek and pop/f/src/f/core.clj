(ns f.core
  (:require
   [clojure.set :refer [map-invert]])
  (:import
   [clojure.lang PersistentQueue]))

;; `peek` and `pop` are typical queue operations

(def q (into PersistentQueue/EMPTY [1 2 3]))
(def v  [1 2 3])
(def l '(1 2 3))

(peek q) ; 1
(peek v) ; 3
(peek l) ; 1

(-> PersistentQueue/EMPTY
    (conj "a" "b" "c") ; <-("a" "b" "c")-<
    pop                ; <-("b" "c")-<
    vec)               ; ["b" "c"]
(pop  ["a" "b" "c"])   ; ["a" "b"]
(pop '("a" "b" "c"))   ; ("b" "c")

;; you can pop an empty queue
(-> PersistentQueue/EMPTY
    pop)
; <-()-<

(comment
  ;; other collection types will throw an exception
  (pop []))
  ; (err) Execution error (IllegalStateException)
  ; (err) Can't pop empty vector

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; create a LIFO queue with a vector
;; LIFO queue (last-in first-out queues, also called stacks)
(defn queue []
  [])

(def push conj)
(def brackets {\[ \] \( \) \{ \}})

(defn balanced-brackets? [form]
  (= []
     (reduce
      (fn [q x]
        (cond
          (brackets x)              (push q x)
          ((map-invert brackets) x) (if (= (brackets (peek q)) x)
                                      (pop q)
                                      (reduced false)) ;; old code: (throw (ex-info (str "Unmatched delimiter " x) {})))
          :else q))
      (queue)
      form)))

(balanced-brackets? "(let [a (inc 1]) (+ a 2))") ; false
(balanced-brackets? "(let [a (inc 1)] (+ a 2))") ; true

;; from the old code with exception throwing
; (err) Execution error (ExceptionInfo) at f.core/check$fn (form-init4109485166469733215.clj:35).
; (err) Unmatched delimiter ]

;; Vector-Based Loops

(defn reverse-mapv-1 [f v]
  (loop [v    v
         res (transient [])]
    (if (peek v)
      (recur
       (pop v)
       (conj! res (f (peek v))))
      (persistent! res))))

(reverse-mapv-1 str (vec (range 10))) ; ["9" "8" "7" "6" "5" "4" "3" "2" "1" "0"]

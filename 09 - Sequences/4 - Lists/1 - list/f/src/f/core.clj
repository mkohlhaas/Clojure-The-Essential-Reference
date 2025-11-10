(ns f.core
  (:require
   [clojure.set    :refer [map-invert]]
   [criterium.core :refer [quick-bench]]))

;; lists are not lazy

(list 1 2 3 4 5) ; (1 2 3 4 5)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(type ())          ; clojure.lang.PersistentList$EmptyList
(type (list))      ; clojure.lang.PersistentList$EmptyList
(type (cons 1 ())) ; clojure.lang.Cons

(-> () (conj 1))                    ; (1)
(-> () (conj 1) (conj 2))           ; (2 1)
(-> () (conj 1) (conj 2) (conj 3))  ; (3 2 1)

(defn rev [coll]
  (into () coll))

(rev (range 10))  ; (9 8 7 6 5 4 3 2 1 0)
(rev [0 1 2 3])   ; (3 2 1 0)

(defn stack []
  ())

(defn push [x stack]
  (conj stack x))

(defn nearest-smaller [xs]
  (letfn [(step [xs stack]
            (lazy-seq
             (when-first [x xs]
               (loop [st stack]
                 (if-let [s (peek st)]
                   (if (< s x)
                     (cons s (step (rest xs) (push x st)))
                     (recur (pop st)))
                   (step (rest xs) (push x st)))))))]
    (step xs (stack))))

(nearest-smaller [0 8 4 12 2 10 6 14 1 9 5 13 3 11 7 15])
; (0 0 4 0 2 2 6 0 1 1 5 1 3 3 7)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (defn alist [n]
    (into (list) (range n)))

  (defn acons [n]
    (reduce #(cons %2 %1) () (range n)))

  (let [l1 (alist 1e5)] (quick-bench (reduce + l1))) ; (out) Execution time mean :  5.452296 ms
  (let [l2 (acons 1e5)] (quick-bench (reduce + l2))) ; (out) Execution time mean : 11.085810 ms

  (let [l1 (alist 1e5)] (quick-bench (count l1)))    ; (out) Execution time mean : 15.737950 ns
  (let [l2 (acons 1e5)] (quick-bench (count l2))))   ; (out) Execution time mean :  2.414748 ms

(def push-me conj)
(def brackets {\[ \] \( \) \{ \}})

(defn check [form stack]
  (reduce
   (fn [q x]
     (cond
       (brackets x)               (push-me q x)
       ((map-invert brackets) x)  (if (= (brackets (peek q)) x)
                                    (pop q)
                                    (throw (ex-info (str "Unmatched delimiter " x) {})))
       :else q))
   stack
   form))

(comment
  (check "(let [a (inc 1]) (+ a 2))" ()))
  ; (err) Execution error (ExceptionInfo)
  ; (err) Unmatched delimiter ]

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (check "(let [a (inc 1)] (+ a 2))" ()) ; ()

  (def small (str (seq (take 100  (iterate list ())))))
  (def large (str (seq (take 1000 (iterate list ())))))

  (quick-bench (check small ()))  ; (out) Execution time mean : 3.938575 ms
  (quick-bench (check small []))  ; (out) Execution time mean : 4.717053 ms

  (quick-bench (check large ()))  ; (out) Execution time mean : 388.989038 ms
  (quick-bench (check large []))) ; (out) Execution time mean : 471.480527 ms

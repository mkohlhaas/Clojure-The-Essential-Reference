(ns f.core
  (:require
   [clojure.set :refer [map-invert]])
  (:import
   [clojure.lang PersistentQueue]))

(def q (into PersistentQueue/EMPTY [1 2 3]))
(def v [1 2 3])
(def l '(1 2 3))

(peek q) ; 1
(peek v) ; 3
(peek l) ; 1

(-> PersistentQueue/EMPTY (conj "a" "b" "c") pop vec)  ; ["b" "c"]

(pop  ["a" "b" "c"])  ; ["a" "b"]
(pop '("a" "b" "c"))  ; ("b" "c")

(defn queue [] [])

(def push conj)
(def brackets {\[ \] \( \) \{ \}})

(defn check [form]
  (reduce
   (fn [q x]
     (cond
       (brackets x)
       (push q x)
       ((map-invert brackets) x)
       (if (= (brackets (peek q)) x)
         (pop q)
         (throw
          (ex-info
           (str "Unmatched delimiter " x) {})))
       :else q))
   (queue) form))

(comment
  (check "(let [a (inc 1]) (+ a 2))"))
  ; (err) Execution error (ExceptionInfo) at f.core/check$fn (form-init4109485166469733215.clj:35).
  ; (err) Unmatched delimiter ]

(check "(let [a (inc 1)] (+ a 2))") ; []

(defn reverse-mapv-1 [f v]
  (loop [v    v
         res (transient [])]
    (if (peek v)
      (recur
       (pop v)
       (conj! res (f (peek v))))
      (persistent! res))))

(reverse-mapv-1 str (vec (range 10))) ; ["9" "8" "7" "6" "5" "4" "3" "2" "1" "0"]

;; difference to previous version????
(defn reverse-mapv-2 [f v]
  (loop [v    v
         res (transient [])]
    (if (peek v)
      (recur
       (pop v)
       (conj! res (f (peek v))))
      (persistent! res))))

(reverse-mapv-2 str (vec (range 10))) ; ["9" "8" "7" "6" "5" "4" "3" "2" "1" "0"]

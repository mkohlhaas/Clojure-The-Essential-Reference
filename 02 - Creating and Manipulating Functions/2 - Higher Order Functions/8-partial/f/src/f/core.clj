(ns f.core
  (:require
   [clojure.string :refer [index-of]]
   [criterium.core :refer [bench]]))

;; ;;;;;;;;;;;
;; Incrementer
;; ;;;;;;;;;;;

(def incrementer (partial + 1))

(incrementer 1)   ; 2
(incrementer 1 1) ; 3

;; ;;;;;;
;; Finder
;; ;;;;;;

(def finder (partial index-of "tons-and-tons-of-text"))

(finder "tons")                ; 0
(finder "tons" (count "tons")) ; 9

(comment
  (finder "tons" 5 "unsupported"))
  ; (err) Wrong number of args (4) passed to: clojure.string/index-of

;; ;;;;;;;;;;;;;;;;;;
;; Anonymous Function
;; ;;;;;;;;;;;;;;;;;;

(let [f (partial str "thank you ")]
  (f "all!"))
; "thank you all!"

(let [f #(str %1 "thank you " %2)]
  (f "A big " "all!"))
; "A big thank you all!"

;; ;;;
;; All
;; ;;;

(defn as [x]
  (partial = x))

(defn same? [item coll]
  (apply (as item) (seq coll)))

(def all-a?   (partial same? \a))
(def all-red? (partial same? :red))

(all-a? "aaaaa")            ; true
(all-a? "aabaa")            ; false
(all-red? [:red :red :red]) ; true
(all-red? [:red :blu :red]) ; false

;; ;;;;;;;;;;;;;;;;
;; Validating a Map
;; ;;;;;;;;;;;;;;;;

(defn- validate [whitelist req]
  (and (every? not-empty (vals req))
       (every? whitelist (keys req))))

(def valid-req
  {:id      "1322"
   :cache   "rb001"
   :product "cigars"})

(def invalid-req
  {:id    "1323"
   :cache "rb004"
   :spoof ""})

(map (partial validate #{:id :cache :product})
     [valid-req invalid-req])
; (true false)

;; ;;;;;;;;
;; Currying
;; ;;;;;;;;

(comment
  (defn f1 [a b c d]
    (+ a b c d))

  (defn f2 [a b c]
    (fn [d]
      (+ a b c d)))

  (defn f3 [a b]
    (fn [c]
      (fn [d]
        (+ a b c d))))

  (defn f4 [a]
    (fn [b]
      (fn [c]
        (fn [d]
          (+ a b c d)))))

  (f1 1 2 3 4)       ; 10
  ((f2 1 2 3) 4)     ; 10
  (((f3 1 2) 3) 4)   ; 10
  ((((f4 1) 2) 3) 4) ; 10

  ((partial f1 1 2 3) 4)  ; 10
  ((partial f1 1 2) 3 4)  ; 10
  ((partial f1 1) 2 3 4)) ; 10

;; ;;;;;;;;;;;;
;; Benchmarking
;; ;;;;;;;;;;;;

(comment
  (let [myvec (partial vector :start)]
    (bench (myvec 1 2 3)))
  ; (out)              Execution time mean : 61.169733 ns

  ;; more than three args
  (let [myvec (partial vector :start)]
    (bench (myvec 1 2 3 4)))
  ; (out)              Execution time mean : 707.478949 ns

  (let [myvec (fn [a b c d] (vector :start a b c d))]
    (bench (myvec 1 2 3 4))))
  ; (out)              Execution time mean : 65.638203 ns

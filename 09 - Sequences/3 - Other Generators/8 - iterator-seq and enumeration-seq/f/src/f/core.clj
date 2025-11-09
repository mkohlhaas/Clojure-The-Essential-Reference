(ns f.core
  (:require
   [clojure.core.reducers :refer [fold]])
  (:import
   [java.util Collections]
   [java.util.concurrent ConcurrentHashMap]))

(def an-iterator    (.iterator               [1 2 3]))
(def an-enumeration (Collections/enumeration [1 2 3]))

(iterator-seq    an-iterator)    ; (1 2 3)
(enumeration-seq an-enumeration) ; (1 2 3)

;; (iterator-seq [iter]): `iter` must implement the java.util.Iterator    interface.
;; (enumeration-seq [e]): `e`    must implement the java.util.Enumeration interface.

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(->> "Clojure is the best language"
     (.splitAsStream #"\s+") ; a stream
     .iterator               ; an iterator
     iterator-seq)           ; a sequence
; ("Clojure" "is" "the" "best" "language")

;; Parallel Distinct

;; java.lang.concurrent.ConcurrentHashMap offers Enumeration
(defn parallel-distinct [v]
  (let [m        (ConcurrentHashMap.)
        combinef (fn ([] m) ([_ _]))
        reducef  (fn [^ConcurrentHashMap m k] (.put m k 1) m)]
    (fold combinef reducef v)
    (enumeration-seq (.keys m))))

(defn many-repeating-numbers [n]
  (into [] (take n (apply concat (repeat (range 10))))))

(comment
  (take 25 (many-repeating-numbers 1e6))) ; (0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4)

(parallel-distinct (many-repeating-numbers 1e6))
; (0 1 2 3 4 5 6 7 8 9)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn dbg-coll [n]
  (let [xs (into () (range n 0 -1))] ; n=10 => (10 9 8 7 6 5 4 3 2 1)
    (map #(do (print % ", ") %) xs)))

;; iterator (lazy but uses chunks of size 32 items)
;; One item is evaluated from the next chunk to check if there is more input.
(first (iterator-seq (.iterator (dbg-coll 100))))
; (out) 1 , 2 , 3 , 4 , 5 , 6 , 7 , 8 , 9 , 10 , 11 , 12 , 13 , 14 , 15 , 16 , 17 , 18 , 19 , 20 , 21 , 22 , 23 , 24 , 25 , 26 , 27 , 28 , 29 , 30 , 31 , 32 , 33 , 
; 1

;; enumeration (fully lazy)
(first (enumeration-seq (Collections/enumeration (dbg-coll 100))))
; (out) 1 , 
; 1

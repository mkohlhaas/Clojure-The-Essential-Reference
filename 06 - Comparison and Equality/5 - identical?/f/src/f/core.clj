(ns f.core
  (:require [clojure.java.javadoc :refer [javadoc]])
  (:import java.util.concurrent.LinkedBlockingQueue))

;; To verify if the arguments are stored at the same memory location.

;; Results are sometimes surprising because several basic types (both Java and Clojure) 
;; implement "interning", an automatic caching mechanism to improve performance.

(let [x #{1 2 3}
      y x]
  (identical? x y))
; true

;; ;;;;;;;;
;; Sentinel
;; ;;;;;;;;

(comment
  (javadoc LinkedBlockingQueue))

(def capacity 1)
(def channel  (LinkedBlockingQueue. capacity))
(def SENTINEL (Object.))

;; prints hash of sent value; stops on receiving SENTINEL
(defn encode []
  (let [e (.take channel)] ; `take` is blocking
    (if (identical? SENTINEL e)
      (println "done")
      (do (println (hash e))
          (recur)))))

(defn start []
  (let [out *out*]
    (.start (Thread.
             #(binding [*out* out]
                (encode))))))

(do
  (start)
  (.offer channel :a)
  (.offer channel (Object.))
  (.offer channel SENTINEL)
  (.offer channel :a))
; (out) -2123407586
; (out) 1292880764
; (out) done

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Surprises using identical?
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Strings, longs between -127 and +128, chars (0-127) and keywords are automatically interned.
(map identical?      ; (true true true true)
     ["A" 1 \a :a]
     ["A" 1 \a :a])

(identical? '() '()) ; true (The empty list literal is the only instance of the empty list inside a running JVM.)
(identical? 2/1 2/1) ; true (The clojure.lang.Ratio instance 2/1 is saved internally as long type: (class 2/1) is java.long.Long. Ratios in general are not interned.)

;; A collection of data values that are not interned, e.g. symbols are not interned.
(map identical?      ; (false false false false false false false false false false)
     [128 \λ 1N 1M 1/2 1. #"1" [1] '(1) 'a]
     [128 \λ 1N 1M 1/2 1. #"1" [1] '(1) 'a])

(identical? 100 100)         ; true
(identical? (Long. 100) 100) ; false

;; To give Java the option to look into the interned cached of numbers, we need to use Long/valueOf instead of the constructor.
(identical? (Long/valueOf "100") (Long/valueOf "100")) ; true
(identical? (Long/valueOf "100") 100)                  ; true

;; ;;;;;;
;; Boxing
;; ;;;;;;

;; vars

(def a 1000)
(def b a)

(identical? a b)    ; true

;; let

(let [x 1000 y x]   ; false
  (identical? x y))

;; the same
(let [x 1000 y x]   ; false
  (identical? (Long/valueOf x) (Long/valueOf y)))

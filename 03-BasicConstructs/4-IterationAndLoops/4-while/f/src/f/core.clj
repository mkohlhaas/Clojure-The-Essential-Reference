(ns f.core
  (:require [clojure.java.io :as io])
  (:import  [java.io File]
            [javax.xml.bind DatatypeConverter]
            [java.security  MessageDigest]
            [java.security  DigestInputStream]))

;; Usage of `while` should be restricted to a few special cases such as
;; Java interoperability, since more idiomatic iteration forms exist
;; in Clojure that don’t require side effects.

;; `while true` expressions in Java are quite common to create daemon threads
(while (< 0.1 (rand))
  (println "loop"))
; (out) loop
; (out) loop
; (out) loop
; (out) loop

(comment
  ;; `while` is a simple macro
  (macroexpand '(while (< 0.1 (rand))
                  (println "loop"))))
  ;; (loop* []
  ;;    (clojure.core/when (< 0.1 (rand))
  ;;      (println "loop")
  ;;      (recur))))

;; ;;;;;;;
;; Threads
;; ;;;;;;;

(comment
  (defn forever []
    (while true
      (Thread/sleep 5000)
      (println "App running. Waiting for input...")))

  (defn status-thread []
    (let [t (Thread. forever)]
      (.start t)
      t))

  (def t (status-thread))
  ;; App running. Waiting for input...
  ;; App running. Waiting for input...
  ;; App running. Waiting for input...

  (.stop t))

;; ;;;;;;;
;; SHA-256
;; ;;;;;;;

;; `while` is usually found with Java IO
(defn sha [file]
  (let [sha (MessageDigest/getInstance "SHA-256")]
    (with-open [dis (DigestInputStream. (io/input-stream file) sha)]
      (while (> (.read dis) -1))) ; no body - the sha instance is updated by just reading from the input stream
    (DatatypeConverter/printHexBinary (.digest sha))))

(sha (File. "/etc/hosts"))
; "10189CC7CF9DD18982685717A27274F1B7E416CBDD977B11538A720FA7EB4465"

;; ;;;;;;;;
;; Do While
;; ;;;;;;;;

;; translating imperative code
(loop [count 1]
  (when (< count 4)
    (println "Count is:" count)
    (recur (inc count))))
; (out) Count is: 1
; (out) Count is: 2
; (out) Count is: 3

(loop [count 1
       res   []]
  (if (< count 4)
    (recur (inc count) (conj res count))
    res))
; [1 2 3]

(dorun (map
        #(println "Count is:" %)
        (range 1 4)))
; (out) Count is: 1
; (out) Count is: 2
; (out) Count is: 3

;; more concise and expressive
(dorun (for [i (range 1 4)]
         (println "Count is:" i)))
; (out) Count is: 1
; (out) Count is: 2
; (out) Count is: 3

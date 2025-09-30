(ns f.core
  (:require [clojure.java.io    :as io]
            [clojure.string     :as s]
            [clojure.core.async :refer [go go-loop chan >! <! <!! close!]]))

;; -let  => nil and false are falsy
;; -some => only nil is falsy - false itself is truish!!!
;;          better understood with a mental translation into -not-nil?
;;            if-some   => if-not-nil?
;;            when-some => when-not-nil?

(if-let  [n "then"] n "else") ; "then"
(if-some [n "then"] n "else") ; "then" 

(if-let  [n nil]    n "else") ; "else"
(if-some [n nil]    n "else") ; "else"

(if-let  [n false]  n "else") ; "else"
(if-some [n false]  n "else") ; false

(when-let  [n "then"] n) ; "then"
(when-some [n "then"] n) ; "then"

(when-let  [n false]  n) ; nil
(when-some [n false]  n) ; false

(when-let  [n nil]    n) ; nil
(when-some [n nil]    n) ; nil

;; ;;;;;;;;;;;;;
;; Lines of Code
;; ;;;;;;;;;;;;;

;; code smell: let followed by when => use when-let
(defn loc [resource]
  (let [f (io/resource resource)]
    (when f
      (count (s/split-lines (slurp f))))))

(defn total-loc [& files]
  (reduce + (keep loc files)))

(comment
  (class (map  loc ["non-existent" "clojure/core.clj" "clojure/pprint.clj"]))  ; (nil 8105 51) clojure.lang.LazySeq
  (class (keep loc ["non-existent" "clojure/core.clj" "clojure/pprint.clj"]))) ; (8105 51)     clojure.lang.LazySeq

(total-loc "non-existent" "clojure/core.clj" "clojure/pprint.clj")
; 8156

#_{:clj-kondo/ignore [:redefined-var]}
(defn loc [resource]
  (when-let [f (io/resource resource)]
    (count (s/split-lines (slurp f)))))

(total-loc "non-existent" "clojure/core.clj" "clojure/pprint.clj")
; 8156

#_{:clj-kondo/ignore [:redefined-var]}
(defn loc [resource]
  (if-let [f (io/resource resource)]
    (count (s/split-lines (slurp f)))
    0))

#_{:clj-kondo/ignore [:redefined-var]}
(defn total-loc [& files]
  (reduce + (map loc files)))

(total-loc "non-existent" "clojure/core.clj" "clojure/pprint.clj")
; 8156

;; ;;;;;;;;;;;;;;;;;;;
;; Master-Worker-Model
;; ;;;;;;;;;;;;;;;;;;;

;; By calling close! on a channel, the producer sends a conventional 
;; nil element to signal the consumer that there are no more items.

;; This is the reason why nil cannot be sent down a channel for other
;; reasons other than to close the communication.

(defn- master [items in]
  (go
    (doseq [item items]
      (>! in item))
    (close! in))) ; when closing the channel a nil value is sent to the worker

(defn- worker [out]
  (let [in (chan)]
    (go-loop []
      (if-some [item (<! in)] ; if-not-nil? (check for end of items)
        (do
          (>! out (str "*" item "*"))
          (recur))
        (close! out)))
    in))

(defn process [items]
  (let [out (chan)]
    (master items (worker out))
    (loop [res []]
      (if-some [item (<!! out)]
        (recur (conj res item))
        res))))

(process '("this" "is" "just" "a" "test"))
; ["*this*" "*is*" "*just*" "*a*" "*test*"]

;; ;;;;;;;;;;;;;;;
;; Scheme's letrec
;; ;;;;;;;;;;;;;;;

;; https://gist.github.com/michalmarczyk/3c6b34b8db36e64b85c0
;; Like let, but the bindings may be mutually recursive, provided that
;; the heads of all values can be evaluated independently.

;; (letrec [is-even? #(or  (zero? %) (is-odd? (dec %)))
;;          is-odd?  #(and (not (zero? %)) (is-even? (dec %)))]
;;         (is-odd? 11))

;; ;;;;;;;;;;;;;;;;;;;;
;; Arc anaphoric macros
;; ;;;;;;;;;;;;;;;;;;;;

;; anaphoric if
(defmacro aif [expr then & [else]]
  `(let [~'it ~expr]
     (if ~'it
       ~then
       ~else)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(aif true  (println "it is" it) (println "no 'it' here"))
; (out) it is true

#_{:clj-kondo/ignore [:unresolved-symbol]}
(aif false (println it) (println "no 'it' here"))
; (out) no 'it' here

;; `it` is used although it shouldn't
(let [it 3]
  (aif true (println "it is" it)))
; (out) it is true

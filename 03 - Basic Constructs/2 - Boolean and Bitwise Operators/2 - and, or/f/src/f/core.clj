(ns f.core
  (:require
   [clojure.walk]))

(comment
  ;; does not work
  (apply and [true true true])  ; (err) Can't take value of a macro: #'clojure.core/and
  (apply or  [true true true])) ; (err) Can't take value of a macro: #'clojure.core/or

;; instead use
(every? identity [true true true]) ; true
(some   identity [true true true]) ; true

(or)  ; nil
(and) ; true

;; short-circuiting
(or  1 2) ; 1
(and 1 2) ; 2

(let [probe {:temp 150 :rpm "max"}]
  (when (and (> (:temp probe) 120)
             (= (:rpm  probe) "max"))
    (println "Too hot, going protection mode.")))
; (out) Too hot, going protection mode.

;; ;;;;;;;;;;;;
;; Path Example
;; ;;;;;;;;;;;;

;; Java interop
(defn path [s]
  (let [s (and s (.trim s))] ; make sure s is not nil before trimming (with Java interop)
    (and
     (seq s)                 ; make sure s is not empty before searching for '/'
     (subs s 0 (.lastIndexOf s "/")))))

(comment
  (.trim "   ") ; "" ✓
  (.trim "")    ; "" ✓
  (.trim nil)   ; (err) Cannot invoke "Object.getClass()" because "target" is null

  (.lastIndexOf "/tmp/exp/lol.txt" "/")) ; 8

(path "/tmp/exp/lol.txt") ; "/tmp/exp"
(path "   ")              ; nil
(path "")                 ; nil
(path nil)                ; nil

;; ;;;;;;;;;;;;;;
;; Default Values
;; ;;;;;;;;;;;;;;

(defn start-server [opts]
  (let [port (or (:port opts) 8080)]
    (str "starting server on localhost:" port)))

(start-server {:port 9001}) ; "starting server on localhost:9001"
(start-server {})           ; "starting server on localhost:8080"

;; ;;;;;;;;;;;
;; `and` Macro
;; ;;;;;;;;;;;

(clojure.walk/macroexpand-all '(and false true true))
; (let*
;  [and__5579__auto__ false]
;  (if
;   and__5579__auto__
;   (let*
;    [and__5579__auto__ true]
;    (if and__5579__auto__ true and__5579__auto__))
;   and__5579__auto__))

(comment
  (let* [and__4467__auto__ false]
        (if and__4467__auto__
          (let* [and__4467__auto__ true]
                (if and__4467__auto__
                  true
                  and__4467__auto__))
          and__4467__auto__))
  ; false

  (clojure.walk/macroexpand-all
   `(and
     false
     ~@(take 1000 (repeat true)))))
  ; (err) Unexpected error (StackOverflowError) macroexpanding clojure.core/and at (src/f/core.clj:60:1).

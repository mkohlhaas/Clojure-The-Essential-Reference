(ns f.doc
  (:require [clojure.repl]))

(defn hello
  "A function to say hello"
  [person]
  (str "Hello " person))

(clojure.repl/doc hello)
; (out) -------------------------
; (out) profilable/hello
; (out) ([person])
; (out)   A function to say hello

(:doc (meta #'hello)) ; "A function to say hello"

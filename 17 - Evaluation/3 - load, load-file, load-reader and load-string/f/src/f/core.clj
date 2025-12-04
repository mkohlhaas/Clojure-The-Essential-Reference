(ns f.core)

;; NOTE: examples work only in the REPL

;; `/` is the root of the class path

;; ;;;;
;; load
;; ;;;;

;; (ns user)
;; (clojure.zip/vector-zip [])
;; ClassNotFoundException clojure.zip

;; (load "zip")
;; FileNotFoundException Could not locate zip__init.class [...]

;; (ns clojure.set)
;; (load "zip")
;; nil

;; (clojure.zip/vector-zip [])
;; [[] nil]

;; (ns user)

;; (binding [clojure.core/*loading-verbosely* true]
;;   (load "clojure.reflect"))

;; aliases are also printed out:
;; (clojure.core/load  "/clojure.reflect")
;; (clojure.core/in-ns 'clojure.reflect)
;; (clojure.core/alias 'set 'clojure.set)
;; (clojure.core/load  "/clojure/reflect/java")
;; (clojure.core/in-ns 'clojure.reflect)
;; (clojure.core/alias 'set 'clojure.set)
;; (clojure.core/in-ns 'clojure.reflect)
;; (clojure.core/alias 'str 'clojure.string)

;; ;;;;;;;;;
;; load-file
;; ;;;;;;;;;

;; (spit "source.clj"
;;   "(ns ns1)
;;    (def a 1)
;;    (def b 2)
;;    (println \"a + b =\" (+ a b))")

;; (load-file "source.clj")
;; a + b = 3

;; ;;;;;;;;;;;
;; load-string
;; ;;;;;;;;;;;

;; (= (eval (read-string "(+ 1 1)"))
;;    (load-string "(+ 1 1)"))
;; true

;; `load-string` keeps track of line numbering ;;

;; (ns user)
;; (def code "(do (def a 1)\n(def b 2)\n(def c 3))")

;; (ns code1)
;; (load-string user/code)
;; (map (comp :line meta) [#'a #'b #'c])
;; (1 2 3)

;; (ns code2)
;; (eval (read-string user/code))
;; (map (comp :line meta) [#'a #'b #'c])
;; (1 1 1)

;; ;;;;;;;;;;;
;; load-reader
;; ;;;;;;;;;;;

;; `load-reader` behaves exactly like `load-string` or `load-file`, but it requires a java.io.Reader input type.
;; `load-reader` is useful to control the specific type of reader to use.

;; no code example

(ns f.core
  (:require [clojure.main :as main]
            [clojure.core.reducers]))

;; NOTE: most of this stuff works only in the REPL

;; ;;;;;;;;;;;
;; load-script
;; ;;;;;;;;;;;

clojure.core.reducers/fold
;; CompilerException java.lang.ClassNotFoundException: clojure.core.reducers

;; if the file starts with "@" or "@/", then `load-script` loads and compiles the file from the classpath
;; (main/load-script "@clojure/core/reducers.clj")

;; clojure.core.reducers/fold
;; #object["clojure.core.reducers$fold@41414539"]

(spit "hello.exe"
      "(ns hello)
   (println \"Hello World!\")")

;; use nvim `gf
;; hello.exe
;; (ns hello)
;;    (println "Hello World!")

;; see output the REPL
(main/load-script "hello.exe")
;; "Hello World!"
;; nil

;; ;;;;
;; repl
;; ;;;;

;; see output in the REPL
(main/repl :init #(println "Welcome to a new REPL! Press ctrl+D to exit."))
;; Welcome to a new REPL! Press ctrl+D to exit.
;; f.core =>

#_{:clj-kondo/ignore [:namespace-name-mismatch]}
(ns calculator)
(defn plus   [x y] (+ x y))
(defn minus  [x y] (- x y))
(defn times  [x y] (* x y))
(defn divide [x y] (/ x y))

#_{:clj-kondo/ignore [:unresolved-namespace]}
(main/repl :init #(require '[calculator :refer :all]))

(plus   2 1) ; 3
(minus  2 1) ; 1
(times  2 1) ; 2
(divide 2 1) ; 2

(def repl-options
  [:init   #(require '[calculator :refer :all])
   :prompt #(printf "enter expression :> ")])

#_{:clj-kondo/ignore [:unresolved-namespace]}
(apply main/repl repl-options)
;; enter expression :> (+ 1 1)
;; 2

#_{:clj-kondo/ignore [:unresolved-namespace]}
;; custom REPL calculates small infix mathematical expressions
(def repl-options1
  [:prompt #(printf "enter expression :> ")
   :read   (fn [request-prompt request-exit]
             (or ({:line-start request-prompt :stream-end request-exit}
                  (main/skip-whitespace *in*))
                 (re-find #"^(\d+)([\+\-\*\/])(\d+)$" (read-line))))
   :eval   (fn [[_ x op y]]
             (({"+" + "-" - "*" * "/" /} op)
              (Integer. x) (Integer. y)))])

#_{:clj-kondo/ignore [:unresolved-namespace]}
(apply main/repl repl-options1)
;; enter expression :> 2*3
;; 6
;; click ctrl+d to exit calculator


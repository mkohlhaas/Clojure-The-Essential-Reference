(ns f.core
  (:require
   [clojure.walk :as w]))

(macroexpand-1 '(when false (println "this will never be printed!")))
; (if false (do (println "this will never be printed!")))

(macroexpand-1 '(when-first [a [1 2 3]] (println a)))
; (clojure.core/when-let
;  [xs__3323__auto__ (clojure.core/seq [1 2 3])]
;  (clojure.core/let
;   [a (clojure.core/first xs__3323__auto__)]
;   (println a)))

;; `macroexpand` loops `macroexpand-1` on the form until the first element doesn’t resolve to a macro anymore.
(macroexpand '(when-first [a [1 2 3]] (println a)))
; (let*
;  [temp250 (clojure.core/seq [1 2 3])]
;  (clojure.core/when
;   temp250
;   (clojure.core/let
;    [xs__3323__auto__ temp250]
;    (clojure.core/let
;     [a (clojure.core/first xs__3323__auto__)]
;     (println a)))))

(w/macroexpand-all '(when-first [a [1 2 3]] (println a)))
; (let*
;  [temp258 (clojure.core/seq [1 2 3])]
;  (if
;   temp258
;   (do
;    (let*
;     [xs__3323__auto__ temp258]
;     (let* [a (clojure.core/first xs__3323__auto__)] (println a))))))

(defn find-invoked-functions [expression]
  (let [!fns (atom #{})
        walkfn! (fn walkfn! [expr]
                  (if (and (seq? expr) (symbol? (first expr)))
                    (let [head (first expr)]
                      (when-not (= 'quote head)
                        (some->> head resolve (swap! !fns conj))
                        (w/walk walkfn! identity expr)))
                    (when (coll? expr)
                      (w/walk walkfn! identity expr))))]
    (walkfn! (w/macroexpand-all expression))
    @!fns))

(find-invoked-functions
 '(when-first [a (vector 1 2 3)]
    (inc a)))
; #{#'clojure.core/first
;   #'clojure.core/inc
;   #'clojure.core/vector
;   #'clojure.core/seq}

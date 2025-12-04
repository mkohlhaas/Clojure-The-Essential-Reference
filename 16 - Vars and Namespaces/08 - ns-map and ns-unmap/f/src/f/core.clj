(ns f.core)

(take 10 (ns-map 'f.core))
; ([primitives-classnames #'clojure.core/primitives-classnames]
;  [+' #'clojure.core/+']
;  [Enum java.lang.Enum]
;  [decimal? #'clojure.core/decimal?]
;  [restart-agent #'clojure.core/restart-agent]
;  [sort-by #'clojure.core/sort-by]
;  [macroexpand #'clojure.core/macroexpand]
;  [ensure #'clojure.core/ensure]
;  [chunk-first #'clojure.core/chunk-first]
;  [eduction #'clojure.core/eduction])

;; (ns myns (:require [clojure.pprint :refer [pprint]]))

;; (pprint (ns-map 'myns))
;; {primitives-classnames #'clojure.core/primitives-classnames,
;;  +' #'clojure.core/+',
;;  Enum java.lang.Enum,
;;  decimal? #'clojure.core/decimal?,
;;  restart-agent #'clojure.core/restart-agent,
;;  sort-by #'clojure.core/sort-by,
;; [...]

;; (distinct (map #(map type %) (ns-map 'myns)))
;; ((clojure.lang.Symbol clojure.lang.Var)
;;  (clojure.lang.Symbol java.lang.Class))

;; (ns-unmap 'myns '+)
;; nil

(+ 1 1)
;; Unable to resolve symbol: +

(refer 'clojure.core :only ['+])
(+ 1 1) ; 2

(ns f.core)

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn clean-ns [ns]
  (let [keys (keys (ns-map ns))]
    (doseq [key keys]
      (ns-unmap ns key))))

(comment
  (take 10 (keys (ns-map *ns*))))
  ; (primitives-classnames
  ;  +'
  ;  Enum
  ;  decimal?
  ;  restart-agent
  ;  sort-by
  ;  macroexpand
  ;  ensure
  ;  chunk-first
  ;  eduction)

(take 10 (keys (ns-map 'f.core)))
; (primitives-classnames
;  +'
;  decimal?
;  restart-agent
;  sort-by
;  macroexpand
;  ensure
;  chunk-first
;  eduction
;  tree-seq)

(comment
  (#'f.core/clean-ns 'f.core)

  (clojure.core/alias 'c 'clojure.core)
  (c/ns-map 'f.core) ; {}

  (def normal-var :public)  ; #'f.core/normal-var

  #_{:clj-kondo/ignore [:unused-private-var]}
  (def ^:private private-var :private) ; #'f.core/private-var

  #_{:clj-kondo/ignore [:unused-import]}
  (c/import 'java.lang.Number)         ; java.lang.Number

  ;; everything
  (c/ns-map 'f.core)
  ; {private-var #'f.core/private-var,
  ;  Number java.lang.Number,
  ;  normal-var #'f.core/normal-var}

  ;; public
  (c/ns-publics 'f.core)
  ; {normal-var #'f.core/normal-var}

  ;; public and private
  (c/ns-interns 'f.core)
  ; {private-var #'f.core/private-var, 
  ;  normal-var  #'f.core/normal-var)

  ;; Java classes
  (c/ns-imports 'f.core))
  ; {Number java.lang.Number}

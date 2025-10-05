(ns f.core)

(definline timespi [x]
  `(* ~x 3.14))

(timespi 3)
; 9.42

;; `definline` is a macro that takes a function body and expands it into
;; a standard defn declaration that also includes an "inlined" version
;; of the same body definition.
(macroexpand-1 '(definline timespi [x] `(* ~x 3.14)))

;; (do
;;  (clojure.core/defn timespi [x] (clojure.core/* x 3.14))
;;  (clojure.core/alter-meta!
;;   #'timespi
;;   clojure.core/assoc
;;   :inline
;;   (clojure.core/fn
;;    timespi
;;    [x]
;;    (clojure.core/seq
;;     (clojure.core/concat
;;      (clojure.core/list 'clojure.core/*)
;;      (clojure.core/list x)
;;      (clojure.core/list 3.14)))))
;;  #'timespi)

;; after removing core namespaces:
;; (do
;;   (defn timespi [x]
;;     (* x 3.14))
;;   (alter-meta! (var timespi)
;;                assoc :inline
;;                (fn timespi [x]
;;                  (seq
;;                    (concat (list (quote *))
;;                            (list x)
;;                            (list 3.14)))))
;;   (var timespi))

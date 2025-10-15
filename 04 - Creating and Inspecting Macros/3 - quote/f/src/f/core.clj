(ns f.core)

(quote (+ 1 2))
; (+ 1 2)

;; reader macro
'(+ 1 2)
; (+ 1 2)

(resolve (read-string "+"))
; #'clojure.core/+

(resolve '+)
; #'clojure.core/+

;; ;;;;;;;;;;
;; defrecord*
;; ;;;;;;;;;;

;; Records created with `defrecord*` will be callable just like maps.

;; NOTE: "unquote-quote" pattern ~' so that the method name will be `invoke` rather than `user/invoke` or `f.core/invoke`.
;; The "unquote-quote" pattern stops auto-qualification in syntax quotes.
(defmacro defrecord* [name fields & impl]
  `(defrecord ~name ~fields
     ~@impl
     clojure.lang.IFn
     (~'invoke [this# key#]
       (get this# key#))
     (~'invoke [this# key# not-found#]
       (get this# key# not-found#))
     (~'applyTo [this# args#]
       (case (count args#)
         (1 2) (this# (first args#) (second args#))
         (throw (AbstractMethodError.))))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defrecord* Foo [a])

#_{:clj-kondo/ignore [:unresolved-symbol]}
((Foo. 1) :a)
; 1

#_{:clj-kondo/ignore [:unresolved-symbol]}
((Foo. 1) :b 2)
; 2

#_{:clj-kondo/ignore [:unresolved-symbol]}
(apply (Foo. 1) [:a])
; 1

#_{:clj-kondo/ignore [:unresolved-symbol]}
(apply (Foo. 1) [:b 2])
; 2

(comment
  (second [1 2]) ; 2
  (second [1])   ; nil

  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (apply (Foo. 1) [:a :b :c]))
  ; (err) Execution error (AbstractMethodError)

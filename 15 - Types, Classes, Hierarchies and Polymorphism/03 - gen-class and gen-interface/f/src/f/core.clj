(ns f.core)

;; `gen-class` and `gen-interface` are macros dedicated to the generation of Java classes and interfaces.

;; Book Interface ;;

(gen-interface
 :name    "user.BookInterface"
 :extends [java.io.Serializable])

(ancestors user.BookInterface) ; #{java.io.Serializable}

;; `reify` implements a protocol or interface
(reify
  user.BookInterface
  Object (toString [_] "A marker interface for books."))
; #object[f.core$eval4112$reify__4113 0x7755c497 "A marker interface for books."]

;; gen-class ;;

(gen-class 'testgenclass) ; nil (a direct call to gen-class does not produce any noticeable effect, like memory, file system, error)

;; `gen-class` checks *compile-files*
(binding [*compile-files* true] ; nil (but still no noticeable effect)
  (gen-class 'testgenclass))

;; `gen-class` is only evaluated when the Clojure runtime is bootstrapping

;; we will call `compile` on a file with `gen-class` in it
(spit (str *compile-path* "/bookgenclass.clj")
      "(ns bookgenclass)
       (gen-class :name book.GenClass 
                  :main true)")

(comment
  (println (slurp (str *compile-path* "/bookgenclass.clj"))))
  ; (out) (ns bookgenclass)
  ; (out) (gen-class :name book.GenClass :main true)

(comment
  ; "…/target/classes"
  *compile-path*)

(compile 'bookgenclass) ; bookgenclass (successfully generated)

(comment
  (import 'book.GenClass)  ; book.GenClass (successfully immported)

  (GenClass/main (make-array String 0)))
  ; (err) Execution error (UnsupportedOperationException)
  ; (err) bookgenclass/-main not defined

;; Oops! We forgot to implement the -main function!
(spit (str *compile-path* "/bookgenclass.clj")
      "(ns bookgenclass)

   (gen-class :name book.GenClass
              :main true)

   (defn -main [& _args]
     (println \"Hello from Java!\"))")

(comment
  (println (slurp (str *compile-path* "/bookgenclass.clj"))))
  ; (out) (ns bookgenclass)
  ; (out) 
  ; (out) (gen-class :name book.GenClass
  ; (out)            :main true)
  ; (out) 
  ; (out) (defn -main [& _args]
  ; (out)   (println "Hello from Java!"))

(compile 'bookgenclass) ; bookgenclass

(comment
  ;; now it works
  (GenClass/main (make-array String 0)))
  ; (out) Hello from Java!

(comment
  #_{:clj-kondo/ignore [:namespace-name-mismatch]}
  ;; `gen-class` can also be embedded into the namespace declaration
  (ns bookgenclass2
    (:gen-class :name book.GenClass2))

  (defn -main [& _args]
    (println "More greetings from Java!")))

;;`gen-class` accepts a long list of parameters to influence the generation of the class,
;; covering the most complicated interoperability scenarios. 

;; There are easier alternatives like `proxy` or `reify` covering the most common scenarios.

;; For this reason, `gen-class` is mostly used as a low level tool when other options fail.

;; One exception is the generation of the main entry point for Clojure applications 
;; for which gen-class is used pervasively!

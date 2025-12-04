(ns f.core)

(comment
  (the-ns 'notavail)) ; (err) No namespace: notavail found
(the-ns 'clojure.edn) ; #object[clojure.lang.Namespace 0x46f2cd97 "clojure.edn"]
(the-ns *ns*)         ; #object[clojure.lang.Namespace 0x5a3a707c "f.core"]

;; NOTE: Works only in the REPL!!!
;;
;; (ns com.package.myns)
;; (ns-name *ns*) ; com.package.myns

(namespace :notcreateyet/a) ; "notcreateyet"
(namespace ::a)             ; "f.core" (implicit full qualification to the current namespace is possible by using the double colon notation)
(namespace 'alsosymbols/s)  ; "alsosymbols"

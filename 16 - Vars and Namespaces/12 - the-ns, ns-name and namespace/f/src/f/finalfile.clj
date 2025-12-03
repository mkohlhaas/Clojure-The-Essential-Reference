;; 1.clj

(the-ns 'notavail)
;; Exception No namespace: notavail found

(the-ns 'clojure.edn)
;; #object[clojure.lang.Namespace 0x20312893 "clojure.edn"]

(the-ns *ns*)
;; #object[clojure.lang.Namespace 0xcc62a3b "user"]

;; 2.clj

(ns com.package.myns)
(ns-name *ns*)
;; com.package.myns

;; 3.clj

(ns user)

(namespace :notcreateyet/a)
;; "notcreateyet"

(namespace ::a)
;; user

(namespace 'alsosymbols/s)
;; "alsosymbols"


;; 1.clj

(pprint (all-ns))
;; (#object[clojure.lang.Namespace 0x20312893 "clojure.edn"]
;;  #object[clojure.lang.Namespace 0x70eecdc2 "clojure.core.server"]

(ns-name (first (all-ns)))
;; clojure.edn

;; 2.clj

(find-ns 'clojure.edn)
;; #object[clojure.lang.Namespace 0x20312893 "clojure.edn"]

(find-ns 'no-ns)
;; nil


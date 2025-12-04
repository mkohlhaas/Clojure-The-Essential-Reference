(ns f.core)

(create-ns 'com.web.tired-of-typing-this.myns)  ; #object[clojure.lang.Namespace 0x152d3c36 "com.web.tired-of-typing-this.myns"]
(ns-aliases 'com.web.tired-of-typing-this.myns) ; {} (is empty after creation)

;; 2.clj

(intern 'com.web.tired-of-typing-this.myns 'myvar 0) ; #'com.web.tired-of-typing-this.myns/myvar
#_{:clj-kondo/ignore [:unresolved-namespace]}
com.web.tired-of-typing-this.myns/myvar              ; 0

(alias 'myns 'com.web.tired-of-typing-this.myns)

(ns-aliases *ns*) ; {myns #object[clojure.lang.Namespace 0x152d3c36 "com.web.tired-of-typing-this.myns"]}

;; much shorter now
myns/myvar ; 0

;; Same effect as `:as` in `ns`:
;; (ns anotherns
;;   (:require [clojure.set :as s])

;; (ns-aliases 'anotherns) ; {s #object[clojure.lang.Namespace 0x5d1fa08b "clojure.set"]}

;; remove aliases:
;; (ns-aliases 'anotherns)    ; {s #object[clojure.lang.Namespace 0x5d1fa08b "clojure.set"]}
;; (ns-unalias 'anotherns 's)
;; (ns-aliases 'anotherns)    ; {}

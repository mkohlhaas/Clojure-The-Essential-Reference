;; 1.clj

(create-ns 'com.web.tired-of-typing-this.myns)
(ns-aliases 'com.web.tired-of-typing-this.myns)
;; {}

;; 2.clj

(intern 'com.web.tired-of-typing-this.myns 'myvar 0)
com.web.tired-of-typing-this.myns/myvar
;; 0

;; 3.clj

(alias 'myns 'com.web.tired-of-typing-this.myns)

(ns-aliases *ns*)
;; {myns #object[clojure.lang.Namespace 0x58d455df
;;  "com.web.tired-of-typing-this.myns"]}

myns/myvar
0

;; 4.clj

(ns anotherns (:require [clojure.set :as s]))

(ns-aliases 'anotherns)
;; {s #object[clojure.lang.Namespace 0x5d1fa08b "clojure.set"]}

;; 5.clj

(ns-aliases 'anotherns)
;; {s #object[clojure.lang.Namespace 0x5d1fa08b "clojure.set"]}
(ns-unalias 'anotherns 's)
(ns-aliases 'anotherns)
;; {}


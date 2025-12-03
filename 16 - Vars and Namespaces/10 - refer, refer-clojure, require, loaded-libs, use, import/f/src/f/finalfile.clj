;; 1.clj

(ns user)

(defn clean-ns [ns]
  (ns 'user)
  (create-ns ns)
  (let [ks (keys (ns-map ns))]
    (doseq [k ks]
      (ns-unmap ns k)))
  (ns-map ns))

(clean-ns 'myns)
;; {}

(binding [*ns* (the-ns 'myns)]
  (refer 'clojure.core
         :only ['+ '-]
         :rename {'+ 'plus '- 'minus}))

(ns-map 'myns)
;; {minus #'clojure.core/-
;;  plus #'clojure.core/+}

;; 2.clj

(binding [*ns* (the-ns 'myns)]
  (refer-clojure
   :only ['+ '-]
   :rename {'+ 'plus '- 'minus}))

;; 3.clj

(contains? (set (map ns-name (all-ns))) 'clojure.set)
;; false

(require 'clojure.set)

(contains? (set (map ns-name (all-ns))) 'clojure.set)
;; true

;; 4.clj

(def libs (loaded-libs))

(pprint libs)
;; #{clojure.core.protocols clojure.core.server clojure.edn
;;   clojure.instant clojure.java.browse clojure.java.io
;;   clojure.java.javadoc clojure.java.shell clojure.main clojure.pprint
;;   clojure.repl clojure.string clojure.uuid clojure.walk}

(require '[clojure.data :refer [diff]])
(def nss (set (map ns-name (all-ns))))
(pprint (diff libs nss))
;; [nil   
;;  #{user clojure.core clojure.set clojure.data} 
;;  #{clojure.core.protocols clojure.core.server clojure.edn 
;;    clojure.instant clojure.java.browse clojure.java.io
;;    clojure.java.javadoc clojure.java.shell clojure.main clojure.pprint
;;    clojure.repl clojure.string clojure.uuid clojure.walk}]

;; 5.clj

(create-ns 'test-require)
(require 'test-require)
;; Could not locate test_require__init.class or test_require.clj on classpath.

;; 6.clj

(ns myns)

(require
 '[clojure.set
   :as se
   :refer [union]]
 '[clojure.string
   :as st
   :refer :all])
;; WARNING: reverse already refers to: #'clojure.core/reverse
;; WARNING: replace already refers to: #'clojure.core/replace

;; 7.clj

(ns myns)

(use '[clojure.java.io
       :only [reader file]
       :rename {reader r}]
     :verbose
     :reload-all)

;; (load "/clojure/java/io")
;; (in-ns 'myns)
;; (refer 'clojure.java.io :only '[reader file] :rename '{reader r})

;; 8.clj

(ns user)
(clean-ns 'myns)
;; {}

(binding [*ns* (the-ns 'myns)]
  (import '[java.util ArrayList HashMap]))

(ns-imports 'myns)
;; {HashMap java.util.HashMap
;;  ArrayList java.util.ArrayList}


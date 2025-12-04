(ns f.core)

;; ;;;;;
;; refer
;; ;;;;;

(defn clean-ns [ns]
  (create-ns ns)
  (let [keys (keys (ns-map ns))]
    (doseq [key keys]
      (ns-unmap ns key)))
  (ns-map ns))

(clean-ns 'myns) ; {}

(binding [*ns* (the-ns 'myns)] ; `refer` uses only current namespace (in *ns*)
  (refer 'clojure.core
         :only   ['+ '-]
         :rename {'+ 'plus '- 'minus}))

(ns-map 'myns)
; {minus #'clojure.core/-,
;  plus  #'clojure.core/+)

(clean-ns 'myns) ; {}

;; the same:
;; (binding [*ns* (the-ns 'myns)]
;;   (refer-clojure
;;    :only   ['+ '-]
;;    :rename {'+ 'plus '- 'minus}))

;; ;;;;;;;
;; require
;; ;;;;;;;

;; NOTE: These examples work only in the REPL!!!

;; (contains? (set (map ns-name (all-ns))) 'clojure.set)
;; false

;; (require 'clojure.set)
;;
;; (contains? (set (map ns-name (all-ns))) 'clojure.set)
;; ;; true
;;
;; (def libs (loaded-libs))
;;
;; (take 10 libs)
;; ; (arrangement.core
;; ;  clj-stacktrace.core
;; ;  clojure.core.protocols
;; ;  clojure.core.reducers
;; ;  clojure.core.rrb-vector
;; ;  clojure.core.rrb-vector.fork-join
;; ;  clojure.core.rrb-vector.interop
;; ;  clojure.core.rrb-vector.nodes
;; ;  clojure.core.rrb-vector.parameters
;; ;  clojure.core.rrb-vector.protocols)
;;
;; (require '[clojure.data :refer [diff]])
;;
;; (def nss (set (map ns-name (all-ns))))
;; (take 2 (diff libs nss))
;; ; (nil 
;; ;  #{cider.nrepl.pprint reply.exports clojure.core myns})
;;
;; ;; 5.clj
;;
;; (create-ns 'test-require)
;; (require 'test-require)
;; ;; Could not locate test_require__init.class or test_require.clj on classpath.
;;
;; ;; 6.clj
;;
;; (ns myns)
;;
;; (require
;;  '[clojure.set
;;    :as se
;;    :refer [union]]
;;  '[clojure.string
;;    :as st
;;    :refer :all])
;; ;; WARNING: reverse already refers to: #'clojure.core/reverse
;; ;; WARNING: replace already refers to: #'clojure.core/replace
;;
;; ;; 7.clj
;;
;; (ns myns)
;;
;; (use '[clojure.java.io
;;        :only [reader file]
;;        :rename {reader r}]
;;      :verbose
;;      :reload-all)
;;
;; ;; (load "/clojure/java/io")
;; ;; (in-ns 'myns)
;; ;; (refer 'clojure.java.io :only '[reader file] :rename '{reader r})
;;
;; ;; 8.clj
;;
;; (ns user)
;; (clean-ns 'myns)
;; ;; {}
;;
;; (binding [*ns* (the-ns 'myns)]
;;   (import '[java.util ArrayList HashMap]))
;;
;; (ns-imports 'myns)
;; ;; {HashMap java.util.HashMap
;; ;;  ArrayList java.util.ArrayList}
;;

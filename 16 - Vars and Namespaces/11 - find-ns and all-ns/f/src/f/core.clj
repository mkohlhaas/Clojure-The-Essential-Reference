(ns f.core)

(take 10 (all-ns))
; (#object[clojure.lang.Namespace 0x3c92ce0 "clojure.spec.gen.alpha"]
;  #object[clojure.lang.Namespace 0x71306cb9 "nrepl.middleware.interruptible-eval"]
;  #object[clojure.lang.Namespace 0x6455ddbd "cider.nrepl.pprint"]
;  #object[clojure.lang.Namespace 0x6053875c "clojure.stacktrace"]
;  #object[clojure.lang.Namespace 0x7c0822b1 "clojure.core.rrb-vector.transients"]
;  #object[clojure.lang.Namespace 0x7b0e6290 "clojure.uuid"]
;  #object[clojure.lang.Namespace 0x31fce079 "clojure.main"]
;  #object[clojure.lang.Namespace 0x4de9e87d "user"]
;  #object[clojure.lang.Namespace 0x704956db "clojure.test"]
;  #object[clojure.lang.Namespace 0x785bb0a0 "clojure.data"])

(ns-name (first (all-ns))) ; clojure.spec.gen.alpha

(find-ns 'clojure.edn) ; #object[clojure.lang.Namespace 0x2cdad749 "clojure.edn"]
(find-ns 'no-ns)       ; nil

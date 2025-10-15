(require '[clojure.repl :refer [doc]])

(doc interleave)
; (out) -------------------------
; (out) clojure.core/interleave
; (out) ([] [c1] [c1 c2] [c1 c2 & colls])
; (out)   Returns a lazy seq of the first item in each coll, then the second etc.

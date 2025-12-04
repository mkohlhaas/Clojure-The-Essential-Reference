(ns f.core)

;; - non-functional requirements are often a good candidate for metadata: debugging information, types, documentation etc.
;; - metadata do not impact equality semantic of the object they are attached to, which means that two equal objects still
;;   compare equal when their metadata are different.

(meta #'+)
; {:added "1.2",
;  :ns #object[clojure.lang.Namespace 0x6c03c5b8 "clojure.core"],
;  :name +,
;  :file "clojure/core.clj",
;  :inline-arities
;  #object[clojure.core$_GT_1_QMARK_ 0x84581ea "clojure.core$_GT_1_QMARK_@84581ea"],
;  :column 1,
;  :line 986,
;  :arglists ([] [x] [x y] [x y & more]),
;  :doc
;  "Returns the sum of nums. (+) returns 0. Does not auto-promote\n  longs, will throw on overflow. See also: +'",
;  :inline
;  #object[clojure.core$nary_inline$fn__5627 0x56441b84 "clojure.core$nary_inline$fn__5627@56441b84"]}

(meta 1) ; nil

(def v (with-meta [1 2 3] {:initial-count 3}))

(meta (conj v 3 4 5))                                ; {:initial-count 3} (metadata didn't change)
(meta (with-meta (with-meta [1 2 3] {:a 1}) {:a 2})) ; {:a 2}             (to change/replace metadata use `with-meta`)
(meta (into [] v))                                   ; nil                (`into` doesn't copy the metadata)

(def v1 (with-meta [1 2 3]
          {:initial-count 3
           :last-modified #inst "1985-04-12"}))

(meta v1)
; {:initial-count 3,
;  :last-modified #inst "1985-04-12T00:00:00.000-00:00")

;; change existing metadata
(def v2 (vary-meta (conj v 4) assoc :last-modified #inst "1985-04-13"))

(meta v2)
; {:initial-count 3,
;  :last-modified #inst "1985-04-13T00:00:00.000-00:00")

(def counter
  (atom 0 :meta {:last-modified #inst "1985-04-12"}))

(meta counter) ; {:last-modified #inst "1985-04-12T00:00:00.000-00:00"}

;; change existing metadata in-place
(alter-meta!
 (do (swap! counter inc) counter)
 assoc :last-modified #inst "1985-04-13")

(meta counter) ; {:last-modified #inst "1985-04-13T00:00:00.000-00:00"}

;; completely replace metadata
(reset-meta! *ns* {:doc "The default user namespace"})
(meta *ns*)     ; {:doc "The default user namespace"}

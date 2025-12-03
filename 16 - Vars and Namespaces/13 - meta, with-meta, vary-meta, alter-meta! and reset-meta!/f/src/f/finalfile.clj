;; 1.clj

(pprint (meta #'+))
; {:added "1.2",
;  :ns #object[clojure.lang.Namespace 0x417a86b0 "clojure.core"],
;  :name +,
;  :file "clojure/core.clj",
;  :inline-arities
;  #object[clojure.core$_GT_1_QMARK_ 0x1aaf28ab "clojure.core$_GT_1_QMARK_@1aaf28ab"],
;  :column 1,
;  :line 986,
;  :arglists ([] [x] [x y] [x y & more]),
;  :doc
;  "Returns the sum of nums. (+) returns 0. Does not auto-promote\n  longs, will throw on overflow. See also: +'",
;  :inline
;  #object[clojure.core$nary_inline$fn__5627 0x2c19ada0 "clojure.core$nary_inline$fn__5627@2c19ada0"]}

;; {:added "1.2",
;;  :ns #object[clojure.lang.Namespace 0x1edb61b1 "clojure.core"],
;;  :name +,
;;  :file "clojure/core.clj",
;;  :inline-arities
;;  #object[clojure.core$_GT_1_QMARK_ 0x7b22ec89 "GT_1_QMARK"],
;;  :column 1,
;;  :line 965,
;;  :arglists ([] [x] [x y] [x y & more]),
;;  :doc
;;  "Returns the sum of nums."
;;  :inline
;;  #object[clojure.core$nary_inline 0x790132f7 "clojure.core$nary_inline"]}

(meta 1)
;; nil

;; 2.clj

(def v (with-meta [1 2 3] {:initial-count 3}))

(meta (conj v 3 4 5))
{:initial-count 3}

(meta (with-meta (with-meta [1 2 3] {:a 1}) {:a 2}))
;; {:a 2}

(meta (into [] v))
;; nil

;; 3.clj

(def v (with-meta [1 2 3]
         {:initial-count 3 :last-modified #inst "1985-04-12"}))

(meta v)
;; {:initial-count 3
;;  :last-modified #inst "1985-04-12T00:00:00.000-00:00"}

(def v (vary-meta (conj v 4) assoc :last-modified #inst "1985-04-13"))

(meta v)
;; {:initial-count 3
;;  :last-modified #inst "1985-04-13T00:00:00.000-00:00"}

;; 4.clj

(def counter
  (atom 0
        :meta {:last-modified #inst "1985-04-12"}))

(meta counter)
;; {:last-modified #inst "1985-04-12T00:00:00.000-00:00"}

(alter-meta!
 (do (swap! counter inc) counter)
 assoc :last-modified #inst "1985-04-13")

(meta counter)
;; {:last-modified #inst "1985-04-13T00:00:00.000-00:00"}

;; 5.clj

(reset-meta! *ns* {:doc "The default user namespace"})
(meta *ns*)
;; {:doc "The default user namespace"}


(ns f.core
  (:require
   [clojure.string :as s]))

(defprotocol Reflect
  (declared-methods [this]))

(extend java.lang.Object
  Reflect
  {:declared-methods
   (fn [this]
     (map
      (comp #(s/replace % #"clojure\.lang\." "cl.")
            #(s/replace % #"java\.lang\." "jl."))
      (.getDeclaredMethods (class this))))})

(declared-methods (atom nil))
; ("public jl.Object cl.Atom.reset(jl.Object)"
;  "public boolean cl.Atom.compareAndSet(jl.Object,jl.Object)"
;  "public jl.Object cl.Atom.swap(cl.IFn,jl.Object)"
;  "public jl.Object cl.Atom.swap(cl.IFn,jl.Object,jl.Object,cl.ISeq)"
;  "public jl.Object cl.Atom.swap(cl.IFn,jl.Object,jl.Object)"
;  "public jl.Object cl.Atom.swap(cl.IFn)"
;  "public jl.Object cl.Atom.deref()"
;  "public cl.IPersistentVector cl.Atom.resetVals(jl.Object)"
;  "public cl.IPersistentVector cl.Atom.swapVals(cl.IFn)"
;  "public cl.IPersistentVector cl.Atom.swapVals(cl.IFn,jl.Object)"
;  "public cl.IPersistentVector cl.Atom.swapVals(cl.IFn,jl.Object,jl.Object,cl.ISeq)"
;  "public cl.IPersistentVector cl.Atom.swapVals(cl.IFn,jl.Object,jl.Object)")

(defprotocol IFace
  (m1 [this])
  (m2 [this])
  (m3 [this]))

(def AFace
  {:m1 (fn [_this] "AFace::m1")
   :m2 (fn [_this] "AFace::m2")})

(defrecord MyFace [])

(extend MyFace
  IFace
  (assoc AFace :m1 (fn [_this] "MyFace::m1")))

(m1 (->MyFace)) ; "MyFace::m1"
(m2 (->MyFace)) ; "AFace::m2"

(comment
  (m3 (->MyFace)))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) No implementation of method: :m3 of protocol: #'f.core/IFace found for class: f.core.MyFace

(def my-face (->MyFace))

(m1 my-face) ; "MyFace::m1"

(extend MyFace
  IFace
  (assoc AFace :m1 (fn [_this] "new")
         :m3 (fn [_this] "m3")))

(m1 my-face) ; "new"
(m3 my-face) ; "m3"

(defprotocol INode   (value [_]))
(defprotocol IBranch (left [_]) (right [_]))
(defprotocol ILeaf   (compute [_]))

(extend f.core.INode IBranch)
(extend f.core.INode ILeaf)

(defrecord Branch [id left right]
  INode   (value [_] (str "Branch::" id))
  IBranch (left [_] left) (right [_] right))

(defrecord Leaf [id]
  INode (value [_] (str "Leaf::" id))
  ILeaf (compute [_] (str "computed:" id)))

(def tree
  (->Branch 1
            (->Branch :A (->Leaf 4) (->Leaf 5))
            (->Branch :B (->Leaf 6) (->Leaf 7))))
; {:id 1,
;  :left {:id :A, :left {:id 4}, :right {:id 5}},
;  :right {:id :B, :left {:id 6}, :right {:id 7}}}

(defn traverse
  ([tree]
   (traverse [] tree))
  ([acc tree]
   (let [acc (conj acc (value tree))]
     (if (satisfies? IBranch tree)
       (into
        (traverse acc (left tree))
        (traverse acc (right tree)))
       (conj acc (compute tree))))))

(traverse tree)
; ["Branch::1" "Branch:::A" "Leaf::4" "computed:4"
;  "Branch::1" "Branch:::A" "Leaf::5" "computed:5"
;  "Branch::1" "Branch:::B" "Leaf::6" "computed:6"
;  "Branch::1" "Branch:::B" "Leaf::7" "computed:7"]

(extend-type MyFace
  IFace
  (m1 [_this] "MyFace::m1")
  (m2 [_this] "MyFace::m2")
  (m3 [_this] "MyFace::m3"))

(m2 my-face) ; "AFace::m2"

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defprotocol Money
  (as-currency [n]))

(extend-protocol Money
  Integer
  (as-currency [n] (format "%s$" n))
  clojure.lang.Ratio
  (as-currency [n]
    (format "%s$ and %sc" (numerator n) (denominator n))))

(extenders Money)         ; (java.lang.Integer clojure.lang.Ratio)
(extends?  Money Integer) ; true

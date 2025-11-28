(ns f.core
  (:require
   [clojure.string :as s]))

;; `extend`, `extend-type` and `extend-protocol` add new dispatch options to a protocol

(defprotocol Reflect
  (declared-methods [this]))

(extend java.lang.Object
  Reflect
  {:declared-methods ; as keyword (a map from keyword - as the function names - to function implementation)
   (fn [this]
     (map
      (comp #(s/replace % #"clojure\.lang\." "cl.")
            #(s/replace % #"java\.lang\."    "jl."))
      (.getDeclaredMethods (class this))))})

;; works on anything bc we extended java.lang.Object
(declared-methods (atom nil)) ; an atom was chosen bc it generates a rather small list
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

;; Lightweight Version of Java Abstract Classes ;;

(defprotocol IFace
  (m1 [this])
  (m2 [this])
  (m3 [this]))

;; default implementations for two methods (will be used as a "mix-in")
(def AFace
  {:m1 (fn [_this] "AFace::m1")
   :m2 (fn [_this] "AFace::m2")})

(defrecord MyFace [])

(extend MyFace
  IFace
  (assoc AFace :m1 (fn [_this] "MyFace::m1")))

(m1 (->MyFace)) ; "MyFace::m1" (overridden implementation)
(m2 (->MyFace)) ; "AFace::m2"  (default    implementation)

(comment
  ;; still no implementation for `m3`
  (m3 (->MyFace)))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) No implementation of method: :m3 of protocol: #'f.core/IFace found for class: f.core.MyFace

;; `defrecord` and `deftype` have two options for implementing a protocol:
;; 1. Implementing the protocol at declaration time adds the methods to the interface of the generated class, 
;;    making them visible from Java but also impossible to extend. Once the implementation is attached at 
;;    declaration time it can't change unless the entire record is redefined.

;; 2. Implementing the protocol by extension allows instances of the record to be extended to the protocol 
;;    at some later time, including the option to change the implementation without redefining the record.

;; change is dynamic and applies to all instances of the same record type
(def my-face (->MyFace))

(m1 my-face) ; "MyFace::m1"

(extend MyFace
  IFace
  (assoc AFace
         :m1 (fn [_this] "new")
         :m3 (fn [_this] "m3")))

;; reflect the new interface implementations
(m1 my-face) ; "new"
(m2 my-face) ; "MyFace::m2"
(m3 my-face) ; "m3"

;; Extending Protocols to Interfaces or Other Protocols ;;

(defprotocol INode
  (value [_])) ; _ = this

(defprotocol IBranch
  (left  [_])
  (right [_]))

(defprotocol ILeaf
  (compute [_]))

;; superfluous
;; (extend IBranch INode)
;; (extend ILeaf   INode)

(defrecord Branch [id left right]
  INode
  (value [_] (str "Branch::" id))
  IBranch
  (left  [_] left)
  (right [_] right))

(defrecord Leaf [id]
  INode (value   [_] (str "Leaf::"    id))
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

(partition 4 (traverse tree))
; (("Branch::1" "Branch:::A" "Leaf::4" "computed:4")
;  ("Branch::1" "Branch:::A" "Leaf::5" "computed:5")
;  ("Branch::1" "Branch:::B" "Leaf::6" "computed:6")
;  ("Branch::1" "Branch:::B" "Leaf::7" "computed:7"))

;; `extend-type` can extend many protocols for one type

;; `extend-type` is a shorter form of `extend` ;;
(extend-type MyFace
  IFace
  (m1 [_this] "MyFace::m1")
  (m2 [_this] "MyFace::m2")
  (m3 [_this] "MyFace::m3"))

(m1 my-face) ; "MyFace::m1"
(m2 my-face) ; "MyFace::m2"
(m3 my-face) ; "MyFace::m3"

;; `extend-protocol` can extend many types for one protocol

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defprotocol Money
  (as-currency [n]))

(extend-protocol Money
  Long
  (as-currency [n] (format "$%s" n))
  clojure.lang.Ratio
  (as-currency [n]
    (format "$%s and %s Cents" (numerator n) (denominator n))))

(extenders Money)      ; (java.lang.Long clojure.lang.Ratio)
(extends?  Money Long) ; true
(as-currency 100)      ; "$100"
(as-currency 22/7)     ; "$22 and 7 Cents"

;; zippers separate the concept of traversal from processing

(ns f.core
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.xml :as xml]
   [clojure.zip :as zip]))

;; ;;;;;;;;;;;;;;;;
;; Building Zippers
;; ;;;;;;;;;;;;;;;;

;; create zipper from a vector
;; Returns a tuple containing the data at the current location (initially the entire input) 
;; and a map that describes the surrounding nodes (initially nil).
(def vzip
  (zip/vector-zip
   [(subvec [1 2 2] 0 2)
    [3 4 [5 10 (vector-of :int 11 12)]]
    [13 14]]))
; [[[1 2] [3 4 [5 10 [11 12]]] [13 14]] nil]

(type vzip) ; clojure.lang.PersistentVector

;; create zipper from a sequence
(def szip
  (zip/seq-zip
   (list
    (range 2)
    (take 2 (cycle [1 2 3]))
    '(3 4 (5 10))
    (cons 1 '(0 2 3)))))
; [((0 1) (1 2) (3 4 (5 10)) (1 0 2 3)) nil]

(type szip) ; clojure.lang.PersistentVector

;; create zipper from xml document
(def xzip
  (zip/xml-zip
   (->
    "<b>
       <a>3764882</a>
       <c>80.12389</c>
       <f>
         <f1>77488</f1>
         <f2>1921.89</f2>
       </f>
     </b>"
    .getBytes io/input-stream xml/parse)))
; [{:tag :b,
;   :attrs nil,
;   :content
;   [{:tag :a, :attrs nil, :content ["3764882"]}
;    {:tag :c, :attrs nil, :content ["80.12389"]}
;    {:tag :f,
;     :attrs nil,
;     :content
;     [{:tag :f1, :attrs nil, :content ["77488"]}
;      {:tag :f2, :attrs nil, :content ["1921.89"]}]}]}
;  nil]

(type xzip) ; clojure.lang.PersistentVector

;; recipe that describes how to traverse the data structure is embedded as metadata 
(pprint (meta vzip))
; (out) #:zip{:branch?   #object[clojure.core$vector_QMARK___5479 0x2f11a07e "clojure.core$vector_QMARK___5479@2f11a07e"],
; (out)       :children  #object[clojure.core$seq__5467 0x76676126 "clojure.core$seq__5467@76676126"],
; (out)       :make-node #object[clojure.zip$vector_zip$fn__9446 0x63af5f7c "clojure.zip$vector_zip$fn__9446@63af5f7c"]}

;; ;;;;;;;;;;;;;;;;;;
;; Location Functions
;; ;;;;;;;;;;;;;;;;;;

;; location of a zipper is the vector of two items returned right after construction (and after calling a location function, see next example)
(pprint vzip)
; (out) [[[1 2] [3 4 [5 10 [11 12]]] [13 14]] nil]

;; changing location (going down)
(pprint (zip/down vzip))
; (out) [[1 2]
; (out)  {:l [],
; (out)   :pnodes [[[1 2] [3 4 [5 10 [11 12]]] [13 14]]],
; (out)   :ppath nil,
; (out)   :r ([3 4 [5 10 [11 12]]] [13 14])}]

;; changing location (going to the rightmost node)
;; :l = left node, :r = right node, :pnodes = parent nodes
(pprint (zip/rightmost (zip/down vzip)))
; (out) [[13 14]
; (out)  {:l [[1 2] [3 4 [5 10 [11 12]]]],
; (out)   :pnodes [[[1 2] [3 4 [5 10 [11 12]]] [13 14]]],
; (out)   :ppath nil,
; (out)   :r nil}]

;; on reaching the edge of the data in any direction, location functions return `nil`
(-> vzip zip/down zip/down zip/down) ; nil

(-> xzip zip/down zip/node)          ; {:tag :a, :attrs nil, :content ["3764882"]}
(-> xzip zip/down zip/children)      ; ("3764882")
(-> xzip zip/down zip/lefts)         ; nil
(-> xzip zip/down zip/rights)
; ({:tag :c, :attrs nil, :content ["80.12389"]}
;  {:tag :f,
;   :attrs nil,
;   :content
;   [{:tag :f1, :attrs nil, :content ["77488"]}
;    {:tag :f2, :attrs nil, :content ["1921.89"]}]})

(zip/node szip) ; ((0 1) (1 2) (3 4 (5 10)) (1 0 2 3))

;; path - a sequence of nodes - to destination location
;; it's idiomatic to compose location functions using `->`
(-> szip
    zip/down zip/right zip/right
    zip/down zip/rightmost
    zip/down
    zip/path)
; [((0 1) (1 2) (3 4 (5 10)) (1 0 2 3)) (3 4 (5 10)) (5 10)]

;; ;;;;;;;;;;;;;;;;;;;;
;; Retrieving Functions
;; ;;;;;;;;;;;;;;;;;;;;

(-> xzip zip/down zip/node)     ; {:tag :a, :attrs nil, :content ["3764882"]}
(-> xzip zip/down zip/children) ; ("3764882")
(-> xzip zip/down zip/lefts)    ; nil
(-> xzip zip/down zip/rights)
;; ({:tag :c, :attrs nil, :content ["80.12389"]}
;;  {:tag :f, ;;   :attrs nil,
;;   :content
;;   [{:tag :f1, :attrs nil, :content ["77488"]}
;;    {:tag :f2, :attrs nil, :content ["1921.89"]}]})

;; ;;;;;;;;;;;;;;;;;;;;;;;
;; Creating Custom Zippers
;; ;;;;;;;;;;;;;;;;;;;;;;;

;; a parsed JSON document
(def json-document
  {:tag :balance
   :meta {:class "bold"}
   :node
   [{:tag :accountId
     :meta nil
     :node [3764882]}
    {:tag :lastAccess
     :meta nil
     :node ["2011/01/01"]}
    {:tag :currentBalance
     :meta {:class "red"}
     :node [{:tag :checking
             :meta nil
             :node [90.11]}]}]})

;; custom zipper for JSON documents (from the xml-zip implementation)
(defn json-zip [root]
  (zip/zipper
   #(some-> % :node first map?)          ; branch?
   (comp seq :node)                      ; children
   (fn [node children]                   ; make-node
     (assoc node :node (vec children)))
   root))                                ; root

;; create json zipper
(def jzip (json-zip json-document))

;; traverse/navigate
(-> jzip zip/down zip/rightmost zip/down zip/node)
; {:tag :checking, :meta nil, :node [90.11]}

;; ;;;;;;;;;;;;;;;;
;; Update Functions
;; ;;;;;;;;;;;;;;;;

;; `replace`, `edit`, `remove`
(-> vzip zip/down zip/rightmost (zip/replace :replaced) zip/up zip/node) ; [[1 2] [3 4 [5 10 [11 12]]] :replaced]
(-> vzip zip/down zip/rightmost (zip/edit conj 15) zip/up zip/node)      ; [[1 2] [3 4 [5 10 [11 12]]] [13 14 15]]
(-> vzip zip/down zip/rightmost zip/remove zip/root)                     ; [[1 2] [3 4 [5 10 [11 12]]]]
(-> vzip zip/down zip/rightmost zip/remove zip/node)                     ; 12

;; this is what happened in the last example
(zip/node vzip) ; [[1 2] [3 4 [5 10 [11 12]]] [13 14]]
;; [[1 2]
;;  [3 4 [5 10 [11 12]]] ; location jump on 12 (2)
;;  [13 14]]             ; removes here        (1)

;; add nodes: `insert-left`, `insert-right`, `insert-child`
(-> vzip zip/down zip/rightmost (zip/insert-left  'INS) zip/up zip/node)  ; [[1 2] [3 4 [5 10 [11 12]]] INS [13 14]]
(-> vzip zip/down zip/rightmost (zip/insert-right 'INS) zip/up zip/node)  ; [[1 2] [3 4 [5 10 [11 12]]] [13 14] INS]
(-> vzip zip/down zip/rightmost (zip/insert-child 'INS) zip/up zip/node)  ; [[1 2] [3 4 [5 10 [11 12]]] [INS 13 14]]

(comment
  (-> vzip zip/down zip/rightmost zip/down (zip/insert-child 'INS)))
  ; (err) called children on a leaf node

;; `insert-child`, `append-child`
(-> vzip zip/down zip/rightmost (zip/insert-child 'INS) zip/up zip/node) ; [[1 2] [3 4 [5 10 [11 12]]] [INS 13 14]]
(-> vzip zip/down zip/rightmost (zip/append-child 'INS) zip/up zip/node) ; [[1 2] [3 4 [5 10 [11 12]]] [13 14 INS]]

(comment
  (-> vzip zip/down zip/rightmost zip/down (zip/insert-child 'INS)))
  ; (err) called children on a leaf node

;; `make-node`
;; remove first child from a node 
(defn remove-child [loc]
  (zip/replace loc (zip/make-node loc (zip/node loc) (rest (zip/children loc)))))

(-> vzip zip/down zip/rightmost remove-child zip/up zip/node)              ; [[1 2] [3 4 [5 10 [11 12]]] [14]]
(-> vzip zip/down zip/rightmost remove-child remove-child zip/up zip/node) ; [[1 2] [3 4 [5 10 [11 12]]] []]

;; ;;;;;;;;;;;;;;;;;;;
;; Traversal Functions
;; ;;;;;;;;;;;;;;;;;;;

;; `next`, `prev`
(-> vzip zip/next zip/node)                            ; [1 2]
(-> vzip zip/next zip/next zip/node)                   ; 1
(-> vzip zip/next zip/next zip/next zip/node)          ; 2
(-> vzip zip/next zip/next zip/next zip/next zip/node) ; [3 4 [5 10 [11 12]]]

(-> vzip zip/next zip/prev zip/node)          ; [[1 2] [3 4 [5 10 [11 12]]] [13 14]]
(-> vzip zip/next zip/next zip/prev zip/node) ; [1 2]

(->> vzip
     (iterate zip/next)
     (take-while (complement zip/end?))
     (map zip/node))
; ([[1 2] [3 4 [5 10 [11 12]]] [13 14]]
;  [1 2]
;  1 2
;  [3 4 [5 10 [11 12]]]
;  3 4
;  [5 10 [11 12]]
;  5 10
;  [11 12]
;  11 12
;  [13 14]
;  13 14)

;; similar to clojure.walk/prewalk
(defn zip-walk [fn zipper]
  (if (zip/end? zipper)
    (zip/root zipper)
    (recur fn (zip/next (fn zipper)))))

(zip-walk
 #(if (zip/branch? %)
    %
    (zip/edit % * 2))
 (zip/vector-zip [1 2 [3 4]]))
; [2 4 [6 8]]

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; There is no going back after traversal
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def zipper-end
  (-> (zip/vector-zip [1 2]) zip/next zip/next zip/next))
; [[1 2] :end]

(zip/end? zipper-end) ; true
(zip/prev zipper-end) ; nil
(zip/root zipper-end) ; [1 2]

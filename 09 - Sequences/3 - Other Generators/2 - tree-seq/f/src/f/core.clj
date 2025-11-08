(ns f.core
  (:require
   [clojure.pprint]
   [clojure.xml :refer [parse]]
   [criterium.core :refer [quick-bench]])
  (:import
   [java.io File]))

;; `tree-seq` is the lazy equivalent of `clojure.walk`.

;; (defn pretty-print [x]
;;   (println (with-out-str (clojure.pprint/write x))))

;; depth-first traversal order
(tree-seq vector? identity [[1 2 [3 [[4 5] [] 6]]]])
; ([[1 2 [3 [[4 5] [] 6]]]]
;  [1 2 [3 [[4 5] [] 6]]]
;  1
;  2
;  [3 [[4 5] [] 6]]
;  3
;  [[4 5] [] 6]
;  [4 5]
;  4
;  5
;  []
;  6)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; collect all children that fullfil `pred?` or `branch?`
(defn collect [pred? branch?]
  (fn [children]
    (filter
     (fn [node]
       (or (branch? node) (pred? node)))
     children)))

(defn collect-if [pred? root]
  (let [branch?   vector?
        children (collect pred? branch?)]
    (->> root
         (tree-seq branch? children)
         (remove   branch?))))

(comment
  (remove vector? (tree-seq vector? identity [[1 2 [3 [[4 5] [] 6]]]]))) ; (1 2 3 4 5 6)

;; positive nodes in depth-first order
(collect-if pos? [[1] [-2 4 [-3 [4] 5 8] -6 7]]) ; (1 4 4 5 8 7)
; without `remove` step:
; ([[1] [-2 4 [-3 [4] 5 8] -6 7]]
;  [1]
;  1
;  [-2 4 [-3 [4] 5 8] -6 7]
;  4
;  [-3 [4] 5 8]
;  [4]
;  4
;  5
;  8
;  7)

;; Lazy Directory Traversal

(take 5 (tree-seq
         (memfn ^File isDirectory)          ; directory is a branch
         (comp seq (memfn ^File listFiles)) ; get files from directory
         (File. "/")))                      ; start at root directory
; (#object[java.io.File 0x63653a9b "/"]
;  #object[java.io.File 0x536c8e57 "/srv"]
;  #object[java.io.File 0x407c1004 "/srv/ftp"]
;  #object[java.io.File 0x741ed2aa "/srv/http"]
;  #object[java.io.File 0x50038375 "/opt"])

;; JSON

(def document
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

;; branch is a map which contains `:node`
(def branch? (every-pred map? :node))

(comment
  (branch? document)                  ; true
  (branch? (first (:node document)))) ; true

(def document-seq
  (tree-seq
   branch?
   :node
   document))
; ({:tag :balance,
;   :meta {:class "bold"},
;   :node
;   [{:tag :accountId, :meta nil, :node [3764882]}
;    {:tag :lastAccess, :meta nil, :node ["2011/01/01"]}
;    {:tag :currentBalance,
;     :meta {:class "red"},
;     :node [{:tag :checking, :meta nil, :node [90.11]}]}]}
;  {:tag :accountId, :meta nil, :node [3764882]}
;  3764882
;  {:tag :lastAccess, :meta nil, :node ["2011/01/01"]}
;  "2011/01/01"
;  {:tag :currentBalance,
;   :meta {:class "red"},
;   :node [{:tag :checking, :meta nil, :node [90.11]}]}
;  {:tag :checking, :meta nil, :node [90.11]}
;  90.11)

(remove branch? document-seq) ; (3764882 "2011/01/01" 90.11)
(keep :meta document-seq)     ; ({:class "bold"} {:class "red"})

;; An eager tree-seq

;; using `persistent!` and `transient`
(defn eager-tree-seq [branch? children root]
  (letfn [(step [res root]
            (let [res (conj! res root)]
              (if (branch? root)
                (reduce step res (children root))
                res)))]
    (persistent! (step (transient []) root))))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (def document (parse "https://nvd.nist.gov/feeds/xml/cve/misc/nvd-rss.xml"))

  ;; tree-seq 
  (let [branch?  (complement string?)
        children (comp seq :content)]
    (quick-bench (dorun (tree-seq branch? children document))))
  ; (out) Execution time mean : 6.231241 ms

  ;; eager-tree-seq 
  (let [branch?  (complement string?)
        children (comp seq :content)]
    (quick-bench (doall (eager-tree-seq branch? children document)))))
  ; (out) Execution time mean : 1.171720 ms

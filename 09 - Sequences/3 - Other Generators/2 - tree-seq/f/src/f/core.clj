(ns f.core
  (:require
   [clojure.pprint]
   [clojure.xml :refer [parse]]
   [criterium.core :refer [quick-bench]])
  (:import
   [java.io File]))

(defn pretty-print [x]
  (println (with-out-str (clojure.pprint/write x))))

(pretty-print
 (tree-seq vector? identity [[1 2 [3 [[4 5] [] 6]]]]))
; (out) ([[1 2 [3 [[4 5] [] 6]]]]
; (out)  [1 2 [3 [[4 5] [] 6]]]
; (out)  1
; (out)  2
; (out)  [3 [[4 5] [] 6]]
; (out)  3
; (out)  [[4 5] [] 6]
; (out)  [4 5]
; (out)  4
; (out)  5
; (out)  []
; (out)  6)

(defn collect [pred? branch?]
  (fn [children]
    (filter
     (fn [node]
       (or (branch? node) (pred? node)))
     children)))

(defn collect-if [pred? root]
  (let [branch? vector?
        children (collect pred? branch?)]
    (->> root
         (tree-seq branch? children)
         (remove branch?))))

(collect-if pos? [[1] [-2 4 [-3 [4] 5 8] -6 7]]) ; (1 4 4 5 8 7)

(take 5 (tree-seq
         (memfn ^File isDirectory)
         (comp seq (memfn ^File listFiles))
         (File. "/")))
; (#object[java.io.File 0x63653a9b "/"]
;  #object[java.io.File 0x536c8e57 "/srv"]
;  #object[java.io.File 0x407c1004 "/srv/ftp"]
;  #object[java.io.File 0x741ed2aa "/srv/http"]
;  #object[java.io.File 0x50038375 "/opt"])

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

(def branch? (every-pred map? :node))

(def document-seq
  (tree-seq
   branch?
   :node
   document))

(remove branch? document-seq) ; (3764882 "2011/01/01" 90.11)

(keep :meta document-seq)     ; ({:class "bold"} {:class "red"})

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

  (let [branch? (complement string?) ; <1>
        children (comp seq :content)]
    (quick-bench (dorun (tree-seq branch? children document))))
;; Execution time mean : 2304.531 µs

  (let [branch? (complement string?) ; <2>
        children (comp seq :content)]
    (quick-bench (doall (eager-tree-seq branch? children document)))))
;; Execution time mean : 437.484386 µs

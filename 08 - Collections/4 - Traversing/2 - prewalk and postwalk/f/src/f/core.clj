(ns f.core
  (:require
   [clojure.walk :refer [postwalk postwalk-demo prewalk prewalk-demo]]))

;; `prewalk`  is a depth-first, pre-order  traversal
;; `postwalk` is a depth-first, post-order traversal

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(prewalk #(do (println %) %) [1 [2 [3]]])
; (out) [1 [2 [3]]]
; (out) 1
; (out) [2 [3]]
; (out) 2
; (out) [3]
; (out) 3
; [1 [2 [3]]]

(def data
  {:type "workflow"
   :action '(do (println "flowchart") :done)
   :nodes [{:type "flowchart"
            :action '(do (println "flowchart") :done)
            :nodes [{:type   "workflow"
                     :action  nil
                     :nodes   false}]}
           {:type "routine"
            :action '(do (println "routine") :done)
            :nodes [{:type   "delimiter"
                     :action '(println "delimiter")
                     :nodes  "2011/01/01"}]}
           {:type "pipeline"
            :action '(do (println "pipeline") :done)
            :nodes [{:type   "workflow"
                     :action '(Thread/sleep 10000) ; 10s sleep
                     :nodes   90.11}]}
           {:type "delimiter"
            :action '(do (println "delimiter") :done)
            :nodes [{:type  "workflow"
                     :nodes  90.11}]}]})

(prewalk-demo data)

;; if a node is of type "pipeline" we don’t want to execute any ":action" in the current or nested nodes
(defn- step [node]
  (if (= (:type node) "pipeline")
    (dissoc node :nodes) ; remove pipeline node and its subnodes (no 10s sleep)
    (do
      (eval (:action node))
      node)))

(time (prewalk step data))
; (out) flowchart
; (out) flowchart
; (out) routine
; (out) delimiter
; (out) delimiter
; (out) "Elapsed time: 6.090129 msecs"
; {:type "workflow",
;  :action (do (println "flowchart") :done),
;  :nodes
;  [{:type "flowchart",
;    :action (do (println "flowchart") :done),
;    :nodes [{:type "workflow", :action nil, :nodes false}]}
;   {:type "routine",
;    :action (do (println "routine") :done),
;    :nodes
;    [{:type "delimiter",
;      :action (println "delimiter"),
;      :nodes "2011/01/01"}]}
;   {:type "pipeline", :action (do (println "pipeline") :done)}
;   {:type "delimiter",
;    :action (do (println "pipeline") :done),
;    :nodes [{:type "workflow", :nodes 90.11}]}]}

(comment
  (time (= (prewalk step data) (postwalk step data))))
  ; prewalk
  ; (out) flowchart
  ; (out) flowchart
  ; (out) routine
  ; (out) delimiter
  ; (out) delimiter

  ; postwalk (10s sleep executed)
  ; (out) flowchart
  ; (out) delimiter
  ; (out) routine
  ; (out) delimiter
  ; (out) flowchart

;; ;;;;;;;;;;;;;;;;;
;; Compound Interest
;; ;;;;;;;;;;;;;;;;;

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
;; in Clojure code is data, data is code (AST)
(defn compound-interest [rate loan-amount period]
  (* loan-amount
     (Math/pow
      (inc (/ rate 100. 12))
      (* 12 period))))

;; the same as a data structure (AST)
(defn compound-interest-data [rate loan-amount period]
  {:function *
   :children
   [loan-amount
    {:function #(Math/pow %1 %2)
     :children [{:function inc
                 :children [{:function /
                             :children [rate 100. 12]}]}
                {:function *
                 :children [12 period]}]}]})

(defn evaluate [node]
  (if-let [f (:function node)] ; use only maps with :function, also has :children (see `postwalk-demo`)
    (apply f (:children node)) ; replaces the node
    node))                     ; node is unchanged

(comment
  (postwalk evaluate (compound-interest-data 7.2 5000 2))
  ; 5771.936460924754

  (compound-interest-data 7.2 5000 2)
  ; {:function *],
  ;  :children
  ;  [5000
  ;   {:function Math/pow,
  ;    :children
  ;    [{:function inc,
  ;      :children
  ;      [{:function /,
  ;        :children [7.2 100.0 12]}]}
  ;     {:function *,
  ;      :children [12 2]}]}]}

  (postwalk-demo (compound-interest-data 7.2 5000 2)))

(ns f.core
  (:require
   [clojure.walk :refer [postwalk prewalk]]))

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
            :nodes [{:type "workflow"
                     :action nil
                     :nodes false}]}
           {:type "routine"
            :action '(do (println "routine") :done)
            :nodes [{:type "delimiter"
                     :action '(println "delimiter")
                     :nodes "2011/01/01"}]}
           {:type "pipeline"
            :action '(do (println "pipeline") :done)
            :nodes [{:type "workflow"
                     :action '(Thread/sleep 10000)
                     :nodes 90.11}]}
           {:type "delimiter"
            :action '(do (println "pipeline") :done)
            :nodes [{:type "workflow"
                     :nodes 90.11}]}]})

(defn- step [node]
  (if (= "pipeline" (:type node))
    (dissoc node :nodes)
    (do
      (eval (:action node))
      node)))

(time (prewalk step data))
; (out) flowchart
; (out) flowchart
; (out) routine
; (out) delimiter
; (out) pipeline
; (out) "Elapsed time: 6.179922 msecs"
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
  ; (out) flowchart
  ; (out) flowchart
  ; (out) routine
  ; (out) delimiter
  ; (out) pipeline
  ; (out) flowchart
  ; (out) delimiter
  ; (out) routine
  ; (out) pipeline
  ; (out) flowchart
  ; (out) "Elapsed time: 10015.026127 msecs"
  ; true

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn compound-interest
  [rate loan-amount period]
  (* loan-amount
     (Math/pow
      (inc (/ rate 100. 12))
      (* 12 period))))

(defn compound-interest-data
  [rate loan-amount period]
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
  (if-let [f (:function node)]
    (apply f (:children node))
    node))

(comment
  (postwalk evaluate (compound-interest-data 7.2 5000 2)))
  ; 5771.936460924754

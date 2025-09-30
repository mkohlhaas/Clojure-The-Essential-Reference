(ns g.core
  (:require [clojure.xml          :as xml]
            [clojure.java.javadoc :refer [javadoc]]
            [clojure.java.io      :as io]
            [clojure.string       :refer [split capitalize join]]
            [clojure.pprint       :refer [cl-format]]))

(def balance
  "<balance>
    <accountId>3764882</accountId>
    <lastAccess>20120121</lastAccess>
    <currentBalance>80.12389</currentBalance>
  </balance>")

(defn- to-double [k m]
  (update-in m [k] #(Double/valueOf %)))

(comment
  (javadoc 42.42)
  (Double/valueOf "42.42")                                  ; 42.42
  (to-double :currentBalance {:currentBalance "80.12390"})) ; {:currentBalance 80.1239}

(defn parse [xml]
  (with-open [xml-in (io/input-stream (.getBytes xml))]
    (->> (xml/parse xml-in)
         :content
         (map #(hash-map (:tag %) (first (:content %))))
         (into {})
         (to-double :currentBalance))))

(comment
  (xml/parse (io/input-stream (.getBytes balance)))
; {:tag :balance,
;  :attrs nil,
;  :content
;  [{:tag :accountId, :attrs nil, :content ["3764882"]}
;   {:tag :lastAccess, :attrs nil, :content ["20120121"]}
;   {:tag :currentBalance, :attrs nil, :content ["80.12389"]}]}
  (:content (xml/parse (io/input-stream (.getBytes balance))))
; [{:tag :accountId, :attrs nil, :content ["3764882"]}
;  {:tag :lastAccess, :attrs nil, :content ["20120121"]}
;  {:tag :currentBalance, :attrs nil, :content ["80.12389"]}]
  (parse balance)) ; {:accountId "3764882", :lastAccess "20120121", :currentBalance 80.12389}

(defn separate-words [s]
  (->> (split s #"(?=[A-Z])")
       (map capitalize)
       (join " ")))

(comment
  (separate-words "lastAccess")     ; "Last Access"
  (separate-words "currentBalance") ; "Current Balance"
  (separate-words "accountId"))     ; "Account Id"

(defn format-decimals [v]
  (if (float? v)
    (cl-format nil "~$" v)
    v))

(comment
  (format-decimals 80.12389)    ; "80.12"
  (format-decimals "20120121")) ; "20120121"

(defn print-balance [xml]
  (let [balance (parse xml)
        ks (map (comp separate-words name) (keys balance))
        vs (map format-decimals (vals balance))]
    (zipmap ks vs)))

(comment
  (name :currentBalance)  ; "currentBalance"
  (str  :currentBalance)) ; ":currentBalance"

(print-balance balance)
; {"Account Id" "3764882",
;  "Last Access" "20120121",
;  "Current Balance" "80.12"}

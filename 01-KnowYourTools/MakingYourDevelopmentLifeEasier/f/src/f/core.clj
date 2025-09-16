(ns f.core
  (:require [clojure.xml          :as xml]
            [clojure.java.javadoc :refer [javadoc]]))

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
  (let [xml-in  (java.io.ByteArrayInputStream. (.getBytes xml))
        results (to-double
                 :currentBalance
                 (apply merge
                        (map #(hash-map (:tag %) (first (:content %)))
                             (:content (xml/parse xml-in)))))]
    (.close xml-in)
    results))

(comment
  (:content (xml/parse (java.io.ByteArrayInputStream. (.getBytes balance))))
  ; [{:tag :accountId, :attrs nil, :content ["3764882"]}
  ;  {:tag :lastAccess, :attrs nil, :content ["20120121"]}
  ;  {:tag :currentBalance, :attrs nil, :content ["80.12389"]}]

  (map #(hash-map (:tag %) (first (:content %)))
       (:content (xml/parse (java.io.ByteArrayInputStream. (.getBytes balance)))))
  ; ({:accountId "3764882"}
  ;  {:lastAccess "20120121"}
  ;  {:currentBalance "80.12389"})

  (apply merge (map #(hash-map (:tag %) (first (:content %)))
                    (:content (xml/parse (java.io.ByteArrayInputStream. (.getBytes balance))))))
  ; {:lastAccess "20120121",
  ;  :currentBalance "80.12389",
  ;  :accountId "3764882"}

  (to-double :currentBalance
             (apply merge (map #(hash-map (:tag %) (first (:content %)))
                               (:content (xml/parse (java.io.ByteArrayInputStream. (.getBytes balance))))))))
  ; {:lastAccess "20120121", :currentBalance 80.12389, :accountId "3764882"}

(defn clean-key [k]
  (let [kstr (str k)]
    (if (= \: (first kstr))
      (apply str (rest kstr))
      kstr)))

(comment
  (clean-key :lastAccess)     ; "lastAccess"
  (clean-key :currentBalance) ; "currentBalance"
  (clean-key :accountId))     ; "accountId"

(defn- up-first [[head & others]]
  (apply str (conj others (.toUpperCase (str head)))))

(comment
  (up-first "last Access")) ; "Last Access"

(defn separate-words [k]
  (let [letters (map str k)]
    (up-first
     (reduce #(str %1 (if (= %2 (.toLowerCase %2)) %2 (str " " %2)))
             ""
             letters))))

(comment
  (map str "lastAccess")                                          ; ("l" "a" "s" "t" "A" "c" "c" "e" "s" "s")
  (reduce #(str %1 (if (= %2 (.toLowerCase %2)) %2 (str " " %2))) ; "last Access"
          ""
          (map str "lastAccess"))
  (separate-words "lastAccess")     ; "Last Access"
  (separate-words "currentBalance") ; "Current Balance"
  (separate-words "accountId"))     ; "Account Id"

(defn format-decimals [v]
  (if (float? v)
    (let [[_ nat dec] (re-find #"(\d+)\.(\d+)" (str v))]
      (cond
        (= (count dec) 1) (str v "0")
        (> (count dec) 2) (apply str nat "." (take 2 dec))
        :else (str v)))
    v))

(comment
  (re-find #"(\d+)\.(\d+)" (str 80.12389))  ; ["80.12389" "80" "12389"]
  (re-find #"(\d+)\.(\d+)" (str 80.0))      ; ["80.0" "80" "0"]
  (format-decimals 80.12389)                ; "80.12"
  (format-decimals "20120121"))             ; "20120121"

(defn print-balance [xml]
  (let [balance (parse xml)]
    (letfn [(transform [acc key]
              (assoc acc
                     (separate-words (clean-key key))
                     (format-decimals (key balance))))]
      (reduce transform {} (keys balance)))))

(print-balance balance)
; {"Last Access" "20120121",
;  "Current Balance" "80.12",
;  "Account Id" "3764882"}

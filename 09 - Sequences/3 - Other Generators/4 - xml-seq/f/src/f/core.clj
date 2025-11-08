(ns f.core
  (:require
   [clojure.java.io :as io]
   [clojure.xml     :as xml]))

;; `xml-seq` is designed to traverse the output of `clojure.xml/parse`.

(def balance
  "<balance>
    <accountId>3764882</accountId>
    <currentBalance>80.12389</currentBalance>
    <contract>
      <contractId>77488</contractId>
      <currentBalance>1921.89</currentBalance>
    </contract>
  </balance>")

(def xml
  (-> balance
      .getBytes
      io/input-stream
      xml/parse))
;; {:tag :balance,
;;  :attrs nil,
;;  :content
;;  [{:tag :accountId, :attrs nil, :content ["3764882"]}
;;   {:tag :currentBalance, :attrs nil, :content ["80.12389"]}
;;   {:tag :contract,
;;    :attrs nil,
;;    :content
;;    [{:tag :contractId, :attrs nil, :content ["77488"]}
;;     {:tag :currentBalance, :attrs nil, :content ["1921.89"]}]}]}

(comment
  (xml-seq xml))
; ({:tag :balance,
;   :attrs nil,
;   :content
;   [{:tag :accountId, :attrs nil, :content ["3764882"]}
;    {:tag :currentBalance, :attrs nil, :content ["80.12389"]}
;    {:tag :contract,
;     :attrs nil,
;     :content
;     [{:tag :contractId, :attrs nil, :content ["77488"]}
;      {:tag :currentBalance, :attrs nil, :content ["1921.89"]}]}]}
;  {:tag :currentBalance, :attrs nil, :content ["1921.89"]} "1921.89")

(filter (comp string? first :content) (xml-seq xml))
; ({:tag :accountId, :attrs nil, :content ["3764882"]}
;  {:tag :currentBalance, :attrs nil, :content ["80.12389"]}
;  {:tag :contractId, :attrs nil, :content ["77488"]}
;  {:tag :currentBalance, :attrs nil, :content ["1921.89"]})

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def feeds
  [[:guardian  "https://git.io/guardian-world-rss-xml"]
   [:wash-post "https://git.io/washpost-rss-xml"]
   [:nytimes   "https://git.io/nyt-world-rss-xml"]
   [:wsj       "https://git.io/wsj-rss-xml"]
   [:reuters   "https://git.io/reuters-rss-xml"]])

(defn search-news [query [feed url]]
  (let [content (comp first :content)]
    [feed
     (sequence
      (comp
       (filter (comp string? content)) ; :content ["Rabbis to Return to German Military Amid Growing Anti-Semitism"]
       (filter (comp #{:title} :tag))  ; {:tag :title,} :attrs nil, :content ["Rabbis to Return to German Military Amid Growing Anti-Semitism"]
       (filter #(re-find query (content %)))
       (map content))
      (xml-seq (xml/parse url)))]))

(comment
  (xml/parse "https://git.io/nyt-world-rss-xml"))

(pmap (partial search-news #"(?i)climate") feeds)
; ([:guardian ()]
;  [:wash-post ()]
;  [:nytimes ("Economic Giants Are Restarting. Here’s What It Means for Climate Change.")]
;  [:wsj ()]
;  [:reuters ()])

(pmap (partial search-news #"German") feeds)
; ([:guardian ("Coronavirus live news: cases in Africa pass 150,000; Germany lifts travel warning for Europe"
;              "Germany's Covid-19 spikes present fresh challenges as lockdown lifts")]
;  [:wash-post ()]
;  [:nytimes ("Rabbis to Return to German Military Amid Growing Anti-Semitism")]
;  [:wsj ()]
;  [:reuters ("German minister hopes 'legitimate' U.S. protests will have an impact")])

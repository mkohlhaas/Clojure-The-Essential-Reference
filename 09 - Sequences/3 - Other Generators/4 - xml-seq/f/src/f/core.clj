(ns f.core
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml]))

(def balance
  "<balance>
    <accountId>3764882</accountId>
    <currentBalance>80.12389</currentBalance>
    <contract>
      <contractId>77488</contractId>
      <currentBalance>1921.89</currentBalance>
    </contract>
  </balance>")

(def xml (-> balance .getBytes io/input-stream xml/parse))

(filter (comp string? first :content) (xml-seq xml))
; ({:tag :accountId, :attrs nil, :content ["3764882"]}
;  {:tag :currentBalance, :attrs nil, :content ["80.12389"]}
;  {:tag :contractId, :attrs nil, :content ["77488"]}
;  {:tag :currentBalance, :attrs nil, :content ["1921.89"]})

(def feeds
  [[:guardian  "https://git.io/guardian-world-rss-xml"]
   [:wash-post "https://git.io/washpost-rss-xml"]
   [:nytimes   "https://git.io/nyt-world-rss-xml"]
   [:wsj       "https://git.io/wsj-rss-xml"]
   [:reuters   "https://git.io/reuters-rss-xml"]])

(defn search-news [q [feed url]]
  (let [content (comp first :content)]
    [feed
     (sequence
      (comp
       (filter (comp string? content))
       (filter (comp #{:title} :tag))
       (filter #(re-find q (content %)))
       (map content))
      (xml-seq (xml/parse url)))]))

(pmap (partial search-news #"(?i)climate") feeds)
; ([:guardian ()]
;  [:wash-post ()]
;  [:nytimes
;   ("Economic Giants Are Restarting. Here’s What It Means for Climate Change.")]
;  [:wsj ()]
;  [:reuters ()])

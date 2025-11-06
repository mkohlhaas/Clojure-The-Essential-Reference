(ns f.core
  (:require
   [clojure.java.io :refer [reader]])
  (:import
   [java.net URL]
   [java.util.zip ZipInputStream]))

(with-open [r (reader "https://tinyurl.com/pi-digits")]
  (count (line-seq r)))
; 29301

(def alexa "https://s3-us-west-1.amazonaws.com/umbrella-static/top-1m.csv.zip")

(defn zip-reader [url]
  (-> (URL. url)
      .openConnection
      .getInputStream
      ZipInputStream.
      (doto .getNextEntry)
      reader))

(defn domain [^String line]
  (some-> line (.split "\\.") last))

(defn first-of-domain [ext]
  (with-open [r (zip-reader alexa)]
    (some #(when (= ext (domain %)) %) (line-seq r))))

;; zip file is empty
(first-of-domain "me") ; "861,loopme.me"

(defn top-10-domains-by-traffic [archive]
  (with-open [r (zip-reader archive)]
    (->> (line-seq r)
         (map domain)
         frequencies
         (sort-by last >)
         (take 10))))

(comment
  (top-10-domains-by-traffic alexa))
  ; (["com" 606236]
  ;  ["net" 140417]
  ;  ["org" 30617]
  ;  ["io" 23713]
  ;  ["uk" 16001]
  ;  ["de" 10199]
  ;  ["cn" 9509]
  ;  ["ru" 7822]
  ;  ["edu" 7673]
  ;  ["gov" 6575])

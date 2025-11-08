(ns f.core
  (:require
   [clojure.java.io :refer [reader]])
  (:import
   [java.net URL]
   [java.util.zip ZipInputStream]))

(let [book-of-pi "https://tinyurl.com/pi-digits"]
  (with-open [r (reader book-of-pi)]
    (count (line-seq r))))
; 29301

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn zip-reader [url]
  (-> (URL. url)
      .openConnection
      .getInputStream
      ZipInputStream.
      (doto .getNextEntry)
      reader))

(defn domain [^String line]
  (some-> line
          (.split "\\.")
          last))

(def alexa "https://s3-us-west-1.amazonaws.com/umbrella-static/top-1m.csv.zip")

(defn first-of-domain [ext]
  (with-open [r (zip-reader alexa)]
    (some #(when (= ext (domain %)) %) (line-seq r))))

(comment
  (line-seq (zip-reader alexa))
  ; ("1,google.com"
  ;  "2,www.google.com"
  ;  "3,microsoft.com"
  ;  "4,data.microsoft.com"
  ;  "5,events.data.microsoft.com"
  ;  …)
  (domain (first (line-seq (zip-reader alexa))))) ; "com"

(comment
  (first-of-domain "com") ; "1,google.com"    (top com-domain)
  (first-of-domain "edu") ; "518,cornell.edu" (top edu-domain)
  (first-of-domain "net") ; "18,office.net"   (top net-domain)
  (first-of-domain "org") ; "112,lencr.org"   (top org-domain)
  (first-of-domain "me")) ; "861,loopme.me"   (top me-domain)

(defn top-10-domains-by-traffic [archive]
  (with-open [r (zip-reader archive)]
    (->> (line-seq r)
         (map domain)
         frequencies
         (sort-by last >)
         (take 10))))

(defn domains-by-traffic-2900 [archive]
  (with-open [r (zip-reader archive)]
    (->> (line-seq r)
         (map domain)
         frequencies
         (sort-by last >)
         (filter #(> (second %) 2900)))))

(comment
  (domains-by-traffic-2900 alexa)
  ; (["com"   607279]
  ;  ["net"   141206]
  ;  ["org"   30175]
  ;  ["io"    23779]
  ;  ["uk"    15622]
  ;  ["de"    10491]
  ;  ["cn"    9719]
  ;  ["ru"    7659]
  ;  ["edu"   7246]
  ;  ["gov"   6328]
  ;  ["us"    6270]
  ;  ["co"    6144]
  ;  ["cloud" 5272]
  ;  ["app"   4408]
  ;  ["nl"    4119]
  ;  ["ai"    4097]
  ;  ["tv"    3642]
  ;  ["me"    2992]
  ;  ["eu"    2954]
  ;  ["xyz"   2928])

  (top-10-domains-by-traffic alexa))
  ; (["com" 607279]
  ;  ["net" 141206]
  ;  ["org" 30175]
  ;  ["io"  23779]
  ;  ["uk"  15622]
  ;  ["de"  10491]
  ;  ["cn"  9719]
  ;  ["ru"  7659]
  ;  ["edu" 7246]
  ;  ["gov" 6328])

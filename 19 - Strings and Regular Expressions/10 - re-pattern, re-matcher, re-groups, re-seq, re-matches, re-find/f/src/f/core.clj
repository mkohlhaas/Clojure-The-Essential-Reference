(ns f.core
  (:require
   [clojure.string :as s]
   [criterium.core :refer [quick-bench]]))

;; functions in the standard library with "-seq" in their names?
(filter
 #(re-find #"-seq" (str (key %)))
 (ns-publics 'clojure.core))
; ([tree-seq        #'clojure.core/tree-seq]
;  [line-seq        #'clojure.core/line-seq]
;  [iterator-seq    #'clojure.core/iterator-seq]
;  [enumeration-seq #'clojure.core/enumeration-seq]
;  [resultset-seq   #'clojure.core/resultset-seq]
;  [re-seq          #'clojure.core/re-seq]
;  [lazy-seq        #'clojure.core/lazy-seq]
;  [stream-seq!     #'clojure.core/stream-seq!]
;  [file-seq        #'clojure.core/file-seq]
;  [chunked-seq?    #'clojure.core/chunked-seq?]
;  [xml-seq         #'clojure.core/xml-seq])

(comment
  (count (ns-publics 'clojure.core)) ; 679

  (take 5 (ns-publics 'clojure.core)))
  ; ([primitives-classnames #'clojure.core/primitives-classnames]
  ;  [+'                    #'clojure.core/+']
  ;  [decimal?              #'clojure.core/decimal?]
  ;  [restart-agent         #'clojure.core/restart-agent]
  ;  [sort-by               #'clojure.core/sort-by])

(def so-contacts (slurp "https://stackoverflow.com/company/contact"))

(set (map last (re-seq #"mailto:(\S+@\S+\.com)" so-contacts)))
; #{"legal@stackoverflow.com"}

(def contacts "Contact us: support@manning.com or 203-626-1510")

(comment
  (let [s contacts]                                        ; (out) Execution time mean :  47.240686 ns
    (quick-bench (s/index-of s "support@manning.com")))

  ;; regex engine is slowest
  (let [s contacts                                         ; (out) Execution time mean : 248.762844 ns
        re #"support@manning.com"]
    (quick-bench (re-find re s)))

  (let [s contacts]                                        ; (out) Execution time mean :  46.610805 ns
    (quick-bench (s/includes? s "support@manning.com"))))

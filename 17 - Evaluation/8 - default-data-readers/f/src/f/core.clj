(ns f.core
  (:require
   [clojure.edn :as edn])
  (:import
   [java.io  File]
   [java.net URL]))

default-data-readers
; {uuid #'clojure.uuid/default-uuid-reader,
;  inst #'clojure.instant/read-instant-date}

(def date (edn/read-string "#inst \"2017-08-23T10:22:22.000-00:00\""))

;; round-tripping
(= date (edn/read-string (pr-str date))) ; true

;; extend `print-method` to enable `pr-str` to print URL objects with a tag that the reader can understand
(defmethod print-method URL [url writer]
  (doto writer
    (.write "#url ")
    (.write "\"")
    (.write (.toString url))
    (.write "\"")))

;; this shows how the URL object will look like once it is transformed into a string
(-> "/etc/hosts" File. .toURL pr-str) ; "#url \"file:/etc/hosts\""

;; going full circle
(binding [*data-readers* {'url #(URL. %)}]
  (-> "/etc/hosts" File. .toURL pr-str read-string))
; #url "file:/etc/hosts"

(ns f.core)

(comment
  (:c {:a 1 :b 2})) ; nil

(comment
  (-> {:a 1 :b 2} :c inc))
  ; (err) java.lang.NullPointerException

;; threads result to the next form unless it is nil
(some-> {:a 1 :b 2} :c inc)
; nil

(comment
  (System/getenv "PORT")) ; nil

(defn system-port []
  (or (some-> (System/getenv "PORT") Integer.)
      4444))

(system-port)
; 4444

(defn titles [doc]
  (some->> doc
           (re-seq #"<title>(.+?)</title>")
           (map peek))) ; peek like last for vector

(titles nil)
; nil

(titles "<html><head>Document without a title</head></html>")
; nil

(titles "<html><head>
            <title>Once upon a time</title>
            <title>Kingston upon Thames</title>
        </head></html>")
; ("Once upon a time" "Kingston upon Thames")

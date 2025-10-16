(import '[javax.xml.parsers SAXParserFactory])

(defn non-validating [s ch] ; <1>
  (..
    (doto
      (SAXParserFactory/newInstance) ; <2>
      (.setFeature "http://apache.org/xml/features/nonvalidating/load-external-dtd" false))
    (newSAXParser)
    (parse s ch)))

(-> conforming .getBytes io/input-stream (xml/parse non-validating)) ; <3>
;; {:tag :html,
;;  :attrs {:xmlns "http://www.w3.org/1999/xhtml"},
;;  :content [{:tag :article, :attrs nil, :content ["Hello"]}]}
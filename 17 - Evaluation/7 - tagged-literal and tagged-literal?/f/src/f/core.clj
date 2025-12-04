(ns f.core
  (:require
   [clojure.edn :as edn]))

(tagged-literal 'point [1 2]) ; #point [1 2]

(:tag  (tagged-literal 'point [1 2])) ; point
(:form (tagged-literal 'point [1 2])) ; [1 2]

(edn/read-string
 {:default tagged-literal}
 "[\"There is no tag for \" #point [1 2] \"or\" #line [[1 2] [3 4]]]")
; ["There is no tag for " #point [1 2] "or" #line [[1 2] [3 4]]]

(binding  [*default-data-reader-fn* tagged-literal]
  (read-string "[\"There is no tag for \" #point [1 2] \"or\" #line [[1 2] [3 4]]]"))
; ["There is no tag for " #point [1 2] "or" #line [[1 2] [3 4]]]

(tagged-literal? (tagged-literal 'tag :form)) ; true

(ns f.core
  (:require
   [clojure.inspector :refer [inspect-table inspect-tree]]))

(comment
  (inspect-tree {:a 1 :b 2 :c [1 2 3 {:d 4 :e 5 :f [6 7 8]}]}))

(def events [{:time "2017-05-04T13:08:57Z" :msg "msg1"}
             {:time "2017-05-04T13:09:52Z" :msg "msg2"}
             {:time "2017-05-04T13:11:03Z" :msg "msg3"}
             {:time "2017-05-04T23:13:10Z" :msg "msg4"}
             {:time "2017-05-04T23:13:23Z" :msg "msg5"}])

(comment
  (inspect-table events))

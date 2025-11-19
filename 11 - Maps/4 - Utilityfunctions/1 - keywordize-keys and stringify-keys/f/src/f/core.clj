(ns f.core
  (:require
   [clojure.walk :refer [keywordize-keys stringify-keys]]))

(keywordize-keys {"a" 1 "b" 2}) ; {:a  1, :b  2}
(stringify-keys  {:a  1 :b  2}) ; {"a" 1, "b" 2}

(keywordize-keys (stringify-keys  {:a  1 :b  2})) ; {:a 1, :b 2}
(stringify-keys  (keywordize-keys {"a" 1 "b" 2})) ; {"a" 1, "b" 2}

;; non-strings, non-keywords are not transformed
(keywordize-keys {1 "a" 2 "b"}) ; {1 "a", 2 "b"}
(stringify-keys  {1 "a" 2 "b"}) ; {1 "a", 2 "b"}

;; nested data structure (maps in maps)
(def products
  [{"type"     "Fixed"
    "bookings" [{"upto" 999, "flat" 249.0}]
    "enabled"  false}
   {"type"     "Variable"
    "bookings" [{"upto" 200, "flat" 20.0}]
    "enabled"  true}])

(keywordize-keys products)
; [{:type     "Fixed",    
;   :bookings [{:upto 999, :flat 249.0}], 
;   :enabled  false
;  {:type     "Variable", 
;   :bookings [{:upto 200, :flat 20.0}],  
;   :enabled  true}]

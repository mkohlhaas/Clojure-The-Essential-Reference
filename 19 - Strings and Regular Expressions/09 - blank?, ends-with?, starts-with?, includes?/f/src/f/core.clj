(ns f.core
  (:require
   [clojure.string :as s]))

(s/blank? " \t \n \f \r ")                          ; true
(s/blank? "\u000B \u001C \u001D \u001E \u001F")     ; true

(s/starts-with? "Bonjure Clojure" "Bon")            ; true
(s/starts-with? "Bonjure Clojure" "Clo")            ; false
(s/starts-with? "" "")                              ; true
(s/starts-with? "Anything starts with nothing." "") ; true

(s/ends-with? "Bonjure Clojure" "ure")              ; true
(s/ends-with? "Bonjure Clojure" "Bon")              ; false
(s/ends-with? "" "")                                ; true
(s/ends-with? "Anything ends with nothing." "")     ; true

(s/includes? "Bonjure Clojure" "e C")               ; true

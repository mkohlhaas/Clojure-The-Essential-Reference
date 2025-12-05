(ns f.core
  (:require
   [clojure.string :as s])
  (:import
   [java.lang StringBuffer]))

(s/index-of      "Bonjure Clojure" \j)       ; 3
(s/index-of      "Bonjure Clojure" "ju")     ; 3

(s/last-index-of "Bonjure Clojure" "ju")     ; 11
(s/index-of      "Bonjure Clojure" \j 4)     ; 11

(s/last-index-of "Bonjure Clojure" "ju" 10)  ; 3

;; not found
(s/index-of      "Bonjure Clojure" "z")      ; nil
(s/index-of      "Bonjure Clojure" "j" 20)   ; nil
(s/last-index-of "Bonjure Clojure" "z")      ; nil
(s/last-index-of "Bonjure Clojure" "j" -1)   ; nil

(s/index-of
 (doto (StringBuffer.)
   (.append "Bonjure")
   (.append \space)
   (.append "Clojure"))
 \j)
; 3

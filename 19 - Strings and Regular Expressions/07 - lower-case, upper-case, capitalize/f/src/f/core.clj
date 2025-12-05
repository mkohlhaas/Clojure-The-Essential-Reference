(ns f.core
  (:require
   [clojure.repl :refer [source]]
   [clojure.string :as s]))

(-> some?
    source
    with-out-str
    s/upper-case
    print)
; (out) (DEFN SOME?
; (out)   "RETURNS TRUE IF X IS NOT NIL, FALSE OTHERWISE."
; (out)   {:TAG BOOLEAN
; (out)    :ADDED "1.6"
; (out)    :STATIC TRUE}
; (out)   [X] (NOT (NIL? X)))

(comment
  (-> some?
      source))
  ; (out) (defn some?
  ; (out)   "Returns true if x is not nil, false otherwise."
  ; (out)   {:tag Boolean
  ; (out)    :added "1.6"
  ; (out)    :static true}
  ; (out)   [x] (not (nil? x)))

(def primary-colors #{"red" "green" "blue"})

(comment
  (def book (slurp "https://tinyurl.com/wandpeace"))

  (->> (s/split book #"\s+")
       (filter primary-colors)
       frequencies)
  ; {"red" 87, "blue" 64, "green" 38}

  (->> (s/split book #"\s+")
       (map s/lower-case)
       (filter primary-colors)
       frequencies))
  ; {"red" 89, "blue" 64, "green" 38}

(def names
  ["john abercrombie"
   "Brad mehldau"
   "Cassandra Wilson"
   "andrew cormack"])

(map
 (fn [name]
   (->> (s/split name #"\b") ; split on word boundaries
        (map s/capitalize)
        s/join))
 names)
; ("John Abercrombie" "Brad Mehldau" "Cassandra Wilson" "Andrew Cormack")

;; Note that `upper-case`, `lower-case` and `capitalize` can be used on any printable object (virtually all Clojure and Java types)
(map s/upper-case ['symbols :keywords 1e10 (Object.)]) ; ("SYMBOLS" ":KEYWORDS" "1.0E10" "JAVA.LANG.OBJECT@23A2405B")

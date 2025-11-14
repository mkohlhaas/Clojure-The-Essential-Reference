(ns f.core
  (:require
   [clojure.java.io :as io]
   [clojure.string  :as s]
   [criterium.core :refer [quick-bench]]))

(zipmap [:a :b :c] [1 2 3])  ; {:a 1, :b 2, :c 3}

(type (zipmap (range 10) (range 10)))  ; clojure.lang.PersistentHashMap

(zipmap (range 10) (range 10))  ; {0 0, 7 7, 1 1, 4 4, 6 6, 3 3, 2 2, 9 9, 5 5, 8 8}

(let [m {:a 1 :b 2 :c 3 :d 4 :e 5}]
  (= m (zipmap (keys m) (vals m))))
; true

(zipmap ["red" "blue" "green"] (repeat 1)) ; {"red" 1, "blue" 1, "green" 1}

(def file-content
  "TITLE,FIRST,LAST,NUMBER,STREET,CITY,POST,JOINED
Mrs,Mary,Black,20,Hillbank St,Kelso,TD5 7JW,01/05/2012 12:51
Miss,Chris,Bowie,44,Hall Rd,Sheffield,S5 7PW,01/05/2012 17:02
Mr,John,Burton,41,Warren Rd,Yarmouth,NR31 9AB,01/05/2012 17:08")

(defn split-line [line]
  (s/split line #","))

(comment
  (defn transform [data]
    (let [lines   (line-seq data)
          headers (s/split (first lines))]
      (eduction
       (map s/split)
       (map (partial zipmap headers))
       (rest lines))))

  (with-open [data (io/reader (char-array file-content))]
    (doall (transform data))))

;; ({"TITLE" "Mrs", "FIRST" "Mary", "LAST" "Black", "NUMBER" "20",
;;   "STREET" "Hillbank St", "CITY" "Kelso", "POST" "TD5 7JW",
;;   "JOINED" "01/05/2012 12:51"}
;;  {"TITLE" "Miss", "FIRST" "Chris", "LAST" "Bowie", "NUMBER" "44",
;;   "STREET" "Hall Rd", "CITY" "Sheffield", "POST" "S5 7PW",
;;   "JOINED" "01/05/2012 17:02"}
;;  {"TITLE" "Mr", "FIRST" "John", "LAST" "Burton", "NUMBER" "41",
;;   "STREET" "Warren Rd", "CITY" "Yarmouth", "POST" "NR31 9AB",
;;   "JOINED" "01/05/2012 17:08"})

(defn zipmap* [keys vals] ; <1>
  (loop [m (transient {})
         ks (seq keys)
         vs (seq vals)]
    (if (and ks vs)
      (recur (assoc! m (first ks) (first vs))
             (next ks)
             (next vs))
      (persistent! m))))

(comment
  (let [s1 (range 1000) s2 (range 1000)] (quick-bench (zipmap s1 s2)))
;; Execution time mean : 150.549758 µs

  (let [s1 (range 1000) s2 (range 1000)] (quick-bench (zipmap* s1 s2))))
;; Execution time mean : 128.869009 µs

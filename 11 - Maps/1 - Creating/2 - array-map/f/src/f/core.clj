(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(def m (array-map :a 1 :b 2))

(m :a) ; 1

(type {0 1 2 3 4 5 6 7 8 9})                               ; clojure.lang.PersistentArrayMap
(type {0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19}) ; clojure.lang.PersistentHashMap

(def an-array-map  (apply  array-map (range 200)))
(def an-array-map? (assoc  an-array-map :a :b))
(def an-array-map! (dissoc an-array-map 0))

(type an-array-map)  ; clojure.lang.PersistentArrayMap
(type an-array-map?) ; clojure.lang.PersistentHashMap
(type an-array-map!) ; clojure.lang.PersistentArrayMap

(def query-results
  [{:date "01/05/2012 12:51" :surname "Black"
    :name "Mary" :title "Mrs" :n "20"
    :address "Hillbank St" :town "Kelso" :postcode "TD5 7JW"}
   {:date "01/05/2012 17:02" :surname "Bowie"
    :name "Chris" :title "Miss" :n "44"
    :address "Hall Rd" :town "Sheffield" :postcode "S5 7PW"}
   {:date "01/05/2012 17:08" :surname "Burton"
    :name "John" :title "Mr" :n "41"
    :address "Warren Rd" :town "Yarmouth" :postcode "NR31 9AB"}])

(defn checkfn [predicate]
  (fn [val]
    (let [_res (predicate val)]
      (if (predicate val)
        val
        (throw (RuntimeException. (str "Error: '" val "' is not valid")))))))

(def customers-format
  (array-map
   'TITLE  [:title (checkfn #{"Mrs" "Miss" "Mr"})]
   'FIRST  [:name (checkfn (comp some? seq))]
   'LAST   [:surname (checkfn (comp some? seq))]
   'NUMBER [:n (checkfn #(re-find #"^\d+$" %))]
   'STREET [:address (checkfn (comp some? seq))]
   'CITY   [:town (checkfn (comp some? seq))]
   'POST   [:postcode (checkfn #(re-find #"^\w{2,4} \w{2,4}$" %))]
   'JOINED [:date (checkfn #(re-find #"^\d{2}/\d{2}/\d{4} \d{2}:\d{2}$" %))]))

(defn csv-str [coll]
  (str (apply str (interpose "," coll)) "\n"))

(defn format-row [format]
  (fn [row]
    (let [specs (map second format)
          data  (map (fn [[column checkfn]]
                       (checkfn (row column))) specs)]
      (csv-str data))))

(defn format-data [data format]
  (let [headers (csv-str (keys format))
        body    (map (format-row format) data)]
    (apply str headers (seq body))))

(comment
  (println (format-data query-results customers-format)))
  ; (out) TITLE,FIRST,LAST,NUMBER,STREET,CITY,POST,JOINED
  ; (out) Mrs,Mary,Black,20,Hillbank St,Kelso,TD5 7JW,01/05/2012 12:51
  ; (out) Miss,Chris,Bowie,44,Hall Rd,Sheffield,S5 7PW,01/05/2012 17:02
  ; (out) Mr,John,Burton,41,Warren Rd,Yarmouth,NR31 9AB,01/05/2012 17:08
  ; (out) 

#_{:clj-kondo/ignore [:redefined-var]}
(def customers-format
  (array-map
   (with-meta 'TITLE  {:db :title}) (checkfn #{"Mrs" "Miss" "Mr"})
   (with-meta 'FIRST  {:db :name}) (checkfn (comp some? seq))
   (with-meta 'LAST   {:db :surname}) (checkfn (comp some? seq))
   (with-meta 'NUMBER {:db :n}) (checkfn #(re-find #"^\d+$" %))
   (with-meta 'STREET {:db :address}) (checkfn (comp some? seq))
   (with-meta 'CITY   {:db :town}) (checkfn (comp some? seq))))

(map (comp :db meta) (keys customers-format))
; (:title :name :surname :n :address :town)

(comment
  (let [r1 (doall (concat (range 1000) (repeat 1000 999)))
        r2 (doall (range 2000))]
    [(quick-bench (apply array-map r1))    ; (out) Execution time mean : 48.036494 ms
     (quick-bench (apply array-map r2))])) ; (out) Execution time mean : 19.855879 ms

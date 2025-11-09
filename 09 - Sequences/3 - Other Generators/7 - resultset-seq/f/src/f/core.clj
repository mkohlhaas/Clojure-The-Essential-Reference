(ns f.core
  (:import
   [java.sql ResultSet ResultSetMetaData]
   [java.sql DriverManager ResultSet]))

;; `resultset-seq` generates a sequence from a java.sql.ResultSet object.

;; If you are dealing with large results and want to process them lazily, you need to be sure the driver supports streaming capabilities.

;; a reified ResultSet object simulates the interaction with a database driver for demonstration purposes
(defn db-driver [attrs]
  (reify
    ResultSet
    (getMetaData [_]
      (reify
        ResultSetMetaData
        (getColumnCount [_]     (count attrs))
        (getColumnLabel [_ idx] (nth attrs (dec idx)))))
    (next [_] true)
    (close [_])
    (^Object getObject [_ ^int _idx] (rand-int 1000))))

(take 10 (resultset-seq (db-driver ["id" "count"])))
; ({:id 179, :count 786}
;  {:id 283, :count 404}
;  {:id 286, :count 180}
;  {:id 984, :count 74}
;  {:id 197, :count 327}
;  {:id 364, :count 459}
;  {:id 917, :count 523}
;  {:id 904, :count 396}
;  {:id 390, :count 553}
;  {:id 335, :count 687})

(comment
  (type (resultset-seq (db-driver ["id" "count"])))) ; clojure.lang.Cons

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn create-sample-data [stmt]
  (.executeUpdate stmt "drop table if exists person")
  (.executeUpdate stmt "create table person (id integer, name string)")
  (.executeUpdate stmt "insert into person values(1, 'leo')")
  (.executeUpdate stmt "insert into person values(2, 'yui')"))

(with-open [conn (DriverManager/getConnection "jdbc:sqlite::memory:")
            stmt (.createStatement conn)]
  (create-sample-data stmt)
  (->> (.executeQuery stmt "SELECT * FROM person")
       resultset-seq
       doall))
; ({:id 1, :name "leo"} {:id 2, :name "yui"})

(ns f.core
  (:import
   [java.sql ResultSet ResultSetMetaData]
   [java.sql DriverManager ResultSet]))

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

(take 3 (resultset-seq (db-driver ["id" "count"])))
; ({:id 617, :count 471} {:id 252, :count 482} {:id 153, :count 275})

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

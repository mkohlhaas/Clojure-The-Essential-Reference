(ns f.core
  (:import
   [java.time Duration Instant]
   [java.time.format DateTimeFormatter]))

(partition     3 (range 10)) ; ((0 1 2) (3 4 5) (6 7 8))
(partition-all 3 (range 10)) ; ((0 1 2) (3 4 5) (6 7 8) (9))

(partition-by count (map str [12 11 8 2 100 102 105 1 3]))
; (("12" "11") ("8" "2") ("100" "102" "105") ("1" "3"))

;; infinite recursion
(partition 3 0 (range 10)) ; ((0 1 2) (0 1 2) (0 1 2) (0 1 2) (0 1 2) …)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(partition 3 3            (range 10)) ; ((0 1 2) (3 4 5) (6 7 8))
(partition 3 2            (range 10)) ; ((0 1 2) (2 3 4) (4 5 6) (6 7 8))
(partition 3 3 [:a :b :c] (range 10)) ; ((0 1 2) (3 4 5) (6 7 8) (9 :a :b))

(partition-all 3   (range 10)) ; ((0 1 2) (3 4 5) (6 7 8) (9))
(partition-all 3 2 (range 10)) ; ((0 1 2) (2 3 4) (4 5 6) (6 7 8) (8 9))

;; Batches of Requests

(def records (map #(-> {:id % :data (str %)}) (range 1000)))
; ({:id 0, :data "0"}
;  {:id 1, :data "1"}
;  {:id 2, :data "2"}
;  {:id 3, :data "3"}
;  {:id 4, :data "4"}
;   …
; )

;; shows the first 70 chars of the query
(defn log [query]
  (str (.substring query 0 70) "…\n"))

(comment
  (log "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMoreThan70Chars…"))
  ; "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA…\n"

(defn insert-query [records]
  (let [->value (fn [{:keys [id data]}] (format "(%s,%s)" id data))
        rows    (apply str (interpose "," (map ->value records)))]
    (log (str "INSERT INTO records (id, data) VALUES " rows " ON DUPLICATE KEY UPDATE"))))

(comment
  ;; creating batches of requests/work
  (partition-all 10 records)
  ;; (({:id 0, :data "0"}
  ;;   {:id 1, :data "1"}
  ;;   …
  ;;   {:id 8, :data "8"}
  ;;   {:id 9, :data "9"})
  ;;  ({:id 10, :data "10"}
  ;;   {:id 11, :data "11"}
  ;;   …
  ;;   {:id 18, :data "18"}
  ;;   {:id 19, :data "19"})
  ;;  ({:id 20, :data "20"}
  ;;   {:id 21, :data "21"}
  ;;   …
  ;;   {:id 28, :data "28"}
  ;;   {:id 29, :data "29"})
  ;;  ({:id 30, :data "30"}
  ;;   {:id 31, :data "31"}
  ;;   …
  ;;   {:id 38, :data "38"}
  ;;   {:id 39, :data "39"})
  ;;  ({:id 40, :data "40"}
  ;;   {:id 41, :data "41"}
  ;;   …
  ;;   {:id 48, :data "48"}
  ;;   {:id 49, :data "49"})
  ;;  ({:id 50, :data "50"}
  ;;   {:id 51, :data "51"}
  ;;   …
  ;;   {:id 58, :data "58"}
  ;;   {:id 59, :data "59"})
  ;;  ({:id 60, :data "60"}
  ;;   {:id 61, :data "61"}
  ;;   …
  ;;   {:id 68, :data "68"}
  ;;   {:id 69, :data "69"})
  ;;  ({:id 70, :data "70"}
  ;;   {:id 71, :data "71"}
  ;;   …
  ;;   {:id 78, :data "78"}
  ;;   {:id 79, :data "79"})
  ;;  ({:id 80, :data "80"}
  ;;   {:id 81, :data "81"}
  ;;   …
  ;;   {:id 88, :data "88"}
  ;;   {:id 89, :data "89"})
  ;;  ({:id 90, :data "90"}
  ;;   {:id 91, :data "91"}
  ;;   …
  ;;   {:id 98, :data "98"}
  ;;   {:id 99, :data "99"})
  ;;  ({:id 100, :data "100"}
  ;;   {:id 101, :data "101"}
  ;;   …
  ;;   {:id 108, :data "108"}
  ;;   {:id 109, :data "109"})
  ;;  …
  ;;  ({:id 970, :data "970"}
  ;;   {:id 971, :data "971"}
  ;;   …
  ;;   {:id 978, :data "978"}
  ;;   {:id 979, :data "979"})
  ;;  ({:id 980, :data "980"}
  ;;   {:id 981, :data "981"}
  ;;   …
  ;;   {:id 988, :data "988"}
  ;;   {:id 989, :data "989"})
  ;;  ({:id 990, :data "990"}
  ;;   {:id 991, :data "991"}
  ;;   …
  ;;   {:id 998, :data "998"}
  ;;   {:id 999, :data "999"}))

  ;; `pmap` on batches created with `partition-all`
  (println (pmap insert-query (partition-all 10 records))))
  ; (out) (INSERT INTO records (id, data) VALUES (0,0),(1,1),(2,2),(3,3),(4,4),(5…
  ; (out)  INSERT INTO records (id, data) VALUES (10,10),(11,11),(12,12),(13,13),…
  ; (out)  INSERT INTO records (id, data) VALUES (20,20),(21,21),(22,22),(23,23),…
  ; (out)  INSERT INTO records (id, data) VALUES (30,30),(31,31),(32,32),(33,33),…
  ; (out)  INSERT INTO records (id, data) VALUES (40,40),(41,41),(42,42),(43,43),…
  ; …
  ; (out) )

;; Temperature Reads                  

(def temps [42 42 42 42 43 43 43 44 44 44 45 45 46 48 45 44 42 42 42 42 41 41])

(map count (partition-by identity temps)) ; (4 3 3 2 1 1 1 1 4 2)

(comment
  (partition-by identity temps)) ; ((42 42 42 42) (43 43 43) (44 44 44) (45 45) (46) (48) (45) (44) (42 42 42 42) (41 41))

;; usage in transducers

;; `eduction` doesn't need `comp`
(eduction
 (map range)
 (partition-all 2)
 (range 6))
; ([() (0)] [(0 1) (0 1 2)] [(0 1 2 3) (0 1 2 3 4)])

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A `partition-with` for other Partition Strategies
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
;; using lazy-cons idioms
(defn my-partition-by [f coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [fst (first s)
           fv  (f fst)
           run (cons fst (take-while #(= fv (f %)) (next s)))]
       (cons run (partition-by f (seq (drop (count run) s))))))))

(defn partition-with [f coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [prev (first s)
           run  (cons prev (take-while #(f prev %) (next s)))]
       (cons run (partition-with f (seq (drop (count run) s))))))))

;; time series
(def events [{:t "2017-05-04T13:08:57Z" :msg "msg1"}
             {:t "2017-05-04T13:09:52Z" :msg "msg2"}
             {:t "2017-05-04T13:11:03Z" :msg "msg3"}
             {:t "2017-05-04T23:13:10Z" :msg "msg4"}
             {:t "2017-05-04T23:13:23Z" :msg "msg5"}])

;; arrow prefix (→) indicates a transformation from one format into another
(defn ->inst [{t :t}]
  (Instant/from (.parse DateTimeFormatter/ISO_INSTANT t)))

(comment
  (->inst {:t "2017-05-04T13:08:57Z" :msg "msg1"})   ; #object[java.time.Instant 0x289b7a9e "2017-05-04T13:08:57Z"]
  (->inst {:t "2017-05-04T13:09:52Z" :msg "msg2"})   ; #object[java.time.Instant 0x25739237 "2017-05-04T13:09:52Z"]

  (Duration/between                                  ; #object[java.time.Duration 0x5c0edff5 "PT55S"]
   (->inst {:t "2017-05-04T13:08:57Z" :msg "msg1"})
   (->inst {:t "2017-05-04T13:09:52Z" :msg "msg2"})))

(defn burst? [t1 t2]
  (let [diff (.getSeconds (Duration/between (->inst t2) (->inst t1)))]
    (<= (Math/abs diff) 120)))

(comment
  ;; diff less than two minutes
  (burst? {:t "2017-05-04T13:08:57Z" :msg "msg1"} {:t "2017-05-04T13:09:52Z" :msg "msg2"})) ; true

;; partition events in 2-min-groups
(partition-with burst? events)
; (({:t "2017-05-04T13:08:57Z", :msg "msg1"}
;   {:t "2017-05-04T13:09:52Z", :msg "msg2"})
;  ({:t "2017-05-04T13:11:03Z", :msg "msg3"})
;  ({:t "2017-05-04T23:13:10Z", :msg "msg4"}
;   {:t "2017-05-04T23:13:23Z", :msg "msg5"}))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; Performance Considerations 
;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 

(comment
  ;; partition functions are lazy (just produce what's necessary)
  (first (partition 3 (map #(do (println %) %) (range))))) ; (0 1 2)
  ; (out) 0
  ; (out) 1
  ; (out) 2

(comment
  ;; laziness with transducers works differently and is in general more eager
  ;; realizes 32 partitions of n elements each: (32 + 1) * 100
  (first
   (sequence
    (comp
     (map #(do (print % ",") %))
     (partition-all 100))
    (range))))
  ; (out) 0 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9, …, 3291 ,3292 ,3293 ,3294 ,3295 ,3296 ,3297 ,3298 ,3299 ,
  ; [0 1 2 3 4 5 6 7 8 9 10 11 … 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99]

(partition-by pos? (range 10)) ; ((0) (1 2 3 4 5 6 7 8 9))

;; `partition-by` is strictly lazy creating only the first partition
(first (partition-by pos? (range))) ; (0)

(comment
  ;; transducer version is more eager and tries to create the second infinite partition
  ;; WARNING: hangs
  (first
   (sequence
    (partition-by pos?)
    (range))))

(ns f.core
  (:import [java.util.concurrent ConcurrentLinkedQueue]))

(take 3 (repeatedly rand))                           ; (0.5136385614646539 0.9705453677835639 0.9726867204778408)

(repeatedly 3 #(if (> (rand) 0.5) true false))       ; (true true true)

(zipmap (map keyword (repeatedly gensym)) (range 5)) ; {:G__4114 0, :G__4115 1, :G__4116 2, :G__4117 3, :G__4118 4}

(def q (ConcurrentLinkedQueue. (range 1000)))
(def ^:const parallel 5)

(defn task [job]
  (Thread/sleep (rand-int 300))
  (println "Work done on" job)
  (inc job))

(def workers
  (repeatedly
   #(let [out *out*]
      (future
        (binding [*out* out]
          (when-let [item (.poll q)]
            (task item)))))))

(defn run [workers]
  (println "-> starting" parallel "new workers")
  (let [done? #(> (reduce + (remove nil? %)) 30)
        futures (doall (take parallel workers))
        results (mapv deref futures)]
    (cond
      (done? results) results
      (.isEmpty q) (println "Empty.")
      :else (recur (drop parallel workers)))))

(run workers)
;; -> starting 5 new workers
;; Work done on 0
;; Work done on 1
;; Work done on 2
;; Work done on 3
;; Work done on 4
;; -> starting 5 new workers
;; Work done on 5
;; Work done on 6
;; Work done on 7
;; Work done on 8
;; Work done on 9
;; [6 7 8 9 10]

(comment
  (let [rands (repeatedly 1e7 rand)] (first rands) (last rands))   ; 0.7872392882189023
  (let [rands (repeatedly 1e7 rand)] (last rands)  (first rands))) ; OutOfMemoryError GC overhead limit exceeded

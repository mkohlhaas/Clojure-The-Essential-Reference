(ns f.core
  (:import
   [java.util.concurrent ConcurrentLinkedQueue]))

;; `repeatedly` is useful to create sequences from functions with side effects 
;; With a pure function you would use `repeat`.

(take 3 (repeatedly rand))                     ; (0.5136385614646539  0.9705453677835639  0.9726867204778408)
(repeatedly 3 rand)                            ; (0.06661271331894036 0.45664634448136343 0.6824924987687692)
(repeatedly 3 #(if (> (rand) 0.5) true false)) ; (true false false)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(zipmap (map keyword (repeatedly gensym)) (range 5)) ; {:G__4114 0, :G__4115 1, :G__4116 2, :G__4117 3, :G__4118 4}

;; futures are also side effecting functions

(def q (ConcurrentLinkedQueue. (range 1000))) ; #object[java.util.concurrent.ConcurrentLinkedQueue "[0, 1, 2, 3, 4, …, 993, 994, 995, 996, 997, 998, 999]"]
(def ^:const parallel 5)

(defn task [job]
  (Thread/sleep (int (rand-int 300)))
  (println "Work done on" job)
  (inc job))

(comment
  (task 42))
  ; (out) Work done on 42
  ; 43

(def workers
  (repeatedly
   #(let [out *out*]
      (future
        (binding [*out* out]
          (when-let [item (.poll q)]
            (task item)))))))

(defn run [workers]
  (println "-> starting" parallel "new workers")
  (let [done?   #(> (reduce + (remove nil? %)) 100)
        futures (doall (take parallel workers))
        results (mapv deref futures)]
    (cond
      (done? results) results
      (.isEmpty q)    (println "Empty.")
      :else (recur (drop parallel workers)))))

(comment
  (run workers))
  ; (out) -> starting 5 new workers
  ; (out) Work done on 4
  ; (out) Work done on 3
  ; (out) Work done on 1
  ; (out) Work done on 2
  ; (out) Work done on 0
  ; (out) -> starting 5 new workers
  ; (out) Work done on 6
  ; (out) Work done on 9
  ; (out) Work done on 8
  ; (out) Work done on 5
  ; (out) Work done on 7
  ; (out) -> starting 5 new workers
  ; (out) Work done on 12
  ; (out) Work done on 10
  ; (out) Work done on 13
  ; (out) Work done on 11
  ; (out) Work done on 14
  ; (out) -> starting 5 new workers
  ; (out) Work done on 18
  ; (out) Work done on 19
  ; (out) Work done on 15
  ; (out) Work done on 16
  ; (out) Work done on 17
  ; (out) -> starting 5 new workers
  ; (out) Work done on 22
  ; (out) Work done on 20
  ; (out) Work done on 21
  ; (out) Work done on 24
  ; (out) Work done on 23
  ; [21 22 23 25 24]

(comment
  #_{:clj-kondo/ignore [:unused-value]}
  ;; Garbage collection can set in as we do not hold on to the first item in the sequence.
  (let [rands (repeatedly 1e7 rand)] (first rands) (last rands))   ; 0.7872392882189023

  ;; Asks again for the last item, but we access also the first item at the end of the evaluation of the form.
  ;; The sequence cannot be garbage collected and needs to stay in memory to satisfy this last request.
  #_{:clj-kondo/ignore [:unused-value]}
  (let [rands (repeatedly 1e8 rand)] (last rands)  (first rands))) ; (err) Execution error (OutOfMemoryError)

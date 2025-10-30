(ns f.core
  (:import java.util.concurrent.LinkedBlockingQueue))

(reduce
 (fn [acc el]
   (if (> el 5)
     (reduced acc)
     (+ acc el)))
 (range 10))
; 15

;; supports the IDeref interface
(def r (reduced "string"))
(reduced? r) ; true

r  ; #<Reduced@2013c6cd: "string">
@r ; "string"

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def random-vectors
  (repeatedly #(vec (drop (rand-int 10) (range 10)))))
;; ([3 4 5 6 7 8 9]
;;  [7 8 9]
;;  [5 6 7 8 9]
;;  [9]
;;  [2 3 4 5 6 7 8 9]
;;  …)

(first
 (drop-while
  #(>= 3 (count %))
  random-vectors))
; [3 4 5 6 7 8 9]

(reduce
 #(when (> (count %2) 3) (reduced %2))
 random-vectors)
; [5 6 7 8 9]

;; ;;;;;;;;;;;;;;;
;; Moving Averages
;; ;;;;;;;;;;;;;;;

(def queue (LinkedBlockingQueue. 1))

(defn value-seq []
  (lazy-seq (cons (.take queue) (value-seq))))

;; custom reducing function
(defn moving-average [[cnt sum _avg] x]
  (let [new-cnt (inc cnt)
        new-sum (+ sum (unreduced x))
        new-avg (/ new-sum (double new-cnt))
        res     [new-cnt new-sum new-avg]]
    (println res)
    (if (reduced? x)
      (reduced res)
      res)))

(defn start []
  (let [out *out*]
    (.start (Thread.
             #(binding [*out* out] ; redirecting the standard output of the new thread to the current one
                (println "Done:"
                         (reduce   ; the call to reduce is blocking and only returns when a reduced element is sent down the queue
                          moving-average
                          [0 0 0]
                          (value-seq))))))))

;; start thread and send some values to the queue
(start)
(.offer queue 10)           ; (out) [1 10 10.0]
(.offer queue 10)           ; (out) [2 20 10.0]
(.offer queue 50)           ; (out) [3 70 23.333333333333332]
(.offer queue (reduced 20)) ; (out) [4 90 22.5]
; (out) Done: [4 90 22.5]

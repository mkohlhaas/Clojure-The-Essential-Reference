(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(map  + (range 10) (range 10)) ; (0 2 4 6 8 10 12 14 16 18)
(pmap + (range 10) (range 10)) ; (0 2 4 6 8 10 12 14 16 18)

;; `pcalls` and `pvalues` build on top of `pmap`

;; `pcalls` for side effecting parallel functions with no arguments
(pcalls
 (constantly "Function")
 #(System/currentTimeMillis)
 #(println "side-effect"))
; (side-effect"Function" 1762524570741 nil)

;; expressions that are evaluated in parallel
(pvalues
 (+ 1 1)
 (Math/sqrt 2)
 (str "last" " " "item"))
; (2 1.4142135623730951 "last item")

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(comment
  ;; map
  (let [xs (range 10000)]
    (quick-bench
     (last (map eval xs))))
  ; (out) Execution time mean : 35.442128 ms

  ;; pmap slower(!)
  (let [xs (range 10000)]
    (quick-bench
     (last (pmap eval xs))))
  ; (out) Execution time mean : 70.068959 ms

  ;; pmap with partitioning much faster
  (let [xs (range 10000)]
    (quick-bench
     (last (last
            (pmap
             #(map eval %)
             (partition-all 1000 xs)))))))
  ; (out) Execution time mean : 8.136368 ms

;; ;;;;;;;;;;;;;;;;;;;;
;; Understanding `pmap`
;; ;;;;;;;;;;;;;;;;;;;;

(defn dechunk [xs]
  (lazy-seq
   (when-first [x xs]
     (cons x (dechunk (rest xs))))))

(comment
  (dechunk (range 10))) ; (0 1 2 3 4 5 6 7 8 9)

(defn f [x]
  (Thread/sleep (+ (* 10 x) 500))
  (println (str "done-" x))
  x)

(comment
  (def s (pmap f (dechunk (range 100))))
  ; (out) done-0
  ; (out) done-1

  (first s) ; 0
  ; (out) done-2
  ; (out) done-3
  ; (out) done-4
  ; (out) done-5
  ; (out) done-6

  (take 2 s) ; (0 1)
  ;; done-7

  (def s (pmap f (range 1000)))
  ; (out) done-0
  ; (out) done-1
  ; (out) done-2
  ; (out) done-3
  ; (out) done-4
  ; …
  ; (out) done-23
  ; (out) done-24
  ; (out) done-25
  ; (out) done-26
  ; (out) done-27
  ; (out) done-28
  ; (out) done-29
  ; (out) done-30
  ; (out) done-31

  (first s) ; 0

  (first (drop 26 s)) ; 26
  ; (out) done-32
  ; (out) done-33
  ; (out) done-34
  ; (out) done-35
  ; (out) done-36
  ; (out) done-37
  ; (out) done-38
  ; (out) done-39
  ; (out) done-40
  ; (out) done-41
  ; (out) done-42
  ; …
  ; (out) done-59
  ; (out) done-60
  ; (out) done-61
  ; (out) done-62
  ; (out) done-63

;; by changing the chunk size, we can get any grade of parallelism
  (defn re-chunk [n xs]
    (lazy-seq
     (when-let [s (seq (take n xs))]
       (let [cb (chunk-buffer n)]
         (doseq [x s] (chunk-append cb x))
         (chunk-cons (chunk cb) (re-chunk n (drop n xs)))))))

  ;; 1000 threads
  (def s (pmap f (re-chunk 1000 (range 1000)))))
  ; (out) done-0
  ; (out) done-1
  ; (out) done-2
  ; (out) done-3
  ; (out) done-4
  ; (out) done-5
  ; (out) done-6
  ; (out) done-7
  ; (out) done-8
  ; (out) done-9
  ; (out) done-10
  ; …
  ; (out) done-995
  ; (out) done-996
  ; (out) done-997
  ; (out) done-998
  ; (out) done-999

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; map
  (let [xs (range 100000)] (quick-bench (last (map inc xs))))
  ; (out) Execution time mean : 13.498015 ms

  ;; pmap (30 times slower!)
  (let [xs (range 100000)] (quick-bench (last (pmap inc xs))))
  ; (out) Execution time mean : 389.250834 ms

  ;; map with aggregation
  (let [xs (partition-all 1000 (range 100000))]
    (quick-bench
     (into [] (comp cat (map inc)) xs)))
  ; (out) Execution time mean : 19.781080 ms

  ;; pmap with aggregation (2 times slower!)
  (let [xs (partition-all 1000 (range 100000))]
    (quick-bench
     (into [] cat (pmap #(map inc %) xs))))
  ; (out) Execution time mean : 36.239490 ms

  (def xs (map #(if (zero? (mod % 32)) 1000 1) (range 0 320)))

  (time (dorun (map #(Thread/sleep %) xs)))
  ; (out) "Elapsed time: 10396.569743 msecs"

  ;; no difference
  (time (dorun (pmap #(Thread/sleep %) xs)))
  ; (out) "Elapsed time: 10006.2885 msecs"

  ;; with `sort` (5 times faster)
  ;; uniformity is key for `pmap`
  (time (dorun (pmap #(Thread/sleep %) (sort xs)))))
  ; (out) "Elapsed time: 2058.496811 msecs"

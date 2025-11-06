(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(pmap + (range 10) (range 10))
; (0 2 4 6 8 10 12 14 16 18)

(pcalls
 (constantly "Function")
 #(System/currentTimeMillis)
 #(println "side-effect"))
; (side-effect"
; Function" 1762386488463 nil)

(pvalues
 (+ 1 1)
 (Math/sqrt 2)
 (str "last" " " "item"))
; (2 1.4142135623730951 "last item")

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (let [xs (range 10000)]
    (quick-bench
     (last (map eval xs))))
;; Execution time mean : 23.182619 ms

  (let [xs (range 10000)]
    (quick-bench
     (last (pmap eval xs))))
;; Execution time mean : 19.001539 ms

  (let [xs (range 10000)]
    (quick-bench
     (last (last
            (pmap
             #(map eval %)
             (partition-all 1000 xs)))))))
;; Execution time mean : 3.208768 ms

(defn dechunk [xs]
  (lazy-seq
   (when-first [x xs]
     (cons x
           (dechunk (rest xs))))))

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

  (defn re-chunk [n xs]
    (lazy-seq
     (when-let [s (seq (take n xs))]
       (let [cb (chunk-buffer n)]
         (doseq [x s] (chunk-append cb x))
         (chunk-cons (chunk cb) (re-chunk n (drop n xs)))))))

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
  (let [xs (range 100000)] (quick-bench (last (map inc xs))))
;; Execution time mean : 4.651943 ms

  (let [xs (range 100000)] (quick-bench (last (pmap inc xs))))
;; Execution time mean : 325.748151 ms

  (let [xs (partition-all 1000 (range 100000))]
    (quick-bench
     (into [] (comp cat (map inc)) xs))) ; <1>
;; Execution time mean : 6.553814 ms

  (let [xs (partition-all 1000 (range 100000))]
    (quick-bench
     (into [] cat (pmap #(map inc %) xs)))) ; <2>
;; Execution time mean : 13.539197 ms

  (def xs (map #(if (zero? (mod % 32)) 1000 1) (range 0 320)))

  (time (dorun (map #(Thread/sleep %) xs)))
;; "Elapsed time: 10019.599748 msecs"

  (time (dorun (pmap #(Thread/sleep %) xs)))
;; "Elapsed time: 10024.762327 msecs"

  (time (dorun (pmap #(Thread/sleep %) (sort xs)))))
;; "Elapsed time: 1028.686387 msecs"

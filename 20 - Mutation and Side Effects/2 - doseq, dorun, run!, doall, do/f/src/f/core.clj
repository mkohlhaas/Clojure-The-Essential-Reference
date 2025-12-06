(ns f.core
  (:require
   [clojure.java.io :refer [reader]]))

;; `doseq`, `dorun`, `run!`, `doall` are designed exclusively for side effects.
;; They walk a lazy sequence throwing away the results and returning nil.

(defn unchunked [n]
  (map #(do (print ".") %)
       (subvec (vec (range n)) 0 n))) ; subvec is one of the few collections supported by seq that is not chunked

(comment
  (subvec (vec (range 10)) 0 10) ; [0 1 2 3 4 5 6 7 8 9]
  (unchunked 10))                ; (out) .......... ; (0 1 2 3 4 5 6 7 8 9)

;; similar to `for`
(doseq [x (unchunked 10) ; nil
        :while (< x 5)]
  x)
; (out) ......

(dorun 5 (unchunked 10)) ; nil
; (out) ......

(run! #(do (print "!") %) (unchunked 10)) ; nil
; (out) .!.!.!.!.!.!.!.!.!.!

;; `doall` is similar in behavior to `dorun` but it returns the output forcing any side effects in the process.
;; `doall` is often used to fully realize a lazy sequence.

(defn get-lines [url]
  (with-open [r (reader url)]
    (line-seq r)))

(comment
  (def lines (get-lines "https://tinyurl.com/pi-digits"))

  (count lines))
  ; (err) Error printing return value (IOException)
  ; (err) Stream closed

(defn get-lines1 [url]
  (with-open [r (reader url)]
    (doall (line-seq r)))) ; realize lazy sequence

(comment
  (def lines1 (get-lines1 "https://tinyurl.com/pi-digits"))

  (count lines1)) ; 29301

(defn count-lines [url]
  (with-open [r (reader url)]
    (count (line-seq r)))) ; also realizes the lazy sequence

(comment
  (count-lines "https://tinyurl.com/pi-digits")) ; 29301

;; `do` evaluates all expression arguments and returns the result of the last.
;; The expressions preceding the last are presumably side effects, as their result is completely ignored.

(do
  (println "hello") ; side-effect
  (+ 1 1))          ; return value
; (out) hello
; 2

(if (some even? [1 2 3])
  (do                                     ; typical use of `do` (then-else blocks only accept single expressions)
    (println "found some even number")
    (apply + [1 2 3]))
  (println "there was no even number."))
; (out) found some even number
; 6

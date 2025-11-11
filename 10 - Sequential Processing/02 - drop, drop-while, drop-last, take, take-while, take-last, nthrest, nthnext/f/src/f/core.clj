(ns f.core
  (:require
   [clojure.java.io :as io])
  (:import
   [java.io File]
   [java.util Calendar]))

;; ______________________________________________________________________
;; | drop       | drops the first n elements                            |
;; | drop-while | drops items until the given predicate returns false   |
;; | drop-last  | drops the last n elements (default to 1)              |
;; | take       | keeps the first n elements, drops the rest            |
;; | take-while | keeps element until the given predicate returns false |
;; | take-last  | keeps the last n elements (no default for n)          |
;; | nthrest    | like drop with inverted arguments - never returns nil |
;; | nthnext    | like nthrest but returns nil if beyond input length   |
;; ----------------------------------------------------------------------

;; take, take-while, drop and drop-while have transducer versions

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def day-of-year (.get (Calendar/getInstance) Calendar/DAY_OF_YEAR)) ; 315 (11.11.2025)

(drop day-of-year (range 1 366)) ;; (316 317 320 … 361 362 363 364 365)

;; Extract Information that always appear at the Beginning of a Collection

;; error code, day of month, name of month, …
(def hub-sample
  [[401  7 :mar "-0800" :GET 1.1 12846]
   [200  9 :mar "-0800" :GET 1.1  4523]
   [200  2 :mar "-0800" :GET 1.1  6291]
   [401 17 :mar "-0800" :GET 1.1  7352]
   [200 23 :mar "-0800" :GET 1.1  5253]
   [200  7 :mar "-0800" :GET 1.1 11382]
   [400 27 :mar "-0800" :GET 1.1  4924]
   [200 27 :mar "-0800" :GET 1.1 12851]])

;; errors in March?
(defn error-in-month? [error-code month]
  (and (>= error-code 400) (= month :mar)))

(defn process-errors [hub-messages]
  (filter #(let [[code _ month] (take 3 %)]
             (error-in-month? code month))
          hub-messages))

(process-errors hub-sample)
; ([401 7  :mar "-0800" :GET 1.1 12846]
;  [401 17 :mar "-0800" :GET 1.1 7352]
;  [400 27 :mar "-0800" :GET 1.1 4924])

;; Rule Driving what should be Taken/Removed

(defn tokenize [pred xs]
  (lazy-seq
   (when-let [ys (seq (drop-while (complement pred) xs))]
     (cons (take-while pred ys)
           (tokenize pred (drop-while pred ys))))))

(def digits '(1 4 1 5 9 2 6 4 3 5 8 9 3 2 6))

(comment
  (seq (drop-while (complement odd?) digits))   ; (1 4 1 5 9 2 6 4 3 5 8 9 3 2 6)
  (seq (drop-while (complement even?) digits))) ; (4 1 5 9 2 6 4 3 5 8 9 3 2 6)

(tokenize odd? digits) ; ((1) (1 5 9) (3 5) (9 3))

;; take, drop and take-while, drop-while provide a transducer version
(transduce (comp (drop 3) (map inc))
           +
           (range 10))
; 49

(comment
  (+ 4 5 6 7 8 9 10)) ; 49

;; Laziness Considerations

(defn xs []
  (map
   #(do (print ".")
        %)
   (iterate inc 0)))

(xs) ; .(0. 1. 2. 3. 4. 5. ‥.)

;; take and drop are lazy (no print outs)
(def take-test (take 10000000 (xs)))
(def time-bomb (drop 10000000 (xs)))

take-test ; (0 1 2 3 4 … 9999999)

(comment
  time-bomb) ; never finishes(!)

;; take-last is eager
(def eager (take-last 1 (take 10 (xs))))
; (out) ..........

eager ; (9)

(def lazy-bomb (drop-last (xs)))

(def lazier (nthrest (xs) 3))
; (out) ...

lazy-bomb ; ..(0. 1. 2. 3. 4. … 9999998)
lazier    ; .(3. 4. 5. 6. 7.)

;; Eager Disposal (with nthrest instead of drop)

(defn generate-file [id]
  (let [file (File/createTempFile (str "temp" id "-") ".tmp")]
    (with-open [fw (io/writer file)]
      (binding [*out* fw]
        (pr id)
        file))))

(comment
  (File/createTempFile (str "temp" 42 "-") ".tmp") ; #object[java.io.File 0x66ae2b0b "/tmp/temp42-15919745506542376205.tmp"]
  (generate-file 42))                              ; #object[java.io.File 0x5fa43a9f "/tmp/temp42-3600896613250648814.tmp"]

(defn fetch-clean [file]
  (let [content (slurp file)]
    (println "Deleting file" (.getName file))
    (io/delete-file file)
    content))

(defn service []
  (let [file-list (map #(generate-file %) (list 1 2 3 4 5))]
    (nthrest (map fetch-clean file-list) 2))) ; nthrest is eager -> really read from and delete files

(comment
  (def consumer (service))
  ; (out) Deleting file temp1-6837490957826528953.tmp
  ; (out) Deleting file temp2-12358182479219111105.tmp

  consumer) ; ("3" "4" "5")

;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; Performance Considerations 
;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 

;; using sequence processing functions on data other than sequential is not usually a good idea

;; when necessary equivalent operations exist for vectors for example `subvec`
(defn takev [n vec]
  (subvec vec 0 n))

(defn dropv [n vec]
  (subvec vec n (count vec)))

(takev 5 (vec (range 10))) ; [0 1 2 3 4]
(dropv 5 (vec (range 10))) ; [5 6 7 8 9]

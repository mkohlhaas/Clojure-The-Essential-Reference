(ns f.core
  (:require
   [criterium.core :refer [quick-bench]])
  (:import
   [java.io FileInputStream InputStream]))

(def b (chunk-buffer 10))

(chunk-append b 0) ; nil
(chunk-append b 1) ; nil
(chunk-append b 2) ; nil

(def first-chunk (chunk b))

(chunk-cons first-chunk ()) ; (0 1 2)

#_{:clj-kondo/ignore [:redefined-var]}
(def b (chunk-buffer 10))
(chunk-append b 0) ; nil
(chunk b)          ; #object[clojure.lang.ArrayChunk 0x76fe0355 "clojure.lang.ArrayChunk@76fe0355"]

(comment
  (chunk-append b 0)) ; (err) Execution error (NullPointerException)

(defn map-chunked [f coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [cf (chunk-first s)
           b  (chunk-buffer (count cf))]
       (.reduce cf (fn [b x] (chunk-append b (f x)) b) b)
       (chunk-cons (chunk b) (map-chunked f (chunk-rest s)))))))

(take 10 (map-chunked inc (range 10000))) ; (1 2 3 4 5 6 7 8 9 10)

(defn byte-seq [^InputStream is size]
  (let [ib (byte-array size)]
    ((fn step []
       (lazy-seq
        (let [n (.read is ib)]
          (when (not= -1 n)
            (let [cb (chunk-buffer size)]
              (dotimes [i size] (chunk-append cb (aget ib i)))
              (chunk-cons (chunk cb) (step))))))))))

;; Arch Linux `$ sudo pacman -S words`
(with-open [fis (FileInputStream. "/usr/share/dict/words")]
  (let [bs (byte-seq fis 4096)]
    (String. (byte-array (take 20 bs)))))
; "A\na\nAA\nAAA\nAachen\nAa"

(comment
  (let [xs (into [] (range 10000))]
    (quick-bench (doall (map inc xs))))
  ;; Execution time mean : 330.650098 µs

  (let [xs (subvec (into [] (range 10000)) 0 9999)]
    (quick-bench (doall (map inc xs)))))
  ;; Execution time mean : 988.394350 µs

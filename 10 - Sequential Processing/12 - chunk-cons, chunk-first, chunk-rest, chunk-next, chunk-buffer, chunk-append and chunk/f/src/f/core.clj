(ns f.core
  (:require
   [criterium.core :refer [quick-bench]])
  (:import
   [java.io FileInputStream InputStream]))

;; chunk-* functions were released as part of Clojure 1.1 and labeled as "implementation details" [173].
;; The release note also specifies that they were made public to allow experimentation.
;; Many years later, chunking functions are used extensively in the standard library and despite being still undocumented,
;; there are no signs of them being deprecated, changed or removed.

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def b (chunk-buffer 10)) ; #object[clojure.lang.ChunkBuffer 0x3c3cebae "clojure.lang.ChunkBuffer@3c3cebae"]

(chunk-append b 0) ; nil
(chunk-append b 1) ; nil
(chunk-append b 2) ; nil

;; `chunk` transforms the temporary buffer `b` into a new chunk.
(def first-chunk (chunk b)) ; #object[clojure.lang.ArrayChunk 0x4bf443b5 "clojure.lang.ArrayChunk@4bf443b5"]

(comment
  ;; a buffer that was used to create a chunk becomes unusable
  (chunk-append b 0))
  ; (err) Execution error (NullPointerException)
  ; (err) Cannot store to object array because "this.buffer" is null

(chunk-cons first-chunk ())       ; (0 1 2)
(chunk-cons first-chunk '(3 4 5)) ; (0 1 2 3 4 5)

(defn map-chunked [f coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [cf (chunk-first s)
           b  (chunk-buffer (count cf))]
       (.reduce cf (fn [b x] (chunk-append b (f x)) b) b)
       (chunk-cons (chunk b) (map-chunked f (chunk-rest s)))))))

(take 10 (map-chunked inc (range 10000))) ; (1 2 3 4 5 6 7 8 9 10)

;; custom chunked sequence
;; is = input stream; ib = input buffer; cb = chunk buffer
(defn byte-seq [^InputStream is size]
  (let [ib (byte-array size)]
    ((fn step [] ; define `step` and invoke it immediately
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

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; with chunking
  ;; vectors support chunking
  (let [xs (into [] (range 10000))]
    (quick-bench (doall (map inc xs))))
  ;; Execution time mean : 330.650098 µs

  ;; without chunking
  ;; subvectors don't support chunking
  (let [xs (subvec (into [] (range 10000)) 0 9999)]
    (quick-bench (doall (map inc xs)))))
  ;; Execution time mean : 988.394350 µs

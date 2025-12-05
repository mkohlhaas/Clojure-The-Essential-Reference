(ns f.core
  (:require
   [clojure.java.io :as io])
  (:import
   java.util.HashMap))

;; pr-*    functions are for computer consumption, e.g `read-string`
;; print-* functions are for human consumption

(pr    "a" 'a \a) ; (out) "a" a \a
(print "a" 'a \a) ; (out) a a a

(def java-map (HashMap. {:a "1" :b nil})) ; {:a "1", :b nil}

(prn     java-map) ; (out) {:a "1", :b nil}
(println java-map) ; (out) #object[java.util.HashMap 0x2a5e507d {:a=1, :b=null}]

(def data {:a [1 2 3]
           :b '(:a :b :c)
           :c {"a" 1 "b" 2}})

;; *-str return strings

(pr-str      data) ; "{:a [1 2 3], :b (:a :b :c), :c {\"a\" 1, \"b\" 2}}"
(prn-str     data) ; "{:a [1 2 3], :b (:a :b :c), :c {\"a\" 1, \"b\" 2}}\n"
(print-str   data) ; "{:a [1 2 3], :b (:a :b :c), :c {a 1, b 2}}"
(println-str data) ; "{:a [1 2 3], :b (:a :b :c), :c {a 1, b 2}}\n"

;; Printing to a file using a java.io.BufferedWriter
;; which is the default object type returned by clojure.java.io/writer
;; enables more efficient printing of very large objects,
;; for example those generated from a lazy sequence.
;; The output of the lazy sequence never exists in memory at once,
;; because as soon as new elements are generated and printed they are
;; immediately garbage collected.
(with-open [w (io/writer "/tmp/range.txt")]
  (binding [*out* w]
    (print (range 100000))))

;; use nvim `gf` (go to file)
;; /tmp/range.txt

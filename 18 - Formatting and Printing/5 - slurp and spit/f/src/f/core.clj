(ns f.core
  (:require
   [clojure.java.io]
   [clojure.repl :refer [doc]]))

(spit  "/tmp/test.txt"    "Look, I can write a file!")
(slurp "/tmp/test.txt") ; "Look, I can write a file!"

(comment
  ;; only option `:encoding` is not mentioned in the `reader` documentation:
  (doc clojure.java.io/reader))
  ; (out) -------------------------
  ; (out) clojure.java.io/reader
  ; (out) ([x & opts])
  ; (out)   Attempts to coerce its argument into an open java.io.Reader.
  ; (out)    Default implementations always return a java.io.BufferedReader.
  ; (out) 
  ; (out)    Default implementations are provided for Reader, BufferedReader,
  ; (out)    InputStream, File, URI, URL, Socket, byte arrays, character arrays,
  ; (out)    and String.
  ; (out) 
  ; (out)    If argument is a String, it tries to resolve it first as a URI, then
  ; (out)    as a local file name.  URIs with a 'file' protocol are converted to
  ; (out)    local file names.
  ; (out) 
  ; (out)    Should be used inside with-open to ensure the Reader is properly
  ; (out)    closed.

(comment
  ; from the doc above: "if argument is a String, it tries to resolve it first as a URI, then as a local file name"
  (def book (slurp "https://tinyurl.com/wandpeace"))

  (reduce str (take 24 book))) ; "﻿\r\nThe Project Gutenberg"

;; default encoding is UTF-8 (actually Java property "file.encoding")
(slurp "/etc/hosts" :encoding "UTF-16")
; "⌠却慴楣⁴慢汥⁬潯歵瀠景爠桯獴湡浥献ਣ⁓敥⁨潳瑳⠵⤠景爠摥瑡楬献਱㈷⸰⸰⸱††††汯捡汨潳琊㨺ㄠ††††††⁬潣慬桯獴�"

(spit  "/tmp/sth.txt" "Something." :append true)
(spit  "/tmp/sth.txt" "Something." :append true)
(slurp "/tmp/sth.txt") ; "Something.Something."

(ns f.core
  (:require
   [clojure.java.io :as io :refer [as-url file reader resource writer]]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.string :refer [upper-case]])
  (:import
   [java.nio.file FileSystems Path]))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Streams, Writers and Readers
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(with-open [r (io/reader "/usr/share/dict/words")]
  (count (line-seq r)))
; 123985

(def s "string->array->reader->bytes->string")

(comment
  (io/reader "string->array->reader->bytes->string"))
  ; (err) Execution error (FileNotFoundException)
  ; (err) string->array->reader->bytes->string (No such file or directory)

;; we first transform string to a char-array
(with-open [r (io/reader (char-array s))]
  (slurp r))
; "string->array->reader->bytes->string"

(with-open [w (io/writer "/tmp/output.txt")]
  (spit w "Hello\nClojure!!"))

(comment
  (println (slurp "/tmp/output.txt")))
  ; (out) Hello
  ; (out) Clojure!!

;; copy a file
(with-open [r (reader "/usr/share/dict/words")
            w (writer "/tmp/words1")]
  (doseq [line (line-seq r)]
    (.append w (str (upper-case line) "\n"))))

;; use nvim's gf
;; /tmp/words1

;; same with :append key and .write method
(with-open [r (reader "/usr/share/dict/words"    :encoding "UTF-16")
            w (writer "/tmp/words2" :append true :encoding "UTF-16")]
  (doseq [line (line-seq r)]
    (.write w (str (upper-case line) "\n"))))

;; use nvim's gf
;; /tmp/words2

;; ;;;;;;;;;;;;;;;;;;
;; Resources and URLs
;; ;;;;;;;;;;;;;;;;;;

;; `resource` => retrieve resources from the class path (using the class loader)
(def cjojure-io (resource "clojure/java/io.clj"))
; #object[java.net.URL 0x60b89dc3 "jar:file:/home/schmidh/.m2/repository/org/clojure/clojure/1.12.2/clojure-1.12.2.jar!/clojure/java/io.clj"]

(take 7 (line-seq (reader cjojure-io)))
; (";   Copyright (c) Rich Hickey. All rights reserved."
;  ";   The use and distribution terms for this software are covered by the"
;  ";   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)"
;  ";   which can be found in the file epl-v10.html at the root of this distribution."
;  ";   By using this software in any fashion, you are agreeing to be bound by"
;  ";   the terms of this license."
;  ";   You must not remove this notice, or any other, from this software.")

(def path
  (.. FileSystems
      getDefault
      (getPath "/tmp" (into-array String ["words"]))
      toUri))
; #object[java.net.URI 0x10aa39e9 "file:///tmp/words"]

(def u1 (as-url "file:///tmp/words"))
(def u2 (as-url (file "/tmp/words")))
(def u3 (as-url path))

(= u1 u2 u3) ; true

(extend-protocol io/Coercions
  Path
  (as-file [path] (io/file   (.toUri path)))
  (as-url  [path] (io/as-url (.toUri path))))

(def path1
  (.. FileSystems
      getDefault
      (getPath "/usr" (into-array String ["share" "dict" "words"]))))
; #object[sun.nio.fs.UnixPath 0x29a9936d "/usr/share/dict/words"]

(comment
  (javadoc java.nio.file.FileSystems)
  (javadoc java.nio.file.FileSystem))

(io/as-url path1)
; #object[java.net.URL 0x745eb832 "file:/usr/share/dict/words"]

;; directly file from path
(io/file path1)
; #object[java.io.File 0x86e17a4 "/usr/share/dict/words"]

;; ;;;;;;;;;;;;;;;;;;
;; Dealing with Files
;; ;;;;;;;;;;;;;;;;;;

(keys io/Coercions)
; (:on
;  :on-interface
;  :doc
;  :sigs
;  :var
;  :method-map
;  :method-builders
;  :impls)

(:doc io/Coercions) ; "Coerce between various 'resource-namish' things."

(:sigs io/Coercions)
; {:as-file
;  {:tag java.io.File,
;   :added "1.2",
;   :name as-file,
;   :arglists ([x]),
;   :doc "Coerce argument to a file."},
;  :as-url
;  {:tag java.net.URL,
;   :added "1.2",
;   :name as-url,
;   :arglists ([x]),
;   :doc "Coerce argument to a URL."}}

;; possible argument types for `io/file`
(keys (:impls io/Coercions))
; (nil                  (1)
;  java.lang.String     (2)
;  java.io.File         (3)
;  java.net.URL         (4)
;  java.net.URI         (5)
;  java.nio.file.Path)  (6)

(comment
  (type io/Coercions) ; clojure.lang.PersistentArrayMap
  (javadoc io/Coercions))

;; `io/file` does not actually create a physical resource, but just a pointer that other functions like `writer` can use. 

;; nil (1)
(io/file nil) ; nil

;; String (2)
(io/file "/a/valid/file/path")
; #object[java.io.File 0xa549480 "/a/valid/file/path"]

;; File (3)
(io/file (io/file "/a/valid/file/path"))
; #object[java.io.File 0x56ea9752 "/a/valid/file/path"]

;; URL (4)
(io/file (io/as-url "file://a/valid/url"))
; #object[java.io.File 0x26e12f21 "/valid/url"]

;; URI (5)
(io/file (.toURI (io/as-url "file://a/valid/uri")))
; #object[java.io.File 0x4effd2f7 "/valid/uri"]

;; (relative file) Path (6)
(io/file "/root" (io/file "not/root") "filename.txt")
; #object[java.io.File 0x1b1dc7c8 "/root/not/root/filename.txt"]

(comment
  ;; all args to `io/file` need to be relative paths
  (io/file "/root" (io/file "/not/relative") "filename.txt"))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) /not/relative is not a relative path

;; Create Content with `io/copy` ;;

(io/copy (io/file "/usr/share/dict/words")
         (io/file "/tmp/words2")) ; nil

(.exists (io/file "/tmp/words2")) ; true

#_{:clj-kondo/ignore [:private-call]}
;; The defmethod definition for io/do-copy is private in clojure.java.io.
;; But we can still make access to it by looking up the related var object (with the reader macro #')
;; and then dereferencing the var with @ (another reader macro).
(defmethod @#'io/do-copy [String String]
  [in out opts]
  (apply io/copy (io/file in) (io/file out) opts))

(io/copy "/usr/share/dict/words" "/tmp/words3") ; nil

(.exists (io/file "/tmp/words3")) ; true

;; Use `make-parents` to Create All Folders Recursively ;;

(def segments ["/tmp" "a" "b" "file.txt"])

(apply io/make-parents segments) ; true (on first invocation when directories don't exist yet)

(io/copy (io/file "/usr/share/dict/words")  ; #object[java.io.File 0x7295011c "/usr/share/dict/words"]
         (apply io/file segments))          ; #object[java.io.File 0x7a631469 "/tmp/a/b/file.txt"]

(count (line-seq (io/reader (io/file "/usr/share/dict/words")))) ; 123985
(count (line-seq (io/reader (apply io/file segments))))          ; 123985

;; Use `delete-file` to Remove Files ;;

(comment
  (io/delete-file "/does/not/exist"))
  ; (err) Execution error (IOException)
  ; (err) Couldn't delete /does/not/exist

(io/delete-file "/does/not/exist" :ignore) ; :ignore

(io/delete-file "/tmp/a/b/file.txt" "This file should exist") ; true ("This file should exist" if the file doesn't exist)

;; `as-relative-path` Retrieves the Path from Resources Objects ;;

(def folders ["root/a/1" "root/a/2" "root/b/1" "root/c/1" "root/c/1/2"])

(map io/make-parents folders) ; (true false true true true)
;; $ tree root
;; root
;; ├── a
;; ├── b
;; └── c
;;     └── 1

(map io/as-relative-path (file-seq (io/file "root")))
; ("root"
;  "root/c"
;  "root/c/1"
;  "root/b"
;  "root/a"

(comment
  (file-seq (io/file "root")))
; (#object[java.io.File 0x5c3165a3 "root"]
;  #object[java.io.File 0x3074e878 "root/c"]
;  #object[java.io.File 0x5e8678e8 "root/c/1"]
;  #object[java.io.File 0x70a724bb "root/b"]
;  #object[java.io.File 0x5209ef22 "root/a"])

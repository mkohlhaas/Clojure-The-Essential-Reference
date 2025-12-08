(ns f.core
  (:require
   [clojure.java.shell :as shell :refer [sh with-sh-dir with-sh-env]]
   [clojure.repl :refer [dir-fn doc]]))

(dir-fn 'clojure.java.shell)
; (*sh-dir* 
;  *sh-env* 
;  sh 
;  with-sh-dir
;  with-sh-env)

(comment
  shell/*sh-dir*  ; nil
  shell/*sh-env*) ; nil

(comment
  (doc sh))
  ; (out) -------------------------
  ; (out) clojure.java.shell/sh
  ; (out) ([& args])
  ; (out)   Passes the given strings to Runtime.exec() to launch a sub-process.
  ; (out) 
  ; (out)   Options are
  ; (out) 
  ; (out)   :in      may be given followed by any legal input source for
  ; (out)            clojure.java.io/copy, e.g. InputStream, Reader, File, byte[],
  ; (out)            or String, to be fed to the sub-process's stdin.
  ; (out)   :in-enc  option may be given followed by a String, used as a character
  ; (out)            encoding name (for example "UTF-8" or "ISO-8859-1") to
  ; (out)            convert the input string specified by the :in option to the
  ; (out)            sub-process's stdin.  Defaults to UTF-8.
  ; (out)            If the :in option provides a byte array, then the bytes are passed
  ; (out)            unencoded, and this option is ignored.
  ; (out)   :out-enc option may be given followed by :bytes or a String. If a
  ; (out)            String is given, it will be used as a character encoding
  ; (out)            name (for example "UTF-8" or "ISO-8859-1") to convert
  ; (out)            the sub-process's stdout to a String which is returned.
  ; (out)            If :bytes is given, the sub-process's stdout will be stored
  ; (out)            in a byte array and returned.  Defaults to UTF-8.
  ; (out)   :env     override the process env with a map (or the underlying Java
  ; (out)            String[] if you are a masochist).
  ; (out)   :dir     override the process dir with a String or java.io.File.
  ; (out) 
  ; (out)   You can bind :env or :dir for multiple operations using with-sh-env
  ; (out)   and with-sh-dir.
  ; (out) 
  ; (out)   sh returns a map of
  ; (out)     :exit => sub-process's exit code
  ; (out)     :out  => sub-process's stdout (as byte[] or String)
  ; (out)     :err  => sub-process's stderr (String via platform default encoding)

(sh "ls" "/usr/share/dict")
; {:exit 0,
;  :out
;  "american-english\nbritish\nbritish-english\ncatala\ncatalan\ncracklib-small\nfinnish\nfrench\ngerman\nitalian\nngerman\nogerman\nspanish\nusa\nwords\n",
;  :err ""}

(def result
  (sh "grep" "5"
      :in (apply str (interpose "\n" (range 30))))) ; "0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17\n18\n19\n20\n21\n22\n23\n24\n25\n26\n27\n28\n29"
; {:exit 0, :out "5\n15\n25\n", :err ""}

(comment
  (println (:out result)))
  ; (out) 5
  ; (out) 15
  ; (out) 25

;; :bytes Encoding ;;

(def image-file "resources/pexels-pixabay-276514.jpg")

(def cmd (sh "cat" image-file :out-enc :bytes))
; {:exit 0,
;  :out
;  [-1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 0, 72, 0, 72, 0,
;   0, -1, -30, 12, 88, 73, 67, 67, 95, 80, 82, 79, 70, 73, 76, 69, 0, 1,
;   …
;   0, 0, 0, 0, 0, 0, 0, 18, 115, 82, 71, 66, 32, 73, 69, 67, 54, 49, 57,
;   54, 54, 45, 50, 46, 49, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
;   …,
;  :err ""}

(count (:out cmd)) ; 76679

;; Command Pipeline ;;

(defn pipe [first-cmd & cmds]
  (reduce
   (fn [{out :out} cmd]
     (apply sh (conj cmd :in out)))
   (apply sh first-cmd)
   cmds))

(comment
  (println
   (:out
    (pipe
     ["env"]
     ["grep" "-i" "java"]))))
  ; (out) LEIN_JVM_OPTS=-Xbootclasspath/a:/usr/share/java/leiningen-2.12.0-standalone.jar -XX:+TieredCompilation -XX:TieredStopAtLevel=1
  ; (out) CLASSPATH=/usr/share/java/leiningen-2.12.0-standalone.jar
  ; (out) LEIN_JAVA_CMD=java
  ; (out) PWD=/home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/22 - The Toolbox/06 - clojure.java.shell/f
  ; (out) _=/usr/bin/java

(def custom-env
  {"VAR1"         "iTerm.app"
   "VAR2"         "/bin/bash"
   "COMMAND_MODE" "Unix2003"})

(comment
  (println
   (:out
    (sh "env" :env custom-env))))
  ; (out) VAR1=iTerm.app
  ; (out) VAR2=/bin/bash
  ; (out) COMMAND_MODE=Unix2003
  ; (out) 

(comment
  (println (:out (sh "pwd")))
  ; (out) /home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/22 - The Toolbox/06 - clojure.java.shell/f

  ;; change working folder
  (println (:out (sh "pwd" :dir "/tmp"))))
  ; (out) /tmp

;; handy macros to set working directory and environment
(with-sh-dir "/usr/share"
  (with-sh-env {:debug "true"}
    [(sh "env") (sh "pwd")]))
; [{:exit 0, :out "debug=true\n", :err ""}
;  {:exit 0, :out "/usr/share\n", :err ""}]

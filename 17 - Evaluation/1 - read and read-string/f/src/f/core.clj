(ns f.core)

;; NOTE: works only in the REPL

;; ;;;;
;; read
;; ;;;;

;; (instance? java.io.PushbackReader *in*)
;; true

;; (def output (read))
;; (+ 1 2)
;; #'user/output

;; output
;; (+ 1 2)

;; (type output)
;; clojure.lang.PersistentList

;; (def example
;;   "#?(:clj     (System/currentTimeMillis)      ; `#?` is the read conditional macro
;;       :cljs    (js/Console :log)
;;       :cljr    (|Dictionary<Int32,String>|.)
;;       :default <anything you want>)")

;; (defn reader-from [s]
;;   (-> (java.io.StringReader. s)
;;       (clojure.lang.LineNumberingPushbackReader.)))

;; (read (reader-from example))
;; RuntimeException Conditional read not allowed

;; (read {:read-cond :allow} (reader-from example))
;; (System/currentTimeMillis)

;; (read {:read-cond :preserve} (reader-from example))
;; #?(:clj     (System/currentTimeMillis)
;;    :cljs    (js/Console :log)
;;    :cljr    (|Dictionary<Int32 String>|.)
;;    :default <anything you want>)

;; (def example "#?(:cljs :cljs :my :my :default <missing>)")

;; (read {:read-cond :allow} (reader-from example))
;; <missing>

;; (read {:read-cond :allow :features #{:my}} (reader-from example))
;; :my

;; (read (reader-from ";; a comment"))
;; RuntimeException EOF while reading

;; (read {:eof nil} (reader-from ";; a comment"))
;; nil

;; (read (reader-from ";; a comment") false nil)
;; nil

;; `#=` (read-eval macro)
;; (read (reader-from "#=(+ 1 2)"))
;; 3

;; (read (reader-from "(java.lang.System/exit 0)"))
;; (java.lang.System/exit 0)

;; WARNING the JVM will exit.
;; (read (reader-from "#=(java.lang.System/exit 0)"))

;; (binding [*read-eval* false]
;;   (read (reader-from "#=(java.lang.System/exit 0)")))
;; RuntimeException EvalReader not allowed when *read-eval* is false

;; (binding [*read-eval* :unknown]
;;   (read (reader-from "(+ 1 2)")))
;; RuntimeException Reading disallowed - *read-eval* bound to :unknown

;; ;;;;;;;;;;;
;; read-string
;; ;;;;;;;;;;;

;; `read-string` supports the same options as `read` (the description of the function is also the same)

;; (read-string "(+ 1 2)")
;; (+ 1 2)

;; (read-string {:eof "nothing to read"} "")
;; "nothing to read"

(ns f.core
  (:require
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.repl :refer [pst]])
  (:import
   [java.net
    ConnectException
    InetAddress
    Socket
    SocketException]))

  ;;`throw` expects a single argument of type java.lang.Throwable or any subclass,
  ;; e.g. java.lang.Exception, java.lang.Error

(comment
  (javadoc Throwable)

  ; no need to import java.lang.Throwable because many classes in java.lang.* are available by default
  (throw (Throwable. "'there was a problem.'"))
  ; (err) Execution error (Throwable)
  ; (err) 'there was a problem.'

  ;; pst = print stack trace
  (pst))
  ; (err) Throwable 'there was a problem.'
  ; (err) 	f.core/eval4107 (form-init473636411051200546.clj:6)
  ; (err) 	f.core/eval4107 (form-init473636411051200546.clj:6)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7751)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7706)
  ; (err) 	clojure.core/eval (core.clj:3232)
  ; (err) 	clojure.core/eval (core.clj:3228)
  ; (err) 	nrepl.middleware.interruptible-eval/evaluate/fn--1268/fn--1269 (interruptible_eval.clj:87)
  ; (err) 	clojure.core/apply (core.clj:667)
  ; (err) 	clojure.core/with-bindings* (core.clj:1990)
  ; (err) 	clojure.core/with-bindings* (core.clj:1990)
  ; (err) 	nrepl.middleware.interruptible-eval/evaluate/fn--1268 (interruptible_eval.clj:87)
  ; (err) 	clojure.main/repl/read-eval-print--9248/fn--9251 (main.clj:437)

(comment
  (try
    (println "Program running as expected")
    (throw (RuntimeException. "Got a problem."))
    (println "program never reaches this line")
    (catch Exception e
      (println "Could not run properly" e)
      "returning home")))
  ; "returning home"

  ; (out) Program running as expected
  ; (out) Could not run properly #error {
  ; (out)  :cause Got a problem.
  ; (out)  :via
  ; (out)  [{:type java.lang.RuntimeException
  ; (out)    :message Got a problem.
  ; (out)    :at [f.core$eval4111 invokeStatic form-init473636411051200546.clj 29]}]
  ; (out)  :trace
  ; (out)  [[f.core$eval4111 invokeStatic form-init473636411051200546.clj 29]
  ; (out)   [f.core$eval4111 invoke form-init473636411051200546.clj 28]
  ; (out)   [clojure.lang.Compiler eval Compiler.java 7751]
  ; (out)   [clojure.lang.Compiler eval Compiler.java 7706]
  ; (out)   …
  ; (out)   [clojure.lang.AFn run AFn.java 22]
  ; (out)   [nrepl.middleware.session$session_exec$main_loop__1371$fn__1375 invoke session.clj 218]
  ; (out)   [nrepl.middleware.session$session_exec$main_loop__1371 invoke session.clj 217]
  ; (out)   [clojure.lang.AFn run AFn.java 22]
  ; (out)   [java.lang.Thread run Thread.java 1474]]}

(comment
  (javadoc SocketException)
  (javadoc ConnectException)

  ;; Class ConnectException
  ;;   java.lang.Object
  ;;       java.lang.Throwable
  ;;           java.lang.Exception
  ;;               java.io.IOException
  ;;                   java.net.SocketException (direct subclasses: BindException, ConnectException, NoRouteToHostException, PortUnreachableException)
  ;;                       java.net.ConnectException 

  (try
    (Socket. (InetAddress/getByName "localhost") 61817)
    ;; catches from more specific to the most general types
    (catch ConnectException ce
      (println "Could not connect. Retry." ce))
    (catch SocketException se
      (println "Communication error" se))
    (catch Exception e
      (println "Something weird happened." e)
      (throw e)))) ; re-throwing signals we're unable to handle the exception but allows other blocks upstream to handle it

;; simplified `with-open` dealing with java.io.Reader objects only
(defmacro with-reader [r file & body]
  `(let [~r (io/reader ~file)]
     (try
       ~@body
       (finally
         (.close ~r)))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(with-reader r "/etc/hosts"
  (doall (line-seq r)))

; ("# Static table lookup for hostnames."
;  "# See hosts(5) for details."
;  "127.0.0.1        localhost"
;  "::1              localhost")

;; NOTE: returns the evaluation of the matching catch!!!
(try
  (/ 1 0)
  (catch Exception _e
    "Returning from catch")
  (finally
    (println "Also executing finally")))
; (out) Also executing finally (from finally)
; "Returning from catch"       (from matching `catch`)

(ns f.core
  (:require [clojure.java.javadoc :refer [javadoc]]))

;; `ex-info` and `ex-data` main purpose is to decorate Java exceptions with metadata

;; `ex-info` hides any Java interoperability detail necessary to create the exception object.
;;  The only information required is a message and the metadata map.
;;  It optionally accepts a third argument to capture or re-throw a root-cause exception.
;;  We can access the metadata with `ex-data`.

(def ex
  (ex-info "Temperature drop!"
           {:time "10:29pm"
            :reason "Front door open."
            :mitigation #(println "close the door")}))

ex
; #error {}
;  :cause "Temperature drop!"
;  :data {:time "10:29pm", :reason "Front door open.", :mitigation #object[f.core$fn__4109 0x390eb612 "f.core$fn__4109@390eb612"]}
;  :via
;  [{:type clojure.lang.ExceptionInfo
;    :message "Temperature drop!"
;    :data {:time "10:29pm", :reason "Front door open.", :mitigation #object[f.core$fn__4109 0x390eb612 "f.core$fn__4109@390eb612"]}
;    :at [clojure.lang.AFn applyToHelper "AFn.java" 156]}]
;  :trace
;  [[clojure.lang.AFn applyToHelper "AFn.java" 156]
;   [clojure.lang.AFn applyTo "AFn.java" 144]
;   [clojure.lang.Compiler$InvokeExpr eval "Compiler.java" 4216]
;   …
;   [nrepl.middleware.session$session_exec$main_loop__1375 invoke "session.clj" 217]
;   [clojure.lang.AFn run "AFn.java" 22]
;   [java.lang.Thread run "Thread.java" 1474]]

(type ex) ; clojure.lang.ExceptionInfo

;; (javadoc clojure.lang.ExceptionInfo)
(comment
  (javadoc ex))

(.getData ex)
; {:time       "10:29pm",
;  :reason     "Front door open.",
;  :mitigation #object[f.core$fn__155 0x67d0281b "f.core$fn__155@67d0281b"]}

(comment
  (try
    (/ 1 0)
    (catch Exception e
      (throw
       (ex-info               ; wraps around `e` and re-throws a new exception decorated with additional metadata
        "Don't do this."      ; message
        {:type "Math"         ; metadata map
         :recoverable? false}
        e)))))                ; root cause
  ; (err) Execution error (ArithmeticException)
  ; (err) Divide by zero

;; to see and use the metadata, we use `ex-data`
(defn randomly-recoverable-operation []
  (throw
   (ex-info "Weak connection."
            {:type :connection
             :recoverable? (< 0.3 (rand))})))

(defn main-program-loop []
  (try
    (println "Attempting operation...")
    (randomly-recoverable-operation)
    (catch Exception e
      (println "metadata: " (ex-data e))
      (let [{:keys [type recoverable?]} (ex-data e)]
        (if (and (= :connection type) recoverable?)
          (main-program-loop) ; try again as long it's recoverable
          (ex-info "Not recoverable problem."
                   {:type :connection}
                   e))))))

(comment
  (main-program-loop))
  ; (out) Attempting operation...
  ; (out) metadata:  {:type :connection, :recoverable? true}
  ; (out) Attempting operation...
  ; (out) metadata:  {:type :connection, :recoverable? true}
  ; (out) Attempting operation...
  ; (out) metadata:  {:type :connection, :recoverable? false}

  ; #error {}
  ;  :cause "Weak connection."
  ;  :data {:type :connection, :recoverable? false}
  ;  :via
  ;  [{:type clojure.lang.ExceptionInfo
  ;    :message "Not recoverable problem."
  ;    :data {:type :connection}
  ;    :at [f.core$main_program_loop invokeStatic "form-init6331554528041914548.clj" 74]}
  ;   {:type clojure.lang.ExceptionInfo
  ;    :message "Weak connection."
  ;    :data {:type :connection, :recoverable? false}
  ;    :at [f.core$randomly_recoverable_operation invokeStatic "core.clj" 42]}]
  ;  :trace
  ;  [[f.core$randomly_recoverable_operation invokeStatic "core.clj" 42]
  ;   [f.core$randomly_recoverable_operation invoke "core.clj" 40]
  ;   [f.core$main_program_loop invokeStatic "form-init6331554528041914548.clj" 68]
  ;   [f.core$main_program_loop invoke "form-init6331554528041914548.clj" 65]
  ;   …
  ;   [nrepl.middleware.session$session_exec$main_loop__1391$fn__1395 invoke "session.clj" 218]
  ;   [nrepl.middleware.session$session_exec$main_loop__1391 invoke "session.clj" 217]
  ;   [clojure.lang.AFn run "AFn.java" 22]
  ;   [java.lang.Thread run "Thread.java" 1474]]

;; `Throwable→map` transforms fragmented information inside a hierarchy of exceptions into a nice Clojure data structure
(def error-data
  (try (throw (ex-info "inner" {:recoverable? false}))
       (catch Throwable t
         (try (throw (ex-info "outer" {:recoverable? false} t))
              (catch Throwable t
                (Throwable->map t))))))

(keys error-data) ; (:via :trace :cause :data)

error-data
; {:via
;  [{:type clojure.lang.ExceptionInfo,
;    :message "outer",
;    :data {:recoverable? false},
;    :at [f.core$fn__163$fn__164 invoke "core.clj" 87]}
;   {:type clojure.lang.ExceptionInfo,
;    :message "inner",
;    :data {:recoverable? false},
;    :at [f.core$fn__163 invokeStatic "core.clj" 85]}],
;  :trace
;  [[f.core$fn__163 invokeStatic "core.clj" 85]
;   [f.core$fn__163 invoke "core.clj" 85]
;   [clojure.lang.AFn applyToHelper "AFn.java" 152]
;   [clojure.lang.AFn applyTo "AFn.java" 144]
;   [clojure.lang.Compiler$InvokeExpr eval "Compiler.java" 4216]
;   …
;   [clojure.lang.RestFn applyTo "RestFn.java" 140]
;   [clojure.lang.Var applyTo "Var.java" 707]
;   [clojure.main main "main.java" 40]],
;  :cause "inner",
;  :data {:recoverable? false}}

(:cause error-data) ; "inner"

(:via error-data)
; [{:type clojure.lang.ExceptionInfo,
;   :message "outer",
;   :data {:recoverable? false},
;   :at
;   [f.core$fn__4121$fn__4122
;    invoke
;    "form-init6400837332085940506.clj"
;    87]}
;  {:type clojure.lang.ExceptionInfo,
;   :message "inner",
;   :data {:recoverable? false},
;   :at
;   [f.core$fn__4121 invokeStatic "form-init6400837332085940506.clj" 85]}]

(nth (:trace error-data) 3) ; [clojure.lang.AFn applyTo "AFn.java" 144]

(:data error-data) ; {:recoverable? false}

(ns f.core
  "This namespace contains useful functions."
  (:require
   [clojure.java.browse]
   [clojure.reflect]
   [clojure.repl :refer [apropos demunge dir dir-fn doc find-doc pst
                         root-cause source source-fn stack-element-str]]))

;; ;;;
;; doc
;; ;;;

(comment
  (def life 42)

  (doc life)
  ; (out) -------------------------
  ; (out) f.core/life

  (alter-meta!
   #'life
   assoc :doc "Answer to the Ultimate Question of Life the Universe and Everything")

  (doc life)
  ; (out) -------------------------
  ; (out) f.core/life
  ; (out)   Answer to the Ultimate Question of Life the Universe and Everything

  (doc doc)
  ; (out) -------------------------
  ; (out) clojure.repl/doc
  ; (out) ([name])
  ; (out) Macro
  ; (out)   Prints documentation for a var or special form given its name,
  ; (out)    or for a spec if given a keyword

  (doc f.core))
  ; (out) -------------------------
  ; (out) f.core
  ; (out)   This namespace contains useful functions.

;; ;;;;;;;;
;; find-doc
;; ;;;;;;;;

(comment
  ;; searches in doc strings
  (find-doc "^to-"))
  ; (out) -------------------------
  ; (out) clojure.core/to-array
  ; (out) ([coll])
  ; (out)   Returns an array of Objects containing the contents of coll, which
  ; (out)   can be any Collection.  Maps to java.util.Collection.toArray().
  ; (out) -------------------------
  ; (out) clojure.core/to-array-2d
  ; (out) ([coll])
  ; (out)   Returns a (potentially-ragged) 2-dimensional array of Objects
  ; (out)   containing the contents of coll, which can be any Collection of any
  ; (out)   Collection.

;; ;;;;;;;
;; apropos
;; ;;;;;;;

;; public "to-"-definitions ("to-" in name only, not whole doc string)
(apropos "to-")
; (clojure.core/into-array
;  clojure.core/seq-to-map-for-destructuring
;  clojure.core/to-array
;  clojure.core/to-array-2d
;  clojure.core.rrb-vector.rrbt/fallback-to-slow-splice-count1
;  clojure.core.rrb-vector.rrbt/fallback-to-slow-splice-count2
;  clojure.core.rrb-vector.rrbt/fallback-to-slow-splice-if-needed
;  incomplete.core/qualified-auto-resolved-keywords
;  incomplete.core/unqualified-auto-resolved-keywords
;  nrepl.util.completion/qualified-auto-resolved-keywords
;  nrepl.util.completion/unqualified-auto-resolved-keywords)

;; ;;;
;; dir
;; ;;;

(comment
  ;; public vars in clojure.reflect (what's available in clojure.reflect?)
  (dir clojure.reflect))
  ; (out) ->AsmReflector
  ; (out) ->Constructor
  ; (out) ->Field
  ; (out) ->JavaReflector
  ; (out) ->Method
  ; (out) ClassResolver
  ; (out) Reflector
  ; (out) TypeReference
  ; (out) do-reflect
  ; (out) flag-descriptors
  ; (out) map->Constructor
  ; (out) map->Field
  ; (out) map->Method
  ; (out) reflect
  ; (out) resolve-class
  ; (out) type-reflect
  ; (out) typename

;; ;;;;;;
;; dir-fn
;; ;;;;;;

(apply str (interpose ", " (dir-fn 'clojure.java.browse)))
; "*open-url-script*, browse-url"

(comment
  (dir-fn 'clojure.java.browse)) ; (*open-url-script* browse-url)

;; ;;;;;;
;; source
;; ;;;;;;

(comment
  (source unchecked-inc-int))
  ; (out) (defn unchecked-inc-int
  ; (out)   "Returns a number one greater than x, an int.
  ; (out)   Note - uses a primitive operator subject to overflow."
  ; (out)   {:inline (fn [x] `(. clojure.lang.Numbers (unchecked_int_inc ~x)))
  ; (out)    :added "1.0"}
  ; (out)   [x] (. clojure.lang.Numbers (unchecked_int_inc x)))

;; ;;;;;;;;;
;; source-fn
;; ;;;;;;;;;

(source-fn 'not-empty)
; "(defn not-empty\n  \"If coll is empty, returns nil, else coll\"\n  {:added \"1.0\"\n   :static true}\n  [coll] (when (seq coll) coll))"

(comment
  (println *1))
  ; (out) (defn not-empty
  ; (out)   "If coll is empty, returns nil, else coll"
  ; (out)   {:added "1.0"
  ; (out)    :static true}
  ; (out)   [coll] (when (seq coll) coll))

;; ;;;
;; pst
;; ;;;

;; `pst` = print stack trace

(comment
  (/ 1 0))
  ; (err) Execution error (ArithmeticException)
  ; (err) Divide by zero

;; the REPL stores a copy of the full stack trace in the *e dynamic variable
*e
; #error {
;  :cause "Divide by zero"
;  :via
;  [{:type java.lang.ArithmeticException
;    :message "Divide by zero"
;    :at [clojure.lang.Numbers divide "Numbers.java" 190]}]
;  :trace
;  [[clojure.lang.Numbers divide "Numbers.java" 190]
;   [clojure.lang.Numbers divide "Numbers.java" 3915]
;   [f.core$eval4223 invokeStatic "form-init13149080240726159843.clj" 102]
;   …
;   [nrepl.middleware.session$session_exec$main_loop__1360$fn__1364 invoke "session.clj" 218]
;   [nrepl.middleware.session$session_exec$main_loop__1360 invoke "session.clj" 217]
;   [clojure.lang.AFn run "AFn.java" 22]
;   [java.lang.Thread run "Thread.java" 1474]]}

(pst)
; (err) ArithmeticException Divide by zero
; (err) 	clojure.lang.Numbers.divide (Numbers.java:190)
; (err) 	clojure.lang.Numbers.divide (Numbers.java:3915)
; (err) 	f.core/eval4223 (form-init13149080240726159843.clj:102)
; (err) 	f.core/eval4223 (form-init13149080240726159843.clj:102)
; (err) 	clojure.lang.Compiler.eval (Compiler.java:7751)
; (err) 	clojure.lang.Compiler.eval (Compiler.java:7706)
; (err) 	clojure.core/eval (core.clj:3232)
; (err) 	clojure.core/eval (core.clj:3228)
; (err) 	nrepl.middleware.interruptible-eval/evaluate/fn--1257/fn--1258 (interruptible_eval.clj:87)
; (err) 	clojure.core/apply (core.clj:667)
; (err) 	clojure.core/with-bindings* (core.clj:1990)
; (err) 	clojure.core/with-bindings* (core.clj:1990)

(def ex (ex-info "Problem." {:status :surprise}))
; #error {
;  :cause "Problem."
;  :data {:status :surprise}
;  :via
;  [{:type clojure.lang.ExceptionInfo
;    :message "Problem."
;    :data {:status :surprise}
;    :at [clojure.lang.AFn applyToHelper "AFn.java" 156]}]
;  :trace
;  [[clojure.lang.AFn applyToHelper "AFn.java" 156]
;   [clojure.lang.AFn applyTo "AFn.java" 144]
;   [clojure.lang.Compiler$InvokeExpr eval "Compiler.java" 4216]
;   [clojure.lang.Compiler$DefExpr eval "Compiler.java" 464]
;   [clojure.lang.Compiler eval "Compiler.java" 7756]
;   …
;   [nrepl.middleware.session$session_exec$main_loop__1360$fn__1364 invoke "session.clj" 218]
;   [nrepl.middleware.session$session_exec$main_loop__1360 invoke "session.clj" 217]
;   [clojure.lang.AFn run "AFn.java" 22]
;   [java.lang.Thread run "Thread.java" 1474]]}

(comment
  (pst ex)
  ; (err) ExceptionInfo Problem. {:status :surprise}
  ; (err) 	clojure.lang.Compiler$InvokeExpr.eval (Compiler.java:4216)
  ; (err) 	clojure.lang.Compiler$DefExpr.eval (Compiler.java:464)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7756)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7706)
  ; (err) 	clojure.core/eval (core.clj:3232)
  ; (err) 	clojure.core/eval (core.clj:3228)
  ; (err) 	nrepl.middleware.interruptible-eval/evaluate/fn--1257/fn--1258 (interruptible_eval.clj:87)
  ; (err) 	clojure.core/apply (core.clj:667)
  ; (err) 	clojure.core/with-bindings* (core.clj:1990)
  ; (err) 	clojure.core/with-bindings* (core.clj:1990)
  ; (err) 	nrepl.middleware.interruptible-eval/evaluate/fn--1257 (interruptible_eval.clj:87)
  ; (err) 	clojure.main/repl/read-eval-print--9248/fn--9251 (main.clj:437)

  (pst ex 4))
  ; (err) ExceptionInfo Problem. {:status :surprise}
  ; (err) 	clojure.lang.Compiler$InvokeExpr.eval (Compiler.java:4216)
  ; (err) 	clojure.lang.Compiler$DefExpr.eval (Compiler.java:464)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7756)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7706)

;; nested exceptions
(def ex1
  (ex-info "Problem." {:status :surprise}
           (try (/ 1 0)
                (catch Exception e
                  (ex-info "What happened?" {:status :unkown} e)))))

(comment
  (pst ex1 3))
  ; (err) ExceptionInfo Problem. {:status :surprise}
  ; (err) 	clojure.lang.Compiler$InvokeExpr.eval (Compiler.java:4216)
  ; (err) 	clojure.lang.Compiler$DefExpr.eval (Compiler.java:464)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7756)
  ; (err) Caused by:
  ; (err) ExceptionInfo What happened? {:status :unkown}
  ; (err) 	f.core/fn--4247 (form-init13149080240726159843.clj:186)
  ; (err) 	f.core/fn--4247 (form-init13149080240726159843.clj:184)
  ; (err) 	clojure.lang.Compiler$InvokeExpr.eval (Compiler.java:4216)
  ; (err) Caused by:
  ; (err) ArithmeticException Divide by zero
  ; (err) 	clojure.lang.Numbers.divide (Numbers.java:190)
  ; (err) 	clojure.lang.Numbers.divide (Numbers.java:3915)
  ; (err) 	f.core/fn--4247 (form-init13149080240726159843.clj:184)

;; ;;;;;;;;;;
;; root-cause
;; ;;;;;;;;;;

(comment
  (pst (root-cause ex) 3))
  ; (err) ExceptionInfo Problem. {:status :surprise}
  ; (err) 	clojure.lang.Compiler$InvokeExpr.eval (Compiler.java:4216)
  ; (err) 	clojure.lang.Compiler$DefExpr.eval (Compiler.java:464)
  ; (err) 	clojure.lang.Compiler.eval (Compiler.java:7756)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; munge, demunge and stack-element-str
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `munge` translates valid Clojure function/namespace names into valid Java class/package names

(defn my-funct! []
  (throw (ex-info "error" {})))

(str my-funct!)           ; "f.core$my_funct_BANG_@7328ea1a"
(demunge (str my-funct!)) ; "f.core/my-funct!@7328ea1a"

(def stack-trace
  (try (my-funct!)
       (catch Exception e
         (.getStackTrace e))))

(nth stack-trace 2)                     ; [f.core$fn__4286 invokeStatic "form-init13149080240726159843.clj" 217]  (Java    names)
(stack-element-str (nth stack-trace 2)) ; "f.core/fn--4286 (form-init13149080240726159843.clj:217)"               (Clojure names)

(ns f.core
  (:require [clojure.string :as s :refer [lower-case]]))

;; Defining a macro is as simple as defining functions and manipulating data:
;; macros are indeed just regular functions that the compiler invokes at compile
;; time, passing as inputs their arguments as if wrapped in an implicit quote
;; invocation and returning a valid Clojure expression that will be evaluated
;; at run time.

;; Syntax Quoting
;; 1. auto-qualification (with the proper namespace)
;; 2. auto-gensym (gensym, #)
;; 3. unquote (~)
;; 4. unquote-splicing (~@)

;; `defmacro` is a macro itself
(macroexpand '(defmacro simple [a] (str a)))

;; https://clojuredocs.org/clojure.core/var
;; #' Var-quote: #'x → ( var x)

;; `defmacro` is built on top of defn.
;; `defmacro` uses the same syntax as defn.
;;  The argument vector is always added 2 implicit arguments, &form and &env.
;; `defmacro` returns a clojure.lang.Var object referencing the macro just created.

; (do
;  (clojure.core/defn simple ([&form &env a] (str a))) ; define a function
;  (. #'simple (setMacro))                             ; transform it into a macro
;  #'simple)                                           ; return the var object

; from the book:
; (do
;   (clojure.core/defn simple ([&form &env a] (str a)))
;   (. (var simple) (setMacro))
;   (var simple))

#_{:clj-kondo/ignore [:redefined-var]}
(defmacro when
  "Evaluates test. If logical true, evaluates
  body in an implicit do."
  {:added "1.0"}
  [test & body]
  (list 'if test (cons 'do body)))

(macroexpand-1 '(when (= 1 2) (println "foo")))

; (if (= 1 2)
;   (do (println "foo")))

(when (= 1 2)
  (println "foo"))
; nil

(when (= 1 1)
  (println "foo"))
; (out) foo
; nil

;; ;;;;;;;;;;;;;;;
;; Network Service
;; ;;;;;;;;;;;;;;;

;; just sleep for some time
(defn backoff! [num-attempts timeout]
  (-> num-attempts
      (inc)
      (rand-int)
      (* timeout)
      (Thread/sleep)))

;; a frequently failing service; throws an exception when failing
(defn frequently-failing! []
  (when-not (-> (range 30)
                (rand-nth)
                (zero?)) ; only succeeds 1/30
    (throw (Exception. "Fake IO Exception"))))

;; typical `with`-macro performing some logging or cleanup logic
(defmacro with-backoff!
  [{:keys [timeout max-attempts warning-after] :or {timeout 100}} & body]
  `(letfn [(warn# [level# n#]       ; syntax quote & automatic gensyms
             (binding [*out* *err*] ; redirect *out* to *err*
               (println
                (format "-%s: expression %s failed %s times" (name level#) '(do ~@body) n#))))]
     (loop [attempt# 1]
       (when (not= :success (try
                              ~@body
                              :success
                              (catch Exception e#)))
         (when (= ~warning-after attempt#)
           (warn# :WARN attempt#))        ; issue a warning
         (if (not= ~max-attempts attempt#)
           (do
             (backoff! attempt# ~timeout) ; just wait
             (recur (inc attempt#)))      ; and try again
           (warn# :ERR attempt#))))))     ; too many failed attempts, bailing out

(comment
  (with-backoff!
    {:timeout 10
     :max-attempts 50
     :warning-after 15}
    (frequently-failing!)))
  ; (err) -WARN: expression (do (frequently-failing!)) failed 15 times
  ; (err) -ERR: expression (do (frequently-failing!)) failed 50 times

;; Syntax Quoting
;; 1. auto-qualification (with the proper namespace)
`s/upper-case
; clojure.string/upper-case

`lower-case
; clojure.string/lower-case

`foo
; user/foo

;; Syntax Quoting
;; 2. auto-gensym (gensym, #)
`(let [x# 1] x#)
; (clojure.core/let [x__4237__auto__ 1] x__4237__auto__)

;; ~' (the tilde-single-quote pattern for producing an UNQUALIFIED symbol)
;; highly discouraged(!!!)
`[foo foo# ~'foo]
; [user/foo foo__45__auto__ foo]

;; Syntax Quoting
;; 3. unquote (~) -> turns on evaluation inside syntax quoting
`[1 2 (+ 1 2)  ~(+ 1 2)]
; [1 2 (clojure.core/+ 1 2) 3]

;; Syntax Quoting
;; 4. unquote-splicing (~@) for collections
`[1 2 [3 4] ~[3 4] ~@[3 4] [3 (+ 1 2)] ~@[3 (+ 1 2)]]
; [1 2 [3 4] [3 4] 3 4 [3 (clojure.core/+ 1 2)] 3 3]

`[~@[1 2]]
; [1 2]

(comment
  [~@[1 2]]
  ; (err) Execution error (IllegalStateException)
  ; (err) Attempting to call unbound fn: #'clojure.core/unquote-splicing

  `[~@:foo])
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Don't know how to create ISeq from: clojure.lang.Keyword

  ;; `~@[1])
  ; IllegalStateException splice not in list clojure.lang.LispReader$SyntaxQuoteReader.syntaxQuote

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The &form and &env implicit arguments (rarelyl used)
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;
;; &form
;; ;;;;;

(defmacro just-print-me [& _args]
  (println &form))

(macroexpand
 '(defmacro just-print-me [& _args]
    (println &form)))

; (do
;  (clojure.core/defn just-print-me ([&form &env & _args] (println &form)))
;  (. #'just-print-me (setMacro))
;  #'just-print-me)

(comment
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (just-print-me foo :bar 123))
  ; (out) (just-print-me foo :bar 123)
  ; nil

#_{:clj-kondo/ignore [:redefined-var]}
;; without `&forms` (but doesn't keep meta information)
(defmacro just-print-me [& args]
  (println (apply list 'just-print-me args)))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(just-print-me foo :bar 123)
; (out) (just-print-me foo :bar 123)
; nil

;; ;;;;
;; &env
;; ;;;;

(defmacro with-locals-to-string [& body]
  (let [locals (vec (keys &env))]
    `(let [~locals (mapv str ~locals)] ; destructuring locals
       ~@body)))

(let [a 1
      b [:foo :bar]]
  (with-locals-to-string [a b]))
; ["1" "[:foo :bar]"]

;; Unfortunately it’s not possible to inspect a macro that uses &env 
;; using macroexpand-1 and preserving the lexical context.
(macroexpand-1
 '(let [a 1
        b [:foo :bar]]
    (with-locals-to-string [a b])))

;; (let* [a 1
;;        b [:foo :bar]]
;;       (with-locals-to-string [a b]))

#_{:clj-kondo/ignore [:redundant-let]}
(let [a 1
      b [:foo :bar]]
  (let [[a b] (mapv str [a b])]
    [a b]))
; ["1" "[:foo :bar]"]

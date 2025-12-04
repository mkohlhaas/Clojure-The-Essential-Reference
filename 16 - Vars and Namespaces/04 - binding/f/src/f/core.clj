(ns f.core
  (:require
   [clojure.pprint]
   [clojure.string]))

;; `binding` creates a context in which vars can be assigned a thread-local value, leaving the root binding untouched

;; sets *ns* to the namespace `f.core` (creating it if needed)
(in-ns 'f.core)

;; Dynamic vars can be used to share simple state between calls in the same thread without necessarily passing the same parameter to all functions.

#_{:clj-kondo/ignore [:uninitialized-var]}
;; works in coordination with `binding`
(def ^:dynamic *trace*)

(defmacro trace! [msg & body]
  `(do
     (when (thread-bound? #'*trace*) ; tracing is subject to the presence of a thread-local value in *trace* which happens only if *trace* appears in a binding context
       (set! *trace* (conj *trace* ~msg)))
     ~@body))

(defn params [query]
  (let [pairs (clojure.string/split query #"&")]
    (trace! (format "Handling params %s" pairs)
            (->> pairs
                 (map #(clojure.string/split % #"="))
                 (map #(apply hash-map %))
                 (apply merge)))))

(comment
  (params "op=+&arg1=1&arg2=2")) ; {"arg1" "1", "arg2" "2", "op" "+"}

(defn handle-request [{:strs [op arg1 arg2]}]
  (let [op (resolve (symbol op))
        x  (Integer. arg1)
        y  (Integer. arg2)]
    (trace!
     (format "Handling request %s %s %s" op x y)
     (op x y))))

; tracing is enabled:
; the presence of the binding signals the need for tracing the request
(binding [*trace* []] ; messages are shared through the *trace* dynamic var without requiring any special synchronization apart from the enclosing binding form
  (let [query "op=+&arg1=1&arg2=2"
        res   (handle-request (params query))]
    (clojure.pprint/pprint *trace*)
    res))

;; ["Handling params [\"op=+\" \"arg1=1\" \"arg2=2\"]"
;;  "Handling request #'clojure.core/+ 1 2"]
;; 3

;; ;;;;;;;;
;; bound-fn
;; ;;;;;;;;

;; lower-level functions:
;; - bound-fn
;; - with-binding
;; - with-binding*
;; - push-thread-bindings
;; - pop-thread-bindings
;; - bound-fn*

#_{:clj-kondo/ignore [:uninitialized-var]}
(def ^:dynamic *debug*)

(defn debug [msg]
  (when (and (thread-bound? #'*debug*) *debug*)
    (println "Debugging..." msg)))

;; no output
(binding [*debug* true]
  (.start (Thread. #(debug "from a thread.")))) ; new inner thread doesn't see the local bindings from the outer thread
; nil

;; output with `bound-fn`
(binding [*debug* true]
  (.start (Thread. (bound-fn* #(debug "from a thread."))))) ; pass to the new thread with `bound-fn`
; (out) Debugging... from a thread.
; nil

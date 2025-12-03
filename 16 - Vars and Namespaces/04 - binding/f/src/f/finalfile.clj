;; 1.clj

(in-ns 'user)

(def ^:dynamic *trace*)

(defmacro trace! [msg & body]
  `(do
     (when (thread-bound? #'*trace*)
       (set! *trace* (conj *trace* ~msg)))
     ~@body))

(defn params [query]
  (let [pairs (clojure.string/split query #"&")]
    (trace! (format "Handling params %s" pairs)
            (->> pairs
                 (map #(clojure.string/split % #"="))
                 (map #(apply hash-map %))
                 (apply merge)))))

(defn handle-request [{:strs [op arg1 arg2]}]
  (let [op (resolve (symbol op))
        x (Integer. arg1)
        y (Integer. arg2)]
    (trace!
     (format "Handling request %s %s %s" op x y)
     (op x y))))

(binding [*trace* []]
  (let [query "op=+&arg1=1&arg2=2"
        res (handle-request (params query))]
    (pprint *trace*)
    res))

;; ["Handling params [\"op=+\" \"arg1=1\" \"arg2=2\"]"
;;  "Handling request #'clojure.core/+ 1 2"]
;; 3

;; 2.clj

(def ^:dynamic *debug*)

(defn debug [msg]
  (when (and (thread-bound? #'*debug*) *debug*)
    (println "Debugging..." msg)))

(binding [*debug* true]
  (.start (Thread. #(debug "from a thread."))))
;; nil

(binding [*debug* true]
  (.start (Thread. (bound-fn* #(debug "from a thread.")))))
;; Debugging... from a thread.
;; nil


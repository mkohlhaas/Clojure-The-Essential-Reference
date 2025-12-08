(ns f.core
  (:require
   [clojure.core.server :as server]
   [clojure.main :as main]
   [clojure.repl :refer [doc]])
  (:import
   [java.io BufferedWriter StringWriter]))

;; NOTE: this works mainly in the REPL

;; clojure.core.server contains functions to expose the Clojure environment through a socket connection and across network boundaries.

(comment
  (doc server/start-server)
  ; (out) -------------------------
  ; (out) clojure.core.server/start-server
  ; (out) ([opts])
  ; (out)   Start a socket server given the specified opts:
  ; (out)     :address Host or address, string, defaults to loopback address
  ; (out)     :port          Port, integer, required
  ; (out)     :name          Name, required
  ; (out)     :accept        Namespaced symbol of the accept function to invoke, required
  ; (out)     :args          Vector of args to pass to accept function
  ; (out)     :bind-err      Bind *err* to socket out stream?, defaults to true
  ; (out)     :server-daemon Is server thread a daemon?, defaults to true
  ; (out)     :client-daemon Are client threads daemons?, defaults to true
  ; (out)    Returns server socket.

  (server/start-server
   {:name "repl1"
    :port 8787
    :accept clojure.core.server/repl})
  ; #object["ServerSocket[addr=localhost/127.0.0.1,localport=8787]"]

  ;; start a new REPL from the existing REPL
  (clojure.core.server/repl)

  :repl/quit)

;; Custom :accept Function from the Replicant Library ;;

(defn data-eval [form]
  (let [out-writer (StringWriter.)
        err-writer (StringWriter.)
        capture-streams (fn []
                          (.flush *out*)
                          (.flush *err*)
                          {:out (.toString out-writer)
                           :err (.toString err-writer)})]
    (binding [*out* (BufferedWriter. out-writer)
              *err* (BufferedWriter. err-writer)]
      (try
        (let [result (eval form)]
          (merge (capture-streams) {:result result}))
        (catch Throwable t
          (merge (capture-streams) {:exception (Throwable->map t)}))))))

(defn data-repl [& kw-opts]
  (println kw-opts)
  (apply main/repl
         (conj kw-opts
               :need-prompt (constantly false)
               :prompt (constantly nil)
               :eval data-eval)))

(comment
  (server/start-server
   {:name   "repl2"
    :port    8788
    :accept 'f.core/data-repl}))

;; $ telnet localhost 8788
;; Trying 127.0.0.1...
;; Connected to localhost.
;; Escape character is '^]'.
;; nil
;; clojure.core=> (+ 1 1)
;; 2

(comment
  (server/stop-server "repl2") ; true

  (server/stop-servers)) ; nil

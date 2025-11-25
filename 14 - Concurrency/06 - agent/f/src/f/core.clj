(ns f.core
  (:require
   [clojure.string :refer [split]])
  (:import
   [java.util.concurrent Executors]))

;; this section also mentions other related functions:
;; - send
;; - send-off
;; - send-via
;; - set-agent-send-executor!
;; - set-agent-send-off-executor!
;; - restart-agent
;; - shutdown-agents
;; - release-pending-sends
;; - await
;; - await-for
;; - agent-error
;; - set-error-handler!
;; - error-handler
;; - error-mode
;; - set-error-mode!

;; An agent is asynchronous, uncoordinated and sequential.
;; An agent is similar to an Erlang actor.

;; ;;;;
;; send
;; ;;;;

; agent options:
; :meta          metadata-map
; :validator     validate-fn
; :error-handler handler-fn
; :error-mode    mode-keyword (:fail or :continue)

(def a (agent 0)) ; #<Agent@150e02d: 0>
(type a)          ; clojure.lang.Agent
(send a inc)      ; #<Agent@4d5a323c: 1>
(deref a)         ; 1
@(send a inc)     ; 1 (dereferncing was to quick!)
a                 ; #<Agent@4d5a323c: 2>
@a                ; 2

;; ;;;;;
;; await
;; ;;;;;

(def a1 (agent 10000))
(def b1 (agent 10000))

(defn slow-update [x]
  (Thread/sleep x)
  (inc x))

(send a1 slow-update)
(send b1 slow-update)

(time (await a1 b1)) ; (out) "Elapsed time: 4773.791032 msecs" (the faster the evaluation, the closer the elapsed is to 10 seconds)
(time (await a1 b1)) ; (out) "Elapsed time:    0.2251   msecs"
@a1                  ; 10001
@b1                  ; 10001

;; ;;;;;;;;;
;; await-for
;; ;;;;;;;;;

(send a1 slow-update)
(time (await-for 2000 a1)) ; false
; (out) "Elapsed time: 2000.691859 msecs"

;; ;;;;;;;;
;; send-off
;; ;;;;;;;;

;; no example
;; `send`     executes on a fixed size thread pool (number of cores + 2 threads) 
;; `send-off` uses an unbounded thread pool

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; Controlling Thread Pools
;; ;;;;;;;;;;;;;;;;;;;;;;;;

;; ;;;;;;;;
;; send-via
;; ;;;;;;;;

;; `send-via` allows to use a custom thread pool

;; custom thread pool - a fork-join pool
(def fj-pool (Executors/newWorkStealingPool))

(defn send-fj [^clojure.lang.Agent a f & args]
  (apply send-via fj-pool a f args))

(def a2 (agent 1)) ; #<Agent@3fdf9e6a: 1>
(send-fj a2 inc)   ; #<Agent@29cb5937: 2>
(await a2)         ; nil
@a2                ; 2

;; custom fork-join pool with 100 concurrent workers (not threads)
(def fj-pool1 (Executors/newWorkStealingPool 100))

;; `send` and `send-off` are going to use the newly created thread pool
(set-agent-send-executor!     fj-pool1) ; #object[java.util.concurrent.ForkJoinPool 0x323549f9 "java.util.concurrent.ForkJoinPool@323549f9[Running, parallelism = 100, size = 1, active = 0, running = 0, steals = 1, tasks = 0, submissions = 0]"]
(set-agent-send-off-executor! fj-pool1) ; #object[java.util.concurrent.ForkJoinPool 0x323549f9 "java.util.concurrent.ForkJoinPool@323549f9[Running, parallelism = 100, size = 0, active = 0, running = 0, steals = 0, tasks = 0, submissions = 0]"]

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The *agent* Dynamic Variable
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def a3 (agent 10)) ; #<Agent@6c6aae9a: 10>

;; the *agent* dynamic variable is set to the current agent instance inside the body of the updating function
(send a3 #(do (println (identical? *agent* a3))
              (inc %))) ; #<Agent@4e207140: 10>
; (out) true

;; ping agent

(def a4 (agent {}))

(defn ping [{:keys [enable url kill] :as m}]
  (when (and enable url)
    (try
      (slurp url)
      (println "alive!")
      (catch Exception e
        (println "dead!" (.getMessage e)))))
  (Thread/sleep 1000)
  (when-not kill (send-off *agent* ping)) ; recursion (dispatch message to itself)
  m)

(comment
  (slurp "https://google.com")   ; "<!doctype html><html itemscope=\"\" itemtype=\"http://schema.org/WebPage\" …>"
  (slurp "http://nowhere.nope")) ; throws UnknownHostException
  ; (err) Execution error (UnknownHostException) at sun.nio.ch.NioSocketImpl/connect (NioSocketImpl.java:569).
  ; (err) nowhere.nope

(send-off a4 ping)                            ; #<Agent@44241dcb: {}>
(send-off a4 assoc :url "https://google.com") ; #<Agent@44241dcb: {}>
a4                                            ; #<Agent@44241dcb: {:url "https://google.com"}>
(send-off a4 assoc :enable true)
; (out) alive!
; (out) alive!
; (out) alive!
;; …

(send-off a4 assoc :url "http://nowhere.nope")
; (out) dead! nowhere.nope
; (out) dead! nowhere.nope
; (out) dead! nowhere.nope
;; …

(send-off a4 assoc :kill true) ; #<Agent@44241dcb: {:url "http://nowhere.nope", :enable true, :kill true}>

;; ;;;;;;;;;;;;;;;;;;;;;
;; release-pending-sends
;; ;;;;;;;;;;;;;;;;;;;;;

;; In case of additional dispatches from within the update function, the default behavior of the agent is to wait 
;; until the state has changed before proceeding sequentially with all created actions. 

(def alpha       (mapv agent (repeat 26 0))) ; [#<Agent@6e8885cf: 0> #<Agent@66f155ad: 0> #<Agent@4c7d0241: 0> …]
(def non-letters (agent 0))                  ; #<Agent@54f49f5b: 0>
(def words       (agent {}))                 ; #<Agent@56e0296: {}>

(def war-and-peace "https://tinyurl.com/wandpeace")
(def book (slurp war-and-peace))

(send-off words
          (fn [state]
            (doseq [letter book
                    :let [l   (Character/toLowerCase letter)
                          idx (- (int l) (int \a))]]
              (send (get alpha idx non-letters) inc))
            (release-pending-sends) ; start collecting letters; don't wait for `frequencies` (an expensive operation) to finish
            (merge-with + state (frequencies (split book #"\s+")))))

words ; {"refraining" 3, "account.”" 1, "sacrifice," 1, "merry." 2, "shouted," 31, …}
(apply await alpha)
(map deref alpha)
;; (67160 11446 21721 38720 105442 19543 16238 55699 57439 862 6044 31540 20725 61230 63890 15291 814 48946 55070 77425 21345 8664 19390 1446 15350 832)

(count '(67160 11446 21721 38720 105442 19543 16238 55699 57439 862 6044 31540 20725 61230 63890 15291 814 48946 55070 77425 21345 8664 19390 1446 15350 832))
; 26

;; ;;;;;;;;;;;;;;;
;; Handling Errors
;; ;;;;;;;;;;;;;;;

(def a5 (agent 2)) ; #<Agent@531eb01f: 2>

(send-off a5 #(/ % 0))  ; #<Agent@71d8472b FAILED: 2>

(agent-error a5)
; #error {
;  :cause "Divide by zero"
;  :via
;  [{:type java.lang.ArithmeticException
;    :message "Divide by zero"
;    :at [clojure.lang.Numbers divide "Numbers.java" 190]}]
;  :trace
;  [[clojure.lang.Numbers divide "Numbers.java" 190]
;   [clojure.lang.Numbers divide "Numbers.java" 3895]
;   …
;   [java.util.concurrent.ForkJoinWorkerThread run "ForkJoinWorkerThread.java" 187]]}

;; ;;;;;;;;;;;;;
;; restart-agent
;; ;;;;;;;;;;;;;

;; with option `:clear-actions true` we could discard any pending work
(restart-agent a5 2) ; 2

(comment
  (restart-agent a5 2))
  ; (err) Agent does not need a restart

(send-off a5 #(/ % 2)) ; #<Agent@71d8472b: 1>
@a5                    ; 1

(comment
  (restart-agent a5 2))
  ; (err) Agent does not need a restart

;; Instead of checking the error state of an agent, we can specify an error handler with `set-error-handler!`

(def a6 (agent 2))

(defn handle-error [agent error]
  (println "Error was" (.getMessage error))
  (println "The agent has value" @agent)
  (restart-agent agent 2))

(set-error-handler! a6 handle-error)
(send-off a6 #(/ % 0))
; (out) Error was Divide by zero
; (out) The agent has value 2

(agent-error a6)
; #error {
;  :cause "Divide by zero"
;  :via
;  [{:type java.lang.ArithmeticException
;    :message "Divide by zero"
;    :at [clojure.lang.Numbers divide "Numbers.java" 190]}]
;  :trace
;  [[clojure.lang.Numbers divide "Numbers.java" 190]
;   [clojure.lang.Numbers divide "Numbers.java" 3895]
;   …
;   [java.util.concurrent.ForkJoinWorkerThread run "ForkJoinWorkerThread.java" 187]]}

@a6 ; 2

;; If all error conditions are considered recoverable and we can always accept to resume 
;; working after an error, we can set the :continue error mode on the agent, completely 
;; ignoring the problem (and not requiring a restart-agent call):
(def a7 (agent 2))

(set-error-mode! a7 :continue)
(send-off a7 #(/ % 0))
@a7 ; 2

(ns f.core)

;; `volatile!` is more about side effects than concurrency as there is no thread-safety.
;; `volatile!` is useful in a few specific scenarios where state changes should be immediately visible to other threads.
;; The main reason for the introduction of `volatile!` are stateful transducers and their use in concurrent environments like core.async.

(def v (volatile! 0)) ; #<Volatile@3c9c8e9c: 0>

(volatile? v)   ; true

(vswap!  v inc) ; 1
(vreset! v 0)   ; 0

;; `volatile!` could be used to solve some thread coordination problems ;;

(def ready  (volatile! false)) ; #<Volatile@4c043373: false>
(def result (volatile! nil))   ; #<Volatile@3ab96f3c: :done>

(defn consumer []
  (future
    (while (not @ready) ; waiting for the result from the producer
      (Thread/yield))
    (println "Consumer got the result:" @result)))

(defn producer []
  (future
    (vreset! result  42)
    (vreset! ready   true)))

(consumer) ; #<Future@46d3bd62: :pending>
(producer) ; #<Future@a499a5c:  :done>
; (out) Consumer got the result: 42

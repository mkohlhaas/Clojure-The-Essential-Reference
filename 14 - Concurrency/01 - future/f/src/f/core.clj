(ns f.core
  (:require
   [clojure.xml :as xml]))

;; `future` takes one or more expressions as input and evaluates them asynchronously in another thread
;; other future functions in this section: search for future-

;; does not block
(defn timer [seconds]
  (future
    (Thread/sleep (* 1000 seconds))
    (println "done" seconds "seconds.")))

(timer 2) ; #<Future@22c425a8: :pending>
; (out) done 2 seconds.

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; future-done? and future?
;; ;;;;;;;;;;;;;;;;;;;;;;;;

(def t2 (timer 2))
(future? t2)       ; true
(future-done? t2)  ; false
; (out) done 2 seconds.
(future-done? t2) ; true

;; deref blocks and caches result
(def two-seconds (future (Thread/sleep 2000) (println "done") (+ 1 1)))
(realized? two-seconds) ; false
(deref     two-seconds) ; 2 (after 2 seconds, blocks)
(realized? two-seconds) ; true
(deref     two-seconds) ; 2 (cached; returns immediately)
@two-seconds            ; 2

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; future-cancel and future-cancelled?
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def an-hour (timer (* 60 60)))
(future-cancelled? an-hour) ; false
(future-cancel     an-hour) ; true
(future-cancelled? an-hour) ; true
(future-cancel     an-hour) ; false

;; ;;;;;;;;;;;
;; future-call
;; ;;;;;;;;;;;

;; `future-call` is the lower level function used by the `future` macro to create a future

(comment
  ;; `future` is a macro
  ;; you can’t pass macros to (higher-order) functions
  (mapv future [:f1 :f2]))
  ; (err) Syntax error compiling at (src/f/core.clj:33:1).
  ; (err) Can't take value of a macro: #'clojure.core/future

;; (defmacro future
;;   "Takes a body of expressions and yields a future object that will
;;   invoke the body in another thread, and will cache the result and
;;   return it on all subsequent calls to deref/@. If the computation has
;;   not yet finished, calls to deref/@ will block, unless the variant of
;;   deref with timeout is used. See also - realized?."
;;   {:added "1.1"}
;;   [& body] `(future-call (^{:once true} fn* [] ~@body)))

(mapv future-call [(^:once fn* [] :f1)    ; ^:once = ^{:once true}
                   (^:once fn* [] :f2)])
; [#<Future@487b22: :f1> #<Future@620a8393: :f2>]

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; Future’s Locals Clearing
;; ;;;;;;;;;;;;;;;;;;;;;;;;

;; If you decide to use `future-call` directly, it is advisable you use the once-only semantic to avoid potential out of memory issues!

(let [s "yes"
      f1 (^{:once true}  fn* [] (str "local-var: " (or s "no")))
      f2 (^{:once false} fn* [] (str "local-var: " (or s "no")))]
  [(f1) (f1) (f2) (f2)])
;; ["local-var: yes"
;;  "local-var: no"
;;  "local-var: yes"
;;  "local-var: yes"]

;; returns immediately (doesn't block)
(defn fetch-async [url]
  (future (doall (xml-seq (xml/parse url)))))

;; all requests start in parallel and results are available when the slowest request completes
(let [guardian (fetch-async "https://git.io/guardian-world-rss-xml")
      nytimes  (fetch-async "https://git.io/nyt-world-rss-xml")
      reuters  (fetch-async "https://git.io/reuters-rss-xml")
      washpost (fetch-async "https://git.io/washpost-rss-xml")
      wsj      (fetch-async "https://git.io/wsj-rss-xml")]
  (count (concat
          (take 10 @guardian)
          (take 5 (drop 15 @nytimes))
          (take 5 (drop 20 @wsj))
          (take 2 (drop 5 @washpost))
          (take 10 @reuters))))
;; 32

(comment
  (def guardian (fetch-async "https://git.io/guardian-world-rss-xml"))
  (count @guardian)) ; 1451

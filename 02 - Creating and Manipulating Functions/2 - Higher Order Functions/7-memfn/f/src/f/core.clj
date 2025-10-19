(ns f.core
  (:import
   [java.time Duration Instant]))

;; `memfn` allows Java instance methods to be passed as arguments to Clojure functions.
;; A static method does not require the use of `memfn`.

(map (memfn toUpperCase) ["keep" "calm" "and" "drink" "tea"])
; ("KEEP" "CALM" "AND" "DRINK" "TEA")

(comment
  (map toUpperCase ["keep" "calm" "and" "drink" "tea"])
  ; (err) Unable to resolve symbol: toUpperCase in this context

  (map .toUpperCase ["keep" "calm" "and" "drink" "tea"]))
  ; (err) Unable to resolve symbol: .toUpperCase in this context

;; ;;;;;;;;;;;;;
;; Time Examples
;; ;;;;;;;;;;;;;

;; lazy infinite sequence of instants
(def instants
  (repeatedly (fn []
                ; https://ask.clojure.org/index.php/12853/built-in-function-for-thread-sleep-to-avoid-type-hints
                (Thread/sleep (long (rand-int 500)))
                (Instant/now))))

(defn durations [instants & [t0]]
  (let [start (or t0 (Instant/now))]
    (->> instants
         (map #(Duration/between % start))
         (map (memfn toMillis)))))

(let [two (doall (take 2 instants))] ; `doall` forces lazy sequence to realize
  (durations two))
; (1377 1082)

(let [t1    (Instant/now)
      times (doall (take 2 instants))]
  (Thread/sleep 200)
  (first (durations times t1)))
; 123411

;; The body of the function uses the Java interop syntax (single-dot) to invoke an instance method on the given argument.
(macroexpand '(memfn toMillis))
; (fn* ([target4323] (. target4323 (toMillis))))

(map (memfn indexOf ch) ["abba" "trailer" "dakar"] ["a" "a" "a"])
; (0 2 1)

(comment
  (map (memfn indexOf "a") ["abba" "trailer" "dakar"])
  ;; CompilerException java.lang.Exception: Unsupported binding form: a

  ;; The macro expansion shows that the string "a" is used as a local binding in a let form.
  ;; This is the reason why all arguments after the first need to be valid symbols (at compile-time)
  ;; and valid for the method signature (at run-time).
  (macroexpand '(memfn indexOf "a"))
  ;; (fn* ([target12358 p__12359]
  ;;   (clojure.core/let ["a" p__12359] (. target12358 (indexOf "a")))))

  (defmacro memfn [name & args]
    (let [t (with-meta (gensym "target") (meta name))]
      `(fn [~t ~@args]
         (. ~t (~name ~@args)))))

  (set! *warn-on-reflection* true)

  ;; no type hint
  (time (dotimes [n 100000] ; <1>
          (map (memfn toLowerCase) ["A" "B"])))
  ; (err) Reflection warning, /home/schmidh/GitRepos/Clojure-The-Essential-Reference/02-CreatingAndManipulatingFunctions/2-HigherOrderFunctions/07-memfn/f/src/f/core.clj:64:14 - call to method toLowerCase can't be resolved (target class is unknown).
  ; (out) "Elapsed time: 22.086286 msecs"

  ;; with type hint
  (time (dotimes [n 100000])) ; <1>
  (time (dotimes [n 100000]
          (map (memfn ^String toLowerCase) ["A" "B"]))))
  ; (out) "Elapsed time: 11.50199 msecs"

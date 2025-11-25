(ns f.core)

;; `locking`: protecting critical code sections

;; locking should not be abused and only appear in some rare interoperability scenarios with pre-existing Java objects
;; locking is a last resort

;; `monitor-enter` and `monitor-exit` are even lower level primitives and they have even less reasons be used explicitly

;; bank transfer with volatiles and locking ;;

(def lock (Object.))        ; #object[java.lang.Object 0x734411e2 "java.lang.Object@734411e2"] (any object reference would do)
(def acc1 (volatile! 1000)) ; #<Volatile@60ac9625: 1000>
(def acc2 (volatile!  300)) ; #<Volatile@43c4781f: 300>

(defn transfer [sum orig dest]
  (locking lock ; comment this line to see a difference
    (let [balance (- @orig sum)]
      (when (pos? balance)
        (vreset! orig balance)
        (vreset! dest (+ @dest sum))))
    [@orig @dest]))

(dotimes [_ 1500]
  (future (transfer 1 acc1 acc2)))

[@acc1 @acc2]
; [1 1299] (with    locking)
; [1 1323] (without locking)
; [1 1311] (without locking)
; … (always different)

;; marking critical section for lock protection with `monitor-enter` and `monitor-exit`
(let [v (volatile! 0)]
  (try
    (monitor-enter lock)     ; start of critical code section
    (vswap! v inc)
    (finally
      (monitor-exit lock)))) ; end of critical code section
; 1

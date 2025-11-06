(ns f.core
  (:require
   [clojure.pprint])
  (:import
   [java.util.concurrent LinkedBlockingQueue]))

(seque (range 10))  ; (0 1 2 3 4 5 6 7 8 9)

(defn fast-producer [n]
  (->> (into () (range n))
       (map #(do (println "produce" %) %))))

(defn slow-consumer [xs]
  (keep
   #(do
      (println "consume" %)
      (Thread/sleep 300))
   xs))

(slow-consumer (fast-producer 5))
; produce 4
; consume 4
; produce 3
; consume 3
; produce 2
; consume 2
; produce 1
; consume 1
; produce 0
; consume 0

(slow-consumer (seque (fast-producer 5)))
; (out) produce 4
; (out) produce 3
; (out) produce 2
; (out) produce 1
; (out) produce 0
; consume 4
; consume 3
; consume 2
; consume 1
; consume 0

(defn slow-producer [n]
  (->> (into () (range n))
       (map
        #(do
           (println "produce" %)
           (Thread/sleep 300) %))))

(defn fast-consumer [xs]
  (map #(do (println "consume" %) %) xs))

(first (fast-consumer (seque (slow-producer 5))))
; (out) produce 4
; (out) produce 3
; (out) produce 2
; (out) consume 4
; 4
; (out) produce 1
; (out) produce 0

(defn by-type [ext]
  (fn [^String fname]
    (.endsWith fname ext)))

(defn lazy-scan []
  (->> (java.io.File. "/")
       file-seq
       (map (memfn getPath))
       (filter (by-type ".txt"))
       (seque 50)))

(defn go []
  (loop [results (partition 5 (lazy-scan))]
    (println (with-out-str (clojure.pprint/write (first results))))
    (println "more?")
    (when (= "y" (read-line))
      (recur (rest results)))))

(go)

;; ("/usr/local/Homebrew/docs/robots.txt"
;;  "/usr/local/Homebrew/LICENSE.txt"
;;  "/usr/local/var/homebrew/linked/z3/todo.txt"
;;  "/usr/local/var/homebrew/linked/z3/LICENSE.txt"
;;  "/usr/local/var/homebrew/linked/z3/share/z3/examples/c++/CMakeLists.txt")
;; more?

(def q (LinkedBlockingQueue. 2000))

(defn counter []
  (let [out *out*]
    (future
      (binding [*out* out]
        (dotimes [n 50]
          (Thread/sleep 1000)
          (println "buffer" (.size q)))))))

#_{:clj-kondo/ignore [:redefined-var]}
(defn lazy-scan []
  (->> (java.io.File. "/")
       file-seq
       (map (memfn getPath))
       (filter (by-type ".txt"))
       (seque q)))

(counter)
;; #object[clojure.core$future_call$reify__8454 0x4b672daa {:status :pending, :val nil}]
;; buffer 0
;; buffer 0
;; buffer 0

(go)

;; ("/usr/local/Homebrew/docs/robots.txt" ; <5>
;;  "/usr/local/Homebrew/LICENSE.txt"
;;  "/usr/local/var/homebrew/linked/z3/todo.txt"
;;  "/usr/local/var/homebrew/linked/z3/LICENSE.txt"
;;  "/usr/local/var/homebrew/linked/z3/share/z3/examples/c++/CMakeLists.txt")
;; more?

;; buffer 544 ; <6>
;; buffer 745
;; buffer 745
;; buffer 749
;; buffer 749
;; ...
;; buffer 2000
;; buffer 2000
;; ...

#_{:clj-kondo/ignore [:redefined-var]}
(def q (LinkedBlockingQueue. 5))

(def sentinel (Object.))

(defmacro start [& body]
  `(let [out# *out*]
     (future
       (binding [*out* out#] ~@body))))

(defn producer [^LinkedBlockingQueue q items]
  (start
   (loop [[x & xs :as items] items]
     (Thread/sleep 1000)
     (let [x (or x sentinel)]
       (println "adding" x)
       (if (.offer q x)
         (when-not (identical? x sentinel) (recur xs))
         (recur (or items sentinel)))))))

(defn seque2 [^LinkedBlockingQueue q]
  (lazy-seq
   (let [x (.take q)]
     (cons
      (if (identical? x sentinel) nil x)
      (seque2 q)))))

(defn consumer [q]
  @(start
    (map
     #(do
        (println "consume" %)
        (Thread/sleep 300))
     (seque2 q))))

(producer q (range 8))
;; adding 0
;; adding 1
;; adding 2
;; adding 3
;; adding 4
;; adding 5
;; adding 5
;; adding 5

(take 3 (consumer q))
;; consume 0
;; adding 5
;; consume 1
;; adding 6
;; consume 2
;; adding 7

(take 5 (consumer q))
;; consume 3
;; consume 4
;; consume 5
;; consume 6
;; consume 7

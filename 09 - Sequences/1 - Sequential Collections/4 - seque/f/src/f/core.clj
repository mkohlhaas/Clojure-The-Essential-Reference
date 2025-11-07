(ns f.core
  (:require
   [clojure.pprint])
  (:import
   [java.util.concurrent LinkedBlockingQueue]))

;; `seque` (pronounced "seek") creates an in-memory queue on top of a producer sequence.
;; The name is indeed a mnemonic for "sequence on a queue".
;; Default size of queue is 100.

;; `seque` is useful to coordinate consumers operating at different speed than producers.

(seque (range 10))  ; (0 1 2 3 4 5 6 7 8 9)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn fast-producer [n]
  (->> (into () (range n))
       (map #(do (println "produce" %) %))))

(comment
  (into () (range 10))) ; (9 8 7 6 5 4 3 2 1 0)

(defn slow-consumer [xs]
  (keep ; like `map` but doesn't produce `nil` output
   #(do
      (println "consume" %)
      (Thread/sleep 300))
   xs))

(comment
  (slow-consumer (fast-producer 5)))
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

(comment
  ;; now with a queue in between producer and consumer
  (slow-consumer (seque (fast-producer 5))))
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

;; opposite roles: slow producer, fast consumer
(defn slow-producer [n]
  (->> (into () (range n))
       (map
        #(do
           (println "produce" %)
           (Thread/sleep 300) %))))

(defn fast-consumer [xs]
  (map #(do (println "consume" %) %) xs))

(comment
  (fast-consumer (slow-producer 5))
  ; produce 4
  ; consume 4
  ; (4produce 3
  ; consume 3
  ;  3produce 2
  ; consume 2
  ;  2produce 1
  ; consume 1
  ;  1produce 0
  ; consume 0
  ;  0)

  (fast-consumer (seque (slow-producer 5)))
  ; (out) produce 4
  ; (out) produce 3
  ; (out) produce 2
  ; (out) produce 1
  ; (out) produce 0
  ; consume 4
  ; (4consume 3
  ;  3consume 2
  ;  2consume 1
  ;  1consume 0
  ;  0

  ;; queue in between
  (first (fast-consumer (seque (slow-producer 5)))))
  ; (out) produce 4
  ; (out) produce 3
  ; (out) produce 2
  ; (out) consume 4
  ; 4
  ; (out) produce 1
  ; (out) produce 0

;; ;;;;;;;;;;;;;;;;;;;;;
;; Look-Ahead Pagination
;; ;;;;;;;;;;;;;;;;;;;;;

(defn by-type [ext]
  (fn [^String fname]
    (.endsWith fname ext)))

(defn lazy-file-scan []
  (->> (java.io.File. "/")
       file-seq
       (map (memfn getPath))
       (filter (by-type ".txt"))
       (seque 50)))

(defn paginate []
  (loop [results (partition 5 (lazy-file-scan))]
    (println (with-out-str (clojure.pprint/write (first results))))
    (println "more?")
    (when (= "y" (read-line))
      (recur (rest results)))))

(comment
  (paginate))

;; ("/usr/local/Homebrew/docs/robots.txt"
;;  "/usr/local/Homebrew/LICENSE.txt"
;;  "/usr/local/var/homebrew/linked/z3/todo.txt"
;;  "/usr/local/var/homebrew/linked/z3/LICENSE.txt"
;;  "/usr/local/var/homebrew/linked/z3/share/z3/examples/c++/CMakeLists.txt")
;; more?

;; `seque` also allows to use a custom queue
(def q (LinkedBlockingQueue. 2000))

;; keeps printing the buffer size for 50 seconds
(defn counter []
  (let [out *out*]
    (future
      (binding [*out* out]
        (dotimes [_n 50]
          (Thread/sleep 1000)
          (println "buffer" (.size q)))))))

(defn lazy-file-scan-custom-queue []
  (->> (java.io.File. "/")
       file-seq
       (map (memfn getPath))
       (filter (by-type ".txt"))
       (seque q))) ; use custom queue

(defn paginate-custom-queue []
  (loop [results (partition 5 (lazy-file-scan-custom-queue))]
    (println (with-out-str (clojure.pprint/write (first results))))
    (println "more?")
    (when (= "y" (read-line))
      (recur (rest results)))))

(comment
  (counter))
  ; (out) buffer 0
  ; (out) buffer 11
  ; (out) buffer 6
  ; (out) buffer 1
  ; (out) buffer 0
  ; …

(comment
  ;; run from repl
  (paginate-custom-queue))
  ; ("/usr/local/Homebrew/docs/robots.txt" ; <5>
  ;  "/usr/local/Homebrew/LICENSE.txt"
  ;  "/usr/local/var/homebrew/linked/z3/todo.txt"
  ;  "/usr/local/var/homebrew/linked/z3/LICENSE.txt"
  ;  "/usr/local/var/homebrew/linked/z3/share/z3/examples/c++/CMakeLists.txt")
  ; more?

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A bit of history: `seque` original design
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; NOTE: Use core.async instead!

(def q-small (LinkedBlockingQueue. 5))

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

(comment
  (producer q-small (range 8))
;; adding 0
;; adding 1
;; adding 2
;; adding 3
;; adding 4
;; adding 5
;; adding 5
;; adding 5

  (take 3 (consumer q-small))
;; consume 0
;; adding 5
;; consume 1
;; adding 6
;; consume 2
;; adding 7

  (take 5 (consumer q-small)))
;; consume 3
;; consume 4
;; consume 5
;; consume 6
;; consume 7

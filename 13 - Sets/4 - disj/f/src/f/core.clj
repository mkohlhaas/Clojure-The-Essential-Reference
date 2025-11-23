(ns f.core
  (:require
   [clojure.java.io      :as io]
   [clojure.set          :refer [difference]]
   [criterium.core       :refer [quick-bench]])
  (:import
   [java.net ServerSocket]))

;; `disj` removes elements from a set

;; unsorted set
(disj #{8 4 1 6} 4 8) ; #{1 6}
(type #{8 4 1 6})     ; clojure.lang.PersistentHashSet
#{8 4 1 6}            ; #{1 4 6 8}

;; sorted set
(disj (sorted-set-by > 8 4 1 6) 4) ; #{8 6 1}
(type (sorted-set-by > 8 4 1 6))   ; clojure.lang.PersistentTreeSet
(sorted-set-by > 8 4 1 6)          ; #{8 6 4 1}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn valid? [alloweds values]
  (empty?
   (apply disj (set values) alloweds)))

(valid? [:a :b :c] [:c :c :d :a]) ; false
(valid? [:a :b :c] [:c :c    :a]) ; true

;; Echo Server

(def ports (atom #{})) ; #<Atom@4a1619a8: #{}>

(defn serve [port]
  (if (= @ports (swap! ports conj port))
    "Port already serving requests."
    (future
      (with-open [server (ServerSocket. port)
                  socket (.accept server)
                  writer (io/writer socket)]
        (.write writer (str (.readLine (io/reader socket)) "\n")) ; read from socket, add newline, write to socket
        (.flush writer))
      (swap! ports disj port))))

(comment
  (serve 5510)  ; starts a new server
  (serve 5511)) ; "Port already serving requests."

;; telnet session:
;; telnet localhost 5510
;; Connected to localhost.
;; Escape character is '^]'.
;; hello
;; hello
;; Connection closed by foreign host.

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; transient disj
(defn disj* [set & keys]
  (persistent!
   (reduce disj! (transient set) keys)))

(comment
  (disj* #{1 2 3} 1 2) ; #{3}
  (def s1 #{1 2 3})    ; #{1 3 2}
  (disj* s1 1 2)       ; #{3}
  s1)                  ; #{1 3 2}

(comment
  ;; dis
  (let [s  (set (range 1000))               ; (out) Execution time mean :  88.033217 µs
        xs (range 400 600)]
    (quick-bench (apply disj s xs)))

  ;; difference
  (let [s  (set (range 1000))               ; (out) Execution time mean : 118.846126 µs
        xs (range 400 600)]
    (quick-bench (difference s (set xs))))

  ;; difference
  (let [s  (set (range 1000))               ; (out) Execution time mean :  93.423708 µs
        xs (set (range 400 600))]
    (quick-bench (difference s xs)))

  ;; disj
  (let [s  (set (range 1000))                ; (out) Execution time mean :  33.368408 µs
        xs (range 400 600)]
    (quick-bench (apply disj* s xs))))

(ns f.core
  (:require [clojure.java.javadoc :as browse]))

;; conditional forms always return a value

;; ;;;;;;;;;;;;;;;;;;;;;
;; Introductory Examples
;; ;;;;;;;;;;;;;;;;;;;;;

(if true :a :b)
; :a

(defn toss []
  (if (> 0.5 (rand))
    :head
    :tail))

(take 5 (repeatedly toss))
; (:head :head :head :tail :tail)

(comment
  (dotimes [n 10]
    (let [num-tosses 1000000]
      (println n (->> (repeatedly toss)
                      (take num-tosses)
                      (filter #(= :head %))
                      count)))))
  ; (out) 0 500019
  ; (out) 1 500468
  ; (out) 2 500275
  ; (out) 3 500796
  ; (out) 4 500288
  ; (out) 5 499763
  ; (out) 6 500313
  ; (out) 7 500491
  ; (out) 8 500082
  ; (out) 9 500024

;; ;;;;
;; Tree
;; ;;;;

(def tree
  [:a 1 :b :c
   [:d
    [1 2 3 :a
     [1 2
      [1 2
       [3
        [4
         [0]]]]]
     [:z
      [1 2
       [1]]]
     8]]
   nil])

(defn walk [depth tree]
  (if-not (vector? tree)
    depth
    (map (partial walk (inc depth)) tree)))

(comment
  (walk 0 tree)
  ; (1 1 1 1 (2 (3 3 3 3 (4 4 (5 5 (6 (7 (8))))) (4 (5 5 (6))) 3)) 1)
  (flatten (walk 0 tree)))
  ; (1 1 1 1 2 3 3 3 3 4 4 5 5 6 7 8 4 5 5 6 3 1)

(defn depth [tree]
  (apply max (flatten (walk 0 tree))))

(depth tree)
; 8

;; ;;;;;;;;;;;;;
;; Socket Server
;; ;;;;;;;;;;;;;

(comment
  ; ServerSocket(int port, int backlog, InetAddress bindAddr)   
  ; Create a server with the specified port, listen backlog, and local IP address to bind to.
  (browse/javadoc java.net.ServerSocket)
  (browse/javadoc Exception))

(defn start []
  (try
    (java.net.ServerSocket. 9393 0 (java.net.InetAddress/getByName "localhost"))
    (catch Exception e
      (println "error starting the socket: " (.getMessage e)))))

(defn stop [s]
  (when s
    (try
      (.close s)
      (catch Exception e
        (println "error closing socket: ") (.getMessage e)))))

(def socket (start))

(.isClosed socket) ; false
(stop socket)      ; nil
(.isClosed socket) ; true

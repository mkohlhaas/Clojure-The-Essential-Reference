(ns f.core
  (:import
   [java.net InetAddress Socket]))

;; start a service listening on port 61817
;; $ sudo pacman -S openbsd-netcat 
;; $ nc -l 61817

;; `delay` can be used to defer evaluation at run-time
;; results are cached

;; also `delay?` and `force`

;; `delay` guarantees that the form is going to be evaluated only once
(def d (delay (println "evaluated")))
@d ; (out) evaluated
@d

;; ;;;;;;;;;;;;;;;;;;;
;; atom-based solution
;; ;;;;;;;;;;;;;;;;;;;

(def connection (atom nil))

(defn connect []
  (swap! connection
         (fn [conn]
           (or conn                                                              ; we already have a connection
               (let [socket (Socket. (InetAddress/getByName "localhost") 61817)] ; create a new connection
                 (print "Socket connected to 61817\n")
                 socket)))))

(defn handle-request [s]
  (let [_conn (connect)]
    (print (format "Doing something with %s\n" s))))

;; Oops! Creating three sockets!
;; (Only one is actually saved in the connection atom.)
(dotimes [i 3]
  (future (handle-request i)))
; (out) Socket connected to 61817
; (out) Socket connected to 61817
; (out) Doing something with 2
; (out) Doing something with 0
; (out) Socket connected to 61817

;; might be necessary
;; (flush)

;; ;;;;;;;;;;;;;;;;;;;;
;; delay-based solution
;; ;;;;;;;;;;;;;;;;;;;;

#_{:clj-kondo/ignore [:redefined-var]}
(def connection
  (delay
    (let [socket (Socket. (InetAddress/getByName "localhost") 61817)]
      (print "Socket connected to 61817\n")
      socket)))

@connection ; #object[java.net.Socket 0x180e4513 "Socket[addr=localhost/127.0.0.1,port=61817,localport=43262]"]

#_{:clj-kondo/ignore [:redefined-var]}
(defn handle-request [s]
  (let [_conn @connection]
    (print (format "Doing something with %s\n" s))))

(dotimes [i 3]
  (future (handle-request i)))
; (out) Doing something with 0
; (out) Doing something with 2
; (out) Doing something with 1

;; might be necessary
;; (flush)

;; ;;;;;;;;;;;;;;;;
;; delay? and force
;; ;;;;;;;;;;;;;;;;

(def d1 (delay (println :evaluated)))

(type d1) ; clojure.lang.Delay

(if (delay? d1)
  :delay
  :normal)
; :delay

;; `force` is useful to deal with objects that could potentially be a delay but we are not sure.
;; If the argument is a delay, it derefs its content, else it returns the object itself.

(def coll [(delay (println :evaluated) :item0)
           :item1
           :item2])

(map force coll)
;; :evaluated
;; (:item0 :item1 :item2)

;; if a delayed computation produces an exception, the same exception object is re-thrown at each deref
(def d2 (delay (throw (ex-info "error" {:cause (rand)}))))

(try @d2
     (catch Exception e
       (ex-data e)))     ; {:cause 0.7818014774884312}

(try @d2
     (catch Exception e
       (ex-data e)))     ; {:cause 0.7818014774884312}

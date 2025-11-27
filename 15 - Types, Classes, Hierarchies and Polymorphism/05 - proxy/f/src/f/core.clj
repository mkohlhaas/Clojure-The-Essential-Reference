(ns f.core
  (:require
   [clojure.java.javadoc :as browse])
  (:import
   [clojure.lang IDeref]
   [java.io File]))

;; `proxy` generates an anonymous Java class (that can implement or extend other classes or interfaces)
;; `reify` generates an anonymous Java interface or Clojure protocol

;; The lesson learned from proxy is that unless you are forced to extend a class from a Java framework
;; in order to use it, you should probably look into reify instead of proxy for the creation of quick
;; throw-away instances. If instead your goal is polymorphism in Clojure, there are better options with
;; protocols and multimethods.

;; type hint is a good idea here
(def ^Runnable r
  (proxy [Runnable] []
    (run [] (println (rand)))))

(comment
  (.run r))
  ; (out) 0.034966701246652354
  ; (out) 0.04368836275090493
  ; (out) 0.990659882736266
  ; (out) 0.42915248274541096
  ; …

;; adding new functionalities to java.io.File 
(definterface Concatenable
  (^java.io.File concat [^java.io.File f]))

(defn cfile [fname]
  (proxy [File Concatenable] [^String fname]
    (concat [^File f]
      (spit (.getPath f) (slurp this) :append true) ; copies the content of `this` into the second file argument
      f)))

(def ^Concatenable etchosts (cfile "/etc/hosts"))
(def ^Concatenable f2       (cfile "temp2.txt"))

;; create f2 with some content
(spit f2 "# need to create this file\n")

(comment
  (println (slurp "temp2.txt")))
  ; (out) # need to create this file

;; add `/etc/hosts` to `temp2.txt`
(.concat etchosts f2)

(comment
  (println (slurp "temp2.txt")))
  ; (out) # need to create this file
  ; (out) # Static table lookup for hostnames.
  ; (out) # See hosts(5) for details.
  ; (out) 127.0.0.1        localhost
  ; (out) ::1              localhost

;; updates the proxy's fn map
(update-proxy
 etchosts
 {"concat"
  #(let [^File f1 %1 ^File f2 %2]                 ; %1 = this; %2 = filename (let block used only for type hinting)
     (.createNewFile ^File f2)                    ; creates a new file file right from the start (if not already existing)
     (spit (.getPath f2) (slurp f1) :append true) ; add f1 to f2
     f2)})                                        ; return the new file

(-> etchosts
    (.concat (cfile "temp3.txt"))        ; temp3.txt      is created automatically
    (.concat (cfile "hosts-copy.txt")))  ; hosts-copy.txt is created automatically

(comment
  ;; same content as /etc/hosts
  (println (slurp "temp3.txt"))
  ; (out) # Static table lookup for hostnames.
  ; (out) # See hosts(5) for details.
  ; (out) 127.0.0.1        localhost
  ; (out) ::1              localhost

  ;; same content as temp3.txt
  (println (slurp "hosts-copy.txt")))
  ; (out) # Static table lookup for hostnames.
  ; (out) # See hosts(5) for details.
  ; (out) 127.0.0.1        localhost
  ; (out) ::1              localhost

;; The function `proxy` groups together class generation, object creation and functional overrides into a single call (which is in general very convenient).
;; However, you can separate the life cycle phases using functions like `get-proxy-class`, `construct-proxy` and `init-proxy`.

;; generation of the SyntaxException happens here at definition time
(def SyntaxException (get-proxy-class Exception IDeref))

(comment
  ;; unused
  (def DocumentException   (get-proxy-class Exception IDeref))
  (def FormattingException (get-proxy-class Exception IDeref)))

(comment
  ;; see constructors for Exception class (for `construct-proxy`)
  (browse/javadoc Exception))

(defn bail
  ([ex msg]
   (throw
    (-> ex
        (construct-proxy msg) ; create an Exception
        (init-proxy
         {"deref" ; overwrite deref
          (fn [_this] (str "Cause: " msg))}))))
  ([ex msg ^Exception cause]
   (throw
    (-> ex
        (construct-proxy msg cause) ; create an Exception
        (init-proxy
         {"deref" ; overwrite deref
          (fn [_this] (str "Root: " (.getMessage cause)))})))))

(defn verify-age [^String s]
  (try
    (Integer/valueOf s)
    (catch Exception e
      (bail SyntaxException "Age is not a number" e))))

(try   ; "Root: For input string: \"AA\""
  (let [age "AA"]
    (verify-age age))
  (catch Exception e @e)) ; deref the SyntaxException with @

;; different bail invocation
(defn verify-age1 [^String s]
  (try
    (Integer/valueOf s)
    (catch Exception _e
      (bail SyntaxException "Age is not a number"))))

(try   ; "Cause: Age is not a number"
  (let [age "AA"]
    (verify-age1 age))
  (catch Exception e @e)) ; deref the SyntaxException with @


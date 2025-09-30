(ns f.crypto
  (:import javax.crypto.spec.SecretKeySpec
           javax.crypto.Mac
           java.util.Base64
           java.net.URLEncoder
           java.nio.charset.StandardCharsets))

(set! *warn-on-reflection* true)

;; ;;;;;;;;;;;;
;; crypto stuff
;; ;;;;;;;;;;;;

(defn get-bytes [s]
  (.getBytes s StandardCharsets/UTF_8))
; NOTE: (err) Reflection warning, call to method getBytes can't be resolved (target class is unknown).

(defn create-spec [secret]
  (SecretKeySpec. (get-bytes secret) "HmacSHA256"))

(defn init-mac [spec]
  (doto (Mac/getInstance "HmacSHA256")
    (.init spec)))

(defn compute-hmac [mac canonical]
  (.doFinal mac (get-bytes canonical)))
; NOTE: (err) Reflection warning, call to method doFinal can't be resolved (target class is unknown).

(defn encode [hmac]
  (URLEncoder/encode
   (.encodeToString (Base64/getEncoder) hmac)))

(defn sign [canonical secret]
  (-> secret
      create-spec
      init-mac
      (compute-hmac canonical)
      encode))

(defn sign-request [url]
  (let [signature (sign url "secret-password")]
    (format "%s?signature=%s" url signature)))

(sign-request "http://example.com/tx/1") ; "http://example.com/tx/1?signature=EtUPpQpumBqQ5c6aCclS8xDIItfP6cINNkKJXtlP1pc%3D"

;; ;;;;;;;;;;;;;;;;;;
;; without type hints
;; ;;;;;;;;;;;;;;;;;;

(time (dotimes [i 100000]
        (sign-request (str "http://example.com/tx/" i))))
; (out) "Elapsed time: 2855.139913 msecs"

;; ;;;;;;;;;;;;;;;
;; with type hints
;; ;;;;;;;;;;;;;;;

#_{:clj-kondo/ignore [:redefined-var]}
(defn get-bytes [^String s]
  (.getBytes s StandardCharsets/UTF_8))

#_{:clj-kondo/ignore [:redefined-var]}
(defn compute-hmac [^Mac mac canonical]
  (.doFinal mac (get-bytes canonical)))

;; four times faster
(time (dotimes [i 100000]
        (sign-request (str "http://example.com/tx/" i))))
; (out) "Elapsed time: 709.322395 msecs"

#_{:clj-kondo/ignore [:namespace-name-mismatch]}
(ns crypto
  (:import javax.crypto.spec.SecretKeySpec
           javax.crypto.Mac
           java.util.Base64
           java.net.URLEncoder
           java.nio.charset.StandardCharsets))

(set! *warn-on-reflection* true)

(defn get-bytes [s]
  (.getBytes s StandardCharsets/UTF_8))

(defn create-spec [secret]
  (SecretKeySpec. (get-bytes secret) "HmacSHA256"))

(defn init-mac [spec]
  (doto (Mac/getInstance "HmacSHA256")
    (.init spec)))

(defn compute-hmac [mac canonical]
  (.doFinal mac (get-bytes canonical)))

(defn encode [hmac]
  (URLEncoder/encode
   (.encodeToString (Base64/getEncoder) hmac)))

(defn sign [canonical secret] ; <2>
  (-> secret
      create-spec
      init-mac
      (compute-hmac canonical)
      encode))

(defn sign-request [url] ; <3>
  (let [signature (sign url "secret-password")]
    (format "%s?signature=%s" url signature)))

(sign-request "http://example.com/tx/1")
; "http://example.com/tx/1?signature=EtUPpQpumBqQ5c6aCclS8xDIItfP6cINNkKJXtlP1pc%3D"

(time (dotimes [i 100000]
        (sign-request (str "http://example.com/tx/" i))))
; (out) "Elapsed time: 6403.074515 msecs"

;; type hints (should be much faster - but isn't)
#_{:clj-kondo/ignore [:redefined-var]}
(defn get-bytes [^String s]
  (.getBytes s StandardCharsets/UTF_8))

#_{:clj-kondo/ignore [:redefined-var]}
(defn compute-hmac [^Mac mac canonical]
  (.doFinal mac (get-bytes canonical)))

(time (dotimes [i 100000]
        (sign-request (str "http://example.com/tx/" i))))
; (out) "Elapsed time: 6389.767123 msecs"

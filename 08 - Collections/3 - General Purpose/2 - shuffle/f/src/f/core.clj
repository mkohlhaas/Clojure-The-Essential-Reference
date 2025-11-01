(ns f.core)

(shuffle (shuffle (range 1 10))) ; [1 5 6 2 8 4 7 9 3]

(comment
  (shuffle {:a 1 :b 2 :c 3})
  ; (err) Execution error (ClassCastException)

  (shuffle nil))
  ; (err) Execution error (NullPointerException)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn round-robin [f hosts]
  (let [hosts (shuffle hosts)
        idx   (atom 0)]
    (fn []
      (f (nth hosts @idx))
      (reset! idx (mod (inc @idx) (count hosts))))))

(comment
  (let [rr (round-robin println ["10.100.89.42" "10.100.86.57" "10.100.23.12"])]
    (rr)
    (rr)
    (rr)
    (rr)))
  ; (out) 10.100.86.57
  ; (out) 10.100.89.42
  ; (out) 10.100.23.12
  ; (out) 10.100.86.57

(defn request [host & [path]]
  (println "calling" (format "http://%s/%s" host (or path "index.html"))))

(def hosts ["10.100.89.42" "10.100.86.57" "10.100.23.12"])
(def get-host (round-robin request hosts))

(get-host)
; (out) calling http://10.100.89.42/index.html
; (out) calling http://10.100.23.12/index.html
; (out) calling http://10.100.86.57/index.html
; (out) calling http://10.100.89.42/index.html

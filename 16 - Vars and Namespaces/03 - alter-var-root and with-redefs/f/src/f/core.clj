(ns f.core)

(def a-var {:a 1})

(alter-var-root
 #'a-var
 update-in [:a] inc) ; fn from old value to new value
; {:a 2}

(def a-var1 1)

(future
  (alter-var-root
   #'a-var1
   (fn [old-value]
     (Thread/sleep 1000)
     (inc old-value))))

;; blocking call for 1 second
a-var1 ; 2

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; alter-var-root and definline
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(definline times-pi [x] `(* ~x 3.14))               ; times-pi is an inline function
(alter-var-root #'times-pi (fn [_] (constantly 1))) ; chaning times-pi
(times-pi 100)                                      ; 314.0 (calling inline version)

(alter-meta! #'times-pi dissoc :inline-arities :inline) ; we need to change or remove the related metadata on the var object to force evaluation through the root binding
(times-pi 100)                                          ; 1 (using root binding)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; with-redefs and with-redefs-fn
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; typically used for testing

(defn fetch-title [url]
  (let [input (slurp url)]
    (last (re-find #"Title: (.*)\." input))))

(comment
  (re-find #"Title: (.*)\." "Some Title: Salary increases announced."))
  ; ["Title: Salary increases announced." "Salary increases announced"]

(def sample-article "Some Title: Salary increases announced.")

;; redefine `slurp` temporarily
(with-redefs [slurp (constantly sample-article)]
  (= "Salary increases announced" (fetch-title "url")))
; true

;; same effect as before
(with-redefs-fn {#'slurp (constantly sample-article)}
  #(= "Salary increases announced" (fetch-title "url")))
; true

(defn x [] 5)
(defn y [] 9)

;; `with-redefs` and `with-redefs-fn` are not thread-safe
(dotimes [_i 10]
  (future (with-redefs [x #(rand)] (* (x) (y))))
  (future (with-redefs [y #(rand)] (* (x) (y)))))

;; in this case y has been permanently changed
[(x) (y)] ; [5 0.6332743914486662]

;; using dynamically thread-bound vars
(defn ^:dynamic x1 [] 5)
(defn ^:dynamic y1 [] 9)

(dotimes [_i 10]
  (future (binding [x1 #(rand)] (* (x1) (y1))))
  (future (binding [y1 #(rand)] (* (x1) (y1)))))

;; problem not fixed
[(x) (y)] ; [5 0.7350618232121194] (???)
          ; [5 9]                  (this should have been the result)

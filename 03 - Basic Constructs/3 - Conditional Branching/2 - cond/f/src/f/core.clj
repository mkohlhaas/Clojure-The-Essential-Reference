(ns f.core
  (:require
   [clojure.walk]))

(cond) ; nil

(let [a false
      b true]
  (cond
    a :a
    b :b
    :else :c))
; :b

#_{:clj-kondo/ignore [:missing-else-branch]}
(let [a false
      b true]
  (if a :a
      (if b :b
          (if :else :c))))
; :b

;; ;;;;;;;;;;;;;;;;
;; Response Example
;; ;;;;;;;;;;;;;;;;

(defn http-response-code [data]
  (cond
    (:error data)                  500
    (not= :failure (:status data)) 200
    :else 400))

(def good-data
  {:id 8498
   :status :success
   :payload "<tx>489ajfk</tx>"})

(def bad-data
  {:id 8490
   :error "database error"
   :status nil
   :payload nil})

(http-response-code good-data) ; 200
(http-response-code bad-data)  ; 500

(clojure.walk/macroexpand-all
 '(cond
    (:error data)                  500
    (not= :failure (:status data)) 200
    :else 400))

;; (if (:error data)
;;   500
;;   (if (not= :failure (:status data))
;;     200
;;     (if :else
;;       400
;;       nil)))

(comment
  (doseq [n (filter even? (range 10000))]
    (println n)
    (clojure.walk/macroexpand-all
     `(cond ~@(take n (repeat false))))))

  ; (out) 0
  ; (out) 2
  ; (out) 4
  ; (out) 6
  ; (out) 8
  ; (out) 10
  ; (out) 12
  ; …
  ; (out) 360
  ; (out) 362
  ; (out) 364
  ; (out) 366
  ; (out) 368
  ; (out) 370
  ; (out) 372
  ; (out) 374
  ; (out) 376
  ; (out) 378
  ; (out) 380
  ; (out) 382
  ; (err) Execution error (StackOverflowError) at (REPL:1).
  ; (err) null

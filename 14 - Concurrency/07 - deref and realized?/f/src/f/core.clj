(ns f.core)

;; `deref` - and its reader macro @ -  and `realized?` work with reference types 
;; `deref` supports a timeout with a default value for blocking reference types

;; ;;;;;;;;;
;; realized?
;; ;;;;;;;;;

;; `realized?` can be used to check if a value is available from a promise or future

(def p (promise)) ; #<Promise@51d24958: :not-delivered>

(def f
  (future
    (loop []
      (let [v (deref p 100 ::na)]
        (if (= ::na v)
          (recur)
          v)))))
; #<Future@65d3cf7f: :pending>

(realized? p) ; false
(realized? f) ; false

(deref f 100 :not-delivered) ; :not-delivered
(deliver p   :finally)       ; #<Promise@51d24958: :finally>
(deref f 100 :not-delivered) ; :finally

(realized? p) ; true
(realized? f) ; true

;; `realized?` also works with lazy sequences to verify if the first item in the sequence has been evaluated (and cached for later use)

(def s1 (map inc (range 100))) ; (1 2 3 4 5 6 7 8 9 10 11 … 94 95 96 97 98 99 100)

(realized? s1) ; false
(first s1)     ; 1
(realized? s1) ; true

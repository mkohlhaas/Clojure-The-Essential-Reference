(ns f.core)

;; (when-first bindings <body>)
;; "bindings" is a vector of exactly 2 elements

(when-first [x (range 10)]
  (str x))
; "0"

#_{:clj-kondo/ignore [:unused-binding]}
(when-first [x ()]
  (print "never gets here"))

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; chunks of 32 elements (although we need only the first)
(first (map #(do (print ".") %) (range 100))) ; 0
; (out) ................................

(defn dechunk [xs]
  (lazy-seq
   (when-first [x xs]
     (cons x (dechunk (rest xs))))))

(comment
  ;; no chunks
  (first (map #(do (print ".") %) (dechunk (range 100)))))  ; 0
  ; (out) .

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (macroexpand
   '(when-first [x coll] (println x))))
  ; (let*
  ;  [temp__5804 (seq coll)] ; `seq` is called on "coll" with the effect of creating a sequential version (if it isn't’ already). It generates nil when "coll" is empty.
  ;  (when
  ;   temp__5804
  ;   (let
  ;    [xs__6360 temp__5804]
  ;    (let
  ;     [x (first xs__6360)]
  ;     (println x)))))

(defn take-first-1 [coll]
  (lazy-seq
   (when (seq coll)
     (cons (first coll) ()))))

;; `sequence` is caching
(take-first-1 (sequence (map #(do (println "eval" %) %)) '(1)))
; (out) eval 1
; (1)

;; `eduction` isn't caching
;; `take-first` evaluates the input twice, once to check if it's empty and the second time to get the first element
(take-first-1 (eduction (map #(do (println "eval" %) %)) '(1)))
; (out) eval 1
; (out) eval 1
; (1)

;; uses `when-first` which evaluates "coll" just once
(defn take-first-2 [coll]
  (lazy-seq
   (when-first [x coll]
     (cons x ()))))

(take-first-2 (sequence (map #(do (println "eval" %) %)) '(1)))
; (out) eval 1
; (1)

(take-first-2 (eduction (map #(do (println "eval" %) %)) '(1)))
; (out) eval 1
; (1)

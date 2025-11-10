(ns f.core
  (:require [clojure.pprint]))

(when-first [x (range 10)] (str x))            ; "0"

#_{:clj-kondo/ignore [:unused-binding]}
(when-first [x ()] (print "never gets here")) ; nil

(first (map #(do (print ".") %) (range 100))) ; 0
; (out) ................................

(defn dechunk [xs]
  (lazy-seq
   (when-first [x xs]
     (cons x (dechunk (rest xs))))))

(comment
  (first (map #(do (print ".") %) (dechunk (range 100)))))  ; 0
  ; (out) .

(comment
  (clojure.pprint/pprint
   (macroexpand
    '(when-first [x coll] (println x)))))
  ; (out) (let*
  ; (out)  [temp__5804 (seq coll)]
  ; (out)  (when
  ; (out)   temp__5804
  ; (out)   (let  [xs__6360 temp__5804]
  ; (out)    (let  [x (first xs__6360)]
  ; (out)     (println x)))))

(defn take-first [coll]
  (lazy-seq
   (when (seq coll)
     (cons (first coll) ()))))

(comment
  (take-first (sequence (map #(do (println "eval" %) %)) '(1)))) ; (1)
  ; (out) eval 1

(take-first (eduction (map #(do (println "eval" %) %)) '(1)))
; eval 1
; eval 1
; (1)

(defn take-first-me [coll]
  (lazy-seq
   (when-first [x coll]
     (cons x ()))))

(comment
  (take-first-me (sequence (map #(do (println "eval" %) %)) '(1)))) ; (1)
  ; (out) eval 1

(take-first-me (eduction (map #(do (println "eval" %) %)) '(1)))
; eval 1
; (1)

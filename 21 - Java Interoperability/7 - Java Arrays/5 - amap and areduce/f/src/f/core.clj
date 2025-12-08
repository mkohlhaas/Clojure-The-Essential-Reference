(ns f.core)

;; `amap` and `areduce` are immutable operations (original array is untouched)

(def a1 (int-array (range 10))) ; [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
(def a2 (amap a1 _idx _ret 1))  ; [1, 1, 1, 1, 1, 1, 1, 1, 1, 1]

(comment
  (vec a1)  ; [0 1 2 3 4 5 6 7 8 9]  (unchanged)
  (vec a2)) ; [1 1 1 1 1 1 1 1 1 1]

(defn debug [idx output]
  (println "idx:" idx "result:" output)
  9)

(def a3 (int-array (range 4))) ; [0, 1, 2, 3]

(amap a3 idx result (debug idx (vec result))) ; [9, 9, 9, 9]
; (out) idx: 0 result: [0 1 2 3]
; (out) idx: 1 result: [9 1 2 3]
; (out) idx: 2 result: [9 9 2 3]
; (out) idx: 3 result: [9 9 9 3]

(defn ainc [a]
  (amap a idx _res (inc (aget a idx))))

(ainc (int-array (range 10))) ; [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

(defn asum-upto [a i]
  (loop [idx 0
         sum 0]
    (if (= idx i)
      sum
      (recur (inc idx) (+ sum (aget a idx))))))

(defn amap-upto [a f limit]
  (amap a idx out
        (let [old (aget a idx)
              new (f old)
              sum (asum-upto out idx)]
          (if (> (+ new sum) limit) old new))))

(def a (int-array (range 10))) ; [0, 1, 2, 3, 4,  5,  6, 7, 8, 9]

(amap-upto a #(* % %) 60)      ; [0, 1, 4, 9, 16, 25, 6, 7, 8, 9]

;; ;;;;;;;;;;;;;;;;;;;;;
;; Arrays and Type Hints
;; ;;;;;;;;;;;;;;;;;;;;;

(set! *warn-on-reflection* true)

(alength a) ; 10
; (err) Reflection warning 

(alength ^"[I" a) ; 10   (type hint with Java array class encoding)
(alength ^ints a) ; 10   (^ints is an array type provided by Clojure)

(set! *unchecked-math* :warn-on-boxed)

;; same as before
(defn amap-upto1 [a f limit]
  (amap a idx out
        (let [old (aget a idx)
              new (f old)
              sum (asum-upto out idx)]
          (if (> (+ new sum) limit) old new))))

; (err) Reflection warning, … call to static method alength on clojure.lang.RT can't be resolved (argument types: unknown).
; (err) Reflection warning, … call to static method aclone on clojure.lang.RT can't be resolved (argument types: unknown).
; (err) Boxed math warning, … call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
; (err) Reflection warning, … call to static method aget on clojure.lang.RT can't be resolved (argument types: unknown, int).
; (err) Boxed math warning, … call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(java.lang.Object,java.lang.Object).
; (err) Boxed math warning, … call: public static boolean clojure.lang.Numbers.gt(java.lang.Object,java.lang.Object).
; (err) Reflection warning, … call to static method aset on clojure.lang.RT can't be resolved (argument types: unknown, int, unknown).

(amap-upto1 a #(* % %) 60) ; [0, 1, 4, 9, 16, 25, 6, 7, 8, 9]

;; warnings elimination exercise
;; with type hints
(defn amap-upto2 [^ints a f limit]                   ; type hint
  (amap a idx out
        (let [old (aget a idx)
              ^int new (f old)                       ; type hint
              ^int sum (asum-upto out idx)]          ; type hint
          (if (> (+ new sum) ^int limit) old new)))) ; type hint

(amap-upto2 a #(* % %) 60) ; [0, 1, 4, 9, 16, 25, 6, 7, 8, 9]

;; Reduction ;;

(areduce a idx acc 0 (+ acc (aget a idx))) ; 45

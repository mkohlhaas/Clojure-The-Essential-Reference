(ns f.core)

;; specialized array cast functions:
;; - ints
;; - booleans
;; - bytes
;; - shorts
;; - chars
;; - longs
;; - floats
;; - doubles

(set! *warn-on-reflection* false)
(set! *unchecked-math*     false)

(defn asum [a1 a2]
  (let [a (aclone (if (< (alength a1) (alength a2)) a1 a2))]
    (amap a idx _ret
          (aset a idx
                (+ (aget a1 idx)
                   (aget a2 idx))))))

(asum (int-array [1 2 3])
      (int-array [4 5 6])) ; [5, 7, 9]

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(comment
  ;; same as before
  (defn asum [a1 a2]
    (let [a (aclone (if (< (alength a1) (alength a2)) a1 a2))]
      (amap a idx _ret
            (aset a idx
                  (+ (aget a1 idx)
                     (aget a2 idx))))))) ;; same as before
; (err) Reflection warning, call to static method alength on clojure.lang.RT can't be resolved (argument types: unknown).
; (err) Reflection warning, call to static method alength on clojure.lang.RT can't be resolved (argument types: unknown).
; (err) Boxed math warning, call: public static boolean clojure.lang.Numbers.lt(java.lang.Object,java.lang.Object).
; (err) Reflection warning, call to static method aclone on clojure.lang.RT can't be resolved (argument types: unknown).
; (err) Reflection warning, call to static method alength on clojure.lang.RT can't be resolved (argument types: unknown).
; (err) Reflection warning, call to static method aclone on clojure.lang.RT can't be resolved (argument types: unknown).
; (err) Boxed math warning, call: public static boolean clojure.lang.Numbers.lt(long,java.lang.Object).
; (err) Reflection warning, call to static method aget on clojure.lang.RT can't be resolved (argument types: unknown, int).
; (err) Reflection warning, call to static method aget on clojure.lang.RT can't be resolved (argument types: unknown, int).
; (err) Boxed math warning, call: public static java.lang.Number clojure.lang.Numbers.unchecked_add(java.lang.Object,java.lang.Object).
; (err) Reflection warning, call to static method aset on clojure.lang.RT can't be resolved (argument types: unknown, int, java.lang.Number).
; (err) Reflection warning, call to static method aset on clojure.lang.RT can't be resolved (argument types: unknown, int, unknown).

;; with types
(defn asum-int [a1 a2]
  (let [a1 (ints a1)
        a2 (ints a2)
        a  (aclone (if (< (alength a1) (alength a2)) a1 a2))]
    (amap a idx _ret
          (aset a idx
                (+ (aget a1 idx)
                   (aget a2 idx))))))

(asum-int (int-array (range 10))     ; (0    1   2   3   4   5   6   7   8  9)
          (int-array (range 11 20))) ; (11  12  13  14  15  16  17  18  19)
                                     ; [11, 13, 15, 17, 19, 21, 23, 25, 27]

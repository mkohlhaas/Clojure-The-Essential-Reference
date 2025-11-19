(ns f.core
  (:require
   [clj-memory-meter.core :as mm]
   [criterium.core :refer [bench quick-bench]]))

(vector-of  :int)                         ; []
(vector-of  :int 16/5 2.8 1M Double/NaN)  ; [3 2 1 0]
((vector-of :int 1 2 3) 2)                ; 3

(sort [(vector-of :int 7 8 9)
       (vector-of :int 0 1 2)])
; ([0 1 2] [7 8 9])

(comment
  (vector-of Integer 1 2 3)
  ; (err) Execution error (IllegalArgumentException)

  (vector-of :double \a \b \c)
  ; (err) Execution error (ClassCastException) at f.core/eval4116 (form-init18062746941727547297.clj:15).

  (vector-of :int 1 2 nil 3 4)
  ; (err) Execution error (NullPointerException)

  (vector-of :short (inc Short/MAX_VALUE)))
  ; (err) Execution error (IllegalArgumentException) at f.core/eval4120 (form-init18062746941727547297.clj:21).

(def max-iterations 99)

(defn calc-mandelbrot [c-re c-im]
  (let [sq    (fn [x] (* x x))
        iter  (reduce (fn [[z-re z-im] i]
                        (if (or (= i 99) (> (+ (sq z-re) (sq z-im)) 4))
                          (reduced i)
                          [(+ c-re (sq z-re) (- (sq z-im)))
                           (+ c-im (* 2 z-re z-im))]))
                      [0 0] (range (inc max-iterations)))]
    (vector-of :double c-re c-im iter)))

(def mandelbrot-set
  (for [im (range 1 -1 -0.05) re (range -2 0.5 0.0315)]
    (calc-mandelbrot re im)))

(doseq [row (partition 80 mandelbrot-set)]
  (doseq [point row]
    (print (if (> max-iterations (get point 2)) "*" " ")))
  (println))
; (out) ********************************************************************************
; (out) ********************************************************************************
; (out) ********************************************************************************
; (out) ************************************************************ *******************
; (out) *********************************************************     ******************
; (out) ********************************************************       *****************
; (out) *********************************************************      *****************
; (out) ******************************************************  *     ** ***************
; (out) ***********************************************  ***                ************
; (out) **********************************************                        **  ******
; (out) ***********************************************                           ******
; (out) *********************************************                            *******
; (out) ********************************************                              ******
; (out) ******************************************                                   ***
; (out) ******************************** *********                                  ****
; (out) ***************************  *     * *****                                  ****
; (out) ***************************           ***                                   ****
; (out) *************************              *                                    ****
; (out) *************************                                                   ****
; (out) *********************                                                     ******
; (out) *******  *****                                                          ********
; (out) *********************                                                     ******
; (out) *************************                                                   ****
; (out) *************************              *                                    ****
; (out) ***************************           ***                                   ****
; (out) ***************************  *     * *****                                  ****
; (out) ******************************** *********                                  ****
; (out) ******************************************                                   ***
; (out) ********************************************                              ******
; (out) *********************************************                            *******
; (out) ***********************************************                           ******
; (out) **********************************************                        **  ******
; (out) ***********************************************  ***                ************
; (out) ******************************************************  *     ** ***************
; (out) *********************************************************      *****************
; (out) ********************************************************       *****************
; (out) *********************************************************     ******************
; (out) ************************************************************ *******************
; (out) ********************************************************************************
; (out) ********************************************************************************

(comment
  (quick-bench (vector-of :int 1 2 3 4))
;; Execution time mean : 15.340593 ns

  (quick-bench (vector-of :int 1 2 3 4 5))
;; Execution time mean : 124.127511 ns

  (def data (doall (range 100000)))

  (bench (apply vector-of :int data))
;; Execution time mean : 6.975521 ms

  (bench (reduce conj (vector-of :int) data))
;; Execution time mean: 5.926824 ms

  (let [xs (range 100)]
    (quick-bench (apply vector xs)))
;;  Execution time mean : 2.051646 µs

  (let [xs (range 100)]
    (quick-bench (apply vector-of :long xs)))
;; Execution time mean : 5.004903 µs

  (defn memory-vector-of []
    (let [items (range 1.0 1e3)
          bytes-vector (mm/measure (apply vector items))
          bytes-vector-of (mm/measure (apply vector-of :double items))
          saving (* (double (/ (- bytes-vector bytes-vector-of) bytes-vector)) 100)]
      (println "Bytes used by vector" bytes-vector)
      (println "Bytes used by vector of" bytes-vector-of)
      (println (str "Saving " (format "%3.2f" saving) "%"))))

  (memory-vector-of)
;; Bytes used by vector 29456
;; Bytes used by vector of 9472
;; Saving 67.84%

  (let [v1 (vec (range 10000))]
    (bench (nth v1 1000)))
;; Execution time mean : 12.264993 ns

  (let [v1 (apply vector-of :int (range 10000))]
    (bench (nth v1 1000))))
;; Execution time mean : 19.324863 ns

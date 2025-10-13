(ns f.core
  (:require [criterium.core :refer [bench]]))

;; The unchecked-*-int family is especially useful to implement hashing algorithms and integer math in general.

(unchecked-add     2.0 1)  ; 3.0
(unchecked-add-int 2.0 1)  ; 3

(type (unchecked-add-int 2.0 1)) ; java.lang.Integer

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Bresenham’s Line Algorithm
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- steep? [x1 x2 y1 y2]
  (> (Math/abs (unchecked-subtract-int y1 y2))
     (Math/abs (unchecked-subtract-int x1 x2))))

(defn- adjust-slope [x1 x2 y1 y2]
  (if (steep? x1 x2 y1 y2)
    [y1 x1 y2 x2]   ; turn by 90°
    [x1 x2 y1 y2])) ; no change

(defn- adjust-direction [x1 x2 y1 y2]
  (if (> (int x1) (int x2))
    [x2 y2 x1 y1]   ; reverse direction
    [x1 y1 x2 y2])) ; no change

(defn- adjust [x1 x2 y1 y2]
  (->> [x1 x2 y1 y2]
       (apply adjust-slope)
       (apply adjust-direction)))

;; swap x and y if steep
(defn- swap [steep?]
  (fn [[x y]]
    (if steep? [y x] [x y])))

(comment
  ;; https://clojure-doc.org/articles/cookbooks/math/
  (set! *unchecked-math* :warn-on-boxed))

;; Bresenham's line algorithm (using integers)
(defn to-points [x1 y1 x2 y2]
  (let [[^int x1 ^int y1 ^int x2 ^int y2] (adjust x1 x2 y1 y2)
        dx (unchecked-subtract-int x2 x1)
        dy (Math/abs (unchecked-subtract-int y1 y2))]
    (map (swap (steep? x1 x2 y1 y2))
         (loop [x x1
                y y1
                error (unchecked-divide-int dx 2)
                points []]
           (if (> x x2)
             points
             (if (< error dy)
               (recur (unchecked-inc-int x)
                      (if (< y1 y2)
                        (unchecked-inc-int y)
                        (unchecked-dec-int y))
                      (unchecked-add-int error
                                         (unchecked-subtract-int dx dy))
                      (conj points [x y]))
               (recur (unchecked-inc-int x)
                      y
                      (unchecked-subtract-int error dy)
                      (conj points [x y]))))))))

(defn draw-line! [^"[[I" img points] ; you can type-hint with a string containing the Java rendition of a bidimensional array of integers "[[I"
  (let [pset (into #{} points)]
    (dotimes [i (alength img)]
      (dotimes [j (alength (aget img 0))]
        (when (pset [i j])
          (aset-int img i j 1)))))
  img)

(defn zeros [n]
  (take n (repeat 0)))

(defn new-image [n]
  (into-array (map int-array (take n (repeat (zeros n))))))

(comment
  (into-array (map int-array (take 5 (repeat (zeros 5))))))
  ; [[0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0], [0, 0, 0, 0, 0]]

(mapv vec (draw-line! (new-image 12) (to-points 2 3 10 10)))
; [[0 0 0 0 0 0 0 0 0 0 0 0]
;  [0 0 0 0 0 0 0 0 0 0 0 0]
;  [0 0 0 1 0 0 0 0 0 0 0 0]
;  [0 0 0 0 1 0 0 0 0 0 0 0]
;  [0 0 0 0 0 1 0 0 0 0 0 0]
;  [0 0 0 0 0 0 1 0 0 0 0 0]
;  [0 0 0 0 0 0 1 0 0 0 0 0]
;  [0 0 0 0 0 0 0 1 0 0 0 0]
;  [0 0 0 0 0 0 0 0 1 0 0 0]
;  [0 0 0 0 0 0 0 0 0 1 0 0]
;  [0 0 0 0 0 0 0 0 0 0 1 0]
;  [0 0 0 0 0 0 0 0 0 0 0 0]]

;; new version of to-points that is using long throughout
(defn to-points-long [x1 y1 x2 y2]
  (let [[^long x1 ^long y1 ^long x2 ^long y2] (adjust x1 x2 y1 y2)
        dx (unchecked-subtract x2 x1)
        dy (Math/abs (unchecked-subtract y1 y2))]
    (map (swap (steep? x1 x2 y1 y2))
         (loop [x x1
                y y1
                error (long (mod dx 2))
                points []]
           (if (> x x2)
             points
             (if (< error dy)
               (recur (unchecked-inc x)
                      (if (< y1 y2)
                        (unchecked-inc y)
                        (unchecked-dec y))
                      (unchecked-add error (unchecked-subtract dx dy))
                      (conj points [x y]))
               (recur (unchecked-inc x)
                      y
                      (unchecked-subtract error dy)
                      (conj points [x y]))))))))

(comment
  (bench (to-points 3 0 214 197))
  ; (out) Execution time mean : 29.050960 µs

  (bench (to-points-long 3 0 214 197)))
  ; (out) Execution time mean : 34.962475 µs

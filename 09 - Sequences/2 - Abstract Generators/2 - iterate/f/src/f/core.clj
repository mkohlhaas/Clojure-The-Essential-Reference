(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

(take 10 (iterate inc 0))   ; (0 1 2 3 4 5 6 7 8 9)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; Fibonacci

(def fibonacci
  (iterate
   (fn [[x y]] [y (+' x y)])
   [0 1]))

(take 10 fibonacci)              ; ([0 1] [1 1] [1 2] [2 3] [3 5] [5 8] [8 13] [13 21] [21 34] [34 55])
(take 10 (map first fibonacci))  ; (0 1 1 2 3 5 8 13 21 34)

;; PI: inverse tangent series at the basis of the Leibniz approximation of Pi 
;; see also `filterv`

(defn calculate-pi [precision]
  (transduce
   (comp
    (map #(/ 4 %))
    (take-while #(> (Math/abs %) precision)))
   +
   (iterate #(* ((if (pos? %) + -) % 2) -1) 1.0)))

(comment
  (calculate-pi 1e-6)         ; 3.141592153589724
  (time (calculate-pi 1e-6))) ; (out) "Elapsed time: 26010.49295 msecs"

;; Game of Life

(defn grid [h w cells]
  (letfn [(concats [& strs]
            (apply str (apply concat strs)))
          (edge [w]
            (concats
             " "
             (repeat (* 2 w) "-")
             " \n"))
          (row [h w cells]
            (concats
             "|"
             (for [x (range w) :let [y h]]
               (if (cells [x y]) "<>" "  "))
             "|\n"))]
    (concats
     (edge w)
     (for [y (range h) :let [x w]]
       (row y x cells))
     (edge w))))

;; START
;; from the "for" chapter in "03 - Basic Constructs/4 - Iteration and Loops/3 - for/"
(defn under-populated? [n alive?] (and (< n 2) alive?))
(defn over-populated?  [n alive?] (and (> n 3) alive?))
(defn healthy?         [n alive?] (or (and alive? (= n 2)) (= n 3)))
(defn reproduce?       [n alive?] (and (= n 3) (not alive?)))

(defn count-neighbors [h w x y cells]
  (->> (for [dx [-1 0 1]
             dy [-1 0 1]
             :let [x' (+ x dx)
                   y' (+ y dy)]
             :when (and (not (= dx dy 0))
                        (<= 0 x' (dec w))
                        (<= 0 y' (dec h)))]
         [x' y'])
       (filter cells)
       count))

(defn apply-rules [h w x y cells]
  (let [n            (count-neighbors h w x y cells)
        alive?       (contains? cells [x y])
        should-live  (or (healthy? n alive?) (reproduce? n alive?))
        should-die   (or (under-populated? n alive?) (over-populated? n alive?))]
    (and should-live (not should-die))))

(defn next-gen [h w cells]
  (into #{}
        (for [x (range 0 w)
              y (range 0 h)
              :when (apply-rules h w x y cells)]
          [x y])))

;; using `iterate`
(defn life [height width init]
  (iterate (partial next-gen height width) init))

;; from the "for" chapter in "03 - Basic Constructs/4 - Iteration and Loops/3 - for/"
;; END

;; period 3 oscillator
(def pulsar-init
  #{[2 4] [2 5] [2 6] [2 10] [2 11] [2 12]
    [4 2] [4 7] [4 9] [4 14]
    [5 2] [5 7] [5 9] [5 14]
    [6 2] [6 7] [6 9] [6 14]
    [7 4] [7 5] [7 6] [7 10] [7 11] [7 12]
    [9 4] [9 5] [9 6] [9 10] [9 11] [9 12]
    [10 2] [10 7] [10 9] [10 14]
    [11 2] [11 7] [11 9] [11 14]
    [12 2] [12 7] [12 9] [12 14]
    [14 4] [14 5] [14 6] [14 10] [14 11] [14 12]})

(defn pulsar []
  (let [height 17 width 17 init pulsar-init]
    (doseq [state (take 3 (life height width init))]
      (println (grid height width state)))))

(comment
  (pulsar))
  ; (out)  ---------------------------------- 
  ; (out) |                                  |
  ; (out) |                                  |
  ; (out) |        <><><>      <><><>        |
  ; (out) |                                  |
  ; (out) |    <>        <>  <>        <>    |
  ; (out) |    <>        <>  <>        <>    |
  ; (out) |    <>        <>  <>        <>    |
  ; (out) |        <><><>      <><><>        |
  ; (out) |                                  |
  ; (out) |        <><><>      <><><>        |
  ; (out) |    <>        <>  <>        <>    |
  ; (out) |    <>        <>  <>        <>    |
  ; (out) |    <>        <>  <>        <>    |
  ; (out) |                                  |
  ; (out) |        <><><>      <><><>        |
  ; (out) |                                  |
  ; (out) |                                  |
  ; (out)  ---------------------------------- 
  ; (out) 
  ; (out)  ---------------------------------- 
  ; (out) |                                  |
  ; (out) |          <>          <>          |
  ; (out) |          <>          <>          |
  ; (out) |          <><>      <><>          |
  ; (out) |                                  |
  ; (out) |  <><><>    <><>  <><>    <><><>  |
  ; (out) |      <>  <>  <>  <>  <>  <>      |
  ; (out) |          <><>      <><>          |
  ; (out) |                                  |
  ; (out) |          <><>      <><>          |
  ; (out) |      <>  <>  <>  <>  <>  <>      |
  ; (out) |  <><><>    <><>  <><>    <><><>  |
  ; (out) |                                  |
  ; (out) |          <><>      <><>          |
  ; (out) |          <>          <>          |
  ; (out) |          <>          <>          |
  ; (out) |                                  |
  ; (out)  ---------------------------------- 
  ; (out) 
  ; (out)  ---------------------------------- 
  ; (out) |                                  |
  ; (out) |                                  |
  ; (out) |        <><>          <><>        |
  ; (out) |          <><>      <><>          |
  ; (out) |    <>    <>  <>  <>  <>    <>    |
  ; (out) |    <><><>  <><>  <><>  <><><>    |
  ; (out) |      <>  <>  <>  <>  <>  <>      |
  ; (out) |        <><><>      <><><>        |
  ; (out) |                                  |
  ; (out) |        <><><>      <><><>        |
  ; (out) |      <>  <>  <>  <>  <>  <>      |
  ; (out) |    <><><>  <><>  <><>  <><><>    |
  ; (out) |    <>    <>  <>  <>  <>    <>    |
  ; (out) |          <><>      <><>          |
  ; (out) |        <><>          <><>        |
  ; (out) |                                  |
  ; (out) |                                  |
  ; (out)  ---------------------------------- 

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (defn iterate* [f x]
    (lazy-seq (cons x (iterate* f (f x)))))

  (quick-bench (into [] (take 1000000) (iterate* inc 0)))
  ; (out) Execution time mean : 374.739980 ms

  ;; `iterate` is implemented in Java
  (quick-bench (into [] (take 1000000) (iterate inc 0)))
  ; (out) Execution time mean : 187.702854 ms

  ;; caching with `iterate*`
  (let [itr (iterate* #(do (println "eval" %) (inc %)) 0)
        v1  (into [] (take 2) itr)
        v2  (into [] (comp (drop 2) (take 2)) itr)]
    (into v1 v2))
  ; (out) eval 0
  ; (out) eval 1
  ; (out) eval 2
  ; (out) eval 3
  ; [0 1 2 3]

  ;; no caching with `iterate`
  (let [itr (iterate #(do (println "eval" %) (inc %)) 0)
        v1 (into [] (take 2) itr)
        v2 (into [] (comp (drop 2) (take 2)) itr)]
    (into v1 v2)))
  ; (out) eval 0
  ; (out) eval 0
  ; (out) eval 1
  ; (out) eval 2
  ; [0 1 2 3]

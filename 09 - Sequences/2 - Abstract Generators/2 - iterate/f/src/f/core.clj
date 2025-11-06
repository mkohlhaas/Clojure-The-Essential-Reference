(ns f.core
  (:require [criterium.core :refer [quick-bench]]))

(take 10 (iterate inc 0))   ; (0 1 2 3 4 5 6 7 8 9)

(def fibo
  (iterate
   (fn [[x y]] [y (+' x y)])
   [0 1]))

(take 10 (map first fibo))  ; (0 1 1 2 3 5 8 13 21 34)

(defn calculate-pi [precision]
  (transduce
   (comp
    (map #(/ 4 %))
    (take-while #(> (Math/abs %) precision)))
   +
   (iterate #(* ((if (pos? %) + -) % 2) -1) 1.0)))

(comment
  (calculate-pi 1e-6)) ; 3.141592153589724

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

;; please see "next-gen" from the "for" chapter.
(defn life [height width init]
  (iterate (partial next-gen height width) init))

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

(pulsar)
;; ----------------------------------
;; |                                  |
;; |        <><><>      <><><>        |
;; |                                  |
;; |    <>        <>  <>        <>    |
;; |    <>        <>  <>        <>    |
;; |    <>        <>  <>        <>    |
;; |        <><><>      <><><>        |
;; |                                  |
;; |        <><><>      <><><>        |
;; |    <>        <>  <>        <>    |
;; |    <>        <>  <>        <>    |
;; |    <>        <>  <>        <>    |
;; |                                  |
;; |        <><><>      <><><>        |
;; |                                  |
;; ----------------------------------
;;
;; ----------------------------------
;; |                                  |
;; |          <>          <>          |
;; |          <>          <>          |
;; |          <><>      <><>          |
;; |                                  |
;; |  <><><>    <><>  <><>    <><><>  |
;; |      <>  <>  <>  <>  <>  <>      |
;; |          <><>      <><>          |
;; |                                  |
;; |          <><>      <><>          |
;; |      <>  <>  <>  <>  <>  <>      |
;; |  <><><>    <><>  <><>    <><><>  |
;; |                                  |
;; |          <><>      <><>          |
;; |          <>          <>          |
;; |          <>          <>          |
;; |                                  |
;; ----------------------------------
;;
;; ----------------------------------
;; |                                  |
;; |        <><>          <><>        |
;; |          <><>      <><>          |
;; |    <>    <>  <>  <>  <>    <>    |
;; |    <><><>  <><>  <><>  <><><>    |
;; |      <>  <>  <>  <>  <>  <>      |
;; |        <><><>      <><><>        |
;; |                                  |
;; |        <><><>      <><><>        |
;; |      <>  <>  <>  <>  <>  <>      |
;; |    <><><>  <><>  <><>  <><><>    |
;; |    <>    <>  <>  <>  <>    <>    |
;; |          <><>      <><>          |
;; |        <><>          <><>        |
;; |                                  |
;; ----------------------------------

(comment
  (defn iterate* [f x]
    (lazy-seq (cons x (iterate* f (f x)))))

  (quick-bench (into [] (take 1000000) (iterate* inc 0)))
;; Execution time mean : 97.414648 ms

  (quick-bench (into [] (take 1000000) (iterate inc 0)))
;; Execution time mean : 44.920465 ms

  (let [itr (iterate* #(do (println "eval" %) (inc %)) 0) ; <1>
        v1 (into [] (take 2) itr)
        v2 (into [] (comp (drop 2) (take 2)) itr)]
    (into v1 v2))
;; eval 0
;; eval 1
;; eval 2
;; eval 3
;; [0 1 2 3]

  (let [itr (iterate #(do (println "eval" %) (inc %)) 0) ; <2>
        v1 (into [] (take 2) itr)
        v2 (into [] (comp (drop 2) (take 2)) itr)]
    (into v1 v2)))
;; eval 0
;; eval 0
;; eval 1
;; eval 2
;; [0 1 2 3]

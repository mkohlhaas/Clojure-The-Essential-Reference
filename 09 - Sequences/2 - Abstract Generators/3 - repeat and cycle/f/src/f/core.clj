(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(take 5 (repeat (+ 1 1))) ; (2 2 2 2 2)
(take 5 (cycle  [1 2 3])) ; (1 2 3 1 2)

(repeat 5 1) ; (1 1 1 1 1)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; bang

(defn bang [sentence]
  (map str (.split #"\s+" sentence) (repeat "!")))

(bang "Add exclamation mark to each word")
; ("Add!" "exclamation!" "mark!" "to!" "each!" "word!")

;; pow

(defn pow [x y] (reduce * (repeat y x)))

(pow 2 3) ; 8

;; tally mark system

(defn to-tally [n]
  (apply str (concat
              (repeat (quot n 5) "卌")
              (repeat (mod n 5) "|"))))

(comment
  (map to-tally (range 10))) ; ("" "|" "||" "|||" "||||" "卌" "卌|" "卌||" "卌|||" "卌||||")

(defn new-tally []
  (let [cnt (atom 0)]
    (fn []
      (to-tally (swap! cnt inc)))))

(def t (new-tally))

(t) ; "|"
(t) ; "||"
(t) ; "|||"
(t) ; "||||"
(t) ; "卌"

(repeatedly 5 t) ; ("卌|" "卌||" "卌|||" "卌||||" "卌卌")

(take 10 (apply concat (repeat [1 2 3]))) ; (1 2 3 1 2 3 1 2 3 1)
(take 10 (cycle                [1 2 3]))  ; (1 2 3 1 2 3 1 2 3 1)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (quick-bench (reduce + (take 1000000 (map * (range) (cycle [1 -1])))))
  ; (out) Execution time mean : 634.644326 ms

  (take 10 (map * (range) (cycle [1 -1]))) ; (0 -1 2 -3 4 -5 6 -7 8 -9)
  (take 10 (map-indexed * (cycle [1 -1]))) ; (0 -1 2 -3 4 -5 6 -7 8 -9)

  (quick-bench (transduce (comp (map-indexed *) (take 1000000)) + (cycle [1 -1])))
  ; (out) Execution time mean : 239.646058 ms

  (map-indexed * '(0 1 2 3 4 5)) ; (0 1 4 9 16 25)
  (map-indexed * '(1 2 3 4 5)))  ; (0 2 6 12 20)

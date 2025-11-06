(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(take 5 (repeat (+ 1 1))) ; (2 2 2 2 2)
(take 5 (cycle  [1 2 3])) ; (1 2 3 1 2)

(repeat 5 1) ; (1 1 1 1 1)

(defn bang [sentence]
  (map str (.split #"\s+" sentence) (repeat "!")))

(bang "Add exclamation each word")
; ("Add!" "exclamation!" "each!" "word!")

(defn pow [x y] (reduce * (repeat y x)))

(pow 2 3) ; 8

(defn to-tally [n]
  (apply str (concat
              (repeat (quot n 5) "卌")
              (repeat (mod n 5) "|"))))

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
;; Execution time mean : 193.403844 ms

  (quick-bench (transduce (comp (map-indexed *) (take 1000000)) + (cycle [1 -1]))))
;; Execution time mean : 80.234017 ms

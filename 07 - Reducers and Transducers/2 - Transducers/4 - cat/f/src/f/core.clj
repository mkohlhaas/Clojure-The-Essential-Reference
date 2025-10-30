(ns f.core)

;; `cat` is the only pure transducer - which is not connected to an already existent sequence function - present in the standard library.
;; `cat` is the only one that can be used directly.

;; `cat` assumes the preceding transducer(s) is/are producing a sequential result.
;; `cat` iterates through the inner sequential step to remove one layer of sequential wrapping.

;; `eduction` is like `comp` for transducers.
;; Actually, `eduction` combines transducers through `comp` internally.
(eduction
 (map range)
 (partition-all 2)  ; ([() (0)] [(0 1) (0 1 2)] [(0 1 2 3)])
 cat                ; (() (0) (0 1) (0 1 2) (0 1 2 3))
 cat                ; (0 0 1 0 1 2 0 1 2 3)
 (range 5))
; (0 0 1 0 1 2 0 1 2 3)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; `cat` works like calling `reduce` once a reducing function has been assigned to it
((cat +)  0 (range 10)) ; 45
(reduce + 0 (range 10)) ; 45

(def team ["jake" "ross" "trevor" "ella"])
(def week ["mon" "tue" "wed" "thu" "fri" "sat" "sun"])

(defn rotate [xs]
  (sequence cat (repeat xs))) ; `cat` eliminates the inner collections forming a flat sequence

(def rota
  (sequence
   (map vector)
   (rotate team)
   (rotate week)))
;; (["jake"   "mon"]
;;  ["ross"   "tue"]
;;  ["trevor" "wed"]
;;  ["ella"   "thu"]
;;  ["jake"   "fri"]
;;  ["ross"   "sat"]
;;  ["trevor" "sun"]
;;  ["ella"   "mon"]
;;  …)

(last (take 8 rota))
; ["ella" "mon"]

;; ;;;;;;;;;;;;;;
;; cat and mapcat
;; ;;;;;;;;;;;;;;

(def seasons ["spring" "summer" "autumn" "winter"])

;; before the introduction of cat in Clojure 1.7 `mapcat` with `identity` was typically used
(nth (mapcat identity (repeat seasons)) 10) ; "autumn"
(nth (eduction cat    (repeat seasons)) 10) ; "autumn"

;; `mapcat` - which is also transducer-ready - is still useful when flattening includes a transformation step
(take 10 (eduction (mapcat range) (range))) ; (0 0 1 0 1 2 0 1 2 3)

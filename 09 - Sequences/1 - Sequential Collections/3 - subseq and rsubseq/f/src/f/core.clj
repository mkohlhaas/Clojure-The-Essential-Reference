(ns f.core
  (:require
   [clojure.string :refer [split]]
   [criterium.core :refer [quick-bench]]))

;; `subseq` and `rsubseq` use sorted collections (sorted-sets, sorted-maps) as input arguments.

(subseq (apply sorted-set (range 10)) > 2 < 8)
; (3 4 5 6 7)

(comment
  (apply sorted-set (range 10))  ; #{0 1 2 3 4 5 6 7 8 9}
  (apply sorted-map (range 10))) ; {0 1, 2 3, 4 5, 6 7, 8 9}

(subseq  (apply sorted-map (range 10)) <= 5) ; ([0 1] [2 3] [4 5])
(rsubseq (apply sorted-map (range 10)) <= 5) ; ([4 5] [2 3] [0 1])

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn smallest>  [coll x] (first (subseq  coll >  x)))
(defn smallest>= [coll x] (first (subseq  coll >= x)))
(defn greatest<  [coll x] (first (rsubseq coll <  x)))
(defn greatest<= [coll x] (first (rsubseq coll <= x)))

(def coll (sorted-map "a" 5 "f" 23 "z" 12 "g" 1 "b" 0))
; {"a" 5, "b" 0, "f" 23, "g" 1, "z" 12}

(smallest>  coll "f") ; ["g" 1]
(greatest<  coll "f") ; ["b" 0]
(smallest>= coll "f") ; ["f" 23]
(greatest<= coll "f") ; ["f" 23]

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; Auto-Completion of Words
;; ;;;;;;;;;;;;;;;;;;;;;;;;

(def dict (into (sorted-set) (split (slurp "/usr/share/dict/words") #"\s+")))
;; #{"A"
;;   "A's"
;;   "AA"
;;   "AA's"
;;   "AAA"
;;   "AB"
;;   "AB's"
;;   "ABA"
;;   "ABC"
;;   "ABC's"
;;   "ABCs"
;;   …}

(defn complete [word dict]
  (take 4 (subseq dict >= word)))

(comment
  (complete "hall" dict)) ; ("hall" "hall's" "hallelujah" "hallelujah's")

(map #(complete % dict) ["c" "cl" "clo" "clos" "closu"])
; (("c" "ca" "cab" "cab's")
;  ("cl" "clack" "clack's" "clacked")
;  ("cloaca" "cloaca's" "cloacae" "cloak")
;  ("close" "close's" "closed" "closefisted")
;  ("closure" "closure's" "closures" "clot"))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (def items (shuffle (range 1e5)))

  (let [x 5000 xs (sort items)]
    (quick-bench (first (drop-while #(>= x %) xs))))
;; Execution time mean : 88.201576 µs

  (let [x 5000 ss (into (sorted-set) items)]
    (quick-bench (first (subseq ss > x)))))
;; Execution time mean : 0.767148269 µs

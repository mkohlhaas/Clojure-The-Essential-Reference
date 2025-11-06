(ns f.core
  (:require
   [clojure.string :refer [split]]
   [criterium.core :refer [quick-bench]]))

(subseq (apply sorted-set (range 10)) > 2 < 8)
; (3 4 5 6 7)

(rsubseq (apply sorted-map (range 10)) <= 5)
; ([4 5] [2 3] [0 1])

(defn smallest>  [coll x] (first (subseq  coll >  x)))
(defn smallest>= [coll x] (first (subseq  coll >= x)))
(defn greatest<  [coll x] (first (rsubseq coll <  x)))
(defn greatest<= [coll x] (first (rsubseq coll <= x)))

(def coll (sorted-map "a" 5 "f" 23 "z" 12 "g" 1 "b" 0))

(smallest>  coll "f") ; ["g" 1]
(smallest>= coll "f") ; ["f" 23]
(greatest<  coll "f") ; ["b" 0]
(greatest<= coll "f") ; ["f" 23]

(def dict (into (sorted-set) (split (slurp "/usr/share/dict/words") #"\s+")))

(defn complete [w dict]
  (take 4 (subseq dict >= w)))

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

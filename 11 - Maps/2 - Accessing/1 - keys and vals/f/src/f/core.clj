(ns f.core)

(keys           {:a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :h 7 :i 8 :j 9}) ; (:e :c :j :h :b :d :f :i :a)
(vals (array-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :h 7 :i 8 :j 9)) ; (1 2 3 4 5 6 7 8 9)

(def filter-odd
  (filter (comp odd? second) {:a 1 :b 2 :c 3 :d 4}))
; ([:a 1] [:c 3])

(map type filter-odd) ; (clojure.lang.MapEntry clojure.lang.MapEntry)

(keys filter-odd) ; (:a :c)

(def matchers
  {"next generation"     10
   "incredible"          10
   "revolution"          10
   "you love"             9
   "more robust"          9
   "additional benefits"  8
   "evolve over time"     8
   "brings"               7
   "perfect"              5
   "better solution"      7
   "now with"             6})

(defn avg-xf [rf]
  (let [cnt (volatile! 0)]
    (fn
      ([]
       (rf))
      ([result]
       (rf (if (zero? @cnt)
             0.
             (float (/ result @cnt)))))
      ([result input]
       (vswap! cnt inc)
       (rf result input)))))

(defn score [text]
  (transduce
   (comp
    (map #(re-find (re-pattern %) text))
    (keep #(matchers %))
    avg-xf)
   +
   (keys matchers)))

(comment
  (keys matchers))
  ; ("more robust"
  ;  "next generation"
  ;  "now with"
  ;  "incredible"
  ;  "brings"
  ;  "evolve over time"
  ;  "perfect"
  ;  "you love"
  ;  "additional benefits"
  ;  "better solution"
  ;  "revolution")

(score
 "All-new XT600 brings all the features
  you love about XT300, now with a new design,
  improved sound and a lower price!")
; 7.3333335

(score
 "We think this book is a perfect fit for the intermediate
  or seasoned Clojure programmer who wants to understand
  how a function (and ultimately Clojure) works")
; 5.0

(def big-map (apply hash-map (range 1e7)))
; {0             1,
;  1574552 1574553,
;  9750558 9750559,
;  1741460 1741461,
;  1813432 1813433,
; …}

(time (first (keys big-map))) ; (out) "Elapsed time: 0.027602 msecs"
(time (last  (keys big-map))) ; (out) "Elapsed time: 1446.166761 msecs"

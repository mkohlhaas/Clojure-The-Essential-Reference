(ns f.core)

;; split at index     (`split-at`) 
;; split at predicate (`split-with`)

;; they return two partitions

(split-at 8 (range 10))                               ; [(0 1 2 3 4 5 6 7) (8 9)]

;; split happens on the first false evaluation of the predicate
(split-with (complement zero?) [1 4 5 0 3 2 0 1 1 0]) ; [(1 4 5) (0 3 2 0 1 1 0)]
(split-with zero? [1 4 5 0 3 2 0 1 1 0])              ; [() (1 4 5 0 3 2 0 1 1 0)]
(split-with zero? [0 0 0 1 4 5 0 3 2 0 1 1 0])        ; [(0 0 0) (1 4 5 0 3 2 0 1 1 0)]

(let [[head others] (split-at 8 (range 10))]
  (+ (last head) (last others)))
; 16

(comment
  (split-at 8 (range 10))) ; [(0 1 2 3 4 5 6 7) (8 9)]

(take 10 (last (split-at 10 (range)))) ; (10 11 12 13 14 15 16 17 18 19)

(comment
  (split-at 10 (range))) ; [(0 1 2 3 4 5 6 7 8 9) (10 11 12 13 14 15 16 17 18 19 …)]

(split-with (complement #{\a \e \i \o \u}) "hello") ; [(\h) (\e \l \l \o)]

(defn k?  [k]   (complement #{k}))
(defn kv? [k v] (complement #{[k v]}))

(split-with (k? 5) (set (range 10)))              ; [(0 7 1 4 6 3 2 9) (5 8)]
(split-with (k? 5) (apply sorted-set (range 10))) ; [(0 1 2 3 4) (5 6 7 8 9)]

(comment
  (set (range 10))               ; #{0 7 1 4 6 3 2 9 5 8}
  (apply sorted-set (range 10))) ; #{0 1 2 3 4 5 6 7 8 9}

(split-with (kv? 4 5) (apply hash-map   (range 10))) ; [([0 1]) ([4 5] [6 7] [2 3] [8 9])]
(split-with (kv? 4 5) (apply sorted-map (range 10))) ; [([0 1] [2 3]) ([4 5] [6 7] [8 9])]

(comment
  (apply hash-map   (range 10))  ; {0 1, 4 5, 6 7, 2 3, 8 9}
  (apply sorted-map (range 10))) ; {0 1, 2 3, 4 5, 6 7, 8 9}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Multiple splits with `split-by`
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn split-by [pred coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [!pred   (complement pred)
           [xs ys] (split-with !pred s)]
       (if (seq xs)
         (cons xs (split-by pred ys))
         (let [skip    (take-while pred s)
               others  (drop-while pred s)
               [xs ys] (split-with !pred others)]
           (cons (concat skip xs)
                 (split-by pred ys))))))))

(take 3 (split-by #(zero? (mod % 5)) (range))) ; ((0 1 2 3 4) (5 6 7 8 9) (10 11 12 13 14))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Implementations of `split-at` and `split-with`

(defn split-at-n [n coll]
  [(take n coll) (drop n coll)])

(defn split-with-pred [pred coll]
  [(take-while pred coll) (drop-while pred coll)])

(split-at-n 8 (range 10))                                  ; [(0 1 2 3 4 5 6 7) (8 9)]

(split-with-pred (complement zero?) [1 4 5 0 3 2 0 1 1 0]) ; [(1 4 5) (0 3 2 0 1 1 0)]

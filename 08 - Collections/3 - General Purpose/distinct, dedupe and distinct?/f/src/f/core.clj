(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(distinct  [1 2 1 1 3 2 4 1]) ; (1 2 3 4)
(distinct?  1 2 3 2 4 1)      ; false
(dedupe    [1 2 1 1 3 2 4 1]) ; (1 2 1 3 2 4 1)

;; ;;;;;
;; Votes
;; ;;;;;

(def votes [{:id 14637 :vote 3 :secs 5}
            {:id 39212 :vote 4 :secs 9}
            {:id 39212 :vote 4 :secs 9}
            {:id 14637 :vote 2 :secs 43}
            {:id 39212 :vote 4 :secs 121}
            {:id 39212 :vote 4 :secs 121}
            {:id 45678 :vote 1 :secs 19}])

(->> votes
     (group-by :id) ; {14637 [{:id 14637, :vote 3, :secs 5} {:id 14637, :vote 2, :secs 43}], 39212 [{:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 121} {:id 39212, :vote 4, :secs 121}], 45678 [{:id 45678, :vote 1, :secs 19}]}
     (reduce-kv
      (fn [m user votes]
        (assoc m user (distinct (map :vote votes))))
      {}))
; {14637 (3 2), 39212 (4), 45678 (1)}

(comment
  (map :vote [{:id 14637, :vote 3, :secs 5} {:id 14637, :vote 2, :secs 43}]) ; (3 2)
  (map :vote [{:id 39212, :vote 4, :secs 9}                                  ; (4 4 4 4)
              {:id 39212, :vote 4, :secs 9}
              {:id 39212, :vote 4, :secs 121}
              {:id 39212, :vote 4, :secs 121}]))

(->> votes
     dedupe         ; ({:id 14637, :vote 3, :secs 5} {:id 39212, :vote 4, :secs 9} {:id 14637, :vote 2, :secs 43} {:id 39212, :vote 4, :secs 121} {:id 45678, :vote 1, :secs 19})
     (group-by :id) ; {14637 [{:id 14637, :vote 3, :secs 5} {:id 14637, :vote 2, :secs 43}], 39212 [{:id 39212, :vote 4, :secs 9} {:id 39212, :vote 4, :secs 121}], 45678 [{:id 45678, :vote 1, :secs 19}]}
     (reduce-kv
      (fn [m user votes]
        (assoc m user (distinct (map :vote votes))))
      {}))
; {14637 (3 2), 39212 (4), 45678 (1)}

(def max-mask-bits 13)

(defn- shift-mask [shift mask hash]
  (-> hash
      (bit-shift-right shift)
      (bit-and mask)))

(defn- maybe-min-hash [hashes]
  (let [mask-bits (range 1 (inc max-mask-bits))
        shift-bits (range 0 31)
        masks (map #(dec (bit-shift-left 1 %)) mask-bits)
        shift-masks (for [mask masks
                          shift shift-bits]
                      [shift mask])]
    (first
     (filter
      (fn [[s m]]
        (apply distinct?
               (map #(shift-mask s m %) hashes)))
      shift-masks))))

(maybe-min-hash
 (map (memfn hashCode) [:a :b :c :d]))

;; [1 3]

;; (case op :a "a" :b "b" :c "c" :d "d")

(map #(shift-mask 1 3 %)
     (map (memfn hashCode) [:a :b :c :d]))
;; (0 2 1 3)

(sequence
 (comp
  (map range)
  cat
  (distinct))
 (range 10))
; (0 1 2 3 4 5 6 7 8)

(sequence
 (dedupe)
 [1 1 1 2 1 1 1 3 1 1])
; (1 2 1 3 1)

(def duplicates [8 1 2 1 1 7 3 3])

(distinct duplicates)       ; (8 1 2 7 3)

(dedupe (sort duplicates))  ; (1 2 3 7 8)

(set duplicates)            ; #{7 1 3 2 8}

;; `distinct` is lazy
(first (distinct (map #(do (print % ",") %) (range))))
; (out) 0 ,
; 0

;; `dedupe` is not lazy
(first (dedupe (map #(do (print % ",") %) (range))))
; (out) 0 ,1 ,2 ,3 ,4 ,5 ,6 ,7 ,8 ,9 ,10 ,11 ,12 ,13 ,14 ,15 ,16 ,17 ,18 ,19 ,20 ,21 ,22 ,23 ,24 ,25 ,26 ,27 ,28 ,29 ,30 ,31 ,32 ,
; 0

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (defn with-dupes [n]
    (shuffle
     (into [] (apply concat (take n (repeat (range n)))))))

  (let [c (with-dupes 1000)]
    (quick-bench (doall (distinct c)))            ; (out) Execution time mean : 288.674933 ms
    (quick-bench (doall (dedupe c)))              ; (out) Execution time mean : 265.701675 ms
    (quick-bench (doall (sequence (distinct) c))) ; (out) Execution time mean : 209.096584 ms
    (quick-bench (doall (sequence (dedupe) c))))) ; (out) Execution time mean : 271.276202 ms

(ns f.core)

;; helpful in creating non-trivial sequences
;; list comprehension

;; :let, :while, :when
(for [i (range 10)
      [k v] {:a "!" :b "?" :c "$"}
      :let [s (str i k v)]
      :while (not= :b k)
      :when (odd? i)]
  s)
; ("1:a!" "3:a!" "5:a!" "7:a!" "9:a!")

;; square numbers
(for [x (range 100)
      y (range 10)
      :when (= x (* y y))]
  [y x])
; ([0 0] [1 1] [2 4] [3 9] [4 16] [5 25] [6 36] [7 49] [8 64] [9 81])

(take 6
      (for [i (range)
            j (range)
            :while (< j 3)]
        (str i j)))
; ("00" "01" "02" "10" "11" "12")

(comment
  ;; WARNING: never ending.
  ;; sequence consists only of 2 elements, can't take 3, waits forever
  (take 3
        (for [i (range)
              j ["a" "b"]
              :when (= i 1)]
          (str i j)))) ; ("1a" "1b")

;; :while affects the immediately preceding binding expression
(for [x (range) :while (< x 4)
      y (range) :while (<= y x)]
  (+ x y))
; (0 1 2 2 3 4 3 4 5 6)

;; ;;;;;;;;;;;;;;;;;;;;;
;; Conway's Game of Life
;; ;;;;;;;;;;;;;;;;;;;;;

;; h = height of grid
;; w = width  of grid
;; cells = set of living x,y-pairs

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

(comment
  ;; commented-out filter and count
  (count-neighbors 5 5 0 0 nil)  ; ([0 1] [1 0] [1 1])
  (count-neighbors 5 5 1 1 nil)) ; ([0 0] [0 1] [0 2] [1 0] [1 2] [2 0] [2 1] [2 2])

;; Conway's rules
(defn under-populated? [n alive?] (and (< n 2) alive?))
(defn over-populated?  [n alive?] (and (> n 3) alive?))
(defn healthy?         [n alive?] (or (and alive? (= n 2)) (= n 3)))
(defn reproduce?       [n alive?] (and (= n 3) (not alive?)))

;; apply Conway's rules to cell x,y
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

;; testing a blinker:
(comment
  (next-gen 5 5 #{[2 1] [2 2] [2 3]})                 ; #{[1 2] [2 2] [3 2]}
  (next-gen 5 5 (next-gen 5 5 #{[2 1] [2 2] [2 3]}))) ; #{[2 1] [2 2] [2 3]}

;; blinker?
(let [init #{[2 1] [2 2] [2 3]}
      ng1  (next-gen 5 5 init)
      ng2  (next-gen 5 5 ng1)]
  (= init ng2))
; true

;; ;;;;;;;;;;;
;; Poker Cards
;; ;;;;;;;;;;;

;; too complicated
(count
 (mapcat
  (fn [i]
    (map
     (fn [a] (str i "-" a))
     ["D" "C" "H" "S"]))
  (range 1 14))) ; 52
; ("1-D" "1-C" "1-H" "1-S"
;  "2-D" "2-C" "2-H" "2-S"
;  "3-D" "3-C" "3-H" "3-S"
;  "4-D" "4-C" "4-H" "4-S"
;  "5-D" "5-C" "5-H" "5-S"
;  "6-D" "6-C" "6-H" "6-S"
;  "7-D" "7-C" "7-H" "7-S"
;  "8-D" "8-C" "8-H" "8-S"
;  "9-D" "9-C" "9-H" "9-S"
;  "10-D" "10-C" "10-H" "10-S"
;  "11-D" "11-C" "11-H" "11-S"
;  "12-D" "12-C" "12-H" "12-S"
;  "13-D" "13-C" "13-H" "13-S")

;; sweet
(count
 (for [i (range 1 14)
       a ["D" "C" "H" "S"]
       :let [card (str i "-" a)]]
   card)) ; 52
; ("1-D" "1-C" "1-H" "1-S"
;  "2-D" "2-C" "2-H" "2-S"
;  "3-D" "3-C" "3-H" "3-S"
;  "4-D" "4-C" "4-H" "4-S"
;  "5-D" "5-C" "5-H" "5-S"
;  "6-D" "6-C" "6-H" "6-S"
;  "7-D" "7-C" "7-H" "7-S"
;  "8-D" "8-C" "8-H" "8-S"
;  "9-D" "9-C" "9-H" "9-S"
;  "10-D" "10-C" "10-H" "10-S"
;  "11-D" "11-C" "11-H" "11-S"
;  "12-D" "12-C" "12-H" "12-S"
;  "13-D" "13-C" "13-H" "13-S")

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(count
 (for [a1 (range 10)
       a2 (range 10)
       a3 (range 10)
       a4 (range 10)
       a5 (range 10)]
   (+ a1 a2 a3 a4 a5)))
; 100000

;; `for` can be thought as a sophisticated machine for lazy-sequence building
(comment
  (macroexpand
   '(for [i (range 3)] i)))

  ;; (let* [main-fn
  ;;        (fn recur-fn [xs]
  ;;          (lazy-seq
  ;;           (loop [xs xs]
  ;;             (when-let [xs (seq xs)]
  ;;               (if (chunked-seq? xs)
  ;;                 (let [fchunk (chunk-first xs)
  ;;                       chunk-size (int (count fchunk))
  ;;                       chunk-buff (chunk-buffer chunk-size)]
  ;;                   (if (loop [i (int 0)]
  ;;                         (if (< i chunk-size)
  ;;                           (let [i (.nth fchunk i)]
  ;;                             (do (chunk-append chunk-buff i)
  ;;                                 (recur (unchecked-inc i))))
  ;;                           true))
  ;;                     (chunk-cons
  ;;                      (chunk chunk-buff)
  ;;                      (recur-fn (chunk-rest xs)))
  ;;                     (chunk-cons (chunk chunk-buff) nil)))
  ;;                 (let [i (first xs)]
  ;;                   (cons i (recur-fn (rest xs)))))))))]
  ;;       (main-fn (range 3)))

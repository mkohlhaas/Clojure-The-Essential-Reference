(ns f.core)

;; returns items from coll with given probability (between 0 and 1)
(random-sample 0.5 (range 10))
; (4 5 9)
; (2 4 6 9)
; (4 5 6 9)
; (0 3 4 5 6 7)
; (0 1 3 6 9)
; (0 1 3 4 6)
; (0 3 4 7 8)
; (0 1 2 3 4 5 6 7 9)

;; ;;;;;;;;;
;; Coin Flip
;; ;;;;;;;;;

;; `random-sample` returns a transducer when no collection is provided
(defn x-flip [n]
  (comp (take n) (random-sample 0.5)))

(def head-tail-stream
  (interleave (repeat "head") (repeat "tail")))

(comment
  head-tail-stream)
  ; ("head" "tail" "head" "tail" "head" "tail" "head" …)

(defn flip-up-to [n]
  (into [] (x-flip n) head-tail-stream))

(flip-up-to 10)
; ["tail" "head" "tail" "head"]
; ["head" "head" "head" "tail" "head"]
; ["tail"]
; ["head" "head" "tail" "tail" "tail"]
; ["tail" "head" "tail" "tail"]

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(take 10 (random-sample 0.01 (cycle (range 10))))
; (1 5 6 9 8 5 2 3 6 9)
; (2 0 4 1 5 8 2 7 6 8)
; (8 9 7 5 4 7 2 2 6 4)
; (7 7 9 4 5 6 3 1 7 2)

(take 10 (random-sample 0.99 (cycle (range 10))))
; (0 1 2 3 4 5 6 7 8 9)
; (0 1 2 3 4 5 6 7 8 9)
; (0 1 2 3 4 5 6 7 8 9)
; (0 1 2 3 4 5 6 7 8 9)
; (0 2 3 4 5 6 7 8 9 0)
; (0 1 3 4 5 6 7 8 9 0)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;
;; Random Password Generator
;; ;;;;;;;;;;;;;;;;;;;;;;;;;

(def letters  (map char (range (int \a) (inc (int \z)))))
(def LETTERS  (map #(Character/toUpperCase %) letters))
(def symbols  "!@£$%^&*()_+=-±§}{][|><?")
(def numbers  (range 10))
(def alphabet (concat letters LETTERS symbols numbers))

(defn generate-password [n]
  (->> (cycle alphabet)
       (random-sample 0.01) ; output will be very different from input
       (take n)
       (apply str)))

(generate-password 10)
; "swN)(yJ=Me"
; "Ic7td£±ipv"
; "c£pve8?opO"
; ")%gk01M%{T"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;           
;; Controlling the Sample Size           
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;           

;; Reservoir Sampling
;; select randomly k elements from s
(defn random-subset [k s]
  (loop [cnt 0 res [] [head & others] s]
    (if head
      (if (< cnt k)
        (recur (inc cnt) (conj res head) others)
        (let [idx (rand-int cnt)]
          (if (< idx k)
            (recur (inc cnt) (assoc res idx head) others)
            (recur (inc cnt) res others))))
      res)))

(random-subset 5 (range 10))
; [0 9 5 3 6]
; [0 7 2 5 4]
; [0 6 9 8 4]
; [7 9 2 3 6]

(random-subset 5 (range 10000))
; [9895 6809 305 205 2742]
; [6399 1486 3385 6326 3531]
; [7767 2554 6861 4559 4134]
; [2477 4091 6118 3613 7237]

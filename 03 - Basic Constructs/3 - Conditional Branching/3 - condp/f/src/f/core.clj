(ns f.core
  (:require [clojure.string :refer [split]]))

;; `condp` throws IllegalArgumentException when a matching clause cannot 
;; be found and no default is provided - does't return nil!!!

;; uses the first evaluation returning logical true

;; When the :>> (so-called "needle") keyword is present in the clause, the last element of the triplet 
;; is considered a function and invoked with the result of the predicate.

(defn op [sel]
  (condp = sel
    "plus"  +   ; (= "plus"  sel)
    "minus" -   ; (= "minus" sel)
    "mult"  *   ; (= "mult"  sel)
    "div"   /)) ; (= "div"   sel)

((op "mult") 3 3)
; 9

;; ;;;;;;;;;;
;; Mime Types
;; ;;;;;;;;;;

(defn extension [url]
  (last (split url #"\.")))

(comment
  (extension "http://example.com/image.jpg"))
  ; "jpg"

(defn mime-type [url]
  (let [ext (extension url)]
    (condp = ext
      "jpg" "image/jpeg"            ; (="jpg" ext)
      "png" "image/png"             ; (="png" ext)
      "bmp" "image/bmp"             ; (="bmp" ext)
      "application/octet-stream"))) ; default

(mime-type "http://example.com/image.jpg")  ; "image/jpeg"
(mime-type "http://example.com/binary.bin") ; "application/octet-stream"

;; ;;;;;;;;;
;; Fizz Buzz
;; ;;;;;;;;;

(defn fizz-buzz [n]
  (condp #(zero? (mod %2 %1)) n
    15 "fizzbuzz" ; (#(zero? (mod %2 %1)) 15 n)
    3  "fizz"     ; (#(zero? (mod %2 %1))  3 n)
    5  "buzz"     ; (#(zero? (mod %2 %1))  5 n)
    n))

(map fizz-buzz (range 1 20))
; (1 2 "fizz" 4 "buzz" "fizz" 7 8 "fizz" "buzz" 11 "fizz" 13 14 "fizzbuzz" 16 17 "fizz" 19)

;; ;;;;;
;; Poker
;; ;;;;;

;; not all rules are implemented

(def card-rank first)
(def card-suit second)

(comment
  ;; clubs (♣) is ":c", diamonds (♦) is ":d", hearts (♥) is ":h" and spades (♠) is ":s":
  (map card-rank #{[2 :h] [2 :s] [2 :c] [2 :d] [8 :h]})  ; (8 2 2 2 2)
  (map card-suit #{[2 :h] [2 :s] [2 :c] [2 :d] [8 :h]})) ; (:h :c :h :d :s)

(defn freq-by-rank [hand]
  (->> hand
       (map card-rank)
       frequencies))

(comment
  (freq-by-rank #{[2 :h] [2 :s] [2 :c] [2 :d] [8 :h]})) ; {8 1, 2 4}

(defn sort-by-rank [hand]
  (->> hand
       (map card-rank)
       sort))

(comment
  (sort-by-rank #{[2 :h] [2 :s] [2 :c] [2 :d] [8 :h]})) ; (2 2 2 2 8)

(defn max-rank [hand]
  (->> hand
       freq-by-rank
       (sort-by second)
       second
       first))

(comment
  (max-rank #{[2 :h] [2 :s] [2 :c] [2 :d] [8 :h]})) ; 2

(defn- n-of-a-kind [hand n]
  (when (->> hand
             freq-by-rank
             vals
             (some #{n}))
    hand))

(comment
  (n-of-a-kind #{[2 :h] [2 :s] [2 :c] [2 :d] [8 :h]} 4))
; #{[8 :h] [2 :c] [2 :h] [2 :d] [2 :s]}

(defn three-of-a-kind [hand]
  (n-of-a-kind hand 3))

(defn four-of-a-kind [hand]
  (n-of-a-kind hand 4))

(comment
  (four-of-a-kind #{[2 :h] [2 :s] [2 :c] [2 :d] [8 :h]}))
  ; #{[8 :h] [2 :c] [2 :h] [2 :d] [2 :s]}

(defn straight-flush [hand]
  (let [sorted   (sort-by-rank hand)
        lower    (card-rank sorted)
        expected (range lower (+ 5 lower))]
    (when (and (= sorted expected)             ; increasing ranks
               (apply = (map card-suit hand))) ; same suit
      hand)))

(defn n-of-a-kind-highest [hands]
  (->> hands
       (sort-by max-rank)
       last))

(defn straight-flush-highest [hands]
  (->> hands
       (filter straight-flush)
       (sort-by (comp card-rank sort-by-rank))
       card-suit))

(defn game [players]
  (sort
   (condp (comp seq filter) players
     straight-flush  :>> straight-flush-highest ; ((comp seq filter) straight-flush players) => (seq (filter straight-flush players))
     four-of-a-kind  :>> n-of-a-kind-highest
     three-of-a-kind :>> n-of-a-kind-highest
     (n-of-a-kind-highest players))))

(def five-players
  [#{[2 :h] [2 :s] [2 :c] [2 :d]  [8 :h]}
   #{[8 :h] [1 :h] [1 :s] [1 :c]  [1 :d]}
   #{[2 :h] [2 :s] [2 :d] [12 :s] [12 :h]}
   #{[5 :d] [4 :s] [7 :d] [14 :s] [14 :h]}
   #{[8 :s] [4 :c] [3 :d] [10 :s] [10 :h]}])

(comment
  ((comp seq filter) four-of-a-kind five-players)
  ; (#{[8 :h] [2 :c] [2 :h] [2 :d] [2 :s]}
  ;  #{[8 :h] [1 :d] [1 :s] [1 :h] [1 :c]})

  (n-of-a-kind-highest ((comp seq filter) four-of-a-kind five-players)))
  ; #{[8 :h] [2 :c] [2 :h] [2 :d] [2 :s]}

;; five players
(game five-players)
; ([2 :c] [2 :d] [2 :h] [2 :s] [8 :h])

(def three-players
  [#{[1 :h] [1 :s] [1 :c] [1 :d] [8 :h]}
   #{[4 :d] [5 :d] [6 :d] [7 :d] [8 :d]}
   #{[3 :h] [5 :h] [4 :h] [7 :h] [6 :h]}])

;; three players
(game three-players)
; ([4 :d] [5 :d] [6 :d] [7 :d] [8 :d])

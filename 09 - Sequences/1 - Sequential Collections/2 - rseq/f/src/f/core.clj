(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(rseq [:b :a :c :d])
; (:d :c :a :b)

(rseq (sorted-map :d 0 :b 3 :a 2))
; ([:d 0] [:b 3] [:a 2])

(conj (rseq [1 2 3]) :a)
; (:a 3 2 1)

(defn complement-dna [nucl]
  ({\a \t \t \a \c \g \g \c} nucl))

(defn is-palindrome? [dna]
  (= (map complement-dna dna) (rseq dna)))

(defn find-palindromes [dna]
  (for [i     (range (count dna))
        j     (range (inc i) (count dna))
        :when (is-palindrome? (subvec dna i (inc j)))]
    [i j]))

(mapv complement-dna [\a \c \c \t \a \g \g \t])
; [\t \g \g \a \t \c \c \a]

(is-palindrome?   [\a])                      ; false
(is-palindrome?   [\a \c \c \t \a \g \g \t]) ; true
(find-palindromes [\a \c \g \t])             ; ([0 3] [1 2])

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (defn complement-dna [nucleotide]
    ({\a \t \t \a \c \g \g \c} nucleotide))

  (defn random-dna [n]
    (repeatedly n #(rand-nth [\a \c \g \t])))

  (defn palindrome-reverse? [dna]
    (= (map complement-dna dna) (reverse dna)))

  (defn palindrome-rseq? [dna]
    (= (map complement-dna dna) (rseq dna)))

  (let [dna (random-dna 1e4)]
    (quick-bench (palindrome-reverse? dna)))
;; Execution time mean : 834.510161 µs

  (let [dna (vec (random-dna 1e4))]
    (quick-bench (palindrome-rseq? dna)))
;; Execution time mean : 2.940745 µs ; <5>

  (let [dna (apply concat (repeat 10000 [\a \c \c \t \a \g \g \t]))]
    (quick-bench (palindrome-reverse? dna)))
;; Execution time mean : 12.991438 ms

  (let [dna (vec (apply concat (repeat 10000 [\a \c \c \t \a \g \g \t])))]
    (quick-bench (palindrome-rseq? dna))))
;; Execution time mean : 11.238614 ms ; <6>

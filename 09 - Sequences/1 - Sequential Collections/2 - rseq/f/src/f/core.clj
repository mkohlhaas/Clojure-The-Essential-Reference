(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

;; `rseq` provides a constant time reverse version for vectors, sorted maps and sorted sets.

(reversible? [:b :a :c :d])               ; true
(reversible? (sorted-map :d 0 :b 3 :a 2)) ; true

(rseq [:b :a :c :d]) ; (:d :c :a :b)

(sorted-map :d 0 :b 3 :a 2)        ; {:a 2, :b 3, :d 0}
(rseq (sorted-map :d 0 :b 3 :a 2)) ; ([:d 0] [:b 3] [:a 2])

(rseq [1 2 3])           ; (3 2 1)
(conj (rseq [1 2 3]) :a) ; (:a 3 2 1)

(reverse [])  ; ()
(reverse nil) ; ()

(rseq    [])  ; nil
(comment
  (rseq nil))
  ; (err) Execution error (NullPointerException) at f.core/eval4819 (form-init11214441101521458985.clj:17).

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; Palindromes in DNA Sequences

(defn complement-dna [nucleotide]
  ({\a \t \t \a \c \g \g \c} nucleotide))

(comment
  (complement-dna \a)                             ; \t
  (complement-dna \t)                             ; \a
  (complement-dna \c)                             ; \g
  (complement-dna \g)                             ; \c

  (map complement-dna [\a \t \c \g])              ; (\t \a \g \c)
  (rseq               [\a \t \c \g])              ; (\g \c \t \a)

  (rseq               [\a \c \c \t \a \g \g \t])  ; (\t \g \g \a \t \c \c \a)
  (map complement-dna [\a \c \c \t \a \g \g \t])) ; (\t \g \g \a \t \c \c \a)

(defn is-dna-palindrome? [dna]
  (= (map complement-dna dna) (rseq dna)))

(comment
  (is-dna-palindrome? [\a \c \c \t \a \g \g \t])) ; true

;; find dna-palindromes in dna sequence
(defn find-palindromes [dna]
  (for [i     (range (count dna))
        j     (range (inc i) (count dna))
        :when (is-dna-palindrome? (subvec dna i (inc j)))]
    [i j]))

(mapv complement-dna [\a \c \c \t \a \g \g \t])
; [\t \g \g \a \t \c \c \a]

(is-dna-palindrome?   [\a])                      ; false
(is-dna-palindrome?   [\a \c \c \t \a \g \g \t]) ; true

(find-palindromes [\a \c \g \t])                 ; ([0 3] [1 2])

(comment
  (is-dna-palindrome? [\a \c \g \t])             ; true
  (is-dna-palindrome? [\c \g]))                  ; true

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
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
  ;; Execution time mean : 2.940745 µs

  (let [dna (apply concat (repeat 10000 [\a \c \c \t \a \g \g \t]))]
    (quick-bench (palindrome-reverse? dna)))
  ;; Execution time mean : 12.991438 ms

  (let [dna (vec (apply concat (repeat 10000 [\a \c \c \t \a \g \g \t])))]
    (quick-bench (palindrome-rseq? dna))))
  ;; Execution time mean : 11.238614 ms

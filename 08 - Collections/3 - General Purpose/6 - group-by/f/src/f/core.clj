(ns f.core)

;; returns a hash-map
(group-by first ["John" "Rob" "Emma" "Rachel" "Jim"])
; {\J ["John" "Jim"], \R ["Rob" "Rachel"], \E ["Emma"]}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; `juxt` determines the creation of a composite key vector
(group-by (juxt odd? (comp count str)) (range 20))
; {[false 1] [0 2 4 6 8],
;  [true  1] [1 3 5 7 9],
;  [false 2] [10 12 14 16 18],
;  [true  2] [11 13 15 17 19]}

;; ;;;;;;;;
;; Anagrams
;; ;;;;;;;;

;; anagrams are permutations of the same group of letters

(def dict (slurp "/usr/share/dict/words"))

(->> dict
     (re-seq #"\S+")                 ; ("A" "a" "AA" "AAA" "Aachen" "Aachen's" "aah" "Aaliyah" "Aaliyah's" …)
     (group-by sort)                 ; {(\a \a \c \e \f \g \n \r \r \s) ["fragrances"], (\d \e \g \i \i \k \l \l \n \s) ["deskilling"], (\g \g \i \l \n \n \o \u) ["lounging"], (\c \i \i \l \o \o \s \s \s) ["scoliosis"], …}
     (sort-by (comp count second) >) ; ([(\a \e \r \s \t) ["aster" "rates" "resat" "stare" "tares" "taser" "tears" "treas"]] [(\a \c \e \r \s \t)] ["carets" "caster" "caters" "crates" "reacts" "recast" "traces"] …)
     (map second)                    ; (["aster" "rates" "resat" "stare" "tares" "taser" "tears" "treas"] ["carets" "caster" "caters" "crates" "reacts" "recast" "traces"] …)
     first)                          ; ["aster" "rates" "resat" "stare" "tares" "taser" "tears" "treas"]
; ["aster" "rates" "resat" "stare" "tares" "taser" "tears" "treas"]

(comment
  (sort "fragrances")) ; (\a \a \c \e \f \g \n \r \r \s)

;; presence of the letter "x" in a word
(->> dict
     (re-seq #"\S+")                            ; ("A" "a" "AA" "AAA" "Aachen" "Aachen's" …)
     (group-by (juxt #(some #{\x \X} %) sort))  ; {[nil (\f \f \g \i \n \o \s)] ["offings"], [nil (\b \i \i \i \i \l \r \s \t \y)] ["risibility"], [nil (\' \e \e \p \s)] ["pee's"], [nil (\V \a \c \e \i \n \o \r)] ["Veronica"], [nil (\c \p \s \t \u \u)] ["cutups"], …}
     (filter ffirst)                            ; ([[\x (\a \e \h \i \l \m \o \o \s \t \u \x \y)] ["homosexuality"]] [[\x (\' \W \c \i \l \o \s \x)] ["Wilcox's"]] [[\x (\a \s \x)] ["sax"]] [[\x (\a \b \e \i \l \n \o \r \x \y)] ["inexorably"]] [[\x (\a \c \o \r \s \t \x)] ["oxcarts"]])
     (sort-by (comp count second) >)            ; ([[\x (\a \c \e \e \r \t \x)] ["exacter" "excreta"]] [[\x (\i \v \x \x \x)] ["xxxiv" "xxxvi"]] [[\x (\c \e \e \p \t \x)] ["except" "expect"]] [[\x (\c \d \e \o \x)] ["codex" "coxed"]] [[\x (\i \v \x)] ["xiv" "xvi"]])
     (map second)                               ; (["exacter" "excreta"] ["xxxiv" "xxxvi"] ["except" "expect"] ["codex" "coxed"] ["xiv" "xvi"])
     (take 3))                                  ; (["exacter" "excreta"] ["xxxiv" "xxxvi"] ["except" "expect"])
; (["exacter" "excreta"] ["xxxiv" "xxxvi"] ["except" "expect"])

(comment
  (some #{\x \X} "exacter")) ; \x

(ns f.core)

(rand-nth (range 10))                   ; 1
(rand-nth "abcdefghijklmnopqrstuvwxyz") ; \y
(rand-nth nil)                          ; nil

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn roll-dice []
  (rand-nth [1 2 3 4 5 6]))

(defn flip-coin []
  (rand-nth ["heads" "tails"]))

(roll-dice) ; 2
(flip-coin) ; "tails"

;; ;;;;;;;;;;;;;;;;;
;; Proverb Generator
;; ;;;;;;;;;;;;;;;;;

(def article   ["A" "The" "A" "All"])
(def adjective ["nearer" "best" "better" "darkest" "good" "bad" "hard" "long" "sharp"])
(def subject   ["fool" "wise" "penny" "change" "friend" "family" "proof" "necessity" "experience" "honesty" "no one" "everyone" "every"])
(def action    ["is" "is not" "are" "are not" "help" "be" "create"])
(def ending    ["dying." "a dangerous thing." "a lot of noise." "no pain." "stronger than words." "those who fall." "nothing."])

(def grammar
  [article adjective subject action ending])

(defn to-sentence [grammar]
  (->> grammar
       (map rand-nth)
       (interpose " ")
       (apply str)))

(defn generate
  ([]
   (generate 1))
  ([n]
   (repeatedly n #(to-sentence grammar))))

(generate)
; ("All better change are no pain.")
; ("A better no one is those who fall.")
; ("A nearer honesty are not a lot of noise.")
; ("All sharp fool help those who fall.")

(generate 5)
; ("A sharp wise are not no pain."
;  "All good family create a lot of noise."
;  "A hard everyone help stronger than words."
;  "The best every are not dying."
;  "A sharp penny is not dying.")

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; the entire lazy sequence is realized
;; prints 100 dots
(def n1 (rand-nth (map #(do (print ".") %) (range 100)))) ; #'f.core/n1
; (out) ....................................................................................................

n1 ; 54

;; realization depends on the random number
;; prints 32 to 100 dots
(def n2 (nth (map #(do (print ".") %) (range 100)) (rand-int 100)))
; (out) ................................

n2 ; 9

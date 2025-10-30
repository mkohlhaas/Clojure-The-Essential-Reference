(ns f.core)

;; all items
(every?     pos? [1 2 3 4])    ; true
(not-every? neg? [-1 -2 0 -3]) ; true

;; at least one item
(some       neg? [-1 -2 0 -3]) ; true
(not-any?   neg? [1 2 0 3])    ; true

;; `every?`, `not-every?`, `not-any?` return boolean value (therefore the `?`).
;; `some` returns the first predicate result that is not nil.

;; `some`
;; Returns the result of applying the predicate to an element directly (there is no boolean transformation).
;; Never returns false! If "el" is an element in "coll" and the result of (pred el) is false, some doesn’t stop but continues with the next item.
;; Returns true only when true is an element of the input collection (and the predicate returns true).
;; Returns nil only if the predicate evaluated nil or false for all elements in the input.

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def address-book
  [{:phone "664 223 8971" :name "Jack"}
   {:phone "222 231 9008" :name "Sarah"}
   {:phone "343 928 9911" :name "Oliver"}])

;; with `filter` and `first`
(->> address-book
     (filter #(= "222 231 9008" (:phone %))) ; ({:phone "222 231 9008", :name "Sarah"})
     first)
; {:phone "222 231 9008" :name "Sarah"}

;; easier with `some`
(some #(when (= "222 231 9008" (:phone %)) %) address-book)
; {:phone "222 231 9008" :name "Sarah"}

;; all collections in a list must have at least one element each
(every? seq (list [:g] [:a :b] [] [:c])) ; false

;; ;;;;;
;; Bingo
;; ;;;;;

(def cards [[37 2 94 4 38] [20 16 87 19 1] [87 20 16 38 4]])

(let [drawn #{4 38 20 16 87}]
  (defn bingo? [card]
    (every? drawn card)))

(comment
  (#{4 38 20 16 87} 20) ; 20
  (#{4 38 20 16 87} 21) ; nil

  (every? #{4 38 20 16 87} [20]) ; true

  (bingo? [4 38 20 16 87]) ; true
  (bingo? [37 2 94 4 38])) ; false

(map bingo? cards) ; (false false true)

(def prizes {"AB334XC" "2 Weeks in Mexico"
             "QA187ZA" "Vespa Scooter"
             "EF133KX" "Gold jewelry set"
             "RE395GG" "65 inches TV set"
             "DF784RW" "Bicycle"})

;; hash-map as a predicate function
(defn win [tickets]
  (or (some prizes tickets) "Sorry, try again"))

(comment
  "returns map's value"
  (some prizes ["TA818GS" "RE395GG" "JJ148XN"])) ; "65 inches TV set"

(win ["TA818GS" "RE395GG" "JJ148XN"]) ; "65 inches TV set"
(win ["MP357SQ" "MB263DK" "QA187ZA"]) ; "Vespa Scooter"
(win ["MP357SQ" "MB263DK" "HF359PB"]) ; "Sorry, try again"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; `every?` and the vacuous truth
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; every statement about the empty set is true
(every? pos? []) ; true
(every? neg? []) ; true

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; one billion iterations
  (time (every? pos? (range 1 (long 1e9)))))
  ; (out) "Elapsed time: 131911.258799 msecs"

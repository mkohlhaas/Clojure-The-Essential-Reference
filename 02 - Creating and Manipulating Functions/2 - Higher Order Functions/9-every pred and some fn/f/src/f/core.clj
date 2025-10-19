(ns f.core
  (:require
   [clojure.string :refer [split]]
   [criterium.core :refer [quick-bench]]))

;; a tricky expression that seems to work correctly when it’s not:
(remove (and number? pos? odd?) (range 10))              ; (0 2 4 6 8)
;; `and` is a macro and its evaluation happens while the form is compiling, resulting in the following being evaluated.
;; The `and` expression returns the last value that is not false or nil, which is the function `odd?` in this case:
(remove odd? (range 10))                                 ; (0 2 4 6 8)
;; we could do something like this:
(remove #(and (number? %) (pos? %) (odd? %)) (range 10)) ; (0 2 4 6 8)

;; ;;;;;;;;;;;
;; Palindromes
;; ;;;;;;;;;;;

;; ;;;;;;;;;;
;; every-pred
;; ;;;;;;;;;;

(defn symmetric? [xs]
  (= (seq xs) (reverse xs)))

;; The situation becomes worse the more predicates need to be combined:
(defn palindromes1 [coll]
  (filter
   (fn [word]
     (and
      (some? word)
      (string? word)
      (not-empty word)
      (symmetric? word)))
   coll))

(palindromes1 ["a" nil :abba 1 "" "racecar" "abba" \a])
; ("a" "racecar" "abba")

;; `every-pred` makes this much more succinct
(defn palindromes2 [coll]
  (filter
   (every-pred some? string? not-empty symmetric?)
   coll))

(palindromes2 ["a" nil :abba 1 "" "racecar" "abba" \a])
; ("a" "racecar" "abba")

;; assigning `every-pred` a name
(def palindrome?
  (every-pred some? string? not-empty symmetric?))

(defn palindromes3 [coll]
  (filter palindrome?  coll))

(palindromes3 ["a" nil :abba 1 "" "racecar" "abba" \a])
; ("a" "racecar" "abba")

;; ;;;;
;; Spam
;; ;;;;

;; ;;;;;;;
;; some-fn
;; ;;;;;;;

;; three functions which take a single arg - `words` is a seq of words
(defn any-unwanted-word? [words]
  (some #{"free" "sexy" "click"} words))

(comment
  ;; using set as a predicate function
  (some #{"free" "sexy" "click"} ["from:" "alex@tiv.com" "just" "wanted" "to" "say" "hi."])          ; nil
  (some #{"free" "sexy" "click"} ["from:" "nobody@all.tw," "click" "here" "for" "a" "free" "gift."]) ; "click"
  (#{"free" "sexy" "click"} "from:")                                                                 ; nil
  (#{"free" "sexy" "click"} "free"))                                                                 ; "free"

(defn any-link? [words]
  (some #(re-find #"http[s]?://.*\." %) words))

(defn any-blacklisted-sender? [words]
  (some #{"spamz@email.com" "phish@now.co.uk"} words))

;; combining these three functions with `some-fn`
(def spam?
  (some-fn any-unwanted-word? any-link? any-blacklisted-sender?))

(defn words [email]
  (split email #"\s+"))

(spam? (words "from: alex@tiv.com just wanted to say hi."))        ; nil
(spam? (words "from: nobody@all.tw, click here for a free gift.")) ; "click"

(comment
  (when-let [match (spam? (words "from: nobody@all.tw, click here for a free gift."))]
    (throw (Exception. (str "Spam found: " match)))))
  ; (err) Spam found: click

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; `every-fn` vs. `every-pred`
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; naming: `every-pred` vs. `some-fn`
;; every-pred returns true/false           -> `-pred`
;; some-fn    returns first matching value -> `-fn`

;; Why is there no `every-fn` in the standard?
;; How an `every-fn` could be implemented:
(defn every-fn [& ps]
  (fn [& xs]
    (partition (count ps)
               (for [x xs
                     p ps]
                 (p x)))))

(def contains-two? #(re-find #"two" %))
(def is-7-long? #(= 7 (count %)))

(comment
  (for [x ["guestimate" "artwork" "threefold"]
        p [contains-two? is-7-long?]]
    (p x)))
  ; (nil false "two" true nil false)

;; `every-fn` returns pretty useless values:
((every-fn contains-two? is-7-long?) "guestimate" "artwork" "threefold")
; ((nil false) ("two" true) (nil false))

;; `every-pred` returns something useful:
((every-pred contains-two? is-7-long?) "guestimate" "artwork" "threefold")
;; false

;; with `juxt` this would be much simpler:
(map (juxt contains-two? is-7-long?) ["guestimate" "artwork" "threefold"])
; ([nil false] ["two" true] [nil false])

;; In other words, `every-fn` is pretty useless!

;; ;;;;;;;;;;;;
;; Benchmarking
;; ;;;;;;;;;;;;

#_{:clj-kondo/ignore [:type-mismatch]}
(comment
  ;; `every-pred` is optimized for up to three parameters
  (quick-bench (every-pred 1))          ; (out) Execution time mean :  9.229305 ns
  (quick-bench (every-pred 1 2))        ; (out) Execution time mean : 10.418893 ns
  (quick-bench (every-pred 1 2 3))      ; (out) Execution time mean : 13.842862 ns
  (quick-bench (every-pred 1 2 3 4))    ; (out) Execution time mean : 69.143181 ns
  (quick-bench (every-pred 1 2 3 4 5))  ; (out) Execution time mean : 83.530496 ns

  ;; `every-pred` is optimized for up to three parameters
  (defn p [_x] true)

  (let [e1 (every-pred p)
        e2 (every-pred p p)
        e3 (every-pred p p p)
        e4 (every-pred p p p p)
        e5 (every-pred p p p p p)]
    (quick-bench (e1 1))    ; (out) Execution time mean :  10.926075 ns
    (quick-bench (e2 1))    ; (out) Execution time mean :  30.914307 ns
    (quick-bench (e3 1))    ; (out) Execution time mean :  33.072156 ns
    (quick-bench (e4 1))    ; (out) Execution time mean : 326.318560 ns
    (quick-bench (e5 1))))  ; (out) Execution time mean : 421.742068 ns

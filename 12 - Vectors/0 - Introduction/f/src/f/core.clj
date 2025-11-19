(ns f.core)

[:a :b :c]     ; [:a :b :c]

(ifn? [:a :b :c]) ; true
([:a :b :c] 2)    ; :c

(comment
  ([:a :b :c] 3))
  ; (err) Execution error (IndexOutOfBoundsException)

(get   [:a :b :c] 2)      ; :c
(nth   [:a :b :c] 2)      ; :c
(assoc [:a :b :c] 2 :d)   ; [:a :b :d]
(conj  [:a :b :c] 3.1 :e) ; [:a :b :c 3.1 :e]
(pop   [:a :b :c])        ; [:a :b]
(peek  [:a :b :c])        ; :c

(contains? [1 2 :a :b] 3)  ; true
(contains? [1 2 :a :b] :a) ; false
(.contains [1 2 :a :b] 3)  ; false
(.contains [1 2 :a :b] :a) ; true

;; all seq-able functions work
(first [:a :b :c]) ; :a

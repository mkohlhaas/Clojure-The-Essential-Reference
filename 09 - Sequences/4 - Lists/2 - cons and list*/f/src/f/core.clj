(ns f.core)

;; Despite the name, `list*` creates a clojure.lang.Cons 
;; not a clojure.lang.PersistentList (as `list` does).
;; `cons*` would likely be a better name.

(cons :a [1 2 3])                       ; (:a 1 2 3)
(cons 1 (cons 2 (cons 3 (cons 4 ()))))  ; (1 2 3 4)
(list* 1 2 3 4 5 ())                    ; (1 2 3 4 5)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(comment
  (list* -2 -1 (range 10) ())        ; (-2 -1 (0 1 2 3 4 5 6 7 8 9))
  (apply list* -2 -1 (range 10) ())) ; (-2 -1 0 1 2 3 4 5 6 7 8 9)

(def l (apply list* -2 -1 (range 10) ()))
; (-2 -1 0 1 2 3 4 5 6 7 8 9)

(type l)         ; clojure.lang.Cons
(type (next  l)) ; clojure.lang.Cons
(type (nnext l)) ; clojure.lang.LongRange

(comment
  (range 9 -3 -1)) ; (9 8 7 6 5 4 3 2 1 0 -1 -2)

(def l1 (reduce #(cons %2 %1) () (range 9 -3 -1)))
; (-2 -1 0 1 2 3 4 5 6 7 8 9)

(type (nthrest l1 10))  ; clojure.lang.Cons

(defn lazy-loop [xs]
  (lazy-seq
   (when-first [x xs]
     (cons x (lazy-loop (rest xs))))))

(last (lazy-loop (range 100000))) ; 99999

(defn lazy-loop-1 [xs]
  (lazy-seq
   (when-first [x xs]
     (conj (lazy-loop (rest xs)) x)))) ; using `conj` instead of `cons`

(last (lazy-loop-1 (range 100000))) ; 99999
;; StackOverflowError

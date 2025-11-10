(ns f.core)

;; `rest` is more lazy than `next`
;; `rest` seems to be favored

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

((juxt rest next butlast nnext fnext) '(0 1 2 3))   ; [(1 2 3) (1 2 3) (0 1 2) (2 3) 1]
((juxt nfirst ffirst)                 '((0 1 2 3))) ; [(1 2 3) 0]

((juxt rest next butlast nnext fnext nfirst ffirst) nil) ; [() nil nil nil nil nil nil]
((juxt rest next butlast nnext fnext nfirst ffirst) [])  ; [() nil nil nil nil nil nil]

;; `rest`-Loop 

;; recursive idiom with `rest`
(defn rest-loop [coll]
  (loop [xs      coll
         results []]
    (if-let [xs (seq xs)]
      (recur
       (rest xs)
       (conj results (first xs)))
      results)))

(rest-loop (range 10)) ; [0 1 2 3 4 5 6 7 8 9]

;; `next`-Loop 

(comment
  (rest nil) ; ()
  (next nil) ; nil (can be used in conditional expressions)
  (seq  nil) ; nil
  (seq  [])) ; nil

(defn next-loop [coll]
  (loop [xs      (seq coll) ; difference from previous version
         results []]
    (if xs                  ; difference from previous version
      (recur
       (next xs)            ; difference from previous version
       (conj results (first xs)))
      results)))

(next-loop (range 10)) ; [0 1 2 3 4 5 6 7 8 9]

;; `rest` and `next` in terms of laziness

(defn lazy-expensive []
  (map
   #(do (println "thinking hard") %)
   (into () (range 10))))

;; using lazy-seq idiom and `next`
(defn lazy-loop-1 [xs]
  (lazy-seq
   (when xs                 ; taking advantage of `next`s nil punning
     (cons
      (first xs)
      (lazy-loop-1 (next xs))))))

(comment
  ;; not very lazy
  (first (lazy-loop-1 (lazy-expensive))))
  ; (out) thinking hard
  ; (out) thinking hard

;; using lazy-seq idiom and `rest`
(defn lazy-loop-2 [xs]
  (lazy-seq
   (when-first [x xs]
     (cons x (lazy-loop-2 (rest xs))))))

(comment
  ;; `rest` is lazier than `next`
  (first (lazy-loop-2 (lazy-expensive))))
  ; (out) thinking hard

;; into

(defn into* [to & args]
  (into
   to
   (apply comp (butlast args))
   (last args)))

(into* []                         (range 10)) ; [0 1 2 3 4 5 6 7 8 9]
(into* [] (map inc)               (range 10)) ; [1 2 3 4 5 6 7 8 9 10]
(into* [] (map inc) (filter odd?) (range 10)) ; [1 3 5 7 9]

;; "look-ahead" behavior in a nested sequence with `ffirst` and `fnext`

(def message [["A" 1 28] ["H" 37 82 11] ["N" 127 0]])

(comment
  (next   message)  ; (["H" 37 82 11] ["N" 127 0])
  (fnext  message)  ; ["H" 37 82 11]
  (ffirst message)) ; "A"

(defn process [message]
  (lazy-seq
   (when-let [xs (seq message)]
     (let [e1 (ffirst xs)
           e2 (fnext  xs)]
       (cons (if (nil? e2)
               {:item e1 :succ :incomplete}
               {:item e1 :succ e2})
             (process (rest xs)))))))

(process message)
; ({:item "A", :succ ["H" 37 82 11]}
;  {:item "H", :succ ["N" 127 0]}
;  {:item "N", :succ :incomplete})

;; Why there is a `rest` and a `next`?

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; The standard library contains chunked sequences for instance, which are evaluated by chunks of 32 items.

(defn counter [cnt]
  (fn [x]
    (swap! cnt inc)
    x))

(defn not-chunked [f]
  (let [cnt (atom 0)]
    (f (drop 31 (map (counter cnt) (into () (range 100))))) ; PersistenList is not chunked
    @cnt))

(defn chunked [f]
  (let [cnt (atom 0)]
    (f (drop 31 (map (counter cnt) (range 100))))           ; LongRange is chunked
    @cnt))

(comment
  (type (into () (range 100))) ; clojure.lang.PersistentList
  (type (range 100)))          ; clojure.lang.LongRange

(not-chunked rest) ; 32
(not-chunked next) ; 33 (`next` looks ahead)
(chunked rest)     ; 32
(chunked next)     ; 64 (`next` looks ahead)

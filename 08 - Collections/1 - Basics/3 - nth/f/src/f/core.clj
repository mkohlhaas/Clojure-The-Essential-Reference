(ns f.core)

;; For performance reasons `nth` should almost exclusively be used with vectors.

(let [coll [0 1 2 3 4]]
  (nth coll 2))
; 2

(comment
  (nth [] 2))     ; (err) Execution error (IndexOutOfBoundsException)

(nth [] 2 0)      ; 0
(nth [1 2 3] 3/4) ; 1 (truncated to integer)
(nth nil 0)       ; nil

;; ;;;;;;;;;;;;;;;;;;;;;
;; Vectorized Hash-Table
;; ;;;;;;;;;;;;;;;;;;;;;

;; basic hash-table on top of vectors
;; We can use the hash function to retrieve a number given the key and use the number to store the item at the index in the vector.

;; limit the hash to 2^16 (0xFFFF) positive integers at the price of increasing the probability of collisions
(defn to-hash [n]
  (bit-and (hash n) 0xFFFF))

(comment
  (to-hash 10)  ; 11580
  (to-hash :a)) ; 24350

(defn grow [upto ht]
  (if (> upto (count ht))
    (let [t (transient ht)] ; using transient-persisten idiom
      (dotimes [_i (- upto (count ht))] (conj! t nil))
      (persistent! t))
    ht))

(comment
  (grow 10 [])) ; [nil nil nil nil nil nil nil nil nil nil]

#_{:clj-kondo/ignore [:unused-value]}
;; assumes the vector holding the hash-table to be already of the right size
(defn assign [ht kvs]
  (let [t (transient ht)]
    (doseq [[hash v] kvs]
      (assoc! t hash v))
    (persistent! t)))

(defn with-hashed-keys [args]
  (map (fn [[k v]] (vector (to-hash k) v))
       (partition 2 args)))

(comment
  (with-hashed-keys [1 2 3 4])) ; ([23876 2] [22451 4])

(defn put [ht & args]
  (cond
    (odd?  (count args)) (throw (IllegalArgumentException.))
    (zero? (count args)) ht
    :else  (let [kvs (with-hashed-keys args)
                 ht  (grow (apply max (map first kvs)) ht)]
             (assign ht kvs))))

(comment
  (count (put [] :a 1 :b 2))             ; 62295
  (count (put (put [] :a 1 :b 2) :c 3))) ; 62295 (there was no need to grow)

;; hashtable constructor
(defn hashtable [& args]
  (apply put [] args))

(comment
  (count (hashtable :a 1 :b 2))) ; 62295

;; get value from hashtable
(defn fetch [ht k]
  (nth ht (to-hash k)))

(def ht (hashtable :a 1 :b 2))

(fetch (put ht :c 3) :b) ; 2
(fetch (put ht :c 3) :c) ; 3

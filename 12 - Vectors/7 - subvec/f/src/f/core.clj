(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

;; the result of `subvec` is an independent view of a range of elements contained in the input vector

(subvec [1 2 3 4] 1 3) ; [2 3]

(def subv (subvec (vector-of :int 1 2 3) 1)) ; [2 3]

(conj subv \a) ; [2 3 97]

(comment
  (conj subv nil))
  ; (err) Execution error (NullPointerException)

;; end is optional
(subvec [1 2 3 4] 1) ; [2 3 4]

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; efficient solution to remove an element
(defn remove-at [v idx]
  (into (subvec v 0 idx)
        (subvec v (inc idx) (count v))))

(remove-at [0 1 2 3 4 5] 3) ; [0 1 2 4 5]

;; norm of a vector
(defn norm-1 [v]
  (loop [v   v
         res 0.]
    (if (= 0 (count v))
      (Math/sqrt res)
      (recur (subvec v 1) ; `subvec` can be used recursively (like first/rest)
             (+ res (Math/pow (nth v 0) 2))))))

(norm-1 [-2 1]) ; 2.23606797749979

(comment
  ;; return head of vector
  (nth [1 2 3] 0)) ; 1

;; merge operation of the merge sort algorithm (v1 and v2 are sorted)
(defn- merge-vectors [v1 v2 cmp]
  (loop [res []
         v1  v1
         v2  v2]
    (cond
      (empty? v1) (into res v2)
      (empty? v2) (into res v1)
      :else (let [[v1-head & v1-tail] v1
                  [v2-head & v2-tail] v2]
              (if (cmp v1-head v2-head)
                (recur (conj res v1-head) v1-tail v2)
                (recur (conj res v2-head) v1 v2-tail))))))

(defn merge-sort
  ([v]
   (merge-sort v <=))
  ([v cmp]
   (if (< (count v) 2)
     v
     (let [split (quot (count v) 2)
           v1    (subvec v 0 split)
           v2    (subvec v split (count v))]
       (merge-vectors (merge-sort v1 cmp) (merge-sort v2 cmp) cmp)))))

(merge-sort [2 1 5 0 3])                                        ; [0 1 2 3 5]
(merge-sort [[2 :b] [2 :a] [1 :c]] #(<= (first %1) (first %2))) ; [[1 :c] [2 :b] [2 :a]]

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; index-based with `subvec`
(defn norm-2 [v]
  (loop [v   v
         res 0.
         idx (dec (count v))]
    (if (< idx 0)
      (Math/sqrt res)
      (recur (subvec v 0 idx)
             (+ res (Math/pow (peek v) 2))
             (dec idx)))))

;; index-based without `subvec`
(defn norm-idx [v]
  (loop [idx (dec (count v))
         res 0.]
    (if (< idx 0)
      (Math/sqrt res)
      (recur (dec idx)
             (+ res (Math/pow (nth v idx) 2))))))

(comment
  (let [v (vec (range 1000))]     ; (out) Execution time mean : 112.584725 µs
    (quick-bench (norm-2 v)))

  (let [v (vec (range 1000))]     ; (out) Execution time mean :  19.212695 µs
    (quick-bench (norm-idx v))))

(defn bigv [n]
  (vec (range n)))

(comment
  ;; The generated sub-vector maintains a reference to the original vector though, which can prevent elements being garbaged collected. 
  (let [v1 (subvec (bigv 1e9) 0 5)
        v2 (subvec (bigv 1e9) 5 10)]
    (into v1 v2))
; (err) Execution error (OutOfMemoryError)
; (err) Java heap space

  ;; should not run out of memory
  (let [v1 (into [] (subvec (bigv 1e9) 0 5))   ; each subvector is transferred into a new vector instance, so their inner vector reference can be garbage collected
        v2 (into [] (subvec (bigv 1e9) 5 10))]
    (into v1 v2)))
  ; (err) Execution error (OutOfMemoryError) at java.lang.Long/valueOf (Long.java:1000).
  ; (err) Java heap space

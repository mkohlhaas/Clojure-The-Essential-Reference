(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(subvec [1 2 3 4] 1 3) ; [2 3]

(def subv (subvec (vector-of :int 1 2 3) 1)) ; [2 3]

(conj subv \a) ; [2 3 97]

(comment
  (conj subv nil))
  ; (err) Execution error (NullPointerException)

(subvec [1 2 3 4] 1) ; [2 3 4]

(defn remove-at [v idx]
  (into (subvec v 0 idx)
        (subvec v (inc idx) (count v))))

(remove-at [0 1 2 3 4 5] 3) ; [0 1 2 4 5]

(defn norm-1 [v]
  (loop [v   v
         res 0.]
    (if (= 0 (count v))
      (Math/sqrt res)
      (recur (subvec v 1)
             (+ res (Math/pow (nth v 0) 2))))))

(norm-1 [-2 1]) ; 2.23606797749979

(defn- merge-vectors [v1-initial v2-initial cmp]
  (loop [result []
         v1     v1-initial
         v2     v2-initial]
    (cond
      (empty? v1) (into result v2)
      (empty? v2) (into result v1)
      :else (let [[v1-head & v1-tail] v1
                  [v2-head & v2-tail] v2]
              (if (cmp v1-head v2-head)
                (recur (conj result v1-head) v1-tail v2)
                (recur (conj result v2-head) v1 v2-tail))))))

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

(merge-sort [2 1 5 0 3]) ; [0 1 2 3 5]

(merge-sort [[2 :b] [2 :a] [1 :c]] #(<= (first %1) (first %2))) ; [[1 :c] [2 :b] [2 :a]]

(defn norm-2 [v] ; <1>
  (loop [v v
         res 0.
         idx (dec (count v))]
    (if (< idx 0)
      (Math/sqrt res)
      (recur (subvec v 0 idx)
             (+ res (Math/pow (peek v) 2))
             (dec idx)))))

(comment
  (let [v (vec (range 1000))]
    (quick-bench (norm-2 v))))
  ; (out) Execution time mean : 112.584725 µs

(defn norm-idx [v] ; <3>
  (loop [idx (dec (count v))
         res 0.]
    (if (< idx 0)
      (Math/sqrt res)
      (recur (dec idx)
             (+ res (Math/pow (nth v idx) 2))))))

(comment
  (let [v (vec (range 1000))]
    (quick-bench (norm-idx v))))
  ; (out)              Execution time mean : 19.212695 µs

(defn bigv [n]
  (vec (range n)))

(comment
  (let [v1 (subvec (bigv 1e9) 0 5)
        v2 (subvec (bigv 1e9) 5 10)]
    (into v1 v2))
; (err) Execution error (OutOfMemoryError)
; (err) Java heap space

  ;; should not run out of memory
  (let [v1 (into [] (subvec (bigv 1e9) 0 5))
        v2 (into [] (subvec (bigv 1e9) 5 10))]
    (into v1 v2)))
  ; (err) Execution error (OutOfMemoryError) at java.lang.Long/valueOf (Long.java:1000).
  ; (err) Java heap space

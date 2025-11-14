(ns f.core
  (:require
   [criterium.core :as c]))

(merge {:a 1 :b 2} {:c 3 :d 4}) ; {:a 1, :b 2, :c 3, :d 4}

(merge-with + {:a 1} {:b 2 :a 10})  ; {:a 11, :b 2}

(def sorted-map-of-keywords
  (sorted-map :z 3 :f 5 :c 4))

(def map-of-ints
  (hash-map 1 "a" 2 "b" 5 "c"))

(comment
  (merge sorted-map-of-keywords map-of-ints))
  ; (err) Execution error (ClassCastException)
  ; (err) class clojure.lang.Keyword cannot be cast to class java.lang.Number

(def map-of-keywords
  (hash-map :a 1 :b 4 :e 4))

(merge sorted-map-of-keywords map-of-keywords) ; {:a 1, :b 4, :c 4, :e 4, :f 5, :z 3}

(let [m1 {:id [11] :colors ["red" "blue"]}
      m2 {:id [10] :colors ["yellow"]}
      m3 {:id [31] :colors ["brown" "red"]}]
  (merge-with into m1 m2 m3))
; {:id [11 10 31], :colors ["red" "blue" "yellow" "brown" "red"]}

(defn merge-into [k ks]
  (fn [m]
    (merge
     (get m k {})
     (select-keys m ks))))

(def product-merge
  (merge-into :product [:fee-attribute :created-at]))

(def product
  {:fee-attributes [49 8 13 38 62]
   :product {:visible false
             :online true
             :name "Switcher AA126"
             :company-id 183
             :part-repayment true
             :min-loan-amount 5000
             :max-loan-amount 1175000}
   :created-at 1504556932728})

(product-merge product)
; {:visible false,
;  :online true,
;  :name "Switcher AA126",
;  :company-id 183,
;  :part-repayment true,
;  :min-loan-amount 5000,
;  :max-loan-amount 1175000,
;  :created-at 1504556932728}

(defprotocol IComplex
  (sum [c1 c2]))

(defrecord Complex [re im]
  IComplex
  (sum [c1 c2] (merge-with + c1 c2)))

(sum (Complex. 2 5) (Complex. 1 3)) ; {:re 3, :im 8}

(let [m1 {:a 1 :b 2}
      m2 {:a 'a :b 'b}
      m3 {:a "a" :b "b"}]
  (merge-with (fn [v1 v2]
                (if (vector? v1)
                  (conj v1 v2)
                  [v1 v2]))
              m1 m2 m3))
; {:a [1 a "a"], :b [2 b "b"]}

(let [m1 {:a [1 3] :b 2}
      m2 {:a 'a :b 'b}
      m3 {:a "a" :b "b"}]
  (merge-with (fn [v1 v2]
                (if (vector? v1)
                  (conj v1 v2)
                  [v1 v2]))
              m1 m2 m3))
; {:a [1 3 a "a"], :b [2 b "b"]}

(let [m1 {:a [1 3] :b 2}
      m2 {:a 'a :b 'b}
      m3 {:a "a" :b "b"}]
  (merge-with (fn [v1 v2]
                (if (:multi (meta v1))
                  (conj v1 v2)
                  ^:multi [v1 v2]))
              m1 m2 m3))
; {:a [[1 3] a "a"], :b [2 b "b"]}

(comment
  (let [m1 (apply hash-map (range 2000))                                  ; (out) Execution time mean : 472.127683 µs
        m2 (apply hash-map (range 1 2001))]
    (c/quick-bench (merge m1 m2)))
;; Execution time mean : 221.025373 µs

  (defn merge* [m & maps]
    (when (some identity maps)
      (persistent!
       (reduce conj! (transient (or m {})) maps))))

  (let [m1 (apply hash-map (range 2000))                                  ; (out) Execution time mean : 298.047479 µs
        m2 (apply hash-map (range 1 2001))]
    (c/quick-bench (merge* m1 m2)))
;; Execution time mean : 162.887879 µs

  (let [m1 (apply hash-map (range 2000))                                  ; (out) Execution time mean : 632.325158 µs
        m2 (apply hash-map (range 1 2001))]
    (c/quick-bench (merge-with + m1 m2)))
;; Execution time mean : 304.863164 µs

  (defn merge-with* [f & maps]
    (when (some identity maps)
      (letfn [(merge-entry [m [k v]]
                (assoc! m k
                        (if-not (= ::none (get m k ::none))
                          (f (get m k) v)
                          v)))
              (merge-into [m1 m2]
                (reduce merge-entry (transient (or m1 {})) (seq m2)))]
        (persistent! (reduce merge-into maps)))))

  (let [m1 (apply hash-map (range 2000))                                  ; (out) Execution time mean : 507.490391 µs
        m2 (apply hash-map (range 1 2001))]
    (c/quick-bench (merge-with* + m1 m2))))
;; Execution time mean : 220.885976 µs


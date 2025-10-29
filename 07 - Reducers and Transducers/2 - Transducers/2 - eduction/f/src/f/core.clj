(ns f.core
  (:import java.util.Date))

(take 10 (eduction (filter odd?) (map inc) (range)))
; (2 4 6 8 10 12 14 16 18 20)

(let [input (sequence (map #(do (print ".") %)) (range 10)) ; .........(0 1 2 3 4 5 6 7 8 9)
      odds  (filter odd?  input)
      evens (filter even? input)]
  (if (> (first odds) (first evens))
    (println "ok")
    (println "ko")))
;; ..........ok

(let [input (eduction (map #(do (print ".") %)) (range 10)) ; (..........0 1 2 3 4 5 6 7 8 9)
      odds  (filter odd?  input)
      evens (filter even? input)]
  (if (> (first odds) (first evens))
    (println "ok")
    (println "ko")))
;; ....................ok

(def data
  [{:fee-attributes [49 8 13 38 100]
    :product {:visible true
              :online true
              :name "Switcher AA126"
              :company-id 183
              :part-repayment true
              :min-loan-amount 5000
              :max-loan-amount 1175000
              :fixed true}
    :created-at 1504556932728}
   {:fee-attributes [11 90 79 7992]
    :product {:visible true
              :online true
              :name "Green Professional"
              :company-id 44
              :part-repayment true
              :min-loan-amount 25000
              :max-loan-amount 3000000
              :floating true}
    :created-at 15045569334789}
   {:fee-attributes [21 12 20 15 92]
    :product {:visible true
              :online true
              :name "Fixed intrinsic"
              :company-id 44
              :part-repayment true
              :min-loan-amount 50000
              :max-loan-amount 1000000
              :floating true}
    :created-at 15045569369839}])

(defn- merge-into [k ks]
  (map (fn [m] (merge (m k) (select-keys m ks)))))

(defn- update-at [k f]
  (map (fn [m] (update m k f))))

(defn- if-key [k]
  (filter (fn [m] (if k (m k) true))))

(defn if-equal [k v]
  (filter (fn [m] (if v (= (m k) v) true))))

(defn if-range [k-min k-max v]
  (filter (fn [m] (if v (<= (m k-min) v (m k-max)) true))))

(def prepare-data
  (comp
   (merge-into :product [:fee-attributes :created-at])
   (update-at  :created-at #(Date. %))))

(defn filter-data [params]
  (comp
   (if-key :visible)
   (if-key (params :rate))
   (if-equal :company-id (params :company-id))
   (if-key (params :repayment-method))
   (if-range :min-loan-amount
             :max-loan-amount
             (params :loan-amount))))

(defn xform [params]
  (comp
   prepare-data
   (filter-data params)))

(defn- best-fee [p1 p2]
  (if (< (peek (:fee-attributes p1))
         (peek (:fee-attributes p2)))
    p1 p2))

(defn best-product [params data best-fn]
  (reduce
   best-fn
   (eduction (xform params) data)))

(best-product {:repayment-method :part-repayment :loan-amount 500000} data best-fee)
; {:name "Fixed intrinsic",
;  :floating true,
;  :fee-attributes [21 12 20 15 92],
;  :company-id 44,
;  :part-repayment true,
;  :online true,
;  :max-loan-amount 1000000,
;  :visible true,
;  :min-loan-amount 50000,
;  :created-at #inst "2446-10-10T12:49:29.839-00:00"}

(def best-part-repayment
  (eduction (xform {:repayment-method :part-repayment}) data))

(def best-fixed
  (eduction (xform {:rate :fixed}) data))

(:name (reduce best-fee best-part-repayment)) ; "Fixed intrinsic"
(:name (reduce best-fee best-fixed))          ; "Switcher AA126"

(def cnt1 (atom 0))
#_{:clj-kondo/ignore [:unused-value]}
(let [res (eduction (map #(do (swap! cnt1 inc) %)) (range 10))]
  (conj (rest res) (first res))
  @cnt1)
; 20

(def cnt2 (atom 0))
#_{:clj-kondo/ignore [:unused-value]}
(let [res (sequence (map #(do (swap! cnt2 inc) %)) (range 10))]
  (conj (rest res) (first res))
  @cnt2)
; 10

(defn busy-mem []
  (str (/ (-
           (.. Runtime getRuntime totalMemory)
           (.. Runtime getRuntime freeMemory))
          1024. 1024.) " Mb"))

(comment
  (System/gc)
  (busy-mem)  ; "12.226638793945312 Mb"

  (def s1 (eduction (map inc) (range 1e7)))
  (last s1)

  (System/gc)
  (busy-mem)  ; "11.974220275878906 Mb"

  (def s2 (sequence (map inc) (range 1e7)))
  (last s2)

  (System/gc)
  (busy-mem)) ; "310.35066986083984 Mb"

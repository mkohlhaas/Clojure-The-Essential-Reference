(ns f.core
  (:require
   [clojure.core.async :refer [<! >! >!! alts!! chan go]]
   [criterium.core     :refer [quick-bench]]))

;; no transducer version (see also `mapf`)

(filter  odd? (range 8)) ; (1 3 5 7)
(filterv odd? (range 8)) ; [1 3 5 7]

(type (filter  odd? (range 8))) ; clojure.lang.LazySeq
(type (filterv odd? (range 8))) ; clojure.lang.PersistentVector

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; Leibniz formula
(defn calculate-pi [precision]
  (->> (iterate #(* ((if (pos? %) + -) % 2) -1) 1.0)
       (map #(/ 4 %))
       (take-while #(> (Math/abs %) precision))
       (reduce +)))

;; Brothers' formulae 
(defn calculate-e [precision]
  (letfn [(factorial [n]
            (reduce * (range 1 (inc n))))]
    (->> (range)
         (map #(/ (+ (* 2.0 %) 2) (factorial (inc (* 2 %)))))
         (take-while #(> (Math/abs %) precision))
         (reduce +))))

(defn get-results [channels]
  (let [[result channel] (alts!! channels)
        new-channels     (filterv #(not= channel %) channels)] ; `channels` is a PersistentVector and `filterv`'s is also(!)
    (if (empty? new-channels)
      [result]
      (conj (get-results new-channels) result))))

(let [[pi-in pi-out e-in e-out] (repeatedly 4 chan)]
  (go (>! pi-out {:type :pi :num (calculate-pi (<! pi-in))}))
  (go (>! e-out  {:type :e  :num (calculate-e  (<! e-in))}))

  (>!! pi-in 1e-4)
  (>!! e-in 1e-5)
  (get-results [e-out pi-out])) ; called with a PersistentVector
; [{:num 3.1415426535898248, :type :pi}
;  {:num 2.718281525573192,  :type :e}]

(comment
  (type [1 2 3])) ; clojure.lang.PersistentVector

;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; Performance Considerations 
;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 

(comment
  (let [r (range 10000)] (quick-bench (into [] (filter odd? r)))) ; (out) Execution time mean : 531.044599 µs (sequence   version of `filter`)
  (let [r (range 10000)] (quick-bench (into [] (filter odd?) r))) ; (out) Execution time mean : 380.440664 µs (transducer version of `filter`)
  (let [r (range 10000)] (quick-bench (filterv odd? r))))         ; (out) Execution time mean : 336.094589 µs (`filterv`)

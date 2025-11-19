(ns f.core
  (:require
   [clojure.core.async :refer [<! >! >!! alts!! chan go]]
   [criterium.core     :refer [quick-bench]]))

(filterv odd? (range 8)) ; [1 3 5 7]

(defn calculate-pi [precision]
  (->> (iterate #(* ((if (pos? %) + -) % 2) -1) 1.0)
       (map #(/ 4 %))
       (take-while #(> (Math/abs %) precision))
       (reduce +)))

(defn calculate-e [precision]
  (letfn [(factorial [n] (reduce * (range 1 (inc n))))]
    (->> (range)
         (map #(/ (+ (* 2.0 %) 2) (factorial (inc (* 2 %)))))
         (take-while #(> (Math/abs %) precision))
         (reduce +))))

(defn get-results [channels]
  (let [[result channel] (alts!! channels)
        new-channels     (filterv #(not= channel %) channels)]
    (if (empty? new-channels)
      [result]
      (conj (get-results new-channels) result))))

(let [[pi-in pi-out e-in e-out] (repeatedly 4 chan)]
  (go (>! pi-out {:type :pi :num (calculate-pi (<! pi-in))}))
  (go (>! e-out  {:type :e  :num (calculate-e  (<! e-in))}))

  (>!! pi-in 1e-4)
  (>!! e-in 1e-5)
  (get-results [e-out pi-out]))
; [{:num 3.1415426535898248, :type :pi}
;  {:num 2.718281525573192,  :type :e}]

(comment
  (let [r (range 10000)] (quick-bench (into [] (filter odd? r))))  ; (out) Execution time mean : 531.044599 µs
  (let [r (range 10000)] (quick-bench (filterv odd? r)))           ; (out) Execution time mean : 336.094589 µs
  (let [r (range 10000)] (quick-bench (into [] (filter odd?) r)))) ; (out) Execution time mean : 380.440664 µs


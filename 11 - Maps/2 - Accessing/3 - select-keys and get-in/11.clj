(require '[criterium.core :refer [quick-bench]])

(defn select-keys2 [m keyseq] ; <1>
  (with-meta
    (transduce
      (keep #(find m %))
      (completing conj! persistent!)
      (transient {})
      keyseq)
    (meta m)))

(let [m (apply hash-map (range 40))] ; <2>
  (quick-bench (select-keys m [0 2 4 6 8 10 12])))
;;  Execution time mean : 773.492163 ns

(let [m (apply hash-map (range 40))] ; <3>
  (quick-bench (select-keys2 m [0 2 4 6 8 10 12])))
;; Execution time mean : 545.102979 ns
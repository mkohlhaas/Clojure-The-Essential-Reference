(ns f.core
  (:require [criterium.core :refer [bench]]))

(= "a" "a" "a") ; true
(not= 1 2)      ; true

(defn generate [& [{:keys [cheat reels] :or {cheat 0 reels 3}}]]
  (->> (repeatedly rand)
       (map #(int (* % 100)))
       (filter pos?)
       (map #(mod (- 100 cheat) %))
       (take reels)))

(comment
  (generate)) ; (8 4 24)
              ; (18 10 37)
              ; (12 14 20)

(defn play [& [opts]]
  (let [res (generate opts)]
    {:win    (apply = res)
     :result res}))

(play)              ; {:win false, :result (25 46 10)}
(play {:cheat 100}) ; {:win true,  :result (0 0 0)}
(play {:reels 10})  ; {:win false, :result (44 30 10 19 29 48 16 2 12 12)}

(== 0.1 (float 0.1)) ; false
(== 1.  (float 1.))  ; true

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(def k (take 1000    (repeat 0)))
(def m (take 1000000 (repeat 0)))
(def half (seq (into [] (concat
                         (take 500000 (repeat 0))
                         (take 1      (repeat 1))
                         (take 500000 (repeat 0))))))

(comment
  (bench (apply = k))
  ;; Execution time mean : 63.865057 µs

  (bench (apply = m))
  ;; Execution time mean : 62885.110 µs

  (bench (apply = half))
  ;; Execution time mean : 18051.236 µs

  (set! *warn-on-reflection* true)

  (defn plain= [m n] (= m n))
  (bench (let [m 1 n 2] (plain= m n)))
  ;; Execution time mean : 6.963935 ns

  (defn raw= [^Long m ^Long n] (.equals m n))

  (bench (let [m 1 n 2] (raw= m n))))
  ;; Execution time mean : 5.215350 ns

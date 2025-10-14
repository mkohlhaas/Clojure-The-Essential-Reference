(ns f.core
  (:require [criterium.core :refer [bench]]))

(= "a" "a" "a") ; true
(not= 1 2)      ; true

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Best Way to Understand Equivalence is by Examples
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord SomeRecord [x y])
(= (SomeRecord. 1 2) (SomeRecord. 1 2))                    ; true
(= (sorted-map :a "a" :b "b") (sorted-map :b "b" :a "a"))  ; true
(= 1N (byte 1))                                            ; true
(= '(1 2 3) [1 2 3])                                       ; true
(= #{1 2 3} #{3 2 1})                                      ; true
(= (sorted-set 2 1) (sorted-set 1 2))                      ; true
(= {:a "a" :b "b"} {:b "b" :a "a"})                        ; true

(deftype SomeType [x y])
(= (SomeType. 1 2) (SomeType. 1 2))                        ; false
(= 1M 1.)                                                  ; false
(= [0 1 2] [2 1 0])                                        ; false
(= [1 2 3] #{1 2 3})                                       ; false
(= "hi" [\h \i])                                           ; false
(= (Object.) (Object.))                                    ; false

;; ;;;;;;;;;;;;
;; Slot Machine
;; ;;;;;;;;;;;;

(defn generate [& [{:keys [cheat reels] :or {cheat 0 reels 3}}]]
  (->> (repeatedly rand)
       (map #(int (* % 100)))
       (filter pos?) ; filter out zeros
       (map #(mod (- 100 cheat) %))
       (take reels)))

(comment
  (generate)) ; (18 10 37)
              ; (12 14 20)

(defn play [& [opts]]
  (let [res (generate opts)]
    {:win    (apply = res)
     :result res}))

(play)              ; {:win false, :result (25 46 10)}
(play {:cheat 97})  ; {:win true,  :result (3 3 3)}
(play {:cheat 100}) ; {:win true,  :result (0 0 0)}
(play {:reels 10})  ; {:win false, :result (44 30 10 19 29 48 16 2 12 12)}

;; You’re pretty much free to use anything you want as a key in a hash map, but there 
;; could be very subtle bugs happening if you use floating point numbers as keys.
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

  ;; shows any use of .equals that forces the compiler to use reflection
  (set! *warn-on-reflection* true)

  (defn plain= [m n] (= m n))

  (bench (let [m 1 n 2] (plain= m n)))
  ;; Execution time mean : 6.963935 ns

  (defn raw= [^Long m ^Long n] (.equals m n))

  (bench (let [m 1 n 2] (raw= m n))))
  ;; Execution time mean : 5.215350 ns

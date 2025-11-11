(ns f.core
  (:require
   [criterium.core :refer [bench]]))

(map    range [1 5 10]) ; ((0) (0 1 2 3 4) (0 1 2 3 4 5 6 7 8 9))
(mapcat range [1 5 10]) ; (0 0 1 2 3 4 0 1 2 3 4 5 6 7 8 9)

;; `mapcat` is also a transducer
(sequence (mapcat repeat) [1 2 3] ["!" "?" "$"]) ; ("!" "?" "?" "$" "$" "$")
(sequence (map    repeat) [1 2 3] ["!" "?" "$"]) ; (("!") ("?" "?") ("$" "$" "$"))

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def hex?
  (set (sequence
        (comp
         (mapcat range)
         (map char))
        [48 65]
        [58 71])))
; #{\A \B \C \D \E \F \0 \1 \2 \3 \4 \5 \6 \7 \8 \9}

(comment
  (char 48) ; \0
  (char 57) ; \9
  (char 65) ; \A
  (char 70) ; \F

  (map char (range 48 58))  ; (\0 \1 \2 \3 \4 \5 \6 \7 \8 \9)
  (map char (range 65 71))) ; (\A \B \C \D \E \F)

(every? hex? "CAFEBABE")          ; true
(every? hex? "0123456789ABCDEF")  ; true
(every? hex? "0123456789ABCDEFZ") ; false

;; Topological Sort

;; library names and their direct dependencies
(def libs {:async        [:analyzer.jvm]
           :analyzer.jvm [:memoize :analyzer :reader :asm]
           :memoize      [:cache]
           :cache        [:priority-map]
           :priority-map []
           :asm          []})

(map    libs (:analyzer.jvm libs)) ; ([:cache] nil nil [])
(mapcat libs (:analyzer.jvm libs)) ; (:cache)

(comment
  (:analyzer.jvm libs) ; [:memoize :analyzer :reader :asm]
  (butlast ()))        ; nil

;; topological sort (without cycle detection)
(defn tp-sort [deps k]
  (loop [res ()
         ks  [k]]
    (if (empty? ks)
      (butlast res)
      (recur (apply conj res ks)
             (mapcat deps ks)))))

(tp-sort libs :async) ; (:priority-map :cache :asm :reader :analyzer :memoize :analyzer.jvm)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; Performance Considerations 
;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 

;; not completely lazy (always consumes the first four arguments)
(def a-1 (mapcat range (map #(do (print ".") %) (into () (range 10)))))
; (out) ....

a-1 ; (0 1 2 3 4 5 6 7 8 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6. 0 1 2 3 4 5. 0 1 2 3 4. 0 1 2 3. 0 1 2. 0 1. 0)

;; completely lazy version
(defn mapcat* [f & colls]
  (letfn [(step [colls]
            (lazy-seq
             (when-first [c colls]
               (concat c (step (rest colls))))))]
    (step (apply map f colls))))

;; lazy
(def a-2 (mapcat* range (map #(do (print ".") %) (into () (range 10)))))

a-2 ; .(0 1 2 3 4 5 6 7 8. 0 1 2 3 4 5 6 7. 0 1 2 3 4 5 6. 0 1 2 3 4 5. 0 1 2 3 4. 0 1 2 3. 0 1 2. 0 1. 0.)

(comment
  ;; no transducer
  (let [xs (range 1000)] (bench (last (mapcat range xs))))            ; (out) Execution time mean : 33.625122 ms

  ;; transducer versions
  (let [xs (range 1000)] (bench (last (sequence (mapcat range) xs)))) ; (out) Execution time mean : 69.561743 ms
  (let [xs (range 1000)] (bench (last (eduction (mapcat range) xs)))) ; (out) Execution time mean : 71.745171 ms

  ;; transducer with `transduce`
  (let [xs (range 1000)] (bench (reduce + (mapcat range xs))))        ; (out) Execution time mean : 23.623076 ms
  (let [xs (range 1000)] (bench (transduce (mapcat range) + xs)))     ; (out) Execution time mean : 24.133590 ms

  ;; transducer generating a vector
  (let [xs (range 1000)] (bench (into [] (mapcat range) xs))))        ; (out) Execution time mean : 18.699647 ms

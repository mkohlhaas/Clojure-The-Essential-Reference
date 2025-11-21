(ns f.core
  (:require
   [clj-memory-meter.core   :as     mm]
   [clojure.data            :refer [diff]]
   [clojure.repl            :refer [doc]]
   [clojure.string          :refer [split-lines]]
   [com.hypirion.clj-xchart :as     chart]
   [criterium.core          :refer [benchmark quick-bench]]))

[:a :b :c]        ; [:a :b :c]
(vector :a :b :c) ; [:a :b :c]

(defn var-args [a b & all]
  (apply vector a b all))

(var-args :a :b :c) ; [:a :b :c]

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def palindromes ["hannah" "kayak" "civic" "deified"])

(defn longest-palindrome [words]
  (->> words
       (filter #(= (seq %) (reverse %))) ; ("hannah" "kayak" "civic" "deified")
       (map #(vector (count %) %))       ; ([6 "hannah"] [5 "kayak"] [5 "civic"] [7 "deified"])
       (sort-by first >)                 ; ([7 "deified"] [6 "hannah"] [5 "kayak"] [5 "civic"])
       first))                           ; [7 "deified"]

;; `(map #([(count %) %]))` wouldn't work
(macroexpand '#([(count %) %]))
; (fn* [p1] ([(count p1) p1]))

(comment
  (seq     "hannah")  ; (\h \a \n \n \a \h)
  (reverse "hannah")) ; (\h \a \n \n \a \h)

(longest-palindrome palindromes) ; [7 "deified"]

(defn to-words [file-name]
  (->> file-name
       slurp
       split-lines
       doall))

(comment
  ;; NOTE: $ sudo pacman -S words
  (count (to-words "/usr/share/dict/words")) ; 123985

  (to-words "/usr/share/dict/words"))
  ; ["A"
  ;  "a"
  ;  "AA"
  ;  "AAA"
  ;  "Aachen"
  ;  …]

(longest-palindrome (to-words "/usr/share/dict/words")) ; [7 "deified"]

;; Real-Estate Instructions

(def old-data
  [{:summary "Bijou love nest" :status "SSTC"}
   {:summary "Country pile"    :status "available"}])

(def new-data
  [{:summary "Bijou love nest" :status "SSTC"}
   {:summary "Country pile"    :status "SSTC"}])

(doseq [[old-instruction new-instruction] (map vector old-data new-data)]
  (let [[only-first only-second _both] (diff old-instruction new-instruction)]
    (when (or only-first only-second)
      (println "Differences:" old-instruction new-instruction))))
; (out) Differences: {:summary Country pile, :status available} 
;                    {:summary Country pile, :status SSTC}))))

(comment
  (map vector old-data new-data))
  ; ([{:summary "Bijou love nest", :status "SSTC"}      {:summary "Bijou love nest", :status "SSTC"}]
  ;  [{:summary "Country pile",    :status "available"} {:summary "Country pile",    :status "SSTC"}])

;; Transpose Matrix 

(def m [[1 2 3]
        [4 5 6]
        [7 8 9]])

(apply map vector m)
; ([1 4 7] 
;  [2 5 8] 
;  [3 6 9]

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; Clojure faster "arities"
;; ;;;;;;;;;;;;;;;;;;;;;;;;

(doc vector)
; (out) -------------------------
; (out) clojure.core/vector
; (out) ([] [a] [a b] [a b c] [a b c d] [a b c d e] [a b c d e f] [a b c d e f & args])
; (out)   Creates a new vector containing the args.

(comment
  (quick-bench (vector 1 2 3 4 5 6))    ; (out) Execution time mean :  41.757544 ns
  (quick-bench (vector 1 2 3 4 5 6 7))) ; (out) Execution time mean : 422.860214 ns

(defmacro defnvector [n]
  (let [args (map #(symbol (str "x" %)) (range n))]
    `(defn ~(symbol (str "vector" n)) [~@args] [~@args])))

(macroexpand '(defnvector 7))
; (def vector7 (fn ([x0 x1 x2 x3 x4 x5 x6] [x0 x1 x2 x3 x4 x5 x6])))

(defnvector  7)
(defnvector  8)
(defnvector  9)
(defnvector 10)

(comment
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (quick-bench (vector7 1 2 3 4 5 6 7))) ; (out) Execution time mean : 46.156533 ns

;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; Performance Considerations 
;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 

(defmacro b [expr]
  `(first (:mean (benchmark ~expr {}))))

(comment
  (def results
    (doall
     (for [i (range 10)]
       (let [num-elements (* (inc i) 100000)
             data         (range num-elements)]
         [(b (vec data))
          (b (apply vector data))]))))

  (let [[vec-results vector-results] (apply map vector results)
        labels                       (range 100000 1100000 100000)]
    (chart/view
     (chart/xy-chart
      {"(vec)"    [vec-results labels]
       "(vector)" [vector-results labels]})))

  (let [reducible (doall (range 1000))
        lazy      (doall (map inc (range 1000)))]
    (quick-bench (vec lazy))
    (quick-bench (apply vector lazy))
    (quick-bench (vec reducible))
    (quick-bench (apply vector reducible))))

;; Execution time mean : 22.523189 µs
;; Execution time mean : 22.624104 µs
;; Execution time mean : 16.058917 µs
;; Execution time mean : 19.471246 µs

(comment
  (defn test-memory-vector-of-jamm []
    (let [results (for [elements (range 100000 1100000 100000)]
                    [elements
                     (mm/measure (make-array Object elements))
                     (mm/measure (vec (repeat elements nil)))])]
      (doseq [i (range 4)]
        (doseq [result results]
          (printf "%11d " (get result i)))
        (println)))))

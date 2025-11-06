(ns f.core
  (:require [clojure.string :as s]))

(concat [1 2 3] () {:a 1} "hi" #{5}) ; (1 2 3 [:a 1] \h \i 5)

(macroexpand '(lazy-cat [1 2 3] (range)))
; (concat
;  (lazy-seq [1 2 3])
;  (lazy-seq (range)))

(defn identifier [x]
  (let [classname #(.getName ^Class %)
        split #(.split % "\\.")
        typex (type x)]
    (apply str
           (interpose "-"
                      (concat
                       (split (classname typex))
                       (mapcat (comp split classname) (supers typex)))))))

(identifier #"regex")
; "java-util-regex-Pattern-java-lang-Object-java-io-Serializable"

(def sold-icecreams
  [[:strawberry :banana :vanilla]
   '(:vanilla :chocolate)
   #{:hazelnut :pistachio}
   [:vanilla :hazelnut]
   [:peach :strawberry]])

(defn next-day-quantities [sold-icecreams]
  (->> (apply concat sold-icecreams)
       frequencies
       (sort-by second >)))

(next-day-quantities sold-icecreams)
; ([:vanilla 3]
;  [:strawberry 2]
;  [:hazelnut 2]
;  [:banana 1]
;  [:chocolate 1]
;  [:pistachio 1]
;  [:peach 1])

(defn trace [x] (println "evaluating" x) x)

(def l1 (map trace (list 1 2 3)))
(def l2 (map trace (list 3 4 5)))

(def l1+l2 (concat l1 l2))

(first l1+l2)
; (out) evaluating 1
; 1

(comment
  (time (first (concat (vec (range 10)) (vec (range 1e7)))))
;; "Elapsed time: 1032.928937 msecs"

  (time (first (lazy-cat (vec (range 10)) (vec (range 1e7))))))
;; "Elapsed time: 0.313782 msecs"

(defn padder [width]
  #(take width (concat % (repeat " "))))

(defn line [width]
  (apply str (repeat (+ 2 width) "-")))

(defn quote-sentence [sentence width]
  (transduce
   (comp
    (map (padder width))
    (map #(apply str %))
    (map #(str "|" % "|\n")))
   (completing str #(str % (line width)))
   (str (line width) "\n")
   (s/split sentence #"\s+")))

(println (quote-sentence "Clojure is my favorite language" 12))
; (out) --------------
; (out) |Clojure     |
; (out) |is          |
; (out) |my          |
; (out) |favorite    |
; (out) |language    |
; (out) --------------

(defn get-batch [id]
  (repeat id id))

(defn step
  ([n] (step n ()))
  ([n res]
   (if (pos? n)
     (recur (dec n) (concat res (get-batch n)))
     res)))

(step 4) ; (4 4 4 4 3 3 3 2 2 1)

(comment
  (first (step 10000)))
  ; (err) Execution error (StackOverflowError)

#_{:clj-kondo/ignore [:redefined-var]}
(defn step
  ([n] (step n ()))
  ([n res]
   (if (pos? n)
     (recur (dec n) (concat (get-batch n) res))
     res)))

(step 4) ; (1 2 2 3 3 3 4 4 4 4)

(comment ; 10000
  (last (step 10000))) ; 10000

(apply concat (map rest [[1 2 3] [4 5 6]])) ; (2 3 5 6)

(mapcat rest [[1 2 3] [4 5 6]])             ; (2 3 5 6)

(def fibs (lazy-cat [0 1] (map +' fibs (rest fibs))))

(take 10 fibs) ; (0 1 1 2 3 5 8 13 21 34)

(comment
  (last (take 1000000 fibs)))
  ;; Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler

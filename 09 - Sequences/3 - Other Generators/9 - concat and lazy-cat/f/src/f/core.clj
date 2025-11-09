(ns f.core
  (:require [clojure.string :as s]))

;; concatenating (several types of sequential) collections; returns a lazy sequence
(concat [1 2 3] () {:a 1} "hi" #{5}) ; (1 2 3 [:a 1] \h \i 5)

;; `lazy-cat` is a macro build on top of `concat`; makes sure everything is lazy (see example further below "Laziness at Work")
(macroexpand '(lazy-cat [1 2 3] (range)))
; (concat
;  (lazy-seq [1 2 3])
;  (lazy-seq (range)))

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; creates a unique object identifier
(defn identifier [x]
  (let [classname #(.getName ^Class %)
        split     #(.split % "\\.")
        typex     (type x)]
    (apply str
           (interpose "-"
                      (concat
                       (split (classname typex))
                       (mapcat (comp split classname) (supers typex)))))))

(comment
  (type #"regex")                    ;  java.util.regex.Pattern
  (.getName ^Class (type #"regex"))  ; "java.util.regex.Pattern"
  (supers (type #"regex")))          ; #{java.lang.Object java.io.Serializable}

(identifier #"regex")         ; "java-util-regex-Pattern-java-lang-Object-java-io-Serializable"
(count (identifier #"regex")) ; 61
(count (identifier [1 2 3]))  ; 723 (huge identifier name)

;; list of today's sold ice creams
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

(comment
  (concat sold-icecreams)
  ; ([:strawberry :banana :vanilla]
  ;  (:vanilla :chocolate)
  ;  #{:pistachio :hazelnut}
  ;  [:vanilla :hazelnut]
  ;  [:peach :strawberry])

  (apply concat sold-icecreams)
  ; (:strawberry
  ;  :banana
  ;  :vanilla
  ;  :vanilla
  ;  :chocolate
  ;  :pistachio
  ;  :hazelnut
  ;  :vanilla
  ;  :hazelnut
  ;  :peach
  ;  :strawberry)

  (frequencies (apply concat sold-icecreams)))
  ; {:strawberry 2,
  ;  :banana     1,
  ;  :vanilla    3,
  ;  :chocolate  1,
  ;  :pistachio  1,
  ;  :hazelnut   2,
  ;  :peach      1}

(next-day-quantities sold-icecreams)
; ([:vanilla    3]
;  [:strawberry 2]
;  [:hazelnut   2]
;  [:banana     1]
;  [:chocolate  1]
;  [:pistachio  1]
;  [:peach      1])

;; Laziness at Work

(defn trace [x]
  (println "evaluating" x)
  x)

;; no evaluations
(def l1 (map trace (list 1 2 3))) ; #'f.core/l1
(def l2 (map trace (list 3 4 5))) ; #'f.core/l2
(def l1+l2 (concat l1 l2))        ; #'f.core/l1+l2

(comment
  (first l1+l2))
  ; (out) evaluating 1
  ; 1

(comment
  ;; concat
  (time (first (concat (vec (range 10))
                       (vec (range 1e7)))))
  ; (out) "Elapsed time: 1129.22118 msecs"
  ; 0

  ;; lazy-cat (defers evaluation of arguments)
  (time (first (lazy-cat (vec (range 10))
                         (vec (range 1e7))))))
  ; (out) "Elapsed time: 0.208144 msecs"
  ; 0

;; String Padding

;; right padding
(defn padder [width]
  #(take width (concat % (repeat " "))))

(comment
  (apply str (take 10 (concat "hello" (repeat " ")))))
  ; "hello     "

(defn line [width]
  (apply str (repeat (+ 2 width) "-")))

(comment
  ;; string of 12 dashes
  (line 10)) ; "------------"

(defn quote-sentence [sentence width]
  (transduce
   (comp                                  ; xform
    (map (padder width))
    (map #(apply str %))
    (map #(str "|" % "|\n")))
   (completing str #(str % (line width))) ; f
   (str (line width) "\n")                ; init
   (s/split sentence #"\s+")))            ; coll

(comment
  (s/split "Clojure is my favorite language" #"\s+") ; ["Clojure" "is" "my" "favorite" "language"]
  (str (line 12) "\n"))                              ; "--------------\n"

(comment
  (println (quote-sentence "Clojure is my favorite language" 12)))
  ; (out) --------------
  ; (out) |Clojure     |
  ; (out) |is          |
  ; (out) |my          |
  ; (out) |favorite    |
  ; (out) |language    |
  ; (out) --------------

;; Using `concat` to Build Results Incrementally

;; in a real scenario this could be a database query
(defn get-batch [id]
  (repeat id id))

(comment
  (get-batch 10)) ; (10 10 10 10 10 10 10 10 10 10)

(defn step-1
  ([n]
   (step-1 n ()))
  ([n res]
   (if (pos? n)
     (recur (dec n)
            (concat res (get-batch n))) ; nested concat calls are eating up the stack
     res)))

(step-1 4) ; (4 4 4 4 3 3 3 2 2 1)

(comment
  (step-1 10000))
  ; (err) Error printing return value (StackOverflowError)

(defn step-2
  ([n]
   (step-2 n ()))
  ([n res]
   (if (pos? n)
     (recur (dec n)
            (concat (get-batch n) res)) ; only change
     res)))

;; But changes also order of results(!)
(step-2 4) ; (1 2 2 3 3 3 4 4 4 4)

(comment ; 10000
  (time (first (step-2 10000)))  ; (out) "Elapsed time:     2.844236 msecs"
  (time (last  (step-2 10000)))) ; (out) "Elapsed time: 17418.881035 msecs"

;; `mapcat`

;; mapcat = map then concat
(apply concat (map rest [[1 2 3] [4 5 6]])) ; (2 3 5 6)
(mapcat rest            [[1 2 3] [4 5 6]])  ; (2 3 5 6)

;; Fibonacci

;; Assigning a lazy sequence created with `lazy-cat` directly to a var is potentially dangerous.

;; `lazy-cat` is a macro and its arguments not evaluated; `concat` would not work
;; elegant but cannot be garbage collected (bc it's a var and holds on to the head)
(def fibs (lazy-cat [0 1] (map +' fibs (rest fibs))))

(take 10 fibs) ; (0 1 1 2 3 5 8 13 21 34)

(comment
  (last (take 1000000 fibs)))
  ;; Exception: java.lang.OutOfMemoryError (depends on JVM settings)

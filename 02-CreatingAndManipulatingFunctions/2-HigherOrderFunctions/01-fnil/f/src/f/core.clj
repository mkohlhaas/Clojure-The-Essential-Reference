(ns f.core
  (:require [clojure.string :refer [replace split] :rename {replace replace-str}]))

;; ;;;;;;;;;
;; Example 1
;; ;;;;;;;;;

(comment
  (inc nil)
  ; (err) java.lang.NullPointerException

  (update {:a 1 :b 2} :c inc))
  ; (err) java.lang.NullPointerException

(update {:a 1 :b 2} :c (fnil inc 0))
; {:a 1, :b 2, :c 1}

;; ;;;;;;;;;
;; Example 2
;; ;;;;;;;;;

(def request-params
  {:name     "Jack"
   :selection nil})

(defn as-nums [selection]
  (let [nums (split selection #",")]
    (map #(Integer/valueOf %) nums)))

(comment
  (split nil #",")
  ; (err) java.lang.NullPointerException

  (as-nums (:selection request-params)))
  ; (err) java.lang.NullPointerException

(def as-nums+ (fnil as-nums "0,1,2"))

(as-nums+ nil)                         ; (0 1 2)
(as-nums+ (:selection request-params)) ; (0 1 2)

;; ;;;;;;;;;
;; Example 3
;; ;;;;;;;;;

(def greetings
  (fnil replace-str "Nothing to replace" "Morning" "Evening"))

(greetings "Good Morning!" "Morning" "Evening") ; "Good Evening!"
(greetings  nil            "Morning" "Evening") ; "Nothing to replace"
(greetings "Good Morning!"  nil      "Evening") ; "Good Evening!"
(greetings "Good Morning!" "Morning"  nil)      ; "Good Evening!"

;; ;;;;;;;;;
;; Example 4
;; ;;;;;;;;;

(defn fnil+ [f & defaults]
  (fn [& args]
    (apply f
           (map (fn [value default] (if (nil? value) default value))
                args
                (concat defaults (repeat nil))))))

(comment
  (+ 1 2 nil 4 5 nil))
  ; (err) Execution error (NullPointerException)

(def zero-defaulting-sum
  (apply fnil+ + (repeat 0)))

(zero-defaulting-sum 1 2 nil 4 5 nil)
; 12

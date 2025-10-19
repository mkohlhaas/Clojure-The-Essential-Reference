(ns f.core
  (:require
   [clojure.string :refer [join]]))

;; function multiplexer
((juxt first second last) (range 10))
; [0 1 9]

;; ;;;;;;;;;;
;; Grid 5 x 5
;; ;;;;;;;;;;

(def dim #{0 1 2 3 4})

(defn up    [[x y]] [x (dec y)])
(defn down  [[x y]] [x (inc y)])
(defn left  [[x y]] [(dec x) y])
(defn right [[x y]] [(inc x) y])

(defn valid? [[x y]]
  (and (dim x) (dim y)))

(defn neighbors [cell]
  (filter valid?
          ((juxt up down left right) cell)))

(neighbors [2 1])
; ([2 0] [2 2] [1 1] [3 1])

(neighbors [0 0])
; ([0 1] [1 0])

;; ;;;;;;;;;;;;
;; Word Example
;; ;;;;;;;;;;;;

(def words ["book" "this" "an" "awesome" "is"])

(map #(vector (count %) %) words)
; ([4 "book"] [4 "this"] [2 "an"] [7 "awesome"] [2 "is"])

(map (juxt count identity) words)
; ([4 "book"] [4 "this"] [2 "an"] [7 "awesome"] [2 "is"])

;; ;;;;
;; Post
;; ;;;;

(def post
  {:formatted-tag "Fireworks 2016"
   :destinations  ["north" "south"]
   :count         200
   :css-align     "ending"
   :normal-title  "people expected tonight"
   :headline      "Admiral Derek on the ship to Nebraska"})

;; extracting values from a map
(->> post
     ((juxt :count :normal-title))
     (join " "))
; "200 people expected tonight"

;; ;;;;;;;;;;;;
;; Sort Example
;; ;;;;;;;;;;;;

(sort-by count            ["wd5" "aba" "yp" "csu" "nwd7"]) ; ("yp" "wd5" "aba" "csu" "nwd7")
(sort-by (juxt count str) ["wd5" "aba" "yp" "csu" "nwd7"]) ; ("yp" "aba" "csu" "wd5" "nwd7")

;; ;;;;;;;;;;;;;;
;; Person Example
;; ;;;;;;;;;;;;;;

 ;; `juxt` is effectively nesting sort and grouping operations

(def person-table
  [{:id 1234567 :name "Annette Kann"      :age 31 :nick "Ann"    :sex :f}
   {:id 1000101 :name "Emma May"          :age 33 :nick "Emma"   :sex :f}
   {:id 1020010 :name "Johanna Reeves"    :age 31 :nick "Jackie" :sex :f}
   {:id 4209100 :name "Stephen Grossmann" :age 33 :nick "Steve"  :sex :m}])

(def sort-criteria  (juxt :age :nick))

(defn sort-by-age-and-nick-name [t]
  (->> t
       (sort-by sort-criteria)
       (map sort-criteria)))

(sort-by-age-and-nick-name person-table)
; ([31 "Ann"] [31 "Jackie"] [33 "Emma"] [33 "Steve"])

(def group-criteria (juxt :age :sex))

(defn group-by-age-and-sex [t]
  (->> t
       (group-by group-criteria)
       (map (fn [[k v]] {k (map sort-criteria v)}))))

(group-by-age-and-sex person-table)
; ({[31 :f] ([31 "Ann"] [31 "Jackie"])}
;  {[33 :f] ([33 "Emma"])}
;  {[33 :m] ([33 "Steve"])})

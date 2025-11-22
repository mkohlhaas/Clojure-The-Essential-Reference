(ns f.core
  (:require
   [clojure.string :refer [split-lines]]))

(sorted-set                     "t" "d" "j" "w" "y") ; #{"d" "j" "t" "w" "y"}
(sorted-set-by #(compare %2 %1) "t" "d" "j" "w" "y") ; #{"y" "w" "t" "j" "d"}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn timed [s]
  (let [t (System/nanoTime)]
    (println "key" s "created at" t)
    (if (instance? clojure.lang.IMeta s)
      (with-meta s {:created-at t}) ; add metadata if technically possible
      s)))

(comment
  (instance? clojure.lang.IMeta 'a)  ; true
  (instance? clojure.lang.IMeta :a)) ; false

;; symbol
(def s1 (sorted-set (timed 'a) (timed 'a))) ; #{a}
; (out) key a created at 8036827783084
; (out) key a created at 8036829460321

;; keeps metadata of first entry
(meta (first s1)) ; {:created-at 8036827783084}

;; keyword
(def s2 (sorted-set (timed :a) (timed :a))) ; #{:a}
; (out) key :a created at 14093896579459
; (out) key :a created at 14093896940091

(meta (first s2)) ; nil

;; Dictionary of Sorted Words

;; sudo pacman -S words
(def dict
  (atom
   (->> "/usr/share/dict/words"
        slurp
        split-lines
        (into (sorted-set)))))

(count @dict) ; 123985

;; add new word to dictionary if so desired
(defn new-word [w]
  (println "Could not find the word:" w)
  (println "Add word to dictionary? [y/n]")
  (when (= "y" (read-line))
    (swap! dict conj w)
    (take 5 (subseq @dict >= w)))) ; show a few entries after newly inserted word (and the new word itself)

(defn spell-check [w]
  (if (contains? @dict w)
    (println "Word spelled correctly")
    (new-word w)))

(defn ask-word []
  (println "Please type word:")
  (when-let [w (read-line)]
    (spell-check w)))

(comment
  (ask-word)
  ; Please type word:
  ; bilky
  ; Could not find the word: bilky
  ; Add word to dictionary? [y/n]
  ; y
  ; word added ("bilky" "bill" "bill's" "billable" "billboard" 

  (ask-word))
  ; Please type word:
  ; bilky
  ; Word spelled correctly

;; ;;;;;;;;;;;;;;;;;;
;; Custom Comparators
;; ;;;;;;;;;;;;;;;;;;

(sorted-set            [1 "b" :x] [1 "a" :y]) ; #{[1 "a" :y] [1 "b" :x]}
(sorted-set-by compare [1 "b" :x] [1 "a" :y]) ; #{[1 "a" :y] [1 "b" :x]}

;; sort by length of collections
;; erroneous
(sorted-set-by
 (fn [a b]      ; #{[1 :a] [:b]}
   (compare (count b) (count a)))
 [1 :a] [:b] [3 :c] [:v])

;; There are two distinct phases involved in adding a new element to a sorted-set - equality and relative ordering:
;; 1. Skip the item if it’s already in the set. To do this, the comparator is called on each existing item 
;;    against the new one. If any comparison returns "0" (which means they are the same for a possibly custom 
;;    definition of equality, then the element is not added to the set.
;; 2. If not already in the set, modify the set structurally to accommodate the new item in the right place.
;;    This phase uses the comparator again to decide where the new item should be added.

;; sort by length of collections
;; correct
(sorted-set-by
 (fn [a b]      ; #{[1 :a] [3 :c] [:b] [:v]}
   (let [cmp (compare (count b) (count a))]
     (if (zero? cmp)
       (compare a b)
       cmp)))
 [1 :a] [:b] [3 :c] [:v])

;; sort by length of collections
;; correct (easier)
(sorted-set-by
 (fn [a b]      ; #{[1 :a] [3 :c] [:b] [:v]}
   (compare [(count b) a] [(count a) b])) ; vector equality compares items by index
 [1 :a] [:b] [3 :c] [:v])


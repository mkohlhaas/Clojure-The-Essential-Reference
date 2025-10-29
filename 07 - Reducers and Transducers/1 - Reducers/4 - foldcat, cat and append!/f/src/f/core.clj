(ns f.core
  (:require [clojure.core.reducers :as r]
            [clojure.string :as s]
            [criterium.core :refer [quick-bench]])
  (:import  [java.util HashSet]))

;; Goal is to avoid frequent updates of temporary persistent data structures.

;; r/append! allows incremental build of mutable data structures while r/cat appends them to a tree.
;; The orchestration of both effects is achieved by r/foldcat which just uses r/cat as "combinef" and r/append! as "reducef":

(def input (r/map inc (into [] (range 1000)))) ; #object[clojure.core.reducers$folder$reify__12275 0xb7009a3 "clojure.core.reducers$folder$reify__12275@b7009a3"]

;; it's like applying `input`
(take 5 (r/fold r/cat r/append! input))
; (1 2 3 4 5)

;; the same: from the docs "Equivalent to (fold cat append! coll)"
(take 5 (r/foldcat input))
; (1 2 3 4 5)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def text
  (-> "https://tinyurl.com/wandpeace"
      slurp
      s/split-lines))

(take 10 text)
; ("﻿"
;  "The Project Gutenberg EBook of War and Peace, by Leo Tolstoy"
;  ""
;  "This eBook is for the use of anyone anywhere at no cost and with almost"
;  "no restrictions whatsoever. You may copy it, give it away or re-use"
;  "it under the terms of the Project Gutenberg License included with this"
;  "eBook or online at www.gutenberg.org"
;  ""
;  ""
;  "Title: War and Peace")

#_{:clj-kondo/ignore [:invalid-arity]}
;; `comp` in reverse order (bottom-up)
(def r-word
  (comp
   (r/map #(vector % (count %)))
   (r/map s/lower-case)
   (r/remove s/blank?)
   (r/map #(re-find #"\w+" %))
   (r/mapcat #(s/split % #"\s+"))))

;; apply `(r-word text)` with foldcat
(def words (r/foldcat (r-word text)))

(take 5 words)
; (["the" 3] ["project" 7] ["gutenberg" 9] ["ebook" 5] ["of" 2])

;; words is not a normal collection (it would have been an ArrayList for a file with less than 512 words)
;; it's a deftype with three fields: count, left, right
(type   words) ; clojure.core.reducers.Cat (this represents the root of a binary tree)
(.count words) ; 565987
(.left  words) ; #object[clojure.core.reducers.Cat 0x28e8dde3 "clojure.core.reducers.Cat@28e8dde3"]
(.right words) ; #object[clojure.core.reducers.Cat 0x1f6c9cd8 "clojure.core.reducers.Cat@1f6c9cd8"]

;; If we walk the tree all the way down to a leaf, we can find a java.util.ArrayList instance created by invoking r/append! on each chunk:
(loop [root words
       cnt 0]
  (if (< (count root) 512)
    (str (type root) " " (count root) " words, depth: " cnt)
    (recur (.left root) (inc cnt))))
; "class java.util.ArrayList 321 words, depth: 8"

;; need to convert Cat object to a sequence:
(count (distinct (seq words))) ; 17202

#_{:clj-kondo/ignore [:redefined-var]}
(def words
  (r/fold
   (r/cat #(HashSet.)) ; swap internal implementation with a HashSet (must expose an .add method)
   r/append!
   (r-word text)))

;; wrong result
(count words) ; 185556

(defn distinct-words [words]
  (letfn [(walk [root res]
            (cond
              (instance? clojure.core.reducers.Cat root) (do (walk (.left  root) res)
                                                             (walk (.right root) res))
              (instance? java.util.HashSet root)         (doto res (.addAll root))
              :else res))]
    (into #{} (walk words (HashSet.)))))

(count (distinct-words words)) ; 17202

(reduce + (r/map last words)) ; 1105565

(defn letter-frequency [words]
  (let [res (r/fold
             (r/cat #(StringBuilder.))
             #(doto %1 (.append %2))
             (r/map first words))]
    (frequencies res)))

(take 5 (sort-by last > (letter-frequency words)))
; ([\e 144853] [\n 82291] [\i 80318] [\s 78874] [\a 78157])

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (quick-bench (doall (r/foldcat (r-word text))))
  ; (out) Execution time mean : 447.749286 ms

  (quick-bench (doall (r/fold concat conj (r-word text)))))
  ; (out) Execution time mean : 1.053018 sec

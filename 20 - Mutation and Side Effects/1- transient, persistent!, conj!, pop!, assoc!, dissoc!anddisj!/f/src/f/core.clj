(ns f.core
  (:require
   [criterium.core :refer [quick-bench]])
  (:import
   [java.util HashMap]))

;; - 1. `transient`
;; - 2. bang functions (`assoc!`, `conj!`, …)
;; - 3. `persistent!`

(type (transient [])) ; clojure.lang.PersistentVector$TransientVector

(comment
  ;; you can't call the 'wrong' functions on transients
  (conj (transient []) 1))
  ; (err) Execution error (ClassCastException)
  ; (err) class clojure.lang.PersistentVector$TransientVector cannot be cast to class clojure.lang.IPersistentCollection

;; supported collections: vectors, sets, maps 
(def v (transient []))  ; #object[clojure.lang.PersistentVector$TransientVector …]
(def s (transient #{})) ; #object[clojure.lang.PersistentHashSet$TransientHashSet …]
(def m (transient {}))  ; #object[clojure.lang.PersistentArrayMap$TransientArrayMap …]

((conj!  v 0) 0)     ; 0
((conj!  s 0) 0)     ; 0
((assoc! m :a 0) :a) ; 0

(comment
   ;; read-only functions like `get`, `nth` or `count` still work
  (count v)  ; 1
  (count s)  ; 1
  (count m)) ; 1

;; the standard library implements many fundamental functions on top of transients, e.g. `frequencies` ;;

;; doesn't use transients
(defn frequencies* [coll]
  (reduce
   (fn [counts x]
     (assoc counts x (inc (get counts x 0)))) {} coll))

(comment
  (let [coll (range 10000)]              ; (out) Execution time mean : 4.321175 ms
    (quick-bench (frequencies* coll)))

  (let [coll (range 10000)]              ; (out) Execution time mean : 2.481001 ms
    (quick-bench (frequencies coll))))

;; we need to be careful about reusing the same reference to the transient ;;

(def transient-map (transient {})) ; #object[clojure.lang.PersistentArrayMap$TransientArrayMap 0xfd137bb "clojure.lang.PersistentArrayMap$TransientArrayMap@fd137bb"]
(def java-map      (HashMap.))     ; {}

(dotimes [i 20]
  (assoc! transient-map i i)
  (.put java-map i i))

;; Oops! Many keys are missing!
(persistent! transient-map) ; {0 0, 1 1, 2 2, 3 3, 4 4, 5 5, 6 6, 7 7}

;; ✓
(into {} java-map)
; {0 0, 7 7, 1 1, 4 4, 15 15, 13 13, 6 6, 17 17, 3 3, 12 12, 2 2,
;  19 19, 11 11, 9 9, 5 5, 14 14, 16 16, 10 10, 18 18, 8 8}

;; Clojure transient doesn’t make any promise that it will mutate the input in place. ;;

(def transient-map1 (transient {}))

;; Each transient operation should instead consider the input obsolete and use the new version returned by the mutating function!
(def m1
  (reduce
   (fn [m k] (assoc! m k k))
   transient-map1
   (range 20)))

;; ✓
(persistent! m1)
; {0 0, 7 7, 1 1, 4 4, 15 15, 13 13, 6 6, 17 17, 3 3, 12 12, 2 2,
;  19 19, 11 11, 9 9, 5 5, 14 14, 16 16, 10 10, 18 18, 8 8}

;; Also remember that transients are unsynchronized.
;; Use of same transient instance by multiple threads can lead to unpredictable results.
;; See locking for a way to ensure transient mutations happen in a synchronized context.

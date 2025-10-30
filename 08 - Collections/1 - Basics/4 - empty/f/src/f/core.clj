(ns f.core)

;; `empty` creates an empty collection of the same type as the input argument.

(empty (range 10))              ; ()
(empty (frequencies [1 1 2 3])) ; {}

(defrecord Address [number street town])

(def home (Address. 12 "High st" "Alberta"))

(coll? home) ; true

(comment
  (empty home))
  ; (err) Execution error (UnsupportedOperationException)
  ; (err) Can't create empty: f.core.Address

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(let [coll [1 2 3]]
  (cond
    (list?   coll) '()
    (map?    coll)  {}
    (vector? coll)  []
    :else "not found"))
; []

(empty [1 2 3])
; []

;; Creating new nodes while maintaining the original type is a typical problem when "walking" collections. 

;; we expect the output to have the same structure as the input
(defn walk [data pred f]
  (letfn [(walk-m [m] (reduce-kv (fn [m k v] (assoc m k (walk v pred f))) {} m)) ; iterate map
          (walk-c [c] (map (fn [item] (walk item pred f)) c))]                   ; iterate collection
    (cond
      (map?  data) (walk-m data)                     ; map
      (coll? data) (into (empty data) (walk-c data)) ; collection
      :else        (if (pred data) (f data) data)))) ; scalar data

(def coll {:a [1 "a" [] {:c "z"} [1 2]] :av 1N}) ; #'f.core/coll

;; increment every odd number
(walk coll (every-pred number? odd?) inc)
; {:a [2 "a" [] {:c "z"} [2 2]], :av 2N}

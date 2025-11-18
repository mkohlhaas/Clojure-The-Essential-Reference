(ns f.core)

(def m {:a "1" :b "2" :c "3"})

;; `assoc` replaces the value of an existing key or inserts a new key if one doesn't exist
(assoc m :b "changed") ; {:a "1", :b "changed", :c "3"}

m ; {:a "1", :b "2", :c "3"}

;; `dissoc` removes keys 
(dissoc m :a :c) ; {:b "2"}

;; nested maps
(let [m {:a "1" :b "2" :c {:x1 {:x2 "z1"}}}]
  (assoc-in m [:c :x1 :x2] "z2"))             ; {:a "1", :b "2", :c {:x1 {:x2 "z2"}}}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(let [m {nil 0 :c 2}]
  (-> m                ; {:c 3, :a 1, :b 2} (result)
      (assoc :a 1)     ; {nil 0, :c 2, :a 1}
      (dissoc nil)     ; {:c 2, :a 1}
      (update :c inc)  ; {:c 3, :a 1}
      (merge {:b 2}))) ; {:c 3, :a 1, :b 2}

;; gradually build a map with `reduce`

(defn lookup [id]
  {:index  "backup"
   :bucket (rand-int (* 100 id))})

(let [requests [12 41 11]]
  (reduce                   ; {12 {:index "backup", :bucket 595}, 41 {:index "backup", :bucket 1683}, 11 {:index "backup", :bucket 398}}
   (fn [m id] (assoc m id (lookup id)))
   {}
   requests))
; {12 {:index "backup", :bucket 595},
;  41 {:index "backup", :bucket 1683},
;  11 {:index "backup", :bucket 398}}

;; `assoc-in` is typically used with nested data structures

;; vector of articles
(def articles
  [{:title  "Another win for India"
    :date   "2017-11-23"
    :ads    [2 5 8]
    :author "John McKinley"}
   {:title  "Hottest day of the year"
    :date   "2018-08-15"
    :ads    [1 3 5]
    :author "Emma Cribs"}
   {:title  "Expected a rise in Bitcoin shares"
    :date   "2018-12-11"
    :ads    [2 4 6]
    :author "Zoe Eastwood"}])

(assoc-in articles [2 :ads 1] 3)
; [{:title "Another win for India",
;   :date "2017-11-23",
;   :ads [2 5 8],
;   :author "John McKinley"}
;  {:title "Hottest day of the year",
;   :date "2018-08-15",
;   :ads [1 3 5],
;   :author "Emma Cribs"}
;  {:title "Expected a rise in Bitcoin shares",
;   :date "2018-12-11",
;   :ads [2 3 6],                                   (this was updated: 4 -> 3)
;   :author "Zoe Eastwood"}]

;; `assoc` is an alternative to `conj` where an element could be replaced or added to a vector

;; [item index]
(let [pairs [[:f 1] [:t 0] [:r 2] [:w 0]]]
  (map                                      ; ([:f :f] [:t 0] [:r 2 :r] [:w 0])
   (fn [[item index :as v]]
     (assoc v index item))
   pairs))

;; ;;;;;;;;;
;; dissoc-in
;; ;;;;;;;;;

;; there is no `dissoc-in` in the standard library

(defn dissoc-in [m [k & ks]]
  (if ks
    (assoc m k (dissoc-in (get m k) ks))
    (dissoc m k))) ; last k is dissociated

(let [m {:a {:b 2 :c {:d 4 :e 5}}}]
  (dissoc-in m [:a :c :d]))          ; {:a {:b 2, :c {:e 5}}}

;; elegant version of `dissoc-in`
(defn dissoc-in-1 [m ks]
  (update-in m (butlast ks) dissoc (last ks)))

(let [m {:a [0 1 2 {:d 4 :e [0 1 2]}]}]
  (dissoc-in-1 m [:a 3 :e]))             ; {:a [0 1 2 {:d 4}]}

(comment
  ;; doesn't work on vectors

  ;; dissoc-in 
  (let [m {:a [0 1 2 {:d 4 :e [0 1 2]}]}]
    (dissoc-in m [:a 3 :e 0]))
; (err) Execution error (ClassCastException)
; (err) class clojure.lang.PersistentVector cannot be cast to class clojure.lang.IPersistentMap

  ;; dissoc-in-1 
  (let [m {:a [0 1 2 {:d 4 :e [0 1 2]}]}]
    (dissoc-in-1 m [:a 3 :e 0])))
  ; (err) Execution error (ClassCastException)
  ; (err) class clojure.lang.PersistentVector cannot be cast to class clojure.lang.IPersistentMap

;; a version of dissoc-in that works also on vectors

(defn remove-at [v idx]
  (into (subvec v 0 idx)
        (subvec v (inc idx) (count v))))

(comment
  (remove-at [0 1 2 3 4] 0)   ; [1 2 3 4]
  (remove-at [0 1 2 3 4] 1)   ; [0 2 3 4]
  (remove-at [0 1 2 3 4] 2)   ; [0 1 3 4]
  (remove-at [0 1 2 3 4] 3)   ; [0 1 2 4]
  (remove-at [0 1 2 3 4] 4)   ; [0 1 2 3]
  (remove-at [0 1 2 3 4] 10)) ; (err) Execution error (IndexOutOfBoundsException) at f.core/remove-at (form-init7950203132224340833.clj:100).

(defn dissoc-in-2 [m [k & ks]]
  (if ks
    (assoc m k (dissoc-in-2 (get m k) ks))
    (cond
      (map?    m) (dissoc m k)
      (vector? m) (remove-at m k)
      :else m)))

(let [m5 {:a [0 1 2 {:d 4 :e [0 1 2]}]}]
  (dissoc-in-2 m5 [:a 3 :e 0]))           ; {:a [0 1 2 {:d 4, :e [1 2]}]}

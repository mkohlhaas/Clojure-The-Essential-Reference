(ns f.core)

(def m
  {:a "1" :b "2" :c "3"})

(assoc m :b "changed")  ; {:a "1", :b "changed", :c "3"}

m  ; {:a "1", :b "2", :c "3"}

(dissoc m :a :c)  ; {:b "2"}

#_{:clj-kondo/ignore [:redefined-var]}
(def m
  {:a "1" :b "2" :c {:x1 {:x2 "z1"}}})

(assoc-in m [:c :x1 :x2] "z2")  ; {:a "1", :b "2", :c {:x1 {:x2 "z2"}}}

#_{:clj-kondo/ignore [:redefined-var]}
(def m {nil 0 :c 2})

(-> m
    (assoc :a 1)    ; {nil 0, :c 2, :a 1}
    (dissoc nil)    ; {:c 2, :a 1}
    (update :c inc) ; {:c 3, :a 1}
    (merge {:b 2})) ; {:c 3, :a 1, :b 2}

(defn lookup [id]
  {:index "backup"
   :bucket (rand-int (* 100 id))})

(def request [12 41 11])

(reduce (fn [m item] (assoc m item (lookup item))) {} request)
; {12 {:index "backup", :bucket 595},
;  41 {:index "backup", :bucket 1683},
;  11 {:index "backup", :bucket 398}}

(def articles
  [{:title "Another win for India"
    :date "2017-11-23"
    :ads [2 5 8]
    :author "John McKinley"}
   {:title "Hottest day of the year"
    :date "2018-08-15"
    :ads [1 3 5]
    :author "Emma Cribs"}
   {:title "Expected a rise in Bitcoin shares"
    :date "2018-12-11"
    :ads [2 4 6]
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
;   :ads [2 3 6],
;   :author "Zoe Eastwood"}]

(def pairs [[:f 1] [:t 0] [:r 2] [:w 0]])

(map (fn [[item index :as v]]
       (assoc v index item)) pairs)
; ([:f :f] [:t 0] [:r 2 :r] [:w 0])

#_{:clj-kondo/ignore [:redefined-var]}
(def m {:a {:b 2 :c {:d 4 :e 5}}})

(defn dissoc-in [m [k & ks]]
  (if ks
    (assoc m k (dissoc-in (get m k) ks))
    (dissoc m k)))

(dissoc-in m [:a :c :d])  ; {:a {:b 2, :c {:e 5}}}

(defn dissoc-in [m ks]
  (update-in m (butlast ks) dissoc (last ks)))

(let [m {:a [0 1 2 {:d 4 :e [0 1 2]}]}]
  (dissoc-in m [:a 3 :e]))
; {:a [0 1 2 {:d 4}]}

(comment
  (let [m {:a [0 1 2 {:d 4 :e [0 1 2]}]}]
    (dissoc-in m [:a 3 :e 0])))
; (err) Execution error (ClassCastException)
; (err) class clojure.lang.PersistentVector cannot be cast to class clojure.lang.IPersistentMap

#_{:clj-kondo/ignore [:redefined-var]}
(def m {:a [0 1 2 {:d 4 :e [0 1 2]}]})

(defn remove-at [v idx]
  (into (subvec v 0 idx)
        (subvec v (inc idx) (count v))))

(defn dissoc-in [m [k & ks]]
  (if ks
    (assoc m k (dissoc-in (get m k) ks))
    (cond
      (map? m) (dissoc m k)
      (vector? m) (remove-at m k)
      :else m)))

(dissoc-in m [:a 3 :e 0]) ; {:a [0 1 2 {:d 4, :e [1 2]}]}

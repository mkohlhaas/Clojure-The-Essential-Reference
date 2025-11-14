(ns f.core
  (:require
   [clojure.string :refer [split split-lines]]
   [criterium.core :refer [quick-bench]]))

(= (create-struct :x :y)
   (create-struct :x :y))
; false

(= (struct (create-struct :x :y) 1 2)
   (struct (create-struct :x :y) 1 2))
; true

(defstruct point-2d :x :y)
(defstruct point-3d :x :y :z)

(= (assoc (struct point-2d 1 2) :z 3)
   (struct point-3d 1 2 3))
; true

(struct (create-struct :a :b :c) 1 2 3)
; {:a 1, :b 2, :c 3}

(type (defrecord abc [a b c])) ; java.lang.Class

(abc. 1 2 3) ; {:a 1, :b 2, :c 3}
; #user.abc{:a 1, :b 2, :c 3}

(def waypoints "https://tinyurl.com/station-locs")
(def lines (-> waypoints slurp split-lines))
; ["VHF  0.000000 0.000000 ABI "
;  "VHF  32.464722 13.170000 ABU "
;  "VHF  -28.570555 16.533890 ABV "
;  "VHF  5.633333 -0.155000 ACC "
;  "VHF  5.277194 -3.919306 AD  "
; …]

(def waypoints
  (let [sdef (create-struct :type :lat :lon :id)]
    (transduce
     (comp
      (map #(split % #"\s+"))
      (map #(apply struct sdef %)))
     conj
     lines)))

(first waypoints) ; {:type "VHF", :lat "0.000000", :lon "0.000000", :id "ABI"}

(defstruct waypoint :type :lat :lon :id)

(def coordinates
  [{:alt 150 :lat "18.3112" :lon "3.1314" :id "XVA"}
   {:alt 312 :lon "10.04883" :id "FFA" :type "XFV"}
   {:temp 78.3 :lat "23.7611" :id "XJP"}])

(defn to-waypoints [coords]
  (map #(apply struct-map waypoint (mapcat identity %)) coords))

(to-waypoints coordinates)
; ({:type nil, :lat "18.3112", :lon "3.1314", :id "XVA", :alt 150}
;  {:type "XFV", :lat nil, :lon "10.04883", :id "FFA", :alt 312}
;  {:type nil, :lat "23.7611", :lon nil, :id "XJP", :temp 78.3})

(def k-type (accessor waypoint :type))
(def k-lat  (accessor waypoint :lat))
(def k-lon  (accessor waypoint :lon))
(def k-id   (accessor waypoint :id))

#_{:clj-kondo/ignore [:redefined-var]}
(def waypoints (to-waypoints coordinates))
; ({:type nil, :lat "18.3112", :lon "3.1314", :id "XVA", :alt 150}
;  {:type "XFV", :lat nil, :lon "10.04883", :id "FFA", :alt 312}
;  {:type nil, :lat "23.7611", :lon nil, :id "XJP", :temp 78.3})

(map k-id waypoints)
; ("XVA" "FFA" "XJP")

(def waypoints "https://tinyurl.com/station-locs")
(def lines (->> waypoints slurp split-lines (map #(split % #"\s+"))))
(last lines)
;; ["ARP" "44.244823" "-84.179802" "Y31"] ; <1>

(defstruct w-struct :type :lat :lon :id) ; <2>
(defrecord w-record [type lat lon id])
(defn w-map [type lat lon id] {:type type :lat lat :lon lon :id id})
(defn w-hmap [type lat lon id] (hash-map :type type :lat lat :lon lon :id id))

(def points-struct (doall (map #(apply struct w-struct %) lines)))
(def points-record (doall (map #(apply ->w-record %) lines)))
(def points-map    (doall (map #(apply w-map %) lines)))
(def points-hmap   (doall (map #(apply w-hmap %) lines)))

(def id (accessor w-struct :id))

(comment
  (quick-bench (doall (map #(apply struct w-struct %) lines))) ; (out) Execution time mean : 16.751639 ms
  (quick-bench (doall (map #(apply ->w-record %) lines)))      ; (out) Execution time mean : 15.579792 ms
  (quick-bench (doall (map #(apply w-map %) lines)))           ; (out) Execution time mean : 17.456291 ms
  (quick-bench (doall (map #(apply w-hmap %) lines)))          ; (out) Execution time mean : 52.904692 ms

  (let [w (first points-struct)] (quick-bench (id  w)))  ; (out) Execution time mean : 5.237529 ns
  (let [w (first points-struct)] (quick-bench (:id  w))) ; (out) Execution time mean : 25.221455 ns
  (let [w (first points-record)] (quick-bench (:id w)))  ; (out) Execution time mean : 15.107096 ns
  (let [w (first points-map)]    (quick-bench (:id w)))  ; (out) Execution time mean : 24.093760 ns
  (let [w (first points-hmap)]   (quick-bench (:id w)))  ; (out) Execution time mean : 48.636786 ns

  (let [^user.w-record w (first points-record)] (quick-bench (.id w))))
;; Execution time mean : 3.612035 ns


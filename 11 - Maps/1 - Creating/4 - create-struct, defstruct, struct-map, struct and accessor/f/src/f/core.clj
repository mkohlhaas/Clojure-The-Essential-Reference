(ns f.core
  (:require
   [clojure.string :refer [split split-lines]]
   [criterium.core :refer [quick-bench]]))

;; structs are not obsolete, but superseded by records

;; A struct is much simpler than the more powerful defrecord.
;; One fundamental difference is that create-struct creates an anonymous struct definition,
;; while defrecord creates a Java class as a side effect.
;; defrecord generates a class by design: this allows powerful features such as inheritance of record types.

;; create-struct: create a struct definition
;; defstruct: create a named struct definition
;; struct: create a struct instance from a struct definition
;; struct-map: create a struct instance from a struct defintion
;; accessor: create an accessor function from a struct defintion for a struct field
;;           optimizes frequent access by skipping the typical hash-based access by a faster array-index lookup

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; structs are map-like types with a minimal set of required keys

;; comparing struct definitions
(= (create-struct :x :y)
   (create-struct :x :y))
; false

;; comparing struct instances (two instances with same keys and values are the same)
(= (struct (create-struct :x :y) 1 2)
   (struct (create-struct :x :y) 1 2))
; true

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstruct point-2d :x :y)

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstruct point-3d :x :y :z)

#_{:clj-kondo/ignore [:unresolved-symbol]}
;; adding the missing key makes them equal
(= (struct point-3d 1 2 3)
   (assoc (struct point-2d 1 2) :z 3))
; true

;; struct can be anonymous
(struct (create-struct :a :b :c) 1 2 3)
; {:a 1, :b 2, :c 3}

;; `defrecord` needs a name and generates a class
(type (defrecord abc [a b c])) ; java.lang.Class

;; new record instance
(abc. 1 2 3) ; {:a 1, :b 2, :c 3}

;; Waypoint (contains the coordinates of a point of interest on Earth)

(def waypoints-url "https://tinyurl.com/station-locs")

(comment
  ;; takes a while
  (def lines (->
              waypoints-url
              slurp
              split-lines))
  ; ["VHF  0.000000 0.000000 ABI "
  ;  "VHF  32.464722 13.170000 ABU "
  ;  "VHF  -28.570555 16.533890 ABV "
  ;  "VHF  5.633333 -0.155000 ACC "
  ;  "VHF  5.277194 -3.919306 AD  "
  ; …]

  (count lines) ; 42909

  (def waypoints
    (let [sdef (create-struct :type :lat :lon :id)]
      (transduce
       (comp
        (map #(split % #"\s+"))       ; [["VHF" "0.000000" "0.000000" "ABI"] ["VHF" "32.464722" "13.170000" "ABU"] ["VHF" "-28.570555" "16.533890" "ABV"] ["VHF" "5.633333" "-0.155000" "ACC"]…]
        (map #(apply struct sdef %)))
       conj
       lines)))
  ; [{:type "VHF", :lat "0.000000",   :lon "0.000000",  :id "ABI"}
  ;  {:type "VHF", :lat "32.464722",  :lon "13.170000", :id "ABU"}
  ;  {:type "VHF", :lat "-28.570555", :lon "16.533890", :id "ABV"}
  ;  {:type "VHF", :lat "5.633333",   :lon "-0.155000", :id "ACC"}
  ;  {:type "VHF", :lat "5.277194",   :lon "-3.919306", :id "AD"

  (count waypoints)) ; 42909

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstruct waypoint :type :lat :lon :id)

(def coordinates
  [{:alt 150 :lat "18.3112" :lon "3.1314" :id "XVA"}
   {:alt 312 :lon "10.04883" :id "FFA" :type "XFV"}
   {:temp 78.3 :lat "23.7611" :id "XJP"}])

#_{:clj-kondo/ignore [:unresolved-symbol]}
;; create waypoints from a list of hash-maps
(defn to-waypoints [coords]
  (map
   #(apply struct-map waypoint (mapcat identity %))
   coords))

(comment
  (mapcat identity {:alt 150 :lat "18.3112" :lon "3.1314" :id "XVA"}))
  ; (:alt 150 :lat "18.3112" :lon "3.1314" :id "XVA")

;; creates a list of waypoint structs
(to-waypoints coordinates)
; ({:type nil,   :lat "18.3112", :lon "3.1314",   :id "XVA", :alt 150}
;  {:type "XFV", :lat nil,       :lon "10.04883", :id "FFA", :alt 312}
;  {:type nil,   :lat "23.7611", :lon nil,        :id "XJP", :temp 78.3})

;; accessors for each key in a waypoint

#_{:clj-kondo/ignore [:unresolved-symbol]}
#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(def k-type (accessor waypoint :type))

#_{:clj-kondo/ignore [:unresolved-symbol]}
#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(def k-lat  (accessor waypoint :lat))

#_{:clj-kondo/ignore [:unresolved-symbol]}
#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(def k-lon  (accessor waypoint :lon))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(def k-id   (accessor waypoint :id))

(map k-id (to-waypoints coordinates))
; ("XVA" "FFA" "XJP")

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (def lines
    (->> waypoints-url
         slurp
         split-lines
         (map #(split % #"\s+")))))
  ; (["VHF" "0.000000" "0.000000" "ABI"]
  ;  ["VHF" "32.464722" "13.170000" "ABU"]
  ;  ["VHF" "-28.570555" "16.533890" "ABV"]
  ;  ["VHF" "5.633333" "-0.155000" "ACC"]
  ;  ["VHF" "5.277194" "-3.919306" "AD"]
  ; …)

#_{:clj-kondo/ignore [:unresolved-symbol]}
(defstruct w-struct :type :lat :lon :id)
(defrecord w-record [type lat lon id])

(defn w-map  [type lat lon id] {:type type :lat lat :lon lon :id id})
(defn w-hmap [type lat lon id] (hash-map :type type :lat lat :lon lon :id id))

(comment
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (def points-struct (doall (map #(apply struct w-struct %) lines)))
  (def points-record (doall (map #(apply ->w-record %)      lines)))
  (def points-map    (doall (map #(apply w-map %)           lines)))
  (def points-hmap   (doall (map #(apply w-hmap %)          lines))))

#_{:clj-kondo/ignore [:unresolved-symbol]}
(def id (accessor w-struct :id))

(comment
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (quick-bench (doall (map #(apply struct w-struct %) lines))) ; (out) Execution time mean : 16.751639 ms
  (quick-bench (doall (map #(apply ->w-record %)      lines))) ; (out) Execution time mean : 15.579792 ms
  (quick-bench (doall (map #(apply w-map %)           lines))) ; (out) Execution time mean : 17.456291 ms
  (quick-bench (doall (map #(apply w-hmap %)          lines))) ; (out) Execution time mean : 52.904692 ms

  (let [w           (first points-struct)] (quick-bench  (id w)))  ; (out) Execution time mean :  5.237529 ns
  (let [w           (first points-struct)] (quick-bench (:id w)))  ; (out) Execution time mean : 25.221455 ns
  (let [w           (first points-record)] (quick-bench (:id w)))  ; (out) Execution time mean : 15.107096 ns
  (let [w           (first points-map)]    (quick-bench (:id w)))  ; (out) Execution time mean : 24.093760 ns
  (let [w           (first points-hmap)]   (quick-bench (:id w)))  ; (out) Execution time mean : 48.636786 ns
  (let [^w-record w (first points-record)] (quick-bench (.id w)))) ; (out) Execution time mean : 0.093315 ns


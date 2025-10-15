(ns f.core)

(filter odd?  [1 2 3 4 5]) ; (1 3 5) (filter-in)
(remove even? [1 2 3 4 5]) ; (1 3 5) (filter-out)

;; ;;;;;;;;;;;;;;;;
;; Weather Stations
;; ;;;;;;;;;;;;;;;;

(def events
  [{:device "AX31F" :owner "heathrow"
    :date "2016-11-19T14:14:35.360Z"
    :payload {:temperature 62
              :wind-speed 22
              :solar-radiation 470.2
              :humidity 38
              :rain-accumulation 2}}
   {:device "AX31F" :owner "heathrow"
    :date "2016-11-19T14:15:38.360Z"
    :payload {:wind-speed 17
              :solar-radiation 200.2
              :humidity 46
              :rain-accumulation 12}}
   {:device "AX31F" :owner "heathrow"
    :date "2016-11-19T14:16:35.362Z"
    :payload {:temperature :error
              :wind-speed 18
              :humidity 38
              :rain-accumulation 2}}
   {:device "AX31F" :owner "heathrow"
    :date "2016-11-19T14:16:35.364Z"
    :payload {:temperature 60
              :wind-speed 18
              :humidity 38
              :rain-accumulation 2}}])

(def event-stream
  (cycle events))

(defn average [kw num-events]
  (let [sum (->> event-stream
                 (map (comp kw :payload))
                 (remove (some-fn nil? keyword?))
                 (take num-events)
                 (reduce + 0))]
    (/ sum num-events)))

(comment
  (->> event-stream
       (map (comp :temperature :payload)) ; (62 nil :error 60 62 nil :error 60 62 nil :error 60 62 nil :error 60 62 nil :error 60 62 nil :error 60 …)
       (remove (some-fn nil? keyword?))   ; (62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 …)
       (take 60)                          ; (62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60 62 60)
       (reduce + 0)))                     ; 3660

(average :temperature     60) ; 61
(average :solar-radiation 60) ; 335.200000004

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Extending Filter to Support Multiple Collections
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; basically `map` with some edge checks
(defn walk-all
  "Returns a lazy-seq of all first elements in coll,
  then all second elements and so on."
  [colls]
  (lazy-seq
   (let [ss (map seq colls)]    ; make sure all colls are not empty
     (when (every? identity ss) ; did we reach end of any collection?
       (cons (map first ss) (walk-all (map rest ss)))))))

(comment
  (map seq  '([1 2 3] [4 5 6]))  ; ((1 2 3) (4 5 6))
  (walk-all '([1 2 3] [4 5 6]))) ; ((1 4) (2 5) (3 6))

(defn filter+
  ([pred coll]
   (filter pred coll))
  ([pred c1 c2 & colls]
   (filter+ #(apply pred %) (walk-all (conj colls c2 c1)))))

;; `re-seq` is a function of two params -> two collections as input
(filter+ re-seq
         (map re-pattern (map str (range))) ; (#"0"     #"1"   #"2"      #"3" #"4" #"5" #"6" #"7" …)
         ["234983" "5671" "84987"])         ; ["234983" "5671" "84987"]
; ((#"1" "5671"))                            

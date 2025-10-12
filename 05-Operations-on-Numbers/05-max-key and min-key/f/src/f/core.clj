(ns f.core)

(min-key last
         [:a 1000]
         [:b 500])
; [:b 500]

(max-key :age
         {:name "anna" :age 31}
         {:name "jack" :age 21})
; {:name "anna", :age 31}

;; ;;;;;;;;;;;;;;
;; Speed of Sound
;; ;;;;;;;;;;;;;;

(def air-temp [[:cellar  4]
               [:loft    25]
               [:kitchen 16]
               [:shed    -4]
               [:porch    0]])

(defn speed-of-sound [temp]
  (* 331.3 (Math/sqrt temp)))

(comment
  (Math/sqrt -4)) ; ##NaN

(apply max-key #(speed-of-sound (last %)) air-temp) ; [:loft 25]

;; ;;;;;
;; Races
;; ;;;;;

(defn update-stats [event stats]
  (let [events (conj (:events stats) event)
        newmin (apply min-key :time events)
        newmax (apply max-key :time events)]
    (assoc stats :events events :min newmin :max newmax)))

(defn new-competition []
  (let [stats (atom {:min {} :max {} :events []})]
    (fn
      ([] (str "The winner is: " (:min @stats)))
      ([t] (swap! stats (partial update-stats t))))))

(def race1 (new-competition))

(:min (race1 {:athlete "Souza J." :time 38.34}))  ; {:athlete "Souza J.",  :time 38.34}
(:min (race1 {:athlete "Kinley F." :time 37.21})) ; {:athlete "Kinley F.", :time 37.21}
(:max (race1 {:athlete "Won T." :time 36.44}))    ; {:athlete "Souza J.",  :time 38.34}

(race1) ; "The winner is: {:athlete \"Won T.\", :time 36.44}"

;; ;;;;;;;;;;;;;;;;;;;
;; Post Office Problem
;; ;;;;;;;;;;;;;;;;;;;

(defn sq  [x] (* x x))
(defn rad [x] (Math/toRadians x))
(defn cos [x] (Math/cos (rad x)))
(defn sq-diff [x y] (sq (Math/sin (/ (rad (- x y)) 2))))

(defn haversine-distance [[lon1 lat1] [lon2 lat2]]
  (let [earth-radius-km 6372.8
        dlat            (sq-diff lat2 lat1)
        dlon            (sq-diff lon2 lon1)
        a               (+ dlat (* dlon (cos lat1) (cos lat1)))]
    (* earth-radius-km 2 (Math/asin (Math/sqrt a)))))

(defn closest [geos geo]
  (->> geos
       (map (juxt (partial haversine-distance geo) identity))
       (apply min-key first)))

(def post-offices
  [[51.75958 -0.22920]
   [51.72064 -0.33353]
   [51.77781 -0.37057]
   [51.77133 -0.29398]
   [51.74836 -0.32237] ; closest
   [51.81622 -0.35177]
   [51.83104 -0.19737]
   [51.79669 -0.18569]
   [51.80334 -0.20863]
   [51.74472 -0.19791]])

(def residence [51.75049331 -0.34248299])

(closest post-offices residence)
; [2.2496423923820656 [51.74836 -0.32237]]

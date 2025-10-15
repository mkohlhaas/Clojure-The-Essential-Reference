(ns f.core)

(map - (range 10))
; (0 -1 -2 -3 -4 -5 -6 -7 -8 -9)

(into {}                               ; {0 :a, 1 :b, 2 :c}
      (map-indexed vector [:a :b :c])) ; ([0 :a] [1 :b] [2 :c])

;; `map` stops at the shortest collection
(map str (range 10) ["a" "b" "c"] "hijklm")
; ("0ah" "1bi" "2cj")

;; ;;;;;;;;;;;;;;;
;; Product Example
;; ;;;;;;;;;;;;;;;

(def products
  [{:id 1 :min-loan 6000 :rate 2.6}
   {:id 2 :min-loan 3500 :rate 3.3}
   {:id 3 :min-loan 500  :rate 7.0}
   {:id 4 :min-loan 5000 :rate 4.8}
   {:id 5 :min-loan 1000 :rate 4.3}])

;; period in years
(defn compound-interest [product loan-amount period]
  (let [rate (inc (/ (:rate product) 100. 12))]
    (* loan-amount (Math/pow rate (* 12 period)))))

(defn add-costs [loan-amount period]
  (fn [product]
    (let [total-cost  (compound-interest product loan-amount period)
          credit-cost (- total-cost loan-amount)]
      (-> product
          (assoc :total-cost  total-cost)
          (assoc :credit-cost credit-cost)))))

(defn min-amount [loan-amount]
  (fn [product]
    (> loan-amount (:min-loan product))))

(defn round-decimals [product]
  (letfn [(round-2 [x] (/ (Math/ceil (* 100 x)) 100))]
    (-> product
        (update-in [:total-cost]  round-2)
        (update-in [:credit-cost] round-2))))

(defn cost-of-credit [loan-amount period]
  (->> products
       (filter (min-amount loan-amount))
       (map (add-costs loan-amount period))
       (map round-decimals)
       (sort-by :credit-cost)))

(defn cheapest-credit [loan-amount period]
  (->> (cost-of-credit loan-amount period)
       (sort-by :total-cost)
       first))

(comment
  ;; loan of 2000, repay in 5 years
  (cost-of-credit 2000 5))
  ; ({:id 5 :min-loan 1000 :rate 4.3 :total-cost 2478.78 :credit-cost 478.78}
  ;  {:id 3 :min-loan 500  :rate 7.0 :total-cost 2835.26 :credit-cost 835.26})

(cheapest-credit 2000 5)
; {:id 5,
;  :min-loan 1000,
;  :rate 4.3,
;  :total-cost 2478.78,
;  :credit-cost 478.78}

;; ;;;;;;;;;;;;;;;
;; Lottery Example
;; ;;;;;;;;;;;;;;;

(def tickets ["QA123A3" "ZR2345Z" "GT4535A" "PP12839" "AZ9403E" "FG52490"])

(comment
  (take 10 (range 5))) ; (0 1 2 3 4)

(defn draw [n tickets]
  (take n (random-sample 0.5 tickets)))

(defn display [winners]
  (map-indexed
   (fn [idx ticket]
     (format "winner %s: %s" (inc idx) ticket))
   winners))

(display (draw 3 tickets))
; ("winner 1: ZR2345Z" "winner 2: GT4535A" "winner 3: AZ9403E")

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (let [res (map inc (range 1e7))] (first res) (last res))   ; 10000000 (GC)
  (let [res (map inc (range 1e7))] (last res)  (first res))) ; 1        (no GC)

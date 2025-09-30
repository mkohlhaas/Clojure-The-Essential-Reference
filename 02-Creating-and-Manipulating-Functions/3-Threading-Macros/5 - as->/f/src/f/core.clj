(ns f.core)

;; as-> enables a precise placement of the evaluation for the next form

(as-> {:a 1 :b 2 :c 3} x
  (assoc x :d 4)   ; {:a 1, :b 2, :c 3, :d 4}
  (vals x)         ; (1 2 3 4)
  (filter even? x) ; (2 4)
  (apply + x))     ; 6

(macroexpand-1
 '(as-> {:a 1 :b 2 :c 3} x
    (assoc x :d 4)
    (vals x)
    (filter even? x)
    (apply + x)))

; (clojure.core/let
;  [x {:a 1, :b 2, :c 3} 
;   x (assoc x :d 4) 
;   x (vals x) 
;   x (filter even? x)]
;  (apply + x))

(let [x {:a 1, :b 2, :c 3}
      x (assoc x :d 4)
      x (vals x)
      x (filter even? x)
      x (apply + x)]
  x)

(defn fetch-data [_url]
  [{:id "aa1" :name "reg-a" :count 2}
   {:id "aa2" :name "reg-b" :count 6}
   {:id "aa7" :name "reg-d" :count 1}
   {:id "aa7" :name nil     :count 1}])

(defn url-from [path]
  (str "http://localhost" "/" path))

;; The choice of the placeholder symbol <$> is arbitrary, but this one is more visible through the forms.
(defn process [path]
  (as-> path <$>
    (url-from <$>)
    (fetch-data <$>)
    (remove #(nil? (:name %)) <$>)
    (reduce + (map :count <$>))))

(process "home/index.html")
; 9

;; with destructuring
;; The same destructuring applies during each evaluation despite appearing only once at the top!!!
(let [point {:x "15.1" :y "84.2"}]
  (as-> point {:keys [x y] :as <$>}
    (update <$> :x #(Double/valueOf %)) ; {:x 15.1, :y "84.2"}
    (update <$> :y #(Double/valueOf %)) ; {:x 15.1, :y 84.2}
    (assoc  <$> :sum (+ x y))           ; {:x 15.1, :y 84.2, :sum 99.3}
    (assoc  <$> :keys (keys <$>))))     ; {:x 15.1, :y 84.2, :sum 99.3, :keys (:x :y :sum)}

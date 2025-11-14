(ns f.core)

(update {:a 1 :b 2} :b inc)  ; {:a 1, :b 3}

(update-in {:a 1 :b {:c 2}} [:b :c] inc)  ; {:a 1, :b {:c 3}}

(def words
  {"morning" 2 "bye" 1 "hi" 5 "gday" 2})

(defn insert-word [w words]
  (update words w (fnil inc 0)))

(insert-word "hello" words) ; {"morning" 2, "bye" 1, "hi" 5, "gday" 2, "hello" 1}

(update [:a :b :c] 3 (fnil keyword "d"))  ; [:a :b :c :d]
;; [:a :b :c :d]

(comment
  (update [:a :b :c] 4 (fnil keyword "d")))
  ; (err) Execution error (IndexOutOfBoundsException)

(def products
  {"A011" {:in-stock 10
           :name "Samsung G5"}
   "B032" {:in-stock 4
           :name "Apple iPhone"}
   "AE33" {:in-stock 13
           :name "Motorola N1"}})

(defn sale [products id]
  (update-in
   products
   [id :in-stock]
   (fnil dec 2)))

(get-in
 (sale products "B032")
 ["B032" :in-stock])
; 3

#_{:clj-kondo/ignore [:redefined-var]}
(def products
  (atom {"A011" {:in-stock 10
                 :name "Samsung G5"}
         "B032" {:in-stock 4
                 :name "Apple iPhone"}
         "AE33" {:in-stock 13
                 :name "Motorola N1"}}))

(defn total-products [products]
  (reduce + (map :in-stock (vals products))))

(total-products @products) ; 27

(defn sale! [products id]
  (swap! products
         update-in
         [id :in-stock]
         (fnil dec 2))
  products)

(defn sale-simulation! [ids]
  (dorun (pmap (partial sale! products) ids)))

(sale-simulation! ["B032" "AE33" "A011" "A011" "AE33" "B032"]) ; nil

(total-products @products) ; 21

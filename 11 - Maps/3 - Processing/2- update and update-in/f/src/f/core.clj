(ns f.core)

;; `update` and `update-in` accept a function of the old value into the new one

(update {:a 1 :b 2} :b inc)  ; {:a 1, :b 3}

(update-in {:a 1 :b {:c 2}} [:b :c] inc)  ; {:a 1, :b {:c 3}}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn insert-word [w words]
  (update words w (fnil inc 0)))

(let [words {"morning" 2, "bye" 1, "hi" 5, "gday" 2}]
  (insert-word "hello" words))                         ; {"morning" 2, "bye" 1, "hi" 5, "gday" 2, "hello" 1}

;; update on vectors allows the addition of a new element at the tail
(update [:a :b :c] 3 (fnil keyword "d")) ; [:a :b :c :d]
(update [:a :b :c] 2 (fnil keyword "d")) ; [:a :b :c]

(comment
  (keyword "d") ; :d

  ;; not the tail
  (update [:a :b :c] 4 (fnil keyword "d"))) ; nil
  ; (err) Execution error (IndexOutOfBoundsException)

;; decrease number of item when product is sold

(def products-db
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

;; get number of items of sold product "B032"
(get-in
 (sale products-db "B032") ; {"A011" {:in-stock 10, :name "Samsung G5"}, "B032" {:in-stock 3, :name "Apple iPhone"}, "AE33" {:in-stock 13, :name "Motorola N1"}}
 ["B032" :in-stock])
; 3

;; Compare and Swap (CAS) transactions
;; `update` (but also `assoc` and their *-in variants) are often seen in conjunction with `swap!`

(def products-atom-db
  (atom
   {"A011" {:in-stock 10
            :name "Samsung G5"}
    "B032" {:in-stock 4
            :name "Apple iPhone"}
    "AE33" {:in-stock 13
            :name "Motorola N1"}}))

(defn num-total-products [products]
  (reduce + (map :in-stock (vals products))))

(comment
  (vals @products-atom-db))
  ; ({:in-stock 10, :name "Samsung G5"}
  ;  {:in-stock  4, :name "Apple iPhone"}
  ;  {:in-stock 13, :name "Motorola N1"})

(num-total-products @products-atom-db) ; 27

(defn sale! [products id]
  (swap! products
         update-in
         [id :in-stock]
         (fnil dec 2))
  products)

(defn sale-simulation! [products ids]
  (dorun (pmap (partial sale! products) ids)))

;; selling 6 items
(sale-simulation! products-atom-db ["B032" "AE33" "A011" "A011" "AE33" "B032"])

@products-atom-db
; {"A011" {:in-stock  8, :name "Samsung G5"},
;  "B032" {:in-stock  2, :name "Apple iPhone"},
;  "AE33" {:in-stock 11, :name "Motorola N1"}}

(num-total-products @products-atom-db) ; 21

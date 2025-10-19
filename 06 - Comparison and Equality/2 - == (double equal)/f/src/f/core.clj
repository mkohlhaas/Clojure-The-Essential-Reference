(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

;; `==` is best for numbers because it respects the general notion that number equivalence 
;; is independent from types or binary representation.

(=  1M 1.)  ; false
(== 1M 1.)  ; true

;; ;;;;;;;;;;;;
;; Stock Orders
;; ;;;;;;;;;;;;

;; big decimals
(def tokyo
  [{:market :TYO :symbol "AAPL" :type :buy  :bid 22.1M}
   {:market :TYO :symbol "CSCO" :type :buy  :bid 12.4M}
   {:market :TYO :symbol "EBAY" :type :sell :bid 22.1M}])

;; integers
(def london
  [{:market :LDN :symbol "AAPL" :type :sell :bid 23}
   {:market :LDN :symbol "AAPL" :type :sell :bid 22}
   {:market :LDN :symbol "INTC" :type :sell :bid 14}
   {:market :LDN :symbol "EBAY" :type :buy  :bid 76}])

;; doubles
(def nyc
  [{:market :NYC :symbol "YHOO" :type :sell :bid 28.1}
   {:market :NYC :symbol "AAPL" :type :buy  :bid 22.0}
   {:market :NYC :symbol "INTC" :type :buy  :bid 31.9}
   {:market :NYC :symbol "PYPL" :type :sell :bid 44.1}])

(defn group-orders [& markets]
  (group-by :symbol (apply concat markets)))

(comment
  (group-orders tokyo london nyc))
; {"AAPL"
;  [{:market :TYO, :symbol "AAPL", :type :buy,  :bid 22.1M}
;   {:market :LDN, :symbol "AAPL", :type :sell, :bid 23}
;   {:market :LDN, :symbol "AAPL", :type :sell, :bid 22}
;   {:market :NYC, :symbol "AAPL", :type :buy,  :bid 22.0}],
;  "CSCO" 
;  [{:market :TYO, :symbol "CSCO", :type :buy,  :bid 12.4M}],))
;  "EBAY"
;  [{:market :TYO, :symbol "EBAY", :type :sell, :bid 22.1M}
;   {:market :LDN, :symbol "EBAY", :type :buy,  :bid 76}],
;  "INTC"
;  [{:market :LDN, :symbol "INTC", :type :sell, :bid 14}
;   {:market :NYC, :symbol "INTC", :type :buy,  :bid 31.9}],
;  "YHOO"
;  [{:market :NYC, :symbol "YHOO", :type :sell, :bid 28.1}],
;  "PYPL"
;  [{:market :NYC, :symbol "PYPL", :type :sell, :bid 44.1}]

(defn- compatible? [{t1 :type b1 :bid}
                    {t2 :type b2 :bid}]
  (and (not= t1 t2) (== b1 b2)))

(defn- matching [orders]
  (for [order1 orders
        order2 orders
        :when (compatible? order1 order2)]
    #{order1 order2}))

(defn exchange [listing]
  (->> listing
       (map last)
       (mapcat matching)
       distinct))

(exchange (group-orders tokyo london nyc))
;; (#{{:bid 22   :market :LDN :symbol "AAPL" :type :sell}
;;    {:bid 22.0 :market :NYC :symbol "AAPL" :type :buy}})

0.1                ; 0.1
(BigDecimal. 0.1)  ; 0.1000000000000000055511151231257827021181583404541015625M

(== 0.3 (+ 0.1 0.1 0.1)) ; false

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  (quick-bench (= 1 1 1 1 1))
  ;; Execution time mean : 86.508844 ns

  (quick-bench (== 1 1 1 1 1)))
  ;; Execution time mean : 63.125153 ns

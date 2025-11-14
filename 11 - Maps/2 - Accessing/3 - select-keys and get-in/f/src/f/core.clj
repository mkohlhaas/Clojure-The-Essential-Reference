(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(select-keys {:a 1 :b 2 :c 3} [:a :c])   ; {:a 1, :c 3}

(get-in {:a 1 :b {:c 3}} [:b :c])        ; 3

(select-keys [:a :b :c :d :e] [1 3])     ; {1 :b, 3 :d}

(get-in [:a :b :c [:d :e]] [3 1])        ; :e

(get-in '(0 1 2 3) [0])                  ; nil

(select-keys {:a 1 :b 2} [])             ; {}

(get-in {:a 1 :b 2} [])                  ; {:a 1, :b 2}

(def large-input-map
  {:a 1 :b 2 :c 3 :d 4 :e 5})

(select-keys large-input-map [:a :c :e]) ; {:a 1, :c 3, :e 5}

(def m ^:original {:a 1 :b 2})           ; {:a 1, :b 2}

(meta m)                                 ; {:original true}
;; {:original true}

(meta (select-keys m [:a]))              ; {:original true}

(let [word "hello"]
  (select-keys (vec word) (filter even? (range (count word)))))
; {0 \h, 2 \l, 4 \o}

(def products
  [{:product
    {:legal-fee-added {:rate "2%" :period "monthly"}
     :company-name "Together"
     :fee-attributes [["Jan" 8] 99 50 13 38 62]
     :initial-rate 9.15
     :initial-term-label {:bank "provided" :form "Coverage"}
     :created-at 1504556932727}}
   {:product
    {:legal-fee-added {:rate "4.2%" :period "yearly"}
     :company-name "SGI"
     :fee-attributes [["Mar" 8] 99 50 13 38 62]
     :initial-rate 2.15
     :initial-term-label {:bank "provided" :form "Coverage"}
     :created-at 1504556432722}}
   {:product
    {:legal-fee-added {:rate "2.6%" :period "monthly"}
     :company-name "Together"
     :fee-attributes [["Jan" 8] 99 50 13 38 62]
     :initial-rate 5.5
     :initial-term-label {:bank "Chase" :form "Assisted"}
     :created-at 1504556332211}}])

(defn rate-at [products idx]
  (get-in products [idx :product :legal-fee-added :rate]))

(rate-at products 0) ; "2%"
(rate-at products 1) ; "4.2%"

(defn select-keys2 [m keyseq]
  (with-meta
    (transduce
     (keep #(find m %))
     (completing conj! persistent!)
     (transient {})
     keyseq)
    (meta m)))

(comment
  (let [m (apply hash-map (range 40))]                  ; (out) Execution time mean : 2.495313 µs
    (quick-bench (select-keys m [0 2 4 6 8 10 12])))

  (let [m (apply hash-map (range 40))]                  ; (out) Execution time mean : 2.335699 µs
    (quick-bench (select-keys2 m [0 2 4 6 8 10 12]))))


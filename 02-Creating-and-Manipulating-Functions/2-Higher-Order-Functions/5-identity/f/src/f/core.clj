(ns f.core)

(identity 1)
; 1

(comment
  (map identity {:a 1 :b 2 :c 3})                 ; ([:a 1] [:b 2] [:c 3])
  (concat [1 2 3] [4 5 6])                        ; (1 2 3 4 5 6)
  (apply concat (map identity {:a 1 :b 2 :c 3}))) ; (:a 1 :b 2 :c 3)

;; idiomatic use of `mapcat` with `identity` to transform a map into a sequence
(mapcat identity {:a 1 :b 2 :c 3})                ; (:a 1 :b 2 :c 3)

;; not idiomatic
(defn custom-filter [x]
  (if (or (nil? x) (false? x))
    false
    true))

;; in Clojure only `false` and `nil` are logical false values
(filter custom-filter   [0 1 2 false 3 4 nil 5]) ; (0 1 2 3 4 5)
(filter identity        [0 1 2 false 3 4 nil 5]) ; (0 1 2 3 4 5)
(filter #(not (nil? %)) [0 1 2 false 3 4 nil 5]) ; (0 1 2 false 3 4 5)
(remove nil?            [0 1 2 false 3 4 nil 5]) ; (0 1 2 false 3 4 5)

;; ;;;;;;;;;;;;;;;
;; Cashier Example
;; ;;;;;;;;;;;;;;;

(def cashiers (ref [1 2 3 4 5]))

(defn next-available []
  (some identity @cashiers))

(next-available)
; 1

(defn make-available! [n]
  (alter cashiers assoc (dec n) n) n)

(defn make-unavailable! [n]
  (alter cashiers assoc (dec n) false) n)

(defn cashier-lane []
  (dosync ; start a transcation
   (if-let [lane (next-available)]
     (make-unavailable! lane)
     (throw (Exception. "All cashiers busy!")))))

(cashier-lane)
(cashier-lane)
(cashier-lane)
; 1
; 2
; 3

@cashiers
; [false false false 4 5]

(next-available)
; 4

(dosync (make-available! 2))
; 2

@cashiers
; [false 2 false 4 5]

(next-available)
; 2

;; ;;;;;;;;;;;;;;;;;;
;; Enthusiast Example
;; ;;;;;;;;;;;;;;;;;;

(def they-say
  [{:user "mark"  :sentence "hmmm this cake looks delicious"}
   {:user "john"  :sentence "Sunday was warm outside."}
   {:user "steve" :sentence "The movie was sooo cool!"}
   {:user "ella"  :sentence "Candies are bad for your health"}])

(defn- enthusiast? [s]
  (> (->> (:sentence s)
          (partition-by identity)
          (map count)
          (apply max))
     2))

(comment
  (->> (:sentence {:user "mark"  :sentence "hmmm this cake looks delicious"}) ; 3
       (partition-by identity)
       (map count)
       (apply max))

  (->> they-say                                                               ; ("mark" "steve")
       (filter enthusiast?)
       (map :user)))

(defn enthusiatic-people [sentences]
  (->> sentences
       (filter enthusiast?)
       (map :user)))

(enthusiatic-people they-say)
; ("mark" "steve")

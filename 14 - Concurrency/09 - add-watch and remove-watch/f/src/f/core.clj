(ns f.core)

;; `add-watch` adds a watch function to an agent/atom/var/ref reference

(def account-1 (ref 1000 :validator pos?))
(def account-2 (ref  500 :validator pos?))

(defn- to-monthly-statement [key _reference old-state new-state]
  (let [direction (if (< old-state new-state) "[OUT]" "[IN] ")]
    (spit (str "/tmp/statement." key)
          (format "%s: %s$\n" direction (Math/abs (- old-state new-state)))
          :append true)))

(add-watch account-1 "acc1" to-monthly-statement)
(add-watch account-2 "acc2" to-monthly-statement)

(defn transfer [amount a1 a2]
  (dosync
   (alter a1 - amount)
   (alter a2 + amount))
  {:account-1 @a1
   :account-2 @a2})

(transfer 300 account-1 account-2)
(transfer 500 account-2 account-1)

(println (slurp "/tmp/statement.acc1"))
; (out) [IN] : 300$
; (out) [OUT]: 500$

(println (slurp "/tmp/statement.acc2"))
; (out) [OUT]: 300$
; (out) [IN] : 500$

(comment
  ;; nothing is recorded
  (transfer 3000 account-1 account-2))
  ; (err) Execution error (IllegalStateException)
  ; (err) Invalid reference state

;; multiple watchers on an atom ;;

;; watchers execute synchronously and only after the new reference state has been set
;; multiple watchers are called in an unspecified order

(def multi-watch (atom 0)) ; #<Atom@60e0fcf4: 0>

(dotimes [i 10]
  (add-watch multi-watch i (fn [key reference old-state new-state] (println [key reference old-state new-state]))))

(swap! multi-watch inc) ; 1
; (out) [0 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [7 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [1 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [4 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [6 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [3 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [2 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [9 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [5 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]
; (out) [8 #object[clojure.lang.Atom 0x60e0fcf4 {:status :ready, :val 1}] 0 1]

;; remove all watchers
(dotimes [i 10]
  (remove-watch multi-watch i))

(swap! multi-watch inc) ; 2

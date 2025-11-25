(ns f.core
  (:require [clojure.repl]))

;; validation is for vars, atoms, agents and refs

(def a1 (atom 1))
(set-validator! a1 pos?)

(comment
  (swap! a1 dec))
  ; (err) Execution error (IllegalStateException)
  ; (err) Invalid reference state

;; we can throw a more descriptive error than the generic IllegalStateException ;;

(def a2 (atom 1))

(defn- should-be-positive [x]
  (if (pos? x)
    x
    (throw
     (ex-info (format "%s should be positive" x)
              {:valid? (pos? x)
               :value   x
               :error  "Should be a positive number"
               :action "State hasn't changed"}))))

(set-validator! a2 should-be-positive)

(comment
  (swap! a2 dec))
  ; (err) Execution error (ExceptionInfo)
  ; (err) 0 should be positive

(try
  (swap! a2 dec)
  (catch Exception e
    (ex-data e)))
;; {:valid? false,
;;  :value  0,
;;  :error  "Should be a positive number",
;;  :action "State hasn't changed"}

;; `get-validator` retrieves the validator function ;;

;; setting a validator for a var
(def a3 1)

;; `var` (reader literal #)
(get-validator  #'a3)      ; #object[clojure.core$pos_QMARK_ 0x5e2ac4e4 "clojure.core$pos_QMARK_@5e2ac4e4"]
(set-validator! #'a3 pos?) ; nil
(get-validator  #'a3)      ; #object[clojure.core$pos_QMARK_ 0x5e2ac4e4 "clojure.core$pos_QMARK_@5e2ac4e4"]

(-> (get-validator #'a3)   ; #object[clojure.core$pos_QMARK_ 0x5e2ac4e4 "clojure.core$pos_QMARK_@5e2ac4e4"]
    class                  ; clojure.core$pos_QMARK_
    .getSimpleName         ; "core$pos_QMARK_"
    clojure.repl/demunge   ; "core/pos?"
    symbol)                ; core/pos?
; core/pos?

(comment
  (def a3 0))
  ; (err) Execution error (IllegalStateException)
  ; (err) Invalid reference state

;; removing the validator
(set-validator! #'a3 nil)

#_{:clj-kondo/ignore [:redefined-var]}
(def a3 0) ; 0

;; for atoms, agents and refs we can install validators at creation time ;;

(def account-1 (ref 1000 :validator pos?))
(def account-2 (ref  500 :validator pos?))

;; transfer amount from a1 to a2
(defn transfer [amount a1 a2]
  (dosync
   (alter a1 - amount)
   (alter a2 + amount))
  {:account-1 @a1
   :account-2 @a2})

(comment
  (transfer 1300 account-1 account-2))
  ; (err) Execution error (IllegalStateException)
  ; (err) Invalid reference state

(transfer 500 account-1 account-2)
; {:account-1 500, :account-2 1000}

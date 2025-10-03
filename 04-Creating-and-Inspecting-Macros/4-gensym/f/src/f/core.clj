(ns f.core)

(gensym)
; G__14

(gensym "my-prefix")
; my-prefix17

;; ;;;;;;;;;;;;;;;;;
;; First order logic
;; ;;;;;;;;;;;;;;;;;

;; (OR (EXIST x (Q x)) (P y))
;; (OR (EXIST x (Q x)) (P y)) <=> (EXIST x (OR (Q x) (P y)))
;; (OR (EXIST x (Q x)) (P x)) <!=> (EXIST x (OR (P x) (Q x)))

(defn- quantifier? [[quant & _args]]
  (#{'EXIST 'ALL} quant))

(defn- emit-quantifier [op expr1 expr2]
  (let [new-local (gensym "local")
        [quant _local [pred _]] expr1]
    `(~quant ~new-local (~op ~expr2 (~pred ~new-local)))))

(defn pull-quantifier [[op expr1 expr2 :as form]]
  (cond (quantifier? expr1) (emit-quantifier op expr1 expr2)
        (quantifier? expr2) (emit-quantifier op expr2 expr1)
        :else form))

(pull-quantifier '(OR (EXIST x (Q x)) (P x)))
; (EXIST local2747 (OR (P x) (Q local2747)))

(pull-quantifier '(OR (P x) (EXIST x (Q x))))
; (EXIST local2750 (OR (P x) (Q local2750)))

(ns f.core
  (:require [clojure.walk :refer [prewalk-replace postwalk-replace]]))

(def data
  [[1 2]
   [3 :a [5 [6 7 :b [] 9] 10 [11 :c]]]
   [:d 14]])

(prewalk-replace {:a "A" :b "B" :c "C" :d "D"} data)
; [[1 2] [3 "A" [5 [6 7 "B" [] 9] 10 [11 "C"]]] ["D" 14]]

(def ^:const greek '[α β γ δ ε ζ η θ ι κ λ μ ν ξ ο π ρ σ τ υ φ χ ψ ω])

(prewalk-replace greek data)
; [[β γ] [δ :a [ζ [η θ :b [] κ] λ [μ :c]]] [:d ο]]

(def formula
  '(and (and a1 a2)
        (or (and a16 a3) (or a5 a8)
            (and (and a11 a9) (or a4 a8)))
        (and (or a5 a13) (and a4 a6)
             (and (or a9 (and a10 a11))
                  (and a12 a15)
                  (or (and a1 a4) a14
                      (and a15 a16))))))

(def ands
  '{(and true  true) true  (and true  false) false
    (and false true) false (and false false) false})

(def ors
  '{(or true  true) true (or true  false) true
    (or false true) true (or false false) false})

(def var-map
  '{a1  false  a2 true   a3 false  a4 false
    a5  true   a6 true   a7 false  a8 true
    a9  false a10 false a11 true  a12 false
    a13 true  a14 true  a15 true  a16 false})

(def transformed-formula
  (postwalk-replace (merge var-map ands ors) formula))

transformed-formula
; (and
;  false
;  (or false true false)
;  (and true false (and false false (or false true false))))

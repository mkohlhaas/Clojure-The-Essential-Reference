(ns f.core
  (:require [clojure.java.javadoc :refer  [javadoc]]))

((fn [x] (* (Math/random) x))
 (System/currentTimeMillis))
; 5.1737984511225604E11

;; reader syntax
;; %, %1, %2, … and variadic catch-all version %& 
(#(* (Math/random) %)
 (System/currentTimeMillis))
; 9.749367277264689E11

;; Grammar:
;; (fn <name>? arities)
;;
;; arities :=> ^<metamap>? [arity] body
;; OR
;; (^<metamap>? [arity1] body)
;; (^<metamap>? [arity2] body)
;; ..
;; (^<metamap>? [arityN] body)
;;
;; arity :=> [<arg1-typehint>? <arg1>
;;            ..
;;            <argN-typehint>? <argN>]
;;
;; body :=> <metamap>? <forms>

;; named recursion
((fn fibo [n]
   (if (< n 2)
     n
     (+ (fibo (- n 1))
        (fibo (- n 2)))))
 10)
; 55

;; tail-call optimization
((fn fibo
   ([n] (fibo 1 0 n))
   ([a b cnt]
    (if (zero? cnt)
      b
      (recur (+ a b) a (dec cnt)))))
 10)
; 55

(def sample-person
  {:person_id         1234567
   :person_name       "John Doe"
   :image             {:url     "http://focus.on/me.jpg"
                       :preview "http://corporate.com/me.png"}
   :person_short_name "John"})

(def cleanup
  {:person_id     [:id str]
   :person_name   [:name (memfn toLowerCase)]
   :image         [:avatar :url]})

(comment
  (javadoc "HALLO")
  (.toLowerCase        "HALLO")  ; "hallo"
  ((memfn toLowerCase) "HALLO")) ; "hallo" (memfn => Java method as a first-class object in Clojure)

(comment
  ;; NOTE: prefer destructuring instead of this mess
  (defn transform [orig mappings] ; <1>
    (apply merge
           (map #(let [old-kw (first %)            ; NOTE: destructuring not possible with reader macro `#()`
                       new-kw (first (second %))
                       f      (second (second %))]
                   {new-kw (f (old-kw orig))})
                mappings))))

(defn transform [orig mappings]
  (apply merge
         (map (fn [[old-kw [new-kw f]]] {new-kw (f (old-kw orig))})
              mappings)))

(transform sample-person cleanup)
; {:id "1234567", :name "john doe", :avatar "http://focus.on/me.jpg"}

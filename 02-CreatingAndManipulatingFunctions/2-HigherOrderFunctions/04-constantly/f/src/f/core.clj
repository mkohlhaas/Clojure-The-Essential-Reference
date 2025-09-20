(ns f.core
  (:require [clojure.test :refer [deftest testing is]]))

(identity   5) ; returns the value 5
(constantly 5) ; returns a function that takes any number of arguments and returns 5

;; ;;;;;;;;;;;;;
;; Notes Example
;; ;;;;;;;;;;;;;

(def notes
  [{:name "f" :volume 60 :duration 118 :expr ">"}
   {:name "f" :volume 63 :duration 120 :expr "<"}
   {:name "a" :volume 64 :duration 123 :expr "-"}])

(defn- expressiveness [avg exp]
  (case exp
    ">" (+ avg 5)
    "<" (- avg 5)
    avg))

(defn- process-note [note fns]
  (letfn [(update-note [note [k f]] (update note k f))]
    (reduce update-note note fns)))

(defn quantize-volume [notes]
  (let [avg-vol (quot (reduce + (map :volume notes)) (count notes))
        fns     {:volume (constantly avg-vol) ; old volume isn't used
                 :expr   (partial expressiveness avg-vol)}]
    (map #(process-note % fns) notes)))

(quantize-volume notes)
; ({:name "f", :volume 62, :duration 118, :expr 67}
;  {:name "f", :volume 62, :duration 120, :expr 57}
;  {:name "a", :volume 62, :duration 123, :expr 62})

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Stubbing Function Calls in Testing
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; the real service
(defn- third-party-service
  "Simulation of expensive call"
  [url p1 p2]
  (println url p1 p2)
  (Thread/sleep 1000)
  {:a "a" :b "b"})

(third-party-service "url" "p1" "p2") ; {:a "a", :b "b"}
; (out) url p1 p2

(defn fn-depending-on-service [s]
  (let [result (third-party-service "url" "p1" "p2")]
    (if (= "b" (:b result))
      (str s "1")
      (str s "2"))))

(deftest test-logic
  (with-redefs [third-party-service (constantly {:b "x"})] ; true (with stubbed response)
    (testing "should concatenate 2"
      (is (= "s2" (fn-depending-on-service "s"))))))

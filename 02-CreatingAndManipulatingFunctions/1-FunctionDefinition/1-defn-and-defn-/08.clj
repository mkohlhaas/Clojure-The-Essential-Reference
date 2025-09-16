(require '[clojure.test :refer [are]])

(defn save! [item]
  {:pre [(are [x] x
           (map? item)
           (integer? (:mult item))
           (#{:double :triple} (:width item)))]
   :post [(clojure.test/is (= 10 (:id %)))]}
  (assoc item :id (* (:mult item) 2)))

(save! {:mult "4" :width :single})
; (err) java.lang.AssertionError: Assert failed: (clojure.test/is (= 10 (:id %))) profilable /home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/02-CreatingAndManipulatingFunctions/1-FunctionDefinition/1-defn-and-defn-/08.clj:1:1

(save! {:mult 4 :width :double})
; (err) java.lang.AssertionError: Assert failed: (clojure.test/is (= 10 (:id %))) profilable /home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/02-CreatingAndManipulatingFunctions/1-FunctionDefinition/1-defn-and-defn-/08.clj:1:1

(save! {:mult 5 :width :double})
; {:mult 5, :width :double, :id 10}

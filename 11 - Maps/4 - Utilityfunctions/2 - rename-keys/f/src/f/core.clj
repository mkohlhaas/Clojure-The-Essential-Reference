(ns f.core
  (:require [clojure.set :refer [rename-keys]]))

(rename-keys {:a 1 :b 2 :c 3} {:a "AA" :b "B1" :c "X"})  ; {"AA" 1, "B1" 2, "X" 3}

(defrecord A [a b c])

(A. 1 2 3)                                    ; {:a 1, :b 2, :c 3}
(rename-keys (A. 1 2 3) {:a :y :b :z})        ; {:c 3, :y 1, :z 2}
(type (rename-keys (A. 1 2 3) {:a :y :b :z})) ; clojure.lang.PersistentArrayMap

(rename-keys (sorted-map :a 1 :b 2 :c 3) {:a :z})  ; {:b 2, :c 3, :z 1}

(comment
  (rename-keys (sorted-map :a 1 :b 2 :c 3) {:a 9})
  ; (err) Execution error (ClassCastException)
  ; (err) class clojure.lang.Keyword cannot be cast to class java.lang.Number

  (struct (create-struct :a :b :c) 1 2 3)                       ; {:a 1, :b 2, :c 3}
  (rename-keys (struct (create-struct :a :b :c) 1 2 3) {:a 9}))
  ; (err) Can't remove struct key

(ns f.core
  (:require
   [criterium.core :refer [quick-bench]]))

(set [1 2 3 4 1 4]) ; #{1 4 3 2}

(type #{1 2 3})         ; clojure.lang.PersistentHashSet | #{1 3 2}
(type (set [1 2 3]))    ; clojure.lang.PersistentHashSet | #{1 3 2}
(type (hash-set 1 2 3)) ; clojure.lang.PersistentHashSet | #{1 3 2}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def input-set (with-meta #{} {:original true}))

;; `set` removes metadata
(meta input-set)       ; {:original true}
(meta (set input-set)) ; nil

;; `set` transforms a sorted set into another sorted set
(type (into #{} (sorted-set 8 7 4 2 1 3))) ; clojure.lang.PersistentHashSet (transformation from ordered to unordered)
(type (set      (sorted-set 8 7 4 2 1 3))) ; clojure.lang.PersistentTreeSet

;; Honeypot Mechanism

(def honeypot-code "HP1234")

(defn honeypot? [req]
  (contains?
   (set (vals req))
   honeypot-code))

(def valid-request
  {:name    "John"
   :phone   "555-1411-112"
   :option1 ""
   :option2 ""})

(def fake-request
  {:name    "Sarah"
   :phone   "555-2413-111"
   :option1 "HP1234"
   :option2 ""})

(comment
  (set (vals valid-request)) ; #{"" "John" "555-1411-112"}
  (set (vals fake-request))) ; #{"" "HP1234" "Sarah" "555-2413-111"}

(honeypot? valid-request) ; false
(honeypot? fake-request)  ; true

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; hash-set
  (let [coll (range 10000)]               ; (out) Execution time mean : 1.509189 ms
    (quick-bench (apply hash-set coll)))

  ;; set
  (let [coll (range 10000)]               ; (out) Execution time mean : 1.452402 ms
    (quick-bench (set coll))))

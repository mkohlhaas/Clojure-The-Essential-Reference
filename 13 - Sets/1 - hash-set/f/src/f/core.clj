(ns f.core
  (:require
   [clojure.set    :refer [union]]
   [criterium.core :refer [bench]]))

(hash-set :yellow :red :green :green) ; #{:yellow :green :red}

;; #{:yellow :red :green :green}
; (err) Duplicate key: :green

(= #{3 2 1} (hash-set 1 2 3))  ; true

;; set as a predicate
(some #{:x :c} [:a :b :c :d :e]) ; :c
(some #{:x :y} [:a :b :c :d :e]) ; nil

(hash-set (rand) (rand) (rand))  ; #{0.3581365844343827 0.46677579307707684 0.5861878602263854}

;; #{(rand) (rand) (rand)}
; (err) Duplicate key: (rand)

(def set-with-meta
  (hash-set
   (with-meta 'a {:pos 1})
   (with-meta 'a {:pos 2})
   (with-meta 'a {:pos 3})))

set-with-meta ; #{a}

;; metadata from first item
(meta (first set-with-meta)) ; {:pos 1}

;; ;;;;;;;;;;;;;;;;;;;;;
;; The Powerset of a Set
;; ;;;;;;;;;;;;;;;;;;;;;

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(def s #{:a :b :c})

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(def powerset-of-s #{#{} #{:a} #{:b} #{:c} #{:a :b} #{:a :c} #{:b :c} #{:a :b :c}})

; [[]   [:a]    [:b]    [:a :b]]     U  (superset of #{:a :b} without :c)
; [[:c] [:a :c] [:b :c] [:a :b :c]]  =  (add :c to every subset from prior line)
; ------------------------------------------------------------------------------
; [[] [:a] [:b] [:c] [:a :b] [:a :c] [:b :c] [:a :b :c]])

;; recursively building the powerset
(defn powerset-1 [s]
  (when-first [x s]
    (let [p (or (powerset-1 (disj s x)) (hash-set #{}))]
      (union p (set (map conj p (repeat x)))))))

(powerset-1 #{1 2 3})    ; #{#{} #{3} #{2} #{1} #{1 3 2} #{1 3} #{1 2} #{3 2}}
(powerset-1 #{:a :b :c}) ; #{#{:c} #{:c :b :a} #{:b} #{} #{:b :a} #{:c :a} #{:a} #{:c :b}}

;; tail-recursive version by building up the powerset
(defn powerset-2 [s]
  (reduce
   (fn [s x]
     (union s (set (map #(conj % x) s))))
   (hash-set #{})
   s))

(powerset-2 #{1 2 3})    ; #{#{} #{3} #{2} #{1} #{1 3 2} #{1 3} #{1 2} #{3 2}}
(powerset-2 #{:a :b :c}) ; #{#{:c} #{:c :b :a} #{:b} #{} #{:b :a} #{:c :a} #{:a} #{:c :b}}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; hash-set
  (let [items (into [] (range 100000))] ; (out) Execution time mean : 19.841621 ms
    (bench (apply hash-set items)))

  ;; reader literal #{}
  (let [items (into [] (range 100000))] ; (out) Execution time mean : 18.441647 ms
    (bench (into #{} items)))

  ;; set
  (let [items (into [] (range 100000))] ; (out) Execution time mean : 18.535374 ms
    (bench (set items))))

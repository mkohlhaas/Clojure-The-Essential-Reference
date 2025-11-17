(ns f.core
  (:import
   [java.util HashMap]))

(defrecord R [a b c])

;; maps use keys
(find (R. 1 2 3) :a)                         ; [:a 1]
(find (hash-map   :a 1 :b 2) :a)             ; [:a 1]
(find (array-map  :a 1 :b 2) :a)             ; [:a 1]
(find (sorted-map :a 1 :b 2) :a)             ; [:a 1]
(find (struct (create-struct :a :b) 1 2) :a) ; [:a 1]
(find (HashMap. {:a 1 :b 2}) :a)             ; [:a 1]

;; `find` returns a MapEntry which consists of a key and its value
(type (find (R. 1 2 3) :a)) ; clojure.lang.MapEntry
(key  (find (R. 1 2 3) :a)) ; :a
(val  (find (R. 1 2 3) :a)) ; 1

;; vectors use indices
(find [:a :b :c] 1)                 ; [1 :b]
(find (subvec [:x :a :b :c] 1 3) 1) ; [1 :b]
(find (vector-of :int 5 42 9) 1)    ; [1 42]

(key (first {:a 1 :b 2}))    ; :a
(key (find  {:a 1 :b 2} :a)) ; :a

((juxt key val) (last (System/getenv)))  ; ["JOURNAL_STREAM" "9:10832"]

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def records
  [{(with-meta 'id {:tags [:expired]}) "1311" 'b "Mary" 'c "Mrs"}
   {(with-meta 'id {:tags []})         "4902" 'b "Jane" 'c "Miss"}
   {(with-meta 'id {:tags []})         "1201" 'b "John" 'c "Mr"}])

(comment
  ;; `map` removes the keys and its metadata
  (map 'id records)) ; ("1311" "4902" "1201")

;; `keep` keeps the keys and its metadata
(def ids
  (keep #(find % 'id) records))
; ([id "1311"] 
;  [id "4902"] 
;  [id "1201"]))

(-> ids    ; ([id "1311"] [id "4902"] [id "1201"])
    first  ;  [id "1311"]
    key    ;  id
    meta   ;  {:tags [:expired]}
    :tags) ;  [:expired]
; [:expired]

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; `find` and GIGO (garbage in, garbage out)
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; vector uses indices
(find [:a :b :c :d] :c) ; nil

(def power-2-32
  (long (Math/pow 2 32)))
; 4294967296

;; goes around (index 4294967296 = index 0)
;; integers are truncated to access array indexes, so 4294967296  becomes 0.
(find [1 2 3] power-2-32)  ; [4294967296 1]

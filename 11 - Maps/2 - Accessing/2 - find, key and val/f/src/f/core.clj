(ns f.core
  (:import [java.util HashMap]))

(defrecord R [a b c])

(find (hash-map :a 1 :b 2) :a)               ; [:a 1]
(find (array-map :a 1 :b 2) :a)              ; [:a 1]
(find (sorted-map :a 1 :b 2) :a)             ; [:a 1]
(find (struct (create-struct :a :b) 1 2) :a) ; [:a 1]
(find (HashMap. {:a 1 :b 2}) :a)             ; [:a 1]
(find (R. 1 2 3) :a)                         ; [:a 1]

(find [:a :b :c] 1)                 ; [1 :b]
(find (subvec [:x :a :b :c] 1 3) 1) ; [1 :b]
(find (vector-of :int 1 2 3) 1)     ; [1 2]

(key (first {:a 1 :b 2}))   ; :a
(key (find {:a 1 :b 2} :a)) ; :a

((juxt key val) (last (System/getenv)))  ; ["JOURNAL_STREAM" "9:10832"]

(def records
  [{(with-meta 'id {:tags [:expired]}) "1311" 'b "Mary" 'c "Mrs"}
   {(with-meta 'id {:tags []}) "4902" 'b "Jane" 'c "Miss"}
   {(with-meta 'id {:tags []}) "1201" 'b "John" 'c "Mr"}])

(def ids
  (keep #(find % 'id) records))
; ([id "1311"] [id "4902"] [id "1201"])

(-> ids first key meta :tags) ; [:expired]

(find [:a :b :c :d] :c) ; nil

(def power-2-32
  (long (Math/pow 2 32)))
; 4294967296

(find [1 2 3] power-2-32)  ; [4294967296 1]

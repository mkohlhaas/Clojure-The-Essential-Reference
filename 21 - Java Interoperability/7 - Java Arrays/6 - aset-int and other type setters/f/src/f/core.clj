(ns f.core)

(set! *warn-on-reflection* true)

;; `aset-int` … spare reflective calls

(def a (int-array [1 2 3])) ; [1, 2, 3]
(type a) ; int/1

(comment
  (aset a 0 9)) ; 9
  ; (err) Reflection warning, call to static method aset on clojure.lang.RT can't be resolved (argument types: unknown, int, long).

(def a-lookup (get (ns-map *ns*) 'a))

(type a-lookup)         ; clojure.lang.Var
(type (deref a-lookup)) ; int/1

;; The compiler can only see that deref returns an object (because a var could really point at anything)
;; and even if specialized aset exists for different primitive types, that information is now lost.
(aset (deref a-lookup) 0 9)
; (err) Reflection warning, … call to static method aset on clojure.lang.RT can't be resolved (argument types: unknown, int, long).
; 9

(aset ^ints a 0 9) ; 9
(aset-int a 0 9)   ; 9

;; Multi-Dimensional Arrays ;;

(def matrix
  (into-array
   (map int-array [[1 2 3] [4 5 6]])))

(aset-int matrix 0 2 99) ; 99

(mapv vec matrix) ; [[1 2 99] [4 5 6]]

(def int-a (int-array 5))       ; [0, 0, 0, 0, 0]
(def double-a (double-array 5)) ; [0.0, 0.0, 0.0, 0.0, 0.0]

;; type conversion ✓ (no loss of precision)
(aset-int double-a 0 99) ; 99

(comment
  ;; type conversion would lead to a loss of precision -> not allowed
  (aset-double int-a 0 99.0))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) argument type mismatch

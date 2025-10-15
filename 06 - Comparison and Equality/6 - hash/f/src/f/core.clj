(ns f.core
  (:import java.util.ArrayList
           java.util.HashSet
           java.util.HashMap))

(hash "hello")         ; 1715862179
(hash nil)             ; 0
(type (hash "hello"))  ; java.lang.Integer

(comment
  (hash-unordered-coll nil)
  (hash-ordered-coll   nil))
  ; (err) Execution error (NullPointerException)
  ; (err) Cannot invoke "java.lang.Iterable.iterator()" because "xs" is null

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def long-keys [-3 -2 -1 0 1 2])
(def composite-keys [#{[8 5] [3 6]} #{[3 5] [8 6]}])

;; Java comes with its own hashing algorithm accessible through the hashCode() method on each object instance. 
;; But not very usable for Clojure -> many collisions!
(map (memfn hashCode) long-keys)      ; (2 1 0 0 1 2)
(map (memfn hashCode) composite-keys) ; (2274 2274)

;; Clojure's hashing algorithm
(map hash long-keys)      ; (-1797448787 -1438076027 1651860712 0 1392991556 -971005196)
(map hash composite-keys) ; (2055406432 -916052234)

;; ;;;;;;;;;;;;;;;;;;;;;;
;; Java Interop Scenarios
;; ;;;;;;;;;;;;;;;;;;;;;;

(def k1 (ArrayList. [1 2 3]))
(def k2 [1 2 3])

(= k1 k2)               ; true
(= (hash k1) (hash k2)) ; false (30817 6)

(comment
  ;; no hashing involved (just values)
  (def arraymap {k1 :v1 k2 :v2}))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Duplicate key: [1, 2, 3]

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
;; hashing involved
(def hashmap (hash-map k1 :v1 k2 :v2)) ; {[1 2 3] :v1, [1 2 3] :v2}

;; hash-unordered-coll -> order doesn't matter
(=
 (hash-unordered-coll  [1 2 3])  ; 439094965
 (hash-unordered-coll  [3 2 1])  ; 439094965
 (hash-unordered-coll #{1 2 3})) ; 439094965
; true

;; hash-ordered-coll -> order does matter
(=
 (hash-ordered-coll  [1 2 3])  ; 736442005
 (hash-ordered-coll  [3 2 1])  ; 791118583
 (hash-ordered-coll #{1 2 3})) ; 646594244
; false

(defn hash-update [m k f]
  (update m (hash-unordered-coll k) f))

#_{:clj-kondo/ignore [:redefined-var]}
(def k2 (HashSet. #{1 2 3}))

(= k1 k2)               ; false
(= (hash k1) (hash k2)) ; false

(comment
  (hash-unordered-coll k1)  ; 439094965
  (hash-unordered-coll k2)) ; 439094965

(def m (hash-map)) ; {}

(-> m
    (hash-update [1 2 3] (fnil inc 0)) ; {439094965 1}
    (hash-update k1 (fnil inc 0))      ; {439094965 2}
    (hash-update k2 inc))              ; {439094965 3}
; {439094965 3}

(comment
  ;; collection must implement java.lang.Iterable interface
  (hash-unordered-coll (HashMap. {:a 1 :b 2})))
  ; (err) Execution error (ClassCastException)
  ; (err) class java.util.HashMap cannot be cast to class java.lang.Iterable

;; Clojure-compatible hashing function that works on java.util.HashMap
(defn hash-java-map [^java.util.Map m]
  (let [iter (.. m entrySet iterator)]
    (loop [ret 0 cnt 0]
      (if (.hasNext iter)
        (let [^java.util.Map$Entry item (.next iter)
              kv [(.getKey item) (.getValue item)]]
          (recur
           (unchecked-add ret ^int (hash kv))
           (unchecked-inc cnt)))
        (.intValue ^Long (mix-collection-hash ret cnt))))))

(= (hash (HashMap. {1 2 3 4}))          ; 10
   (hash {1 2 3 4}))                    ; -812819982
; false

(= (hash-java-map (HashMap. {1 2 3 4})) ; -812819982
   (hash {1 2 3 4}))                    ; -812819982
; true

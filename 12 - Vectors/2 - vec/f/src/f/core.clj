(ns f.core
  (:require
   [criterium.core :refer [bench quick-benchmark]])
  (:import
   [java.util ArrayList LinkedList UUID]))

;; `vec` returns a persistent vector instance containing the elements of the input collection.

(vec  '(:a 1 nil {})) ; [:a 1 nil {}]
(vector :a 1 nil {})  ; [:a 1 nil {}]

(type (vec  '(:a 1 nil {}))) ; clojure.lang.PersistentVector
(type (vector :a 1 nil {}))  ; clojure.lang.PersistentVector

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def a1 (make-array Long 3)) ; Java array of reference type Long (not long)
(def v1 (vec a1))

a1             ; [nil, nil, nil]
v1             ; [nil nil nil]
(aset a1 1 99) ; 99
a1             ; [nil, 99, nil]
v1             ; [nil 99 nil]

(def a2 (long-array 3))      ; Java array of long primitives (not references)
(def v2 (vec a2))

a2             ; [0, 0, 0]
v2             ; [0 0 0]
(aset a2 1 99) ; 99
a2             ; [0, 99, 0]
v2             ; [0 0 0]

;; Caching Search Results

;; mocking a search
(defn search-merchandise [& _search-options]
  '({:description "Pencil Dress"           , :type  :dress, :color :blue, :price 60}
    {:description "Asymmetric Lace Dress"  , :type  :dress, :color :blue, :price 70}
    {:description "Short Sleeve Wrap Dress", :type  :dress, :color :blue, :price 45}))

;; thread safe
(def cache (atom {}))

(defn cache-user-search-results! [search-id search-results]
  (swap! cache assoc search-id (vec search-results)))

(defn retrieve-user-search-results [search-id page-num]
  (get (get @cache search-id) page-num))

;; Cheshire would be a professional lib for rendering JSON
(defn render-to-json [{:keys [description price]}]
  (format "[{'description':'%s', 'price':'%s'}]" description price))

(def search-id (str (UUID/randomUUID))) ; "5ed3724b-0002-4ebe-9cd2-a6162176f623"

(cache-user-search-results!
 search-id
 (search-merchandise {:type :dress :color :blue}))
; {"5ed3724b-0002-4ebe-9cd2-a6162176f623"
;  [{:description "Pencil Dress",            :type :dress, :color :blue, :price 60}
;   {:description "Asymmetric Lace Dress",   :type :dress, :color :blue, :price 70}
;   {:description "Short Sleeve Wrap Dress", :type :dress, :color :blue, :price 45}]}

(-> (retrieve-user-search-results search-id 0)
    render-to-json)
; "[{'description':'Pencil Dress', 'price':'60'}]"

(-> (retrieve-user-search-results search-id 1)
    render-to-json)
; "[{'description':'Asymmetric Lace Dress', 'price':'70'}]"

(-> (retrieve-user-search-results search-id 2)
    render-to-json)
; "[{'description':'Short Sleeve Wrap Dress', 'price':'45'}]"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmacro b [expr]
  `(first (:mean (quick-benchmark ~expr {}))))

(comment
  (let [c1 (range 1000)
        c2 (map inc c1)
        c3 (ArrayList. c1)
        c4 (LinkedList. c1)]
    (for [t [c1 c2 c3 c4]]
      [(type t) (b (vec t))])))

;; ([clojure.lang.LongRange 1.0874153277485413E-5]
;;  [clojure.lang.LazySeq   2.0303272494887527E-5]
;;  [java.util.ArrayList    1.0039155519384434E-5]
;;  [java.util.LinkedList   1.4243774103139014E-5])

(comment
  (let [l (range 1000)] (bench (vec l)))
;; Execution time mean : 16.765533 µs

  (let [l (range 1000)] (bench (into [] l))))
;; Execution time mean : 17.946582 µs

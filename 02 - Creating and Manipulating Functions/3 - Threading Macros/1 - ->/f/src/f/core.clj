(ns f.core)

;; thread first macro or thrush operator

(-> {:a 2} ; 3
    :a
    inc)

(macroexpand '(-> {:a 2} :a inc))
; (inc (:a {:a 2}))

(def req {:host         "http://mysite.com"
          :path         "/a/123"
          :x            "15.1"
          :y            "84.2"
          :trace        [:received]
          :x-forward-to "AFG45HD32BCC"})

(defn prepare1 [req]
  (update
   (dissoc
    (assoc req
           :url   (str (:host req) (:path req))
           :coord [(Double/valueOf (:x req)) (Double/valueOf (:y req))])
    :x-forward-to :x :y)
   :trace conj :prepared))

(prepare1 req)
; {:host  "http://mysite.com",
;  :path  "/a/123",
;  :trace [:received :prepared],
;  :url   "http://mysite.com/a/123",
;  :coord [15.1 84.2]}

(defn prepare2 [req]
  (-> req
      (assoc :url   (str (:host req) (:path req))
             :coord [(Double/valueOf (:x req)) (Double/valueOf (:y req))])
      (dissoc :x-forward-to :x :y)
      (update :trace conj :prepared)))

(prepare2 req)
; {:host  "http://mysite.com",
;  :path  "/a/123",
;  :trace [:received :prepared],
;  :url   "http://mysite.com/a/123",
;  :coord [15.1 84.2]}

(def items [:a :a :b :c :d :d :e])

(comment
  (map #({:count 1 :item %}) items))
  ; map is called with 0 args but expects 1 or 2 [invalid-arity])
  ; ArityException Wrong number of args (0) passed to: PersistentArrayMap

;; calling an array-map as a function without arguments (which fails)
(macroexpand '#({:count 1 :item %}))
; (fn* [%1] ({:count 1, :item %1}))

;; hash-map is an idiomatic choice.
(map #(hash-map  :count 1 :item %)  items) ; (map (fn* [p1__4228#] (hash-map :count 1 :item p1__4228#)) items)
;; confusing
(map #(identity {:count 1 :item %}) items) ; (map (fn* [p1__4231#] (identity {:count 1, :item p1__4231#})) items)
;; confusing
(map #(do       {:count 1 :item %}) items) ; (map (fn* [p1__4234#] (do {:count 1, :item p1__4234#})) items)
;; When applied to a single argument, -> behaves similarly the identity function.
;; short and to the point
(map #(->       {:count 1 :item %}) items) ; (map (fn* [p1__4237#] (-> {:count 1, :item p1__4237#})) items)
; ({:count 1, :item :a}
;  {:count 1, :item :a}
;  {:count 1, :item :b}
;  {:count 1, :item :c}
;  {:count 1, :item :d}
;  {:count 1, :item :d}
;  {:count 1, :item :e})

;; both idiomatic solutions are slightly different
(map type (map #(hash-map :count 1 :item %)  [1])) ; (clojure.lang.PersistentHashMap)
(map type (map #(->      {:count 1 :item %}) [1])) ; (clojure.lang.PersistentArrayMap)

(/ (Math/abs (- (* (inc 1) 5) 1)) 3) ; 3

(-> 1                                ; 3
    inc
    (* 5)
    (- 1)
    (Math/abs)
    (/ 3))

(comment
  (-> 1 (fn [x] (inc x))))
  ; IllegalArgumentException Parameter declaration 1 should be a vector

;; -> does not support nested function with arguments
;; (acts like a limited T-combinator)
(macroexpand-1 '(-> 1 (fn [x] (inc x))))
; (fn 1 [x] (inc x))

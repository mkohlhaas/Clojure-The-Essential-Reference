(ns f.core)

;; [Destructuring in Clojure](https://clojure.org/guides/destructuring)

(destructure '[[x y] [1 2]])
; [vec__4117
;  [1 2]
;  x
;  (clojure.core/nth vec__4117 0 nil)
;  y
;  (clojure.core/nth vec__4117 1 nil)]

; [vec__14 [1 2]
;  x (nth vec__14 0 nil)
;  y (nth vec__14 1 nil)]

(eval
 `(let ~(destructure '[[x y] [1 2]])
    (+ ~'x ~'y)))
; 3

(let [vec__4221 [1 2]
      x (nth vec__4221 0 nil)
      y (nth vec__4221 1 nil)]
  (+ x y))
; 3

;; ;;;;;;;;;;;;;;;;;;;;;;;;
;; Sequential Destructuring
;; ;;;;;;;;;;;;;;;;;;;;;;;;

(let [my-vec [1 2 3 4]
      [a b] my-vec
      [_ _ & r] my-vec
      [_ _ c d e :as v] my-vec]
  [a b c d e r v])
;[1 2 3 4 nil (3 4) [1 2 3 4]]

;; ;;;;;;;;;;;;;
;; Dedupe String
;; ;;;;;;;;;;;;;

(defn dedupe-string [s]
  (loop [[el & more] s
         [cur ret :as state] [nil ""]]
    (cond
      (not el)   (str ret cur)
      (= el cur) (recur more state)
      :else (recur more [el (str ret cur)]))))

(dedupe-string "")       ; ""
(dedupe-string "foobar") ; "fobar"
(dedupe-string "fubar")  ; "fubar"

;; ;;;;;;;;;;;;;;;;;;;;;;;;;
;; Associative Destructuring
;; ;;;;;;;;;;;;;;;;;;;;;;;;;

(let [my-map {:x 1 :y 2 :z nil}
      {_x :x _y :y :as m} my-map
      {:keys [x y]} my-map
      {:keys [z t] :or {z 3 t 4}} my-map]
  [x y z t m])
; [1 2 nil 4 {:x 1, :y 2, :z nil}]

;; namespaced keywords
;; `::` denotes a keyword qualified with the current namespace (e.g. at the REPL this would be `user`)
(let [{:keys [::x foo/bar]} {::x 1 :foo/bar 2}]
  [x bar])
; [1 2]

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Nested and Composed Destructuring
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-info [{:keys [name surname]
                     {:keys [street city]} :address
                     [primary-contact secondary-contact] :contacts}]
  (println name surname "lives at" street "in" city)
  (println "His primary contact is:" primary-contact)
  (when secondary-contact
    (println "His secondary contact is:" secondary-contact)))

(extract-info {:name "Foo" :surname "Bar"
               :address {:street "Road Fu 123" :city "Baz"}
               :contacts ["123-456-789", "987-654-321"]})
; (out) Foo Bar lives at Road Fu 123 in Baz
; (out) His primary contact is: 123-456-789
; (out) His secondary contact is: 987-654-321

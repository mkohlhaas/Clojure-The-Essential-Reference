(ns f.core)

;; `get` is specifically dedicated to maps (although it works on other types too)
;; `get` design is to avoid throwing exception, preferring `nil` when the collection type is not supported

(get {:a "a" :b "b"} :a) ; "a"
(get (list 1 2 3)    1)  ; nil

(get nil nil)  ; nil
(get nil 0)    ; nil
(get nil 0 42) ; 42

(let [colls    [[1 2 3] [1 2 3] {:a 1 :b 2} '(1 2 3)]
      ks       [1 -1 :z 0]
      defaults ["not found" "not found" "not found" "not supported"]]
  (map get colls ks defaults))
; (2 "not found" "not found" "not supported")

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(get (sorted-map :a 1 :b 2) :c "not-found") ; "not-found"

(comment
  ;; string cannot be compared to keywords
  (get (sorted-map :a 1 :b 2) "c" "not-found"))
  ; (err) Execution error (ClassCastException)

(get (transient {:a 1 :b 2}) :a) ; 1
(get (transient [1 2 3]) 0)      ; 1
(get (transient #{0 1 2}) 1)     ; 1

(+ 2 (* 2 Integer/MAX_VALUE))
; 4294967296

(.intValue 4294967296)
; 0

;; Numerical keys are allowed for vectors, strings and arrays
(get ["a" "b" "c"]       4294967296) ; "a"
(get "abcd"              4294967296) ; \a
(get (int-array [0 1 2]) 4294967296) ; 0

;; `get` works on every type (even scalars) at the cost of returning `nil` instead of throwing exception
(let [mixed-bag [{1 "a"} [0 2 4] nil "abba" 3 '(())]]
  (map #(get % 1) mixed-bag))
; ("a" 2 nil \b nil nil)

;; get also accepts objects implementing the java.util.Map interface
(defn select-matching [m k]
  (let [regex (re-pattern (str ".*" k ".*"))]
    (->> (keys m)                                   ; ("java.specification.version" "sun.jnu.encoding" "clojure.debug" …)
         (filter #(re-find regex (.toLowerCase %))) ; ("user.timezone" "user.country" "user.home" "user.language" "user.name" "user.dir")
         (reduce #(assoc %1 (keyword %2) (get m %2)) {}))))

(comment
  (keyword "user.timezone") ; :user.timezone
  (System/getProperties)    ; {"java.specification.version" "25", "sun.jnu.encoding" "UTF-8", "clojure.debug" "false", …}

  (select-matching (System/getProperties) "user"))
  ; {:user.timezone "Europe/Berlin",
  ;  :user.country "US",
  ;  :user.home "/home/schmidh",
  ;  :user.language "en",
  ;  :user.name "schmidh",
  ;  :user.dir
  ;  "/home/schmidh/Gitrepos/Clojure-The-Essential-Reference/08 - Collections/2 - Polymorphic/2 - get/f"}

(defn search [k]
  (merge (select-matching (System/getProperties) k)
         (select-matching (System/getenv) k)))

(search "user")
; {:user.timezone "Europe/Berlin",
;  :user.country "US",
;  :user.home "/home/schmidh",
;  :user.language "en",
;  :user.name "schmidh",
;  :user.dir
;  "/home/schmidh/Gitrepos/Clojure-The-Essential-Reference/08 - Collections/2 - Polymorphic/2 - get/f",
;  :USERNAME "schmidh",
;  :USER "schmidh"}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The many ways to access maps in Clojure
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `map` as an invokable function
({:a 1 :b 2} :b)                            ; 2
({:a 1 :b 2} :c "Not found")                ; "Not found"

;; keywords as invokable functions
(:b {:a 1 :b 2})                            ; 2
(:c {:a 1 :b 2} "Not found")                ; "Not found"

;; ;;;;;;;;;;;;
;; Java Interop
;; ;;;;;;;;;;;;

(.get          {:a 1 :b 2} :b)              ; 2
(.getOrDefault {:a 1 :b 2} :c "Not found")  ; "Not found"

;; ;;;;
;; Find
;; ;;;;

;; `find` returns the map entry - a tuple - for a key
(find {:a 1 :b 2} :b)  ; [:b 2]

(type (find {:a 1 :b 2} :b))  ; clojure.lang.MapEntry

;; nested `get`s
(let [m {:a "a" :b [:x :y :z]}]
  (get (get m :b) 0))
; :x

;; the same with `get-in`
(let [m {:a "a" :b [:x :y :z]}]
  (get-in m [:b 0]))
; :x

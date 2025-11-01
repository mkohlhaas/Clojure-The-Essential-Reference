(ns f.core
  (:require
   [clojure.java.io :as io]))

;; `conj` (abbreviation for conjoining) inserts one or more elements into an existent collection.

(conj [1 "a" :c]  \x)          ; [1 "a" :c \x]
(conj (range 3)   99)          ; (99 0 1 2)
(conj {:a 1 :b 2} {:c 3 :d 4}) ; {:a 1, :b 2, :c 3, :d 4}
(conj {:a 1}      [:b 2])      ; {:a 1, :b 2}
(conj ()          1 2 3)       ; (3 2 1)
(conj nil         1 2 3)       ; (3 2 1)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defrecord Person  [name age])
(defrecord Address [number street zip])
(defrecord Phone   [mobile work])

;; records implement map semantic
(conj
 (Person.  "Jake" "38")
 (Address. 18 "High Street" 60160)
 (Phone.   "801-506-213" "299-12-213-22"))
; {:name "Jake",
;  :age "38",
;  :number 18,
;  :street "High Street",
;  :zip 60160,
;  :mobile "801-506-213",
;  :work "299-12-213-22"}

(let [q (conj clojure.lang.PersistentQueue/EMPTY 1 2 3)]
  (peek q))
; 1

;; ;;;;;;;;;;;;;;;;;
;; Snippets to Files
;; ;;;;;;;;;;;;;;;;;

(defn- full-file-name [dir filename]
  (str dir "/" filename ".clj"))

(comment
  (full-file-name "/tmp/add" 0))  ; "/tmp/add/0.clj"

(defn write [examples root]
  (loop [[title forms :as more] examples
         files []]
    (if title
      (let [dir   (str root "/" title)
            paths (map-indexed #(vector (full-file-name dir %1) %2) forms)]
        (doseq [[path content] paths]
          (io/make-parents (io/file path))
          (spit path content))
        (recur (nnext more) (apply conj files paths)))
      (map first files))))

;; titles and snippets
(def examples
  ["add" ["(+ 1 1)" "(+ 1 2 2)" "(apply + (range 10))"]
   "sub" ["(map - [1 2 3])" "(- 1)"]
   "mul" ["(*)" "(fn sq [x] (* x x))"]
   "div" ["(/ 1 2)" "(/ 1 0.)"]])

(comment
  (nnext examples))
  ; ("sub"
  ;  ["(map - [1 2 3])" "(- 1)"]
  ;  "mul"
  ;  ["(*)" "(fn sq [x] (* x x))"]
  ;  "div"
  ;  ["(/ 1 2)" "(/ 1 0.)"])

(write examples "/tmp")
; ("/tmp/add/0.clj"
;  "/tmp/add/1.clj"
;  "/tmp/add/2.clj"
;  "/tmp/sub/0.clj"
;  "/tmp/sub/1.clj"
;  "/tmp/mul/0.clj"
;  "/tmp/mul/1.clj"
;  "/tmp/div/0.clj"
;  "/tmp/div/1.clj")

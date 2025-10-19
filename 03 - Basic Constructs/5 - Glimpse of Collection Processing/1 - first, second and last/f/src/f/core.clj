(ns f.core
  (:require
   [clojure.string]))

;; ;;;;;
;; Lists
;; ;;;;;

(first  nil) ; nil
(second nil) ; nil
(last   nil) ; nil

(first  '()) ; nil
(second '()) ; nil
(last   '()) ; nil

(first  (list)) ; nil
(second (list)) ; nil
(last   (list)) ; nil

(def numbers '(1 2 3 4))

(first  numbers) ; 1
(second numbers) ; 2
(last   numbers) ; 4

;; ;;;;
;; Maps
;; ;;;;

(first  (hash-map)) ; nil
(second (hash-map)) ; nil
(last   (hash-map)) ; nil

(def a-map (hash-map :a 1 :b 2 :c 3 :d 4 :e 5 :f 6 :g 7 :h 8 :i 9))

(first  a-map) ; [:e 5]
(second a-map) ; [:g 7]
(last   a-map) ; [:a 1]

;; ;;;;
;; Sets
;; ;;;;

(first  (set nil)) ; nil
(second (set nil)) ; nil
(last   (set nil)) ; nil

(def a-set #{1 2 3 4 5 6 7 8 9})

(first  a-set) ; 7
(second a-set) ; 1
(last   a-set) ; 8

;; ;;;;;
;; first
;; ;;;;;

;; Telephone Numbers 

(def phone-numbers ["221 610-5007"
                    "221 433-4185"
                    "661 471-3948"
                    "661 653-4480"
                    "661 773-8656"
                    "555 515-0158"])

(defn unique-area-codes [numbers]
  (->> numbers
       (map #(clojure.string/split % #" ")) ; (["221" "610-5007"] ["221" "433-4185"] ["661" "471-3948"] ["661" "653-4480"] ["661" "773-8656"] ["555" "515-0158"])
       (map first)                          ; ("221" "221" "661" "661" "661" "555")
       distinct))                           ; ("221" "661" "555")

(unique-area-codes phone-numbers)
; ("221" "661" "555")

;; Positives

(defn all-positives? [coll]
  (cond
    (empty? coll) true
    (pos? (first coll)) (recur (rest coll))
    :else false))

(all-positives? (list 1 2 3))  ; true
(all-positives? (list -1 0 1)) ; false

;; ;;;;;;
;; second
;; ;;;;;;

;; Temperatures

;; '(temperature, max temp, min temp)
(def temp '((60661 95.2 72.9) (38104 84.5 50.0) (80793 70.2 43.8)))

(defn max-recorded [temp]
  (->> temp
       (sort-by second >) ; ((60661 95.2 72.9) (38104 84.5 50.0) (80793 70.2 43.8))
       first              ; (60661 95.2 72.9)
       second))           ; 95.2

(max-recorded temp)
; 95.2

;; ;;;;
;; last
;; ;;;;

;; Messages

(def message "user:root echo[b]
              user:ubuntu mount /dev/so
              user:root chmod 755 /usr/bin/pwd")

(->> message
     (re-seq #"user\:\S+") ; ("user:root" "user:ubuntu" "user:root")
     last)                 ; "user:root"

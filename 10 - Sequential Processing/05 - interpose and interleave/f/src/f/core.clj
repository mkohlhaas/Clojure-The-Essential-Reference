(ns f.core
  (:require
   [clojure.string :as s]
   [criterium.core :refer [quick-bench]]))

(interpose :orange [:green :red :green :red])            ; (:green :orange :red :orange :green :orange :red)
(sequence (interpose :orange) [:green :red :green :red]) ; (:green :orange :red :orange :green :orange :red)

(interleave [:green :red :blue] [:yellow :magenta :cyan])  ; (:green :yellow :red :magenta :blue :cyan)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(apply str (interpose \, "XYZ"))                         ; "X,Y,Z"

(def grocery ["apple" "banana" "mango" "other fruits"])

(apply str (interpose ", " grocery))     ; "apple, banana, mango, other fruits"
(transduce (interpose ", ") str grocery) ; "apple, banana, mango, other fruits"

;; Rotate the Members of a Single Team

(defn team [& names]
  (apply interleave (map repeat names)))

(defn shifts [& teams]
  (apply interleave teams))

(def a-team (team :john :rob :jessica))
(def b-team (team :arthur :giles))
(def c-team (team :paul :eva :donald :jake))

(take 10 (shifts a-team b-team))        ; (:john :arthur :rob :giles :jessica :arthur :john :giles :rob :arthur)
(take 10 (shifts a-team b-team c-team)) ; (:john :arthur :paul :rob :giles :eva :jessica :arthur :donald :john)

;; ;;;;;;;;;;;;;;;;;;;;;;;
;; Inverse of `interleave`
;; ;;;;;;;;;;;;;;;;;;;;;;;

(defn untangle [n xs]
  (letfn [(step [xs]
            (lazy-seq
             (cons
              (take-nth n xs)
              (step (rest xs)))))]
    (take n (step xs))))

(untangle 2 (interleave (range 3) (repeat 3 "."))) ; ((0 1 2) ("." "." "."))

(comment
  (take-nth 2 '(0 "." 1 "." 2 "."))         ; (0 1 2)
  (take-nth 2 (rest '(0 "." 1 "." 2 ".")))) ; ("." "." ".")

(def infinite
  (interleave
   (iterate inc 1)     ; (1 2 3 4 …)
   (iterate dec 0)     ; (0 -1 -2 -3 -4 …)
   (iterate inc 1/2))) ; (1/2 3/2 5/2 7/2 9/2 …)
; (1 0 1/2 2 -1 3/2 3 -2 5/2)

(def untangled (untangle 3 infinite))

(take 10 (first  untangled)) ; (1 2 3 4 5 6 7 8 9 10)
(take 10 (second untangled)) ; (0 -1 -2 -3 -4 -5 -6 -7 -8 -9)
(take 10 (last   untangled)) ; (1/2 3/2 5/2 7/2 9/2 11/2 13/2 15/2 17/2 19/2)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment
  ;; GC can take place (and will)
  (let [s (interleave (range 1e7) (range 1e7))]
    (- (first s) (last s)))
  ; -9999999

  ;; likely OOM (Out of Memory)
  ;; holding onto the head makes GC impossible
  (let [s (interleave (range 1e7) (range 1e7))]
    (- (last s) (first s))))
  ; 9999999

(comment
  ;; load book "War and Peace" by Leo Tolstoy
  (def lines (s/split-lines (slurp "https://tinyurl.com/wandpeace")))

  (quick-bench (last (eduction (interpose "|") lines))) ; (out) Execution time mean : 19.890866 ms

  (quick-bench (last (interpose "|" lines))))           ; (out) Execution time mean : 23.096023 ms

(defn plainform [xs]
  (->> xs
       (mapcat #(s/split % #"\s+"))
       (map    s/upper-case)
       (remove #(re-find #"\d+" %))
       (interpose "|")))

;; transducer version
(def xform
  (comp
   (mapcat #(s/split % #"\s+"))
   (map    s/upper-case)
   (remove #(re-find #"\d+" %))
   (interpose "|")))

(comment
  (quick-bench (last (plainform lines)))      ; (out) Execution time mean : 506.818934 ms
  (quick-bench (last (eduction xform lines))) ; (out) Execution time mean : 439.688698 ms

  ;; reduce
  (quick-bench                                ; (out) Execution time mean : 34.578671 ms
   (str
    (reduce
     #(.append ^StringBuilder %1 %2)
     (StringBuilder.)
     (interpose "|" lines))))

  ;; if additional processing is required, the transducer version performs better 
  ;; transduce
  (quick-bench                                ; (out) Execution time mean : 11.224608 ms
   (transduce
    (interpose "|")
    (completing #(.append ^StringBuilder %1 %2) str)
    (StringBuilder.)
    lines))

  ;; idomatic
  (quick-bench (s/join "|" lines)))          ; (out) Execution time mean : 11.322646 ms

;; Summary: if the main goal of using interpose is to completely evaluate the
;; sequential output (giving up laziness) it’s worth investigating the option
;; offered by interpose as transducer.
;;
;; If laziness is still important, interpose transducer can offer some advantage
;; only when there are other sequential transformations in the same transducer chain.

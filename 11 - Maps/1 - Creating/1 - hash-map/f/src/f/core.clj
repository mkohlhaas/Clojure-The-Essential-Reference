(ns f.core
  (:require
   [clojure.string :as s]
   [criterium.core :refer [quick-bench]])
  (:import [java.util HashMap]))

(def phone-book
  (hash-map
   "Jack N"    "381-883-1312"
   "Book Shop" "381-144-1256"
   "Lee J."    "411-742-0032"
   "Jack N"    "534-131-9922")) ; duplicate
; {"Book Shop" "381-144-1256",
;  "Jack N"    "534-131-9922",  ; last one used
;  "Lee J."    "411-742-0032"}

(phone-book "Jack N") ; "534-131-9922"
(type phone-book)     ; clojure.lang.PersistentHashMap

;; metadata on the key are retained from the original key
(def map-with-meta
  (hash-map
   (with-meta 'k {:m 1}) 1
   (with-meta 'k {:m 2}) 2))
; {k 2}

(map-with-meta 'k) ; 2

;; returns meta data from the first 'k key
(-> map-with-meta
    (find 'k) ; [k 2]
    first     ; k
    meta)     ; {:m 1}
; {:m 1}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; create hash-map at run-time
(apply
 hash-map
 (mapcat ; (7 0 3 1 7 2 6 3)
  vector
  (repeatedly #(rand-int 10)) ; (7 3 7 6 2 1 …)
  (range 4)))                 ; (0 1 2 3)
; {7 0, 3 1, 7 2, 6 3)
; {4 2, 6 0, 3 1, 8 3}
; {4 3, 3 2, 2 1}       ; collision happened
; {0 2, 7 3, 1 0, 2 1}

(comment)
  ;; trying to create hash-map at compile-time with {}-literal
  ; {(rand-int 10) 3
  ;  (rand-int 10) 2
  ;  (rand-int 10) 0})
  ; (err) Syntax error reading source
  ; (err) Duplicate key: (rand-int 10)

;; URL Parameters

(def long-url
  (str "https://notifications.google.com/u/0/_"
       "/NotificationsOgbUi/data/batchexecute?"
       "f.sid=4896754370137081598&hl=en&soc-app=208&"
       "soc-platform=1&soc-device=1&_reqid=53227&rt="))
; "https://notifications.google.com/u/0/_/NotificationsOgbUi/data/batchexecute?f.sid=4896754370137081598&hl=en&soc-app=208&soc-platform=1&soc-device=1&_reqid=53227&rt="

(defn split-pair [pair]
  (let [[k v] (s/split pair #"=")]
    (if v
      [k v]
      [k nil])))

(defn params [url]
  (as-> url x
    (s/split x #"\?")     ; ["https://notifications.google.com/u/0/_/NotificationsOgbUi/data/batchexecute" "f.sid=4896754370137081598&hl=en&soc-app=208&soc-platform=1&soc-device=1&_reqid=53227&rt="]
    (last x)              ; "f.sid=4896754370137081598&hl=en&soc-app=208&soc-platform=1&soc-device=1&_reqid=53227&rt="
    (s/split x #"\&")     ; ["f.sid=4896754370137081598" "hl=en" "soc-app=208" "soc-platform=1" "soc-device=1" "_reqid=53227" "rt="]
    (mapcat split-pair x) ; ("f.sid" "4896754370137081598" "hl" "en" "soc-app" "208" "soc-platform" "1" "soc-device" "1" "_reqid" "53227" "rt" nil)
    (apply hash-map x)))  ; {"soc-device" "1", "_reqid" "53227", "soc-platform" "1", "f.sid" "4896754370137081598", "rt" nil, "soc-app" "208", "hl" "en"}

(params long-url)
; {"soc-device"   "1",
;  "_reqid"       "53227",
;  "soc-platform" "1",
;  "f.sid"        "4896754370137081598",
;  "rt"            nil,
;  "soc-app"      "208",
;  "hl"           "en"}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; zip two vectors using transients returning a map
(defn zipmap* [vec1 vec2]
  (let [cnt (count vec1)]
    (loop [m   (transient {})
           idx 0]
      (if (< idx cnt)
        (recur (assoc! m (vec1 idx) (vec2 idx))
               (unchecked-inc idx))
        (persistent! m)))))

(comment
  (zipmap* [1 2 3] [4 5 6])                    ; {1 4, 2 5, 3 6}
  (zipmap* (vec (range 10)) (vec (range 10)))) ; {0 0, 7 7, 1 1, 4 4, 6 6, 3 3, 2 2, 9 9, 5 5, 8 8}

(comment
  ;; creating large maps with 1 million keys with different methods

  (let [pairs (into [] (range 2e6))]                         ; (out) Execution time mean : 557.585189 ms
    (quick-bench (apply hash-map pairs)))

  (let [pairs (into [] (map-indexed vector (range 1e6)))]    ; (out) Execution time mean : 547.377811 ms
    (quick-bench (into {} pairs)))

  (let [m (HashMap. (apply hash-map (into [] (range 2e6))))] ; (out) Execution time mean : 607.784837 ms
    (quick-bench (into {} m)))

  (let [ks (doall (range 1e6)) vs (doall (range 1e6))]       ; (out) Execution time mean : 588.640813 ms
    (quick-bench (zipmap ks vs)))

  (let [v1 (into [] (range 1e6)) v2 (into [] (range 1e6))]   ; (out) Execution time mean : 573.699050 ms
    (quick-bench (zipmap* v1 v2))))

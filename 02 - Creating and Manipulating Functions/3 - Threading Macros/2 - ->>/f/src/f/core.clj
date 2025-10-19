(ns f.core
  (:require
   [clojure.string :refer [split]]))

;; Thread Last Macro

(filter pos?
        (filter #(apply = (str %))
                (filter #(zero? (mod % 3))
                        (filter even? (range 1000)))))
; (6 66 222 444 666 888)

(->> (range 1000)
     (filter even?)
     (filter #(zero? (mod % 3)))
     (filter #(apply = (str %)))
     (filter pos?))
; (6 66 222 444 666 888)

(def sample-query "guidx=123&flip=true")

(defn params1 [query]
  (apply merge                            ; {"guidx" "123", "flip" "true"}
         (map #(apply hash-map %)         ; ({"guidx" "123"} {"flip" "true"})
              (map #(split % #"=")        ; (["guidx" "123"] ["flip" "true"])
                   (split query #"&"))))) ; ["guidx=123" "flip=true"]

(params1 sample-query)
; {"guidx" "123", "flip" "true"}

(defn params2 [query]
  (->> (split query #"&")        ; ["guidx=123" "flip=true"]
       (map #(split % #"="))     ; (["guidx" "123"] ["flip" "true"])
       (map #(apply hash-map %)) ; ({"guidx" "123"} {"flip" "true"})
       (apply merge)))           ; {"guidx" "123", "flip" "true"}

(params2 sample-query)
; {"guidx" "123", "flip" "true"}

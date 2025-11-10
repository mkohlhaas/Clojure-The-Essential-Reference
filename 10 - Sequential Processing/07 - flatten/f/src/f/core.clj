(ns f.core
  (:require
   [clojure.walk :as w])
  (:import
   [java.util ArrayList]))

(flatten [[1 2 [2 3] '(:x :y [nil []])]]) ; (1 2 2 3 :x :y nil)

(flatten [[{:a 1} #{2 3} (doto (ArrayList.) (.add 1) (.add 2))]])  ; ({:a 1} #{3 2} [1 2])

(defn core-fns [form]
  (->> (w/macroexpand-all form)
       flatten
       (map str)
       (map #(re-find #"clojure\.core/(.*)" %))
       (keep last)
       distinct))

(core-fns
 '(for [[head & others] coll
        :while #(< i %)
        :let [a (mod i 2)]]
    (when (zero? a)
      (doseq [item others]
        (print item)))))
; ("seq"
;  "chunked-seq?"
;  "chunk-first"
;  "int"
;  "count"
;  "chunk-buffer"
;  "<"
;  "first"
;  "next"
;  "chunk-append"
;  "unchecked-inc"
;  "chunk-rest"
;  "chunk-cons"
;  "chunk"
;  "cons"
;  "rest")

(->>
 (range)
 (map range)
 (map-indexed vector)
 flatten
 (take 10))
; (0 1 0 2 0 1 3 0 1 2)

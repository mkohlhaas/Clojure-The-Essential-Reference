(ns f.core
  (:require
   [clojure.walk :refer [postwalk-demo prewalk-demo walk]]))

(defn inner [x] (println "inner on" x) x)
(defn outer [x] (println "outer on" x) x)

(walk inner outer [1 [2] #{:a 1} 4])
; (out) inner on 1
; (out) inner on [2]
; (out) inner on #{1 :a}
; (out) inner on 4
; (out) outer on [1 [2] #{1 :a} 4]
; [1 [2] #{1 :a} 4]

(prewalk-demo [1 [2 [3]] 4])
; (out) Walked: [1 [2 [3]] 4]
; (out) Walked: 1
; (out) Walked: [2 [3]]
; (out) Walked: 2
; (out) Walked: [3]
; (out) Walked: 3
; (out) Walked: 4
; [1 [2 [3]] 4]

(postwalk-demo [1 [2 [3]] 4])
; (out) Walked: 1
; (out) Walked: 2
; (out) Walked: 3
; (out) Walked: [3]
; (out) Walked: [2 [3]]
; (out) Walked: 4
; (out) Walked: [1 [2 [3]] 4]
[1 [2 [3]] 4]

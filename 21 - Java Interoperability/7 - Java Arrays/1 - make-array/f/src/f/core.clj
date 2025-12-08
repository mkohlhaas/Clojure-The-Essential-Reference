(ns f.core)

;; Arrays are special type of objects initialized to contain a fixed amount of items. 

(def a (make-array Boolean/TYPE 3))   ; [false, false, false]               (Boolean/TYPE = Java boolean) 
(def b (make-array Boolean      3))   ; [nil, nil, nil]

(def c (make-array Integer/TYPE 4 2)) ; [[0, 0], [0, 0], [0, 0], [0, 0]]
(def d (make-array Integer      4 2)) ; [[nil, nil], [nil, nil], [nil, nil], [nil, nil]]

(comment
  (vec a)       ; [false false false]
  (vec b)       ; [nil nil nil]
  (mapv vec c)  ; [[0 0] [0 0] [0 0] [0 0]]
  (mapv vec d)) ; [[nil nil] [nil nil] [nil nil] [nil nil]]

(defn optimal-size [n m]
  (let [size (quot n m)                               ; <1>
        left? (zero? (rem n m))]                      ; <2>
    (if left?
      size
      (inc size))))

(optimal-size 900 22)                                 ; <3>
;; 41

(partition-all (optimal-size 900 22) (range 900))  ; <4>
;; ((0 1 2 3 ... 38 39 40) (41 42 43 ...
;; 79 80 81) ...  (82 83 84 ...
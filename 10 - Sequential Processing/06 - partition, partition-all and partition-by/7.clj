(partition-all 3 (range 10)) ; <1>
;; ((0 1 2) (3 4 5) (6 7 8) (9))

(partition-all 3 2 (range 10)) ; <2>
;; ((0 1 2) (2 3 4) (4 5 6) (6 7 8) (8 9))
(first (reverse (map #(do (print % "") %) (range 100)))) ; <1>
;; 0 1 2 3 4...98 99 99
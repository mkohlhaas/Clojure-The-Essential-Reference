(macroexpand-1 '(when false (println "this will never be printed!"))) ; <1>
;; (if false (do (println "this will never be printed!")))
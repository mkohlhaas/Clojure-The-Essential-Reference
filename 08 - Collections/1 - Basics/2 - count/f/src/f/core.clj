(ns f.core
  (:require [clojure.java.io :as io]))

(count [1 2 3]) ; 3
(count nil)     ; 0

(comment
  (count (range (inc Integer/MAX_VALUE))))
  ; (err) Execution error (ArithmeticException)
  ; (err) integer overflow

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(defn- print-usage []
  (println "Usage: copy 'file-name' 'to-location' ['work-dir']"))

(defn- copy
  ([in out]
   (copy in out "./"))
  ([in out dir]
   (io/copy (io/file (str dir in)) (io/file out))))

(defn -main [& args]
  (cond
    (< (count args) 2) (print-usage)
    (= (count args) 2) (copy (first args) (second args))
    (> (count args) 2) (copy (first args) (second args) (last args))))

(-main "project.clj" "/tmp/copy1.clj")
(-main "copy1.clj"   "/tmp/copy2.clj" "/tmp/")

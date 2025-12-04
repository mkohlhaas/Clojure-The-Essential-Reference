(ns f.core)

(reader-conditional '(:clj :code)   false) ; #?(:clj :code)
(reader-conditional '(:clj [1 2 3]) true)  ; #?@(:clj [1 2 3])

;; splicing (with @)
(read-string {:read-cond :allow} "(list #?(:clj  [1 2 3]))") ; (list [1 2 3])
(read-string {:read-cond :allow} "(list #?@(:clj [1 2 3]))") ; (list 1 2 3)

(def parse (read-string {:read-cond :preserve} "#?(:clj [1 2 3])")) ; #?(:clj [1 2 3])

(comment
  (type parse)) ; clojure.lang.ReaderConditional

(reader-conditional? parse) ; true

(def parse1 (read-string {:read-cond :preserve} "#?(:clj [1 2 3])")) ; #?(:clj [1 2 3])

(:form      parse1) ; (:clj [1 2 3])
(:splicing? parse1) ; false

(def parse2 (read-string {:read-cond :preserve} "#?@(:clj [1 2 3])")) ; #?@(:clj [1 2 3])

(:form      parse2) ; (:clj [1 2 3])
(:splicing? parse2) ; true

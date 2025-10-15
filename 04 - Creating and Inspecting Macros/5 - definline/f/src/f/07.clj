(ns f.07
  (:import IntegerMath))

;; `definline` allows type discovery at compile-time, offering the
;; client a way to communicate type information to the compiler.
(definline plus [x y]
  `(IntegerMath/plus ~x ~y))

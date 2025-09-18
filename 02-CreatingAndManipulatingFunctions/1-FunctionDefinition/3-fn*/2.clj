#_{:clj-kondo/ignore [:redefined-var]}
(defmacro future [& body]
  `(future-call (^{:once true} fn* [] ~@body)))

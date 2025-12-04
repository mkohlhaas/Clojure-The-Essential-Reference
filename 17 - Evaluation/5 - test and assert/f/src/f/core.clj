(ns f.core)

;; :test and `assert` provide basic form of testing 

;; use a testing framework instead (e.g. clojure.test)

(defn sqrt
  {:test
   #(when-not (== (sqrt 4) 2.)
      (throw (RuntimeException. "sqrt(4) should be 2")))}
  [x]
  (loop [guess 1.] ; Newton's formula
    (if (> (Math/abs (- (* guess guess) x)) 1e-8)
      (recur (/ (+ (/ x guess) guess) 2.))
      guess)))

(comment
  (test #'sqrt))
  ; (err) Execution error
  ; (err) sqrt(4) should be 2

(comment
  (assert (= 1 (+ 3 3)) "It should be 6"))
  ; (err) Execution error (AssertionError)
  ; (err) Assert failed: It should be 6
  ; (err) (= 1 (+ 3 3))

(defn sqrt1
  {:test #(assert (== (sqrt1 4) 2.) "sqrt(4) should be 2")}
  [x]
  (loop [guess 1.]
    (if (> (Math/abs (- (* guess guess) x)) 1e-8)
      (recur (/ (+ (/ x guess) guess) 2.))
      guess)))

(comment
  (test #'sqrt1))
  ; (err) Execution error (AssertionError) at f.core/fn (form-init9898313183324999854.clj:25).
  ; (err) Assert failed: sqrt(4) should be 2
  ; (err) (== (sqrt 4) 2.0)

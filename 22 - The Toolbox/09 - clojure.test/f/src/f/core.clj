(ns f.core
  (:require
   [clojure.test :as t :refer [are deftest deftest deftest- is run-all-tests
                               run-tests test-all-vars test-ns test-var
                               testing use-fixtures with-test]]))

;; 01.clj

(defn sqrt [x]
  (when-not (neg? x)
    (loop [guess 1.]
      (if (> (Math/abs (- (* guess guess) x)) 1e-8)
        (recur (/ (+ (/ x guess) guess) 2.))
        guess))))

;; 02.clj

(comment
  (sqrt 4.0) ; 2.000000000000002
  (sqrt 4))  ; 2.000000000000002

(deftest sqrt-test (assert (= 2 (sqrt 4)) "Expecting 2"))

(:test (meta #'sqrt-test))
; #object[f.core$fn__4122 0x1d596fcf "f.core$fn__4122@1d596fcf"]

(comment
  (test #'sqrt-test))
  ; (err) Execution error (AssertionError)
  ; (err) Assert failed: Expecting 2
  ; (err) (= 2 (sqrt 4))

;; 03.clj

(macroexpand '(deftest sqrt-test))
;; (def sqrt-test
;;   (clojure.core/fn []
;;     (clojure.test/test-var (var sqrt-test))))

;; 04.clj

(deftest- this-is-not-public)

(keys (ns-publics *ns*))
; (sqrt sqrt-test f.core.proxy$java.lang.Object$SignalHandler$d8c00ec7)

;; (clojuredocs help find-name sqrt sqrt-test
;;  cdoc cjio apropos-better)

;; 05.clj

(with-test
  (defn sum [a b] (+ a b))
  (println "test called"))

(test #'sum) ; :ok
; (out) test called

;; 06.clj

(deftest sqrt-test1 (is (= 2 (sqrt 4)) "Expecting 2"))

(test-var #'sqrt-test1)
; (out) FAIL in (sqrt-test1) (form-init3226720579933441567.clj:61)
; (out) Expecting 2
; (out) expected: (= 2 (sqrt 4))
; (out)   actual: (not (= 2 2.000000000000002))
; (out) -  2
; (out) +  2.000000000000002

;; 07.clj

(deftest sqrt-test2
  (testing "The basics of squaring a number"
    (is (= 3 (sqrt 9))))
  (testing "Known corner cases"
    (is (= 0 (sqrt 0)))
    (is (= Double/NaN (sqrt Double/NaN)))))

(test-var #'sqrt-test2)
; (out) 
; (out) FAIL in (sqrt-test2) (form-init7458335723788687330.clj:75)
; (out) The basics of squaring a number
; (out) expected: (= 3 (sqrt 9))
; (out)   actual: (not (= 3 3.000000001396984))
; (out) -  3
; (out) +  3.000000001396984
; (out) 
; (out) FAIL in (sqrt-test2) (form-init7458335723788687330.clj:77)
; (out) Known corner cases
; (out) expected: (= 0 (sqrt 0))
; (out)   actual: (not (= 0 6.103515625E-5))
; (out) -  0
; (out) +  6.103515625E-5
; (out) 
; (out) FAIL in (sqrt-test2) (form-init7458335723788687330.clj:78)
; (out) Known corner cases
; (out) expected: (= Double/NaN (sqrt Double/NaN))
; (out)   actual: (not (= ##NaN 1.0))
; (out) -  ##NaN
; (out) +  1.0

;; 08.clj

(deftest sqrt-test3
  (are [x y] (= (sqrt x) y)
    9 3
    0 0
    Double/NaN Double/NaN))

(test-var #'sqrt-test3)
; (out) 
; (out) FAIL in (sqrt-test3) (form-init7458335723788687330.clj:106)
; (out) expected: (= (sqrt 9) 3)
; (out)   actual: (not (= 3.000000001396984 3))
; (out) -  3.000000001396984
; (out) +  3
; (out) 
; (out) FAIL in (sqrt-test3) (form-init7458335723788687330.clj:106)
; (out) expected: (= (sqrt 0) 0)
; (out)   actual: (not (= 6.103515625E-5 0))
; (out) -  6.103515625E-5
; (out) +  0
; (out) 
; (out) FAIL in (sqrt-test3) (form-init7458335723788687330.clj:106)
; (out) expected: (= (sqrt Double/NaN) Double/NaN)
; (out)   actual: (not (= 1.0 ##NaN))
; (out) -  1.0
; (out) +  ##NaN

;; 09.clj

(deftest sqrt-test4
  (is (thrown? IllegalArgumentException (sqrt -4)))
  (is (thrown-with-msg? IllegalArgumentException #"negative" (sqrt -4)))
  (is (instance? Double (sqrt nil))))

(binding [t/*stack-trace-depth* 3]
  (t/test-var #'sqrt-test4))
; (out) 
; (out) FAIL in (sqrt-test4) (form-init7458335723788687330.clj:134)
; (out) expected: (thrown? IllegalArgumentException (sqrt -4))
; (out)   actual: nil
; (out) 
; (out) FAIL in (sqrt-test4) (form-init7458335723788687330.clj:135)
; (out) expected: (thrown-with-msg? IllegalArgumentException #"negative" (sqrt -4))
; (out)   actual: nil
; (out) 
; (out) ERROR in (sqrt-test4) (Numbers.java:1099)
; (out) expected: (instance? Double (sqrt nil))
; (out)   actual: java.lang.NullPointerException: Cannot invoke "Object.getClass()" because "x" is null
; (out)  at clojure.lang.Numbers.ops (Numbers.java:1099)
; (out)     clojure.lang.Numbers.isNeg (Numbers.java:127)
; (out)     f.core$sqrt.invokeStatic (form-init7458335723788687330.clj:10)

;; 10.clj

(defmethod t/assert-expr 'roughly [msg form]
  `(let [op1# ~(nth form 1)
         op2# ~(nth form 2)
         tolerance# (if (= 4 ~(count form)) ~(last form) 2)
         decimals# (/ 1. (Math/pow 10 tolerance#))
         result# (< (Math/abs (- op1# op2#)) decimals#)]
     (t/do-report
      {:type (if result# :pass :fail)
       :message ~msg
       :expected (format "%s should be roughly %s with %s tolerance"
                         op1# op2# decimals#)
       :actual result#})
     result#))
#_{:clj-kondo/ignore [:unresolved-symbol]}
(deftest sqrt-test5
  (is (roughly 2 (sqrt 4) 14))
  (is (roughly 2 (sqrt 4) 15)))

(t/test-var #'sqrt-test5)
; (out) 
; (out) FAIL in (sqrt-test5) (form-init7458335723788687330.clj:174)
; (out) expected: "2 should be roughly 2.000000000000002 with 1.0E-15 tolerance"
; (out)   actual: false

;; 11.clj

(deftest a (is (= 1 (+ 2 2))))
(deftest b (is (= 2 (+ 2 2))))
(deftest c (is (= 4 (+ 2 2))))

;; tests everyting in this file
(test-all-vars 'f.core)
;; Original output from the book:
;; FAIL in (a) (form-init205934.clj:1)
;; expected: (= 1 (+ 2 2))
;;   actual: (not (= 1 4))
;;
;; FAIL in (b) (form-init20593408.clj:1)
;; expected: (= 2 (+ 2 2))
;;   actual: (not (= 2 4))

;; 12.clj

(deftest fail-a (is (= 1 (+ 2 2))))
(deftest fail-b (is (= 1 (+ 2 2))))
(deftest fail-c (is (= 1 (+ 2 2))))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn test-ns-hook [] (fail-a) (fail-c))

(test-ns 'f.core)
; (out) 
; (out) Testing f.core
; (out) 
; (out) FAIL in (fail-a) (form-init7458335723788687330.clj:201)
; (out) expected: (= 1 (+ 2 2))
; (out)   actual: (not (= 1 4))
; (out) -  1
; (out) +  4
; (out) 
; (out) FAIL in (fail-c) (form-init7458335723788687330.clj:203)
; (out) expected: (= 1 (+ 2 2))
; (out)   actual: (not (= 1 4))
; (out) -  1
; (out) +  4

;; 13.clj

(deftest a1 (is (= 4 (+ 2 2))))
(deftest b1 (is (= 4 (+ 2 2))))
(deftest c1 (is (= 4 (+ 2 2))))

(run-tests) ; {:test 2, :pass 0, :fail 2, :error 0, :type :summary}
; (out) 
; (out) Testing f.core
; (out) 
; (out) FAIL in (fail-a) (form-init7458335723788687330.clj:201)
; (out) expected: (= 1 (+ 2 2))
; (out)   actual: (not (= 1 4))
; (out) -  1
; (out) +  4
; (out) 
; (out) FAIL in (fail-c) (form-init7458335723788687330.clj:203)
; (out) expected: (= 1 (+ 2 2))
; (out)   actual: (not (= 1 4))
; (out) -  1
; (out) +  4
; (out) 
; (out) Ran 2 tests containing 2 assertions.
; (out) 2 failures, 0 errors.

;; 14.clj

(deftest a-1 (is (= 4 (+ 2 2))))
(deftest a-2 (is (= 4 (+ 2 2))))

(deftest b-1 (is (= 4 (+ 2 2))))
(deftest b-2 (is (= 4 (+ 2 2))))

(run-all-tests #".*new.*") ; {:test 2, :pass 0, :fail 2, :error 0, :type :summary}
; (out) 
; (out) Testing f.core
; (out) 
; (out) FAIL in (fail-a) (form-init7458335723788687330.clj:201)
; (out) expected: (= 1 (+ 2 2))
; (out)   actual: (not (= 1 4))
; (out) -  1
; (out) +  4
; (out) 
; (out) FAIL in (fail-c) (form-init7458335723788687330.clj:203)
; (out) expected: (= 1 (+ 2 2))
; (out)   actual: (not (= 1 4))
; (out) -  1
; (out) +  4
; (out) 
; (out) Ran 2 tests containing 2 assertions.
; (out) 2 failures, 0 errors.

;; 15.clj

(defn setup [tests]
  (println "### before")
  (tests)
  (println "### after"))

(use-fixtures :each setup)

(deftest a-test (is (= 1 1)))
(deftest b-test (is (= 1 1)))

(run-tests)
;; Testing fixture-test-1
;; ### before
;; ### after
;; ### before
;; ### after
;;
;; Ran 2 tests containing 2 assertions.
;; 0 failures, 0 errors.
;; {:test 2, :pass 2, :fail 0, :error 0, :type :summary}

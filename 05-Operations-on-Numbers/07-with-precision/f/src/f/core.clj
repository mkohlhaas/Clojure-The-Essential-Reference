(ns f.core)

(/ 22. 7) ; 3.142857142857143

(comment
  ;; BigDecimal can result in an ArithmeticException when the number of decimals is non-terminating.
  (/ 22M 7))
  ; (err) Execution error (ArithmeticException)
  ; (err) Non-terminating decimal expansion; no exact representable decimal result.

(with-precision 4  (/ 22M 7)) ; 3.143M
(with-precision 10 (/ 22M 7)) ; 3.142857143M

;; HALF_UP is the default rounding type
(with-precision 10 (/ 1M 3))                     ; 0.3333333333M
(with-precision 10 :rounding HALF_DOWN (/ 1M 3)) ; 0.3333333333M

(defn share-qty [account price]
  (let [accountM (bigdec account)
        priceM   (bigdec price)]
    (if (zero? priceM)
      0
      (long (with-precision 5 :rounding DOWN (/ accountM priceM))))))

(share-qty 800 1.03) ; 776

(defn increment [current price qty]
  (let [currentM (bigdec current)
        priceM   (bigdec price)]
    (double (with-precision 5 (+ currentM (* priceM qty))))))

(increment 210 0.38 20) ; 217.6

;; this is why we need BigDecimals
(- 1.03 0.42)                   ; 0.6100000000000001
(- (bigdec 1.03) (bigdec 0.42)) ; 0.61M

(binding [*math-context* (java.math.MathContext. 10 java.math.RoundingMode/UP)]
  (/ 10M 3))
; 3.333333334M

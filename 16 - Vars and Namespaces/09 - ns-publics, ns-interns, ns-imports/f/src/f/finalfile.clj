;; 1.clj

(ns user)

(defn clean-ns [ns]
  (let [ks (keys (ns-map ns))]
    (doseq [k ks]
      (ns-unmap ns k))))

(ns myns)
(#'user/clean-ns 'myns)
(clojure.core/alias 'c 'clojure.core)

(c/ns-map 'myns)
;; {}

;; 2.clj

(def normal-var :public)
(def ^:private private-var :private)
(c/import 'java.lang.Number)

(c/ns-map 'myns)
;; {private-var #'myns/private-var,
;;  Number java.lang.Number,
;;  normal-var #'myns/normal-var}

(c/ns-publics 'myns)
;; {normal-var #'myns/normal-var}

(c/ns-interns 'myns)
;; {private-var #'myns/private-var
;;  normal-var #'myns/normal-var}

(c/ns-imports 'myns)
;; {Number java.lang.Number}


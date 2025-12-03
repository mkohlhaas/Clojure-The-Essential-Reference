;; 1.clj

(def a-var {:a 1})

(alter-var-root
 #'a-var
 update-in [:a] inc)
;; {:a 2}

;; 2.clj

(def a-var 1)

(future
  (alter-var-root
   #'a-var
   (fn [old]
     (Thread/sleep 10000)
     (inc old))))

;; blocking call for 10 seconds
a-var
;; 2

;; 3.clj

(definline timespi [x] `(* ~x 3.14))
(alter-var-root #'timespi (fn [_] (constantly 1)))
(timespi 100)
;; 314.0

(alter-meta! #'timespi dissoc :inline-arities :inline)
(timespi 100)
;; 1

;; 4.clj

(defn fetch-title [url]
  (let [input (slurp url)]
    (last
     (re-find #"Title: (.*)\." input))))

(def sample-article "Some Title: Salary increases announced.")

(with-redefs [slurp (constantly sample-article)]
  (= "Salary increases announced" (fetch-title "url")))
;; true

;; 5.clj

(with-redefs-fn {#'slurp (constantly sample-article)}
  #(= "Salary increases announced" (fetch-title "url")))
;; true

;; 6.clj

(defn x [] 5)
(defn y [] 9)

(dotimes [i 10]
  (future (with-redefs [x #(rand)] (* (x) (y))))
  (future (with-redefs [y #(rand)] (* (x) (y)))))

[(x) (y)]
;; [0.6022778872500808 9]

;; 7.clj

(defn ^:dynamic x [] 5)
(defn ^:dynamic y [] 9)

(dotimes [i 10]
  (future (binding [x #(rand)] (* (x) (y))))
  (future (binding [y #(rand)] (* (x) (y)))))

[(x) (y)]
;; [5 9]


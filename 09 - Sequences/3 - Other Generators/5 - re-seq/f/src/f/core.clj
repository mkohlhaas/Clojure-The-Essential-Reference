(ns f.core
  (:import
   [java.io InputStream]
   [java.net URL]
   [java.util Scanner]
   [java.util.regex Pattern]))

(re-seq #"\d+" "This sentence has 2 numbers and 6 words.")  ; ("2" "6")

(def sb (doto (StringBuilder.)
          (.append "23")
          (.append "aa 42")))

(re-seq #"\d+" sb)                               ; ("23" "42")

(seq "hello")                                    ; (\h \e \l \l \o)

(map (memfn toUpperCase) (map str "hello"))      ; ("H" "E" "L" "L" "O")

(map (memfn toUpperCase) (re-seq #"\w" "hello")) ; ("H" "E" "L" "L" "O")

(def signed-up
  "Jack 221 610-5007 (call after 9pm),
   Anna 221 433-4185,
   Charlie 661 471-3948,
   Hugo 661 653-4480 (busy on Sun),
   Jane 661 773-8656,
   Ron 555 515-0158")

(let [people (re-seq #"(\w+) (\d{3}) \d{3}-\d{4}" signed-up)]
  {:names (map second people) :area (map last people)})
; {:names ("Jack" "Anna" "Charlie" "Hugo" "Jane" "Ron"),
;  :area ("221" "221" "661" "661" "661" "555")}

(comment
  (def pi-digits (slurp "https://tinyurl.com/pi-digits"))

  (def pi-seq
    (sequence
     (comp
      cat
      (map int)
      (map #(mod % 48)))
     (re-seq #"\d{10}" pi-digits)))

  (take 20 pi-seq)) ; (1 4 1 5 9 2 6 5 3 5 8 9 7 9 3 2 3 8 4 6)

(defn restream-seq [^Pattern re ^InputStream is]
  (let [s (Scanner. is "UTF-8")]
    ((fn step []
       (if-let [token (.findInLine s re)]
         (cons token (lazy-seq (step)))
         (when (.hasNextLine s) (.nextLine s) (step)))))))

(defn pi-seq-fn [is]
  (sequence
   (comp
    cat
    (map int)
    (map #(mod % 48)))
   (restream-seq #"\d{10}" is)))

(def pi-digits (URL. "https://tinyurl.com/pi-digits"))

(with-open [is (.openStream pi-digits)]
  (doall (take 20 (pi-seq-fn is))))
; (1 4 1 5 9 2 6 5 3 5 8 9 7 9 3 2 3 8 4 6)

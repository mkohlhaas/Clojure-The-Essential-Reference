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
; #object[java.lang.StringBuilder 0x16ef8c13 "23aa 42"]

;; also works with StringBuilders
(re-seq #"\d+" sb)                               ; ("23" "42")

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(seq "hello")                                    ; (\h \e \l \l \o)
(map (memfn toUpperCase) (map str "hello"))      ; ("H" "E" "L" "L" "O")
(map (memfn toUpperCase) (re-seq #"\w" "hello")) ; ("H" "E" "L" "L" "O")

 ;; Teach Programming to a Group of Kids

(def signed-up
  "Jack 221 610-5007 (call after 9pm),
   Anna 221 433-4185,
   Charlie 661 471-3948,
   Hugo 661 653-4480 (busy on Sun),
   Jane 661 773-8656,
   Ron 555 515-0158")

(comment
  (re-seq #"(\w+) (\d{3}) \d{3}-\d{4}" signed-up))
; (["Jack 221 610-5007" "Jack" "221"]
;  ["Anna 221 433-4185" "Anna" "221"]
;  ["Charlie 661 471-3948" "Charlie" "661"]
;  ["Hugo 661 653-4480" "Hugo" "661"]
;  ["Jane 661 773-8656" "Jane" "661"]
;  ["Ron 555 515-0158" "Ron" "555"])

(let [people (re-seq #"(\w+) (\d{3}) \d{3}-\d{4}" signed-up)]
  {:names (map second people) :areas (map last people)})
; {:names ("Jack" "Anna" "Charlie" "Hugo" "Jane" "Ron"),
;  :areas ("221" "221" "661" "661" "661" "555")}

;; Book with a Million Digits of Pi
(def ebook-of-pi "https://tinyurl.com/pi-digits")

(comment
  (def pi-digits (slurp ebook-of-pi))

  (def pi-seq
    (sequence
     (comp
      cat
      (map int)
      (map #(mod % 48)))
     (re-seq #"\d{10}" pi-digits))) ;; ("1415926535" "8979323846" "2643383279" "5028841971" "6939937510" "5820974944"

  (take 20 pi-seq)) ; (1 4 1 5 9 2 6 5 3 5 8 9 7 9 3 2 3 8 4 6)

;; Lazier than lazy (Making the book loading lazy, too.)

;; The sequence generation downloads just enough HTTP request to satisfy the number of digits to print, 
;; preventing the entire book to reside in memory all at once.
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

(def pi-digits (URL. ebook-of-pi))

(with-open [is (.openStream pi-digits)]
  (doall (take 20 (pi-seq-fn is))))
; (1 4 1 5 9 2 6 5 3 5 8 9 7 9 3 2 3 8 4 6)

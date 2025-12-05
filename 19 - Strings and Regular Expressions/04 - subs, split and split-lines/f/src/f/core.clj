(ns f.core
  (:require
   [clojure.java.shell :refer [sh]]
   [clojure.string :as s]
   [criterium.core :refer [quick-bench]]))

(def s "The quick brown fox\njumps over the lazy dog")

(comment
  (def ds (apply str (apply concat (repeat 10 (range 10)))))
  ; "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789"

  (subs ds 20 30)  ; "0123456789"
  (subs ds 2  29)) ; "234567890123456789012345678"

(subs s 20 30) ; "jumps over"

;; split on white-space
(s/split s #"\s") ; ["The" "quick" "brown" "fox" "jumps" "over" "the" "lazy" "dog"]

(s/split-lines s) ; ["The quick brown fox" "jumps over the lazy dog"]

;; input string has a fixed structure
(def errors
  ["String index out of range: 34"
   "String index out of range: 48"
   "String index out of range: 3"])

(map #(subs % 27) errors) ; ("34" "48" "3")

;; location of the sub-string is not fixed
(def errors1
  ["String is out of bound: 34"
   "48 is not a valid index."
   "Position 3 is out of bound."])

;; using `split`
(map #(peek (s/split % #"\D+")) errors1) ; ("34" "48" "3")

(comment
  (s/split "48 is not a valid index." #"\D+")            ; ["48"]
  (s/split "Position 3 is out of bound." #"\D+")         ; ["" "3"]
  (type (s/split "Position 3 is out of bound." #"\D+"))  ; clojure.lang.PersistentVector
  (peek (s/split "Position 3 is out of bound." #"\D+"))) ; "3"

;; Locally Installed Fonts ;;

(def ls (:out (sh "ls" "-al" "/etc/fonts/conf.d")))

(def fonts (s/split-lines ls))
; ["total 44"
;  "drwxr-xr-x 2 root root 4096 Jul  2 19:56 ."
;  "drwxr-xr-x 3 root root 4096 Jul  2 19:56 .."
;  "lrwxrwxrwx 1 root root   57 Jun 11  2024 10-hinting-slight.conf -> /usr/share/fontconfig/conf.default/10-hinting-slight.conf"
;  "lrwxrwxrwx 1 root root   61 Jun 11  2024 10-scale-bitmap-fonts.conf -> /usr/share/fontconfig/conf.default/10-scale-bitmap-fonts.conf"
;  "lrwxrwxrwx 1 root root   56 Jun 11  2024 10-yes-antialias.conf -> /usr/share/fontconfig/conf.default/10-yes-antialias.conf"
;  …
;  "lrwxrwxrwx 1 root root   51 Jun 11  2024 69-urw-z003.conf -> /usr/share/fontconfig/conf.default/69-urw-z003.conf"
;  "lrwxrwxrwx 1 root root   52 Jun 11  2024 80-delicious.conf -> /usr/share/fontconfig/conf.default/80-delicious.conf"
;  "lrwxrwxrwx 1 root root   52 Jun 11  2024 90-synthetic.conf -> /usr/share/fontconfig/conf.default/90-synthetic.conf"
;  "-rw-r--r-- 1 root root  979 Jul  2 08:43 README"]

(last fonts) ; "-rw-r--r-- 1 root root  979 Jul  2 08:43 README"

(sequence
 (comp (map #(s/split % #"\s+"))         ; split on white-space
       (map last)                        ; only last item
       (filter #(re-find #"\.conf" %)))  ; only config files
 fonts)
; ("/usr/share/fontconfig/conf.default/10-hinting-slight.conf"
;  "/usr/share/fontconfig/conf.default/10-scale-bitmap-fonts.conf"
;  "/usr/share/fontconfig/conf.default/10-yes-antialias.conf"
;  "/usr/share/fontconfig/conf.default/69-urw-fallback-specifics.conf"
;  "/usr/share/fontconfig/conf.default/69-urw-gothic.conf"
;  "/usr/share/fontconfig/conf.default/69-urw-nimbus-mono-ps.conf"
;  "/usr/share/fontconfig/conf.default/69-urw-nimbus-roman.conf"
;  "/usr/share/fontconfig/conf.default/69-urw-nimbus-sans.conf"
;  …
;  "/usr/share/fontconfig/conf.default/69-urw-p052.conf"
;  "/usr/share/fontconfig/conf.default/69-urw-standard-symbols-ps.conf"
;  "/usr/share/fontconfig/conf.default/69-urw-z003.conf"
;  "/usr/share/fontconfig/conf.default/80-delicious.conf"
;  "/usr/share/fontconfig/conf.default/90-synthetic.conf")

(comment
  ;; subs
  (let [s "String index out of range: 34"] ; (out) Execution time mean :  45.407205 ns
    (quick-bench (subs s 27)))

  ;; split (using regex engine)
  (let [s "String index out of range: 34"  ; (out) Execution time mean : 651.464259 ns
        re #"\D+"]
    (quick-bench (s/split s re))))

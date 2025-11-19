(ns f.core
  (:require
   [clojure.set :refer [map-invert]])
  (:import
   [java.util HashMap]))

(map-invert {:a 1 :b 2}) ; {1 :a, 2 :b}

(def m (hash-map :y 0 :y 0 :d 0 :k 0 :w 0 :i 0
                 :f 0 :a 0 :n 0 :v 0 :s 0 :w 0))

m ; {:y 0, :v 0, :n 0, :w 0, :s 0, :k 0, :d 0, :f 0, :i 0, :a 0}
(map-invert m) ; {0 :a}

(map-invert {}) ; {}
(map-invert []) ; {}
(map-invert ()) ; {}
(map-invert "") ; {}

(map-invert (hash-map :a 1 :b 2 :c 3))               ; {3 :c, 2 :b, 1 :a}
(map-invert (array-map :a 1 :b 2 :c 3))              ; {1 :a, 2 :b, 3 :c}
(map-invert (sorted-map :a 1 :b 2 :c 3))             ; {1 :a, 2 :b, 3 :c}
(map-invert (struct (create-struct :a :b :c) 1 2 3)) ; {1 :a, 2 :b, 3 :c}

(defrecord A [a b c])

(map-invert (A. 1 2 3))                  ; {1 :a, 2 :b, 3 :c}
(map-invert (HashMap. {:a 1 :b 2 :c 3})) ; {3 :c, 1 :a, 2 :b}

(def scramble-key
  {\a \t \b \m \c \o \d \l
   \e \z \f \i \g \b \h \u
   \i \h \j \n \k \s \l \r
   \m \a \n \q \o \d \p \e
   \q \k \r \y \s \f \t \c
   \u \p \v \w \w \x \x \j
   \y \g \z \v \space \space})

(defn scramble [text]
  (apply str (map scramble-key text)))

(defn unscramble [text]
  (apply str (map (map-invert scramble-key) text)))

(scramble   "try to read this if you can") ; "cyg cd yztl cuhf hi gdp otq"
(unscramble "cyg cd yztl cuhf hi gdp otq") ; "try to read this if you can"

(unscramble (scramble "try to read this if you can")) ; "try to read this if you can"

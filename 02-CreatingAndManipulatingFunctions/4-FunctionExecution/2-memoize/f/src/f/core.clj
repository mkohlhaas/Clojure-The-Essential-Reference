(ns f.core
  (:require [clojure.string]))

;; ;;;;;;;;;
;; Example 1
;; ;;;;;;;;;

(defn- f* [a b]
  (println (format "Cache miss for [%s %s]" a b))
  (+ a b))

(def f (memoize f*))

(f 1 2)
; Cache miss for [1 2]
; 3

(f 1 2)
; 3

(f 1 3)
; Cache miss for [1 3]
; 4

;; ;;;;;;;;;
;; Example 2
;; ;;;;;;;;;

(defn levenshtein* [[c1 & rest1 :as str1]
                    [c2 & rest2 :as str2]]
  (let [len1 (count str1)
        len2 (count str2)]
    (cond (zero? len1) len2
          (zero? len2) len1
          :else
          (min (inc (levenshtein* rest1 str2))
               (inc (levenshtein* str1 rest2))
               (+ (if (= c1 c2) 0 1) (levenshtein* rest1 rest2))))))

(def levenshtein1 (memoize levenshtein*))

(defn to-words [file-name init]
  (->> file-name
       slurp
       clojure.string/split-lines
       (filter #(.startsWith % init))
       (remove #(> (count %) 8))
       doall))

(defn best1 [word dict]
  (->> dict
       (map #(-> [% (levenshtein1 word %)]))
       (sort-by last)
       (take 3)))

;; NOTE: $ sudo pacman -S words
(defn dict [init]
  (to-words "/usr/share/dict/words" init))

(def dict-ac (dict "ac"))

(time (best1 "achive" dict-ac))
; (out) "Elapsed time: 1717.126972 msecs"
; (["achieve" 1] ["active" 1] ["ache" 2])

(time (best1 "achive" dict-ac))
; (out) "Elapsed time: 1.132431 msecs"
; (["achieve" 1] ["active" 1] ["ache" 2])

;; ;;;;;;;;;
;; Example 3
;; ;;;;;;;;;

(defn memoize2 [f]
  (let [mem   (atom {})
        hits  (atom 0)
        miss  (atom 0)
        calls (atom 0)]
    (fn [& args]
      (if (identical? :done (first args))
        (let [count-chars (reduce + (map count (flatten (keys @mem))))]
          {:calls       @calls
           :hits        @hits
           :misses      @miss
           :count-chars count-chars
           :bytes       (* (int (/ (+ (* count-chars 2) 45) 8)) 8)})
        (do (swap! calls inc)
            (if-let [e (find @mem args)]
              (do (swap! hits inc) (val e))
              (let [ret (apply f args)
                    _ (swap! miss inc)]
                (swap! mem assoc args ret)
                ret)))))))

(def levenshtein2 (memoize2 levenshtein*))

(defn best2 [word dict]
  (->> dict
       (map #(-> [% (levenshtein2 word %)]))
       (sort-by last)
       (take 3)))

(best2 "achive" dict-ac)
; (["achieve" 1] ["active" 1] ["ache" 2])

(levenshtein2 :done)
; {:calls 176, :hits 0, :misses 176, :count-chars 2250, :bytes 4544}

(best2 "achive" dict-ac)
; (["achieve" 1] ["active" 1] ["ache" 2])

(levenshtein2 :done)
; {:calls 352, :hits 176, :misses 176, :count-chars 2250, :bytes 4544}

(ns f.core)

;; cond-> and cond->>
;;   - are NOT short-circuiting
;;   - the clauses don't have access to the evaluation of other forms but just the surrounding local bindings(!)

(let [x \c]
  (cond-> x
    (char? x)   int     ; 99
    (char? x)   inc     ; 100
    (string? x) reverse ; 100
    (= \c x)    (/ 2))) ; 50

(let [x [\a 1 2 3 nil 5]]
  (cond->> x
    (char? (first x)) rest          ; (1 2 3 nil 5)
    true              (remove nil?) ; (1 2 3 5)
    (> (count x) 5)   (reduce +)))  ; 11

;; idiomatic use of `cond->`
(let [x "123"] (if (string? x) (Integer. x) x)) ; 123
(let [x "123"] (cond-> x (string? x) Integer.)) ; 123

(defn same-initial? [m]
  (apply = (map (comp first name) (keys m))))

(defn shape-up [m]
  (cond-> m
    :always           (assoc-in [:k3 :j1] "default")
    (same-initial? m) (assoc :same true)
    (map? (:k2 m))    (assoc :k2 (apply str (vals (:k2 m))))))

(map shape-up
     [{:k1 "k1" :k2 {:h1 "h1" :h2 "h2"} :k3 {:j2 "j2"}}
      {:k1 "k1" :k2 "k2"}
      {:k1 "k1" :k2 {:h1 "h1" :h3 "h3"} :k3 {:j1 "j1"}}])
; ({:k1 "k1", :k2 "h1h2", :k3 {:j2 "j2", :j1 "default"}, :same true}
;  {:k1 "k1", :k2 "k2", :k3 {:j1 "default"}, :same true}
;  {:k1 "k1", :k2 "h1h3", :k3 {:j1 "default"}, :same true})

(def signals
  [111 214 311 413
   107 221 316 421
   112 222 317 471
   115 223 308 482])

(defn process [signals opts]
  (let [{:keys [_boost? bypass? interpolate? noise? cutoff?]} opts]
    (cond->> signals
      (< (count signals) 10) (map inc)
      interpolate?           (mapcat range)
      bypass?                (filter bypass?)
      noise?                 (random-sample noise?)
      cutoff?                (take-while #(< % cutoff?)))))

;; always different output bc `random-sample`
(process signals {:bypass? even? :interpolate? true :noise? 0.5 :cutoff? 200})
; (6 10 12 14 20 22 24 26 28 30 36 42 44 50 52 54 58 60 68 72 84 86 88 90 92 96 100 106 108 110
;  0 12 14 16 20 24 32 34 38 44 48 52 58 64 66 74 84 86 94 100 102 104 108 110 112 116 118 120 136
;  140 142 144 146 148 164 166 168 170 174 178 192 198)

(ns f.core
  (:require [clojure.data :refer [diff]]))

(diff {:a "1" :b "2"} {:b "2" :c "4"})
; ({:a "1"}  ; things only in first  arg
;  {:c "4"}  ; things only in second arg
;  {:b "2"}) ; things in both args

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(diff 1.0 1)
;; [1.0 1 nil]

;; Any interleaving nil occurrence in the resulting triplet should be ignored, as it 
;; could be the result of diff internal processing and not an actual occurrence of 
;; nil in the input arguments.

(diff [1 "x" 3 4]
      '(1 "y" 3 5))
; [[nil "x" nil 4]
;  [nil "y" nil 5]
;  [1 nil 3]]

(diff {:a "a" :b {:c "c"}}
      {:a 1   :b {:c 2}})
; ({:a "a" :b {:c "c"}}
;  {:a 1 :b {:c 2}}
;  nil)

(diff [1 {:a [1 2] :b {:c "c"}}]
      [1 {:a [1 3] :b {:c "c" :d "d"}}])
; [[nil {:a [nil 2]}]
;  [nil {:a [nil 3] :b {:d "d"}}]
;  [1 {:a [1] :b {:c "c"}}]]

(diff (int-array [1 2 3])
      (int-array [1 4 3]))
; [[nil 2] 
;  [nil 4] 
;  [1 nil 3])

(diff #{:a :c :b} #{:c :b :a})
; [nil 
;  nil 
;  #{:a :b :c})

(comment
  ;; A known bug afflicting diff with sorted-map.
  (diff {"x" 42} (sorted-map :x 42)))
  ; (err) Execution error (ClassCastException)
  ; (err) class clojure.lang.Keyword cannot be cast to class java.lang.String

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Services Written in Clojure
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; original service
(def orig
  {:defproject :prj1
   :description "the prj"
   :url "https://theurl"
   :license {:name "EPL"
             :url "http://epl-v10.html"}
   :dependencies {:dep1 "1.6.0"
                  :dep2 "1.0.13"
                  :dep6 "1.7.5"}
   :profiles {:uberjar {:main 'some.core :aot "all"}
              :dev {:dependencies {:dep8 "1.6.3"}
                    :plugins {:dep9 "3.1.1" :dep11 {:id 13}}}}})

;; new service
(def new-service
  {:defproject :prj1
   :description "the prj"
   :url "https://theurl"
   :license {:name "EPL"
             :url "http://epl-v10.html"}
   :dependencies {:dep1 "1.6.0"
                  :dep2 "1.0.13"
                  :dep6 "1.7.5"}
   :profiles {:uberjar {:main 'some.core :aot :all}
              :dev {:dependencies {:dep8 "1.6.1"}
                    :plugins {:dep9 "3.1.1" :dep11 {:id 13}}}}})

(defn walk-diff [d path]
  (if (map? d)
    (map #(walk-diff (% d) (conj path %)) (keys d))
    path))

(comment
  (walk-diff orig []))
  ; ([:defproject]
  ;  [:description]
  ;  [:url]
  ;  ([:license :name] [:license :url])
  ;  ([:dependencies :dep1] [:dependencies :dep2] [:dependencies :dep6])
  ;  (([:profiles :uberjar :main] [:profiles :uberjar :aot])
  ;   (([:profiles :dev :dependencies :dep8])
  ;    ([:profiles :dev :plugins :dep9]
  ;     ([:profiles :dev :plugins :dep11 :id])))))

(defn flatten-paths [paths]
  (->> paths
       (tree-seq seq? identity)
       (filter vector?)))

(comment
  (flatten-paths (walk-diff orig [])))
  ; ([:defproject]
  ;  [:description]
  ;  [:url]
  ;  [:license :name]
  ;  [:license :url]
  ;  [:dependencies :dep1]
  ;  [:dependencies :dep2]
  ;  [:dependencies :dep6]
  ;  [:profiles :uberjar :main]
  ;  [:profiles :uberjar :aot]
  ;  [:profiles :dev :dependencies :dep8]
  ;  [:profiles :dev :plugins :dep9]
  ;  [:profiles :dev :plugins :dep11 :id])

  ;; without `(filter vector?)`
  ; (([:defproject]
  ;   [:description]
  ;   [:url]
  ;   ([:license :name] [:license :url])
  ;   ([:dependencies :dep1] [:dependencies :dep2] [:dependencies :dep6])
  ;   (([:profiles :uberjar :main] [:profiles :uberjar :aot])
  ;    (([:profiles :dev :dependencies :dep8])
  ;     ([:profiles :dev :plugins :dep9]
  ;      ([:profiles :dev :plugins :dep11 :id])))))
  ;  [:defproject]
  ;  [:description]
  ;  [:url]
  ;  ([:license :name] [:license :url])
  ;  [:license :name]
  ;  [:license :url]
  ;  ([:dependencies :dep1] [:dependencies :dep2] [:dependencies :dep6])
  ;  [:dependencies :dep1]
  ;  [:dependencies :dep2]
  ;  [:dependencies :dep6]
  ;  (([:profiles :uberjar :main] [:profiles :uberjar :aot])
  ;   (([:profiles :dev :dependencies :dep8])
  ;    ([:profiles :dev :plugins :dep9]
  ;     ([:profiles :dev :plugins :dep11 :id]))))
  ;  ([:profiles :uberjar :main] [:profiles :uberjar :aot])
  ;  [:profiles :uberjar :main]
  ;  [:profiles :uberjar :aot]
  ;  (([:profiles :dev :dependencies :dep8])
  ;   ([:profiles :dev :plugins :dep9]
  ;    ([:profiles :dev :plugins :dep11 :id])))
  ;  ([:profiles :dev :dependencies :dep8])
  ;  [:profiles :dev :dependencies :dep8]
  ;  ([:profiles :dev :plugins :dep9]
  ;   ([:profiles :dev :plugins :dep11 :id]))
  ;  [:profiles :dev :plugins :dep9]
  ;  ([:profiles :dev :plugins :dep11 :id])
  ;  [:profiles :dev :plugins :dep11 :id])

(defn diff-to-path [orig other]
  (let [d (diff orig other)]
    (flatten-paths
     (walk-diff (first d) []))))

(comment
  (diff orig new-service)
  ; ({:profiles
  ;   {:dev {:dependencies {:dep8 "1.6.3"}}, :uberjar {:aot "all"}}}
  ;  {:profiles
  ;   {:dev {:dependencies {:dep8 "1.6.1"}}, :uberjar {:aot :all}}}
  ;  {:profiles
  ;   {:dev {:plugins {:dep9 "3.1.1", :dep11 {:id 13}}},
  ;    :uberjar {:main some.core}},
  ;   :dependencies {:dep1 "1.6.0", :dep2 "1.0.13", :dep6 "1.7.5"},
  ;   :license {:name "EPL", :url "http://epl-v10.html"},
  ;   :url "https://theurl",
  ;   :description "the prj",
  ;   :defproject :prj1})

  (first (diff orig new-service))
  ; {:profiles
  ;  {:dev {:dependencies {:dep8 "1.6.3"}}, :uberjar {:aot "all"}}}

  (flatten-paths
   (walk-diff (first (diff orig new-service)) [])))
  ; ([:profiles :dev :dependencies :dep8] [:profiles :uberjar :aot])

(diff-to-path orig new-service) ; ([:profiles :dev :dependencies :dep8] [:profiles :uberjar :aot])

(get-in orig [:profiles :dev :dependencies :dep8])        ; "1.6.3"
(get-in orig [:profiles :uberjar :aot])                   ; "all"

(get-in new-service [:profiles :dev :dependencies :dep8]) ; "1.6.1"
(get-in new-service [:profiles :uberjar :aot])            ; :all

;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; Performance Considerations 
;; ;;;;;;;;;;;;;;;;;;;;;;;;;; 

;; `diff` consumes the stack to perform the traversal. 

(defn generate [n]
  (reduce (fn [m e] (assoc-in m (range e) {e e}))
          {}
          (range 1 n)))

(comment
  (generate 3)    ; {0 {1 {2 2}}}
  (generate 10)   ; {0 {1 {2 {3 {4 {5 {6 {7 {8 {9 9}}}}}}}}}}
  (generate 11))  ; {0 {1 {2 {3 {4 {5 {6 {7 {8 {9 {10 10}}}}}}}}}}}

(defn blow-the-stack [depth]
  (doseq [n (range depth 100 -50)]
    (let [a (generate n)
          b (generate (inc n))]
      (try
        (diff a b)
        (catch StackOverflowError _
          (println "StackOverflow at" n "deep."))))))

(comment
  (blow-the-stack 700))
  ; (out) StackOverflow at 700 deep.
  ; (out) StackOverflow at 650 deep.
  ; (out) StackOverflow at 600 deep.
  ; (out) StackOverflow at 550 deep.
  ; (out) StackOverflow at 500 deep.
  ; (out) StackOverflow at 450 deep.
  ; … from here `diff` starts working correctly from the bottom of the stack

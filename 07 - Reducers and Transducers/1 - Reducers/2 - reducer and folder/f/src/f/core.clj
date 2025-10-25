(ns f.core
  (:require [clojure.core.reducers :as r]))

;; reducer and folder

;; The input function "xf" in both reducer and folder gets a chance to intercept the current call to the 
;; original reducing function and potentially alter the results. 

;; xf in the docs stands for transforming and reducing, respectively.

;; ;;;;;
;; Intro
;; ;;;;;

;; divisible-by-10 is an example of transformation on a reducing function; creates a new reducing function.
;; reducer transforms the input collection using divisible-by-10 as the new reducing behavior. 
(defn divisible-by-10 [current-reducing-fn] ; current-reducing-fn is provided by the framework.
  (fn [acc el]
    (if (zero? (mod el 10)) ; verifies if the current element is divisible by 10 and applies the current reducing function only in that case
      (current-reducing-fn acc el)
      acc)))

;; into is used here to show how the collection is now transformed.
;; into is implemented on top of reduce(!!!) hence why the transformation takes place.
;; into uses conj as reducing function.
(into []
      (r/reducer
       (range 100)
       divisible-by-10))
; [0 10 20 30 40 50 60 70 80 90]

(r/fold
 (r/monoid merge (constantly {}))
 (fn [m k v] (assoc m k (+ 3 v))) ; current reducing function
 (r/folder ; creates a foldable collection from a foldable collection using a reducing function
  (zipmap (range 100) (range 100)) ; {0 0, 1 1, 2 2, 3 3, …}
  (fn [rf] (fn [m k v] (if (zero? (mod k 10)) (rf m k v) m))))) ; rf = reducing function, creates a new reducing function from the current reducing function
; {0 3, 70 73, 20 23, 60 63, 50 53, 40 43, 90 93, 30 33, 10 13, 80 83}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; Stateful reducers typically define a local variable of type volatile! or an atom.
(defn reducer-dedupe [coll]
  (r/reducer coll
             (fn [rf]
               (let [prev (volatile! ::none)]
                 (fn [acc el]
                   (let [v @prev]
                     (vreset! prev el)
                     (if (= v el)
                       acc
                       (rf acc el))))))))

(->> (range 10)
     (r/map range)   ; [() (0) (0 1) (0 1 2) (0 1 2 3) (0 1 2 3 4) (0 1 2 3 4 5) (0 1 2 3 4 5 6) (0 1 2 3 4 5 6 7) (0 1 2 3 4 5 6 7 8)]
     (r/mapcat conj) ; [0 0 1 0 1 2 0 1 2 3 0 1 2 3 4 0 1 2 3 4 5 0 1 2 3 4 5 6 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 8]
     (r/filter odd?) ; [1 1 1 3 1 3 1 3 5 1 3 5 1 3 5 7 1 3 5 7]
     reducer-dedupe  ; [1 3 1 3 1 3 5 1 3 5 1 3 5 7 1 3 5 7]
     (into []))
; [1 3 1 3 1 3 5 1 3 5 1 3 5 7 1 3 5 7]

(->> (range 1500)
     (into []) ; [0 1 2 3 4 5 … 1499]
     (r/map
      #(do (println
            (str (Thread/currentThread))) %))
     (r/map range)
     (r/mapcat conj)
     (r/filter odd?)
     reducer-dedupe ; doesn't provide a fold implementation
     (r/fold +))    ; no parallelism
;; everything happens on the main thread; no parallelism
; (out) Thread[#30,nREPL-session-33150efd-6e43-4165-a575-4d9b3ca7987b,5,main]
; (out) Thread[#30,nREPL-session-33150efd-6e43-4165-a575-4d9b3ca7987b,5,main]
; (out) Thread[#30,nREPL-session-33150efd-6e43-4165-a575-4d9b3ca7987b,5,main]
; 280687748

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Designing a parallel reducer
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; A collection of 1600 numbers is split into 8 partitions of 200 items each.
;; Standard drop is used to remove the first 10 items on each partition.
;; The numbers are finally summed up together.
(->> (vec (range 1600))
     (partition 200)
     (mapcat #(drop 10 %))
     (reduce +))
; 1222840

(set! *warn-on-reflection* true)

;; parallel drop using r/folder
(defn pdrop [n coll]
  (r/folder coll
            (fn [rf] ; current reducing function
              (let [nv (volatile! n)]
                (fn
                  ([result input]
                   (let [n @nv]
                     (vswap! nv dec)
                     (if (pos? n)
                       result
                       (rf result input)))))))))

;; result always different
(distinct
 (for [_i (range 1000)]
   (->> (vec (range 1600))
        (pdrop 10)
        (r/fold 200 + +))))
; (1279155 1275979 1271155 1275973)

(defn stateful-foldvec [vec n combinef reducef]
  (cond
    (empty? vec)       (combinef)
    (<= (count vec) n) (reduce (reducef) (combinef) vec)
    :else            (let [split (quot (count vec) 2)
                           v1    (subvec vec 0 split)
                           v2    (subvec vec split (count vec))
                           fc    (fn [child] #(stateful-foldvec child n combinef reducef))]
                       (#'r/fjinvoke
                        #(let [f1 (fc v1)
                               t2 (#'r/fjtask (fc v2))]
                           (#'r/fjfork t2)
                           (combinef (f1) (#'r/fjjoin t2)))))))

#_{:clj-kondo/ignore [:redefined-var]}
(defn pdrop [dropn coll]
  (reify r/CollFold
    (coll-fold [_this n combinef reducef]
      (stateful-foldvec coll n combinef
                        (fn []
                          (let [nv (volatile! dropn)]
                            (fn
                              [result input]
                              (let [n @nv]
                                (vswap! nv dec)
                                (if (pos? n)
                                  result
                                  (reducef result input))))))))))

;; now stable
(distinct
 (for [_i (range 1000)]
   (->> (vec (range 1600))
        (pdrop 10)
        (r/fold 200 + +))))
; (1222840)

(defn drop-xform [n]
  (fn [rf]
    (fn []
      (let [nv (volatile! n)]
        (fn
          ([]             (rf))
          ([result]       (rf result))
          ([result input] (let [n @nv]
                            (vswap! nv dec)
                            (if (pos? n)
                              result
                              (rf result input)))))))))

(defn stateful-folder [coll]
  (reify r/CollFold
    (coll-fold [_this n combinef reducef]
      (stateful-foldvec coll n combinef reducef))))

;; result always different
(distinct
 (for [_i (range 1000)]
   (r/fold 200
           +
           ((drop 10) +)
           (vec (range 1600)))))
; (1279155 1261656 1275180)

;; now stable using drop-xform and stateful-folder
(distinct
 (for [_i (range 1000)]
   (r/fold 200
           +
           ((drop-xform 10) +)
           (stateful-folder (vec (range 1600))))))
; (1222840)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance Considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; reducer and folder are implemented in terms of reify 

(comment
  ;; r/take-while, r/take and r/drop do not use folder, thus preventing parallelism in certain conditions
  ;; works but no parallelism(!)
  (time (->> (range 50000)
             (into [])
             (r/map range)
             (r/mapcat conj)
             (r/drop 0)      ; only difference -> no parallelism
             (r/filter odd?)
             (r/fold +)))
  ; (out) "Elapsed time: 233238.694487 msecs"
  ; 10416041675000

  (time (->> (range 50000)
             (into [])
             (r/map range)
             (r/mapcat conj)
             (r/filter odd?)
             (r/fold +))))
  ; (out) "Elapsed time: 56052.242669 msecs"
  ; 10416041675000

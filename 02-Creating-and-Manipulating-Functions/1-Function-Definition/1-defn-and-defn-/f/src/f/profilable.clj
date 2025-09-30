(ns f.profilable)

;; ;;;;;;;;;;;;;;
;; test functions
;; ;;;;;;;;;;;;;;

(defn ^:bench profile-me [ms]
  (println "Crunching bits for" ms "ms")
  (Thread/sleep ms))

(defn dont-profile-me [_ms]
  (println "not expecting profiling"))

;; ;;;;;;;;;;;;;;;;;;;
;; benchmark framework
;; ;;;;;;;;;;;;;;;;;;;

(defn- wrap [f]
  (fn [& args]
    (time (apply f args))))

(defn- make-profilable [f]
  (alter-var-root f (constantly (wrap @f))))

(defn- tagged-by [tag nsname]
  (->> (ns-publics nsname)
       vals
       (filter #(get (meta %) tag))))

(comment
  (ns-publics 'f.profilable)
  ; {dont-profile-me #'f.profilable/dont-profile-me,
  ;  profile-me      #'f.profilable/profile-me,
  ;  prepare-bench   #'f.profilable/prepare-bench}

  (vals (ns-publics 'f.profilable)))
  ; (#'f.profilable/dont-profile-me
  ;  #'f.profilable/profile-me
  ;  #'f.profilable/prepare-bench)

(defn prepare-bench [nsname]
  (->> (tagged-by :bench nsname) ; get all functions to be benchmarked
       (map make-profilable)     ; `map` returns a lazy sequence
       dorun))                   ; force evaluation of lazy sequence

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; before preparing benchmarks
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;

(profile-me 500)
; (out) Crunching bits for 500 ms

(dont-profile-me 0)
; (out) not expecting profiling

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; after preparing benchmarks
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

(prepare-bench 'f.profilable)

(profile-me 500)
; (out) Crunching bits for 500 ms
; (out) "Elapsed time: 500.332547 msecs"

(dont-profile-me 0)
; (out) not expecting profiling

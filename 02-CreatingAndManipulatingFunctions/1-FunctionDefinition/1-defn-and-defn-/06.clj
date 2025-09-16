#_{:clj-kondo/ignore [:namespace-name-mismatch]}
(ns profilable)

(defn ^:bench profile-me [ms]
  (println "Crunching bits for" ms "ms")
  (Thread/sleep ms))

(defn dont-profile-me [_ms]
  (println "not expecting profiling"))

(ns user)

(defn- wrap [f]
  (fn [& args]
    (time (apply f args))))

(defn- make-profilable [v]
  (alter-var-root v (constantly (wrap @v))))

(defn- tagged-by [tag nsname]
  (->> (ns-publics nsname)
       vals
       (filter #(get (meta %) tag))))

(defn prepare-bench [nsname]
  (->> (tagged-by :bench nsname)
       (map make-profilable)
       dorun))

#_{:clj-kondo/ignore [:unresolved-namespace]}
(profilable/profile-me 500)
; (out) Crunching bits for 500 ms

(prepare-bench 'profilable)

#_{:clj-kondo/ignore [:unresolved-namespace]}
(profilable/profile-me 500)
; (out) Crunching bits for 500 ms
; (out) "Elapsed time: 500.332547 msecs"

#_{:clj-kondo/ignore [:unresolved-namespace]}
(profilable/dont-profile-me 0)
; (out) not expecting profiling

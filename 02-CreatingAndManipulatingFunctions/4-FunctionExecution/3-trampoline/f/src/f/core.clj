(ns f.core)

(defn- invoke
  [f-key & args]
  (apply (resolve (symbol (name f-key))) args))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn green [[light & lights]]
  #(case light
     :red false
     nil  true
     (invoke light lights)))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn red [[light & lights]]
  #(case light
     :amber false
     nil true
     (invoke light lights)))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn amber [[light & lights]]
  #(case light
     :green false
     nil true
     (invoke light lights)))

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn flashing-red [[light & lights]]
  #(if (nil? light)
     true
     (invoke light lights)))

(defn flashing-amber [[light & lights]]
  #(if (nil? light)
     true
     (invoke light lights)))

(defn traffic-light [lights]
  (trampoline flashing-amber lights))

(traffic-light [:red :amber :red])
; false

(traffic-light [:red :green :amber :red])
; true

(time (traffic-light (take 10000000 (cycle [:amber :red :green]))))
; (out) "Elapsed time: 11418.777125 msecs"
; true

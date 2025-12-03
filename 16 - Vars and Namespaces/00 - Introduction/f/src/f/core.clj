(ns f.core)

(def user-ns-mapping (ns-map 'user))
(def lookup-var      ('+ user-ns-mapping))

(lookup-var 1 1) ; 2

(comment
  (ns-map 'user)
  ; {primitives-classnames #'clojure.core/primitives-classnames,
  ;  +' #'clojure.core/+',
  ;  Enum java.lang.Enum,
  ;  decimal? #'clojure.core/decimal?,
  ;  restart-agent #'clojure.core/restart-agent,
  ;  sort-by #'clojure.core/sort-by,
  ;  macroexpand #'clojure.core/macroexpand,
  ;  ensure #'clojure.core/ensure,
  ;  …
  ;  unreduced #'clojure.core/unreduced,
  ;  the-ns #'clojure.core/the-ns,
  ;  …}

  (count (ns-map 'user)) ; 778

  (ns-aliases 'user) ; {}
  (loaded-libs))
  ; #{arrangement.core
  ;   clj-stacktrace.core
  ;   clojure.core.protocols
  ;   clojure.core.reducers
  ;   clojure.core.rrb-vector
  ;   clojure.core.rrb-vector.fork-join
  ;   clojure.core.rrb-vector.interop
  ;   clojure.core.rrb-vector.nodes
  ;   clojure.core.rrb-vector.parameters
  ;   …
  ;   nrepl.util.completion
  ;   nrepl.util.lookup
  ;   nrepl.version
  ;   puget.color
  ;   puget.color.ansi
  ;   puget.color.html
  ;   puget.dispatch
  ;   puget.printer}

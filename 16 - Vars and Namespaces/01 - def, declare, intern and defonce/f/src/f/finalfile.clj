;; 1.clj

(ns myns)

(type (def mydef "thedef"))
;; clojure.lang.Var

mydef
"thedef"

(identical? (var mydef) ((ns-map 'myns) 'mydef))
;; true

(meta (var mydef))
;; {:line 1,
;;  :column 7,
;;  :file "/private/var/form-init3920299731829243523.clj",
;;  :name mydef,
;;  :ns #object[clojure.lang.Namespace 0x68ff111c "myns"]}

;; 2.clj

(ns myns)

(def ^{:created-at "date"}
  def-meta-doc
  "A def with metadata and docstring." 1)

(clojure.repl/doc def-meta-doc)
;; -------------------------
;; myns/def-meta-doc
;;   A def with metadata and docstring.

(:created-at (meta (var def-meta-doc)))
;; "date"

;; 3.clj

(ns myns)

(def unbound-var)
;; #'myns/unbound-var

(type unbound-var)
;; clojure.lang.Var$Unbound

;; 4.clj

(declare state-one)

(def state-zero
  #(if (= \0 (first %))
     (state-one (next %))
     (if (nil? %) true false)))

(def state-one
  #(if (= \1 (first %))
     (state-zero (next %))
     (if (nil? %) true false)))

(state-zero "0100100001")
;; false
(state-zero "0101010101")
;; true

;; 5.clj

(ns myns)

(create-ns 'ext)

*ns*
;; #object[clojure.lang.Namespace 0x68ff111c "myns"]

(intern 'ext 'ext-var 1)
;; #'ext/ext-var

((ns-map 'ext) 'ext-var)
;; #'ext/ext-var

(intern 'yet-to-exist 'a 1)
;; Exception No namespace: yet-to-exist found

;; 6.clj

(def definitions
  {'ns1 [['a1 1] ['b1 2]]
   'ns2 [['a2 2] ['b2 2]]})

(defn defns [definitions]
  (for [[ns defs] definitions
        [name body] defs]
    (do
      (create-ns ns)
      (intern ns name body))))

(defns definitions)
;; (#'ns1/a1 #'ns1/b1 #'ns2/a2 #'ns2/b2)

;; 7.clj

(def redefine "1")
(defonce dont-redefine "1")
(def redefine "2")
(defonce dont-redefine "2")

redefine
;; "2"

dont-redefine
;; "1"


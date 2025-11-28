(ns f.core)

;; Functions and macros controlling multimethods:
;; - defmulti
;; - defmethod
;; - remove-all-methods
;; - remove-method
;; - prefer-method
;; - methods
;; - get-method
;; - prefers

;; - a multimethod is a special Clojure function that has multiple implementations
;; - the choice for a specific implementation is done through a dispatch function that the user has to provide
;; - multimethods is the most flexible flavor of polymorphism in Clojure
;; - compared to protocols, multimethods can dispatch on anything, not just types
;; - multimethods don't need the explicit definition of a Java interface and can use custom derivation rules to design complex dispatch hierarchies

(comment
  ;; repaying a $150,000 loan over 10 years at the annual interest rate of 3.16%.
  (* 150000 (Math/pow (+ 1 (/ 3.16 100 12)) (* 12 10)))) ; 205659.10262863498 (total payments)

(def total-payments
  {:op 'times
   :expr
   [[:loan 150000] ; constant
    {:op 'pow
     :expr
     [{:op 'plus
       :expr
       [[:incr 1] ; constant
        {:op 'divide
         :expr [[:rate 3.16] [:decimals 100] [:months 12]]}]}
      {:op 'times
       :expr [[:months 12] [:years 10]]}]}]})

;; map op to function
(def ops1
  {'plus   +
   'times  *
   'divide /
   'pow    #(Math/pow %1 %2)})

(defmulti calculate1
  (fn [form]
    (:op form))) ; using :op to select an implementation

(defmethod calculate1 'plus
  [{:keys [op expr]}] ; destructuring `form`
  (apply (ops1 op) (map calculate1 expr)))

(defmethod calculate1 'times
  [{:keys [op expr]}]
  (apply (ops1 op) (map calculate1 expr)))

(defmethod calculate1 'divide
  [{:keys [op expr]}]
  (apply (ops1 op) (map calculate1 expr)))

(defmethod calculate1 'pow
  [{[x y] :expr}]
  (Math/pow (calculate1 x) (calculate1 y)))

(comment
  ;; constant :incr is 20
  (:op [:incr 20])) ; nil

(defmethod calculate1 nil
  [[_descr number]] ; constant
  number)

(defmethod calculate1 :default
  [form]
  (throw (RuntimeException. (str "Don't know how to calculate" form))))

(calculate1 total-payments) ; 205659.10262863498

;; Improved Implementation with a Custom Hierarchy ;;

;; dispatches on operation types (:onearg, :twoargs, :varargs)

;; custom hierarchy
;; {'plus   -> :varargs
;;  'times  -> :varargs
;;  'divide -> :varargs
;;  'pow    -> :twoargs}
(def ops
  {'plus   [+                 :varargs]
   'times  [*                 :varargs]
   'divide [/                 :varargs]
   'pow    [#(Math/pow %1 %2) :twoargs]})

(defn- create-custom-hierarchy [hierarchy ops]
  (reduce
   (fn [h [op [_f kind]]]
     (derive h op kind)) ; creates a new hierarchy
   hierarchy
   ops))

(def custom-hierarchy
  (create-custom-hierarchy (make-hierarchy) ops))

; {:parents
;  {plus   #{:varargs}, ; this is basically the hierarchy
;   times  #{:varargs},
;   divide #{:varargs},
;   pow    #{:twoargs}},
;  :ancestors
;  {plus   #{:varargs},
;   times  #{:varargs},
;   divide #{:varargs},
;   pow    #{:twoargs}},
;  :descendants {:varargs #{divide times plus}, :twoargs #{pow}}}

(defn resolve-op-fn [ops op]
  (first (ops op)))

(comment
  +                          ; #object[clojure.core$_PLUS_ 0x3dec3f14 "clojure.core$_PLUS_@3dec3f14"]
  (resolve-op-fn ops 'plus)) ; #object[clojure.core$_PLUS_ 0x3dec3f14 "clojure.core$_PLUS_@3dec3f14"]

(defmulti calculate
  (fn [form]                     ; dispatch function
    (:op form))
  :hierarchy #'custom-hierarchy) ; custom-hierarchy is used for hierarchical dispatch (this is just another level of indirection)
                                 ; dispatch function e.g. results in 'plus this will be looked-up in the hiearchy and translated to :varargs
                                 ; :varargs is then the new dispatch value

(defmethod calculate :onearg
  [{op :op [x] :expr}]
  ((resolve-op-fn ops op) (calculate x)))

(defmethod calculate :twoargs
  [{op :op [x y] :expr}]
  ((resolve-op-fn ops op) (calculate x) (calculate y)))

(defmethod calculate :varargs
  [{:keys [op expr]}]
  (apply (resolve-op-fn ops op) (map calculate expr)))

(defmethod calculate nil
  [[_ number]]
  number)

(defmethod calculate :default
  [form]
  (throw (RuntimeException. (str "Don't know how to calculate " form))))

(calculate total-payments) ; 205659.10262863498

;; Extending Dynamically Multimethods ;;

;; (* 738.189 (Math/sqrt (inc (/ temp 273.15))))
(defn sound-speed-by-temp [temp]
  {:op 'with-mapping     ; special operator: in its presence we alter the hierarchy and the operator mappings to introduce new operations (inc, sqrt)
   :expr
   [{'inc  [inc :onearg] ; defining new operators
     'sqrt [(fn [x] (Math/sqrt x)) :onearg]}
    {:op 'times
     :expr
     [[:mph 738.189]
      {:op 'sqrt  ; using a new operator
       :expr
       [{:op 'inc ; using a new operator
         :expr
         [{:op 'divide
           :expr [[:celsius temp] [:zero 273.15]]}]}]}]}]})

(comment
  (calculate (sound-speed-by-temp -60)))
  ; (err) Execution error
  ; (err) Don't know how to calculate {:op with-mapping, :expr [{inc [#object[ … [[:celsius -60] [:zero 273.15]]}]}]}]}]}

(defmethod calculate :default
  [{op :op [ops forms] :expr :as form}]
  (if (= 'with-mapping op)
    (do
      (alter-var-root #'custom-hierarchy create-custom-hierarchy ops)  ; adding new operators to the custom hierarchy(?)
      (alter-var-root #'ops into ops)                                  ; add standard operators(?)
      (calculate forms))                                               ; calculate with the new hierarchy
    (throw (RuntimeException. (str "Don't know how to calculate " form)))))

(- (calculate (sound-speed-by-temp -60))  ; 652.0931459170323
   (calculate (sound-speed-by-temp  20))) ; 764.736671003387
; -112.64352508635466

(calculate {:op 'pow :expr [[:int 2] [:int 2]]}) ; 4.0

;; `remove-all-methods` would remove all methods (dough!!!)
(remove-method calculate :twoargs)

(comment
  (calculate {:op 'pow :expr [[:int 2] [:int 2]]}))
  ; (err) Execution error
  ; (err) Don't know how to calculate {:op pow, :expr [[:int 2] [:int 2]]}

;; nil, :onearg, :default, :varargs still in the hierarchy, but :twoargs has been removed
(methods calculate)
; {nil      #object[f.core$eval4442$fn__4444 0x546b9791 "f.core$eval4442$fn__4444@546b9791"],
;  :onearg  #object[f.core$eval4424$fn__4426 0x548838a2 "f.core$eval4424$fn__4426@548838a2"],
;  :default #object[f.core$eval4463$fn__4465 0x334cb922 "f.core$eval4463$fn__4465@334cb922"],
;  :varargs #object[f.core$eval4418$fn__4420 0x4e3ae341 "f.core$eval4418$fn__4420@4e3ae341"]}

;; `prefers` and `prefer-method` ;;

(defmulti edges
  "Retrieves first and last from a collection"
  type) ; dispatch on type

(defmethod edges clojure.lang.IPersistentList
  [x]
  ((juxt first last) (seq x)))

(defmethod edges java.lang.Iterable
  [x]
  ((juxt first last) (seq x)))

(comment
  ;; `(list 1 2 3)` is both
  (type (list 1 2 3))                         ; clojure.lang.PersistentList
  (instance? java.lang.Iterable (list 1 2 3)) ; true

  ;; this is typical situation extending multimethods to interface types, as Java allows inheritance from multiple interfaces
  (edges (list 1 2 3)))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Multiple methods in multimethod 'edges' match dispatch value: class clojure.lang.PersistentList -> interface clojure.lang.IPersistentList and interface java.lang.Iterable, …
  ;       … and neither is preferred)

;; would lead to the same result:
;; (prefer-method edges java.lang.Iterable clojure.lang.IPersistentList)
(prefer-method edges clojure.lang.IPersistentList java.lang.Iterable)

(edges (list 1 2 3)) ; [1 3]

(prefers print-dup)
; {clojure.lang.IRecord
;  #{clojure.lang.IPersistentCollection
;    java.util.Map
;    clojure.lang.IPersistentMap},
;  clojure.lang.IPersistentCollection
;  #{clojure.lang.Fn java.util.Collection java.util.Map},
;  java.util.Collection #{clojure.lang.Fn},
;  java.util.Map #{clojure.lang.Fn},
;  clojure.lang.ISeq
;  #{clojure.lang.IPersistentCollection java.util.Collection}}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 
;; Recursive Multimethods (with local names)
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 

(defmulti recursive identity)

;; dispatch on `1`
(defmethod recursive 1
  recursive-impl  ; recursive-impl is the local name of the anonymous function that follows
  [cnt]
  (if (< cnt 5)
    (do (print cnt " ")
        (recursive-impl (inc cnt)))
    cnt))

(recursive 1) ; 5
; (out) 1  2  3  4  

;; the local name is also useful for debugging ;;

(defmulti throwing identity)

(defmethod throwing :default
  throwing-impl ; local name
  [x]
  (throw (RuntimeException. (str "Problems with" x))))

(comment
  ;; `throwing-impl` shows up in the stack trace
  (throwing (symbol " this fn")))
  ; (err) Execution error at f.core/eval4516$throwing-impl (form-init6038527260514405107.clj:203).
  ; (err) Problems with this fn

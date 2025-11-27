(ns f.core)

(def total-payments
  {:op 'times
   :expr
   [[:loan 150000]
    {:op 'pow
     :expr
     [{:op 'plus
       :expr
       [[:incr 1]
        {:op 'divide
         :expr [[:rate 3.16]
                [:decimals 100]
                [:months 12]]}]}
      {:op 'times
       :expr [[:months 12] [:years 10]]}]}]})

(def ops
  {'plus +
   'times *
   'divide /
   'pow #(Math/pow %1 %2)})

(defmulti calculate
  (fn [form] (:op form)))

(defmethod calculate 'plus
  [{:keys [op expr]}]
  (apply (ops op) (map calculate expr)))

(defmethod calculate 'times
  [{:keys [op expr]}]
  (apply (ops op) (map calculate expr)))

(defmethod calculate 'divide
  [{:keys [op expr]}]
  (apply (ops op) (map calculate expr)))

(defmethod calculate 'pow
  [{[x y] :expr}]
  (Math/pow (calculate x) (calculate y)))

(defmethod calculate nil
  [[_descr number]]
  number)

(defmethod calculate :default [form]
  (throw (RuntimeException. (str "Don't know how to calculate" form))))

(calculate total-payments) ; 205659.10262863498

#_{:clj-kondo/ignore [:redefined-var]}
(def ops
  {'plus   [+ :varargs]
   'times  [* :varargs]
   'divide [/ :varargs]
   'pow    [#(Math/pow %1 %2) :twoargs]})

(defn- add-ops [hierarchy ops]
  (reduce
   (fn [h [op [_f kind]]] (derive h op kind))
   hierarchy
   ops))

(def hierarchy
  (add-ops (make-hierarchy) ops))

(defn resolve-op [ops op]
  (first (ops op)))

#_{:clj-kondo/ignore [:redefined-var]}
(do
  (def calculate nil)
  (defmulti calculate
    (fn [form] (:op form))
    :hierarchy #'hierarchy))

(defmethod calculate :varargs
  [{:keys [op expr]}]
  (apply (resolve-op ops op) (map calculate expr)))

(defmethod calculate :onearg
  [{op :op [x] :expr}]
  ((resolve-op ops op) (calculate x)))

(defmethod calculate :twoargs
  [{op :op [x y] :expr}]
  ((resolve-op ops op) (calculate x) (calculate y)))

(defmethod calculate nil
  [[_ number]]
  number)

(defmethod calculate :default
  [form]
  (throw
   (RuntimeException.
    (str "Don't know how to calculate " form))))

(calculate total-payments) ; 205659.10262863498

(defn sound-speed-by-temp [temp]
  {:op 'with-mapping
   :expr
   [{'inc [inc :onearg]
     'sqrt [(fn [x] (Math/sqrt x)) :onearg]}
    {:op 'times
     :expr
     [[:mph 738.189]
      {:op 'sqrt
       :expr
       [{:op 'inc
         :expr
         [{:op 'divide
           :expr [[:celsius temp]
                  [:zero 273.15]]}]}]}]}]})

(comment
  (calculate (sound-speed-by-temp -60)))
  ; (err) Execution error at f.core/eval4450$fn (form-init6038527260514405107.clj:101).
  ; (err) Don't know how to calculate {:op with-mapping, :expr [{inc [#object[clojure.core$inc 0x68c25bc5 "clojure.core$inc@68c25bc5"] :onearg], sqrt [#object[f.core$sound_speed_by_temp$fn__4458 0x3be26925 "f.core$sound_speed_by_temp$fn__4458@3be26925"] :onearg]} {:op times, :expr [[:mph 738.189] {:op sqrt, :expr [{:op inc, :expr [{:op divide, :expr [[:celsius -60] [:zero 273.15]]}]}]}]}]}

(defmethod calculate :default
  [{op :op [ops forms] :expr :as form}]
  (if (= 'with-mapping op)
    (do
      (alter-var-root #'hierarchy add-ops ops)
      (alter-var-root #'ops into ops)
      (calculate forms))
    (throw (RuntimeException. (str "Don't know how to calculate " form)))))

(- (calculate (sound-speed-by-temp -60))
   (calculate (sound-speed-by-temp  20)))
; -112.64352508635466

(remove-method calculate :twoargs)

(comment
  (calculate {:op 'pow :expr [[:int 2] [:int 2]]}))
  ; (err) Execution error at f.core/eval4463$fn (form-init6038527260514405107.clj:133).
  ; (err) Don't know how to calculate {:op pow, :expr [[:int 2] [:int 2]]}

(methods calculate)
; {nil
;  #object[f.core$eval4442$fn__4444 0x546b9791 "f.core$eval4442$fn__4444@546b9791"],
;  :onearg
;  #object[f.core$eval4424$fn__4426 0x548838a2 "f.core$eval4424$fn__4426@548838a2"],
;  :default
;  #object[f.core$eval4463$fn__4465 0x334cb922 "f.core$eval4463$fn__4465@334cb922"],
;  :varargs
;  #object[f.core$eval4418$fn__4420 0x4e3ae341 "f.core$eval4418$fn__4420@4e3ae341"]}

(defmulti edges
  "Retrieves first and last from a collection" type)

(defmethod edges java.lang.Iterable [x]
  ((juxt first last) (seq x)))

(defmethod edges clojure.lang.IPersistentList [x]
  ((juxt first last) (seq x)))

(comment
  (edges (list 1 2 3)))
  ; (err) Execution error (IllegalArgumentException)
  ; (err) Multiple methods in multimethod 'edges' match dispatch value: class clojure.lang.PersistentList -> interface clojure.lang.IPersistentList and interface java.lang.Iterable, and neither is preferred

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

(defmulti recursive identity)

(defmethod recursive 1 recursive-impl [cnt]
  (if (< cnt 5)
    (do (println cnt)
        (recursive-impl (inc cnt)))
    cnt))

(recursive 1) ; 5
; (out) 1
; (out) 2
; (out) 3
; (out) 4

(defmulti  throwing identity)

(defmethod throwing :default throwing-impl [x]
  (throw (RuntimeException. (str "Problems with" x))))

(comment
  (throwing (symbol " this fn")))
  ; (err) Execution error at f.core/eval4516$throwing-impl (form-init6038527260514405107.clj:203).
  ; (err) Problems with this fn

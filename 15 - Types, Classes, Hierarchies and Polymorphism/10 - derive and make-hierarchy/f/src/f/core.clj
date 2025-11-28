(ns f.core)

;; Group of functions dedicated to create and manage hierarchies in Clojure:
;; - derive
;; - make-hierarchy
;; - underive
;; - isa?
;; - parents
;; - ancestors   (going up)
;; - descendants (going down)

;; You can use derivation functions with multimethods to express polymorphic behavior which is not based on types. 
;; `isa?` is used by multimethods instead of plain equality to enable derivation on keywords or symbols. 

(defn custom-hierarchy [& derivations]
  (reduce
   (fn [hierarchy [child parent]]
     (derive hierarchy child parent)) ; `derive` without `hierarchy` would use the global hierarchy object
   (make-hierarchy)
   derivations))

(def h (custom-hierarchy
        [:clerk :person]   ; clerk  is a person      (keywords work)
        ['owner 'person]   ; owner  is a person      (symbols  work)
        [String :person])) ; String is a person (?!) (Java classes can only be children)
; {:parents     {:clerk #{:person}, owner #{person}, java.lang.String #{:person}},
;  :ancestors   {:clerk #{:person}, owner #{person}, java.lang.String #{:person}},
;  :descendants {:person #{:clerk java.lang.String}, person #{owner}}}

(isa? h 'owner 'person) ; true
(isa? h :clerk :person) ; true
(isa? h String :person) ; true

;; transitivity
(def h1 (custom-hierarchy
         [:unix :os]
         [:bsd  :unix]
         [:mac  :bsd]))

(isa? h1 :mac :unix) ; true

(def h2 (custom-hierarchy
         [:linux   :os]    ; os
         [:unix    :os]
         [:windows :os]
         [:os2     :os]
         [:redhat  :linux] ; linux
         [:debian  :linux]
         [:linux   :unix]  ; unix
         [:bsd     :unix]
         [:mac     :bsd])) ; bsd

(descendants h2 :unix) ; #{:redhat :linux :debian :bsd :mac}  (going down in the hierarchy -> what is a unix?)
(ancestors   h2 :mac)  ; #{:unix :os :bsd}                    (going up   in the hierarchy -> mac's lineage)

;; changing the hiararchy dynamically with `underive`
(def h3 (custom-hierarchy
         [:unix    :os]
         [:windows :unix] ; deliberately wrong 
         [:mac     :unix]))

(isa? h3 :windows :unix) ; true (Oops!)

(def h4 (underive h :windows :unix)) ; fixing the error

(isa? h4 :windows :unix) ; false (fixed dynamically)

(def h5 (custom-hierarchy
         [:clerk :person]
         [:owner :person]
         [:unix  :os]
         [:bsd   :unix]
         [:mac   :bsd]))

;; similar to how map works
(isa? h5 [:mac :owner] [:unix :person]) ; true (:mac is a :unix, :owner is a :person)

;; might be helpful in casting or type hinting
(ancestors String)
; #{java.lang.constant.ConstantDesc
;   java.lang.Object
;   java.lang.CharSequence
;   java.io.Serializable
;   java.lang.constant.Constable
;   java.lang.Comparable}

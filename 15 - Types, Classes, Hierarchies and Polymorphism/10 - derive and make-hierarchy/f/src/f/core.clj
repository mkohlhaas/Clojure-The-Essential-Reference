(ns f.core)

(defn custom-hierarchy [& derivations]
  (reduce (fn [h [child parent]] (derive h child parent))
          (make-hierarchy)
          derivations))

(def h (custom-hierarchy
        [:clerk :person]
        ['owner 'person]
        [String :person]))

(isa? h 'owner 'person) ; true
(isa? h :clerk :person) ; true
(isa? h String :person) ; true

(def h1 (custom-hierarchy
         [:unix :os]
         [:bsd  :unix]
         [:mac  :bsd]))

(isa? h1 :mac :unix) ; true

(def h2 (custom-hierarchy
         [:unix :os] [:windows :os] [:os2 :os]
         [:redhat :linux] [:debian :linux]
         [:linux :os] [:linux :unix] [:bsd :unix]
         [:mac :bsd]))

(descendants h2 :unix) ; #{:redhat :linux :debian :bsd :mac}
(ancestors   h2 :mac)  ; #{:unix :os :bsd}

(def h3 (custom-hierarchy
         [:unix :os]
         [:windows :unix]
         [:mac :unix]))

(isa? h3 :windows :unix) ; true

(def h4 (underive h :windows :unix))

(isa? h4 :windows :unix) ; false

(def h5 (custom-hierarchy
         [:clerk :person]
         [:owner :person]
         [:unix :os]
         [:bsd :unix]
         [:mac :bsd]))

(isa? h5 [:mac :owner] [:unix :person]) ; true

(ancestors String)
; #{java.lang.constant.ConstantDesc
;   java.lang.Object
;   java.lang.CharSequence
;   java.io.Serializable
;   java.lang.constant.Constable
;   java.lang.Comparable}

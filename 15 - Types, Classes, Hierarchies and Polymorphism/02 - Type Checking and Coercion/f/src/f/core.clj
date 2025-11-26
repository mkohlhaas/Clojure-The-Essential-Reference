(ns f.core)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;
;; type, instance? and class
;; ;;;;;;;;;;;;;;;;;;;;;;;;;

(let [add-meta (with-meta [1 2 3] {:type "MyVector"})
      no-meta  [1 2 3]]
  [(type  add-meta)
   (class add-meta)
   (type  no-meta)])
; ["MyVector"                        (type uses :type in metadata)
;  clojure.lang.PersistentVector 
;  clojure.lang.PersistentVector])

;; simple form of polymorphism using maps ;;

(defn make-type [obj type]
  (vary-meta obj assoc :type type))

(def person  (make-type {:name "John"    :title "Mr"}     :person))
(def manning (make-type {:name "Manning" :owner "Marjan"} :business))

(comment
  (type person)   ; :person
  (type manning)) ; :business

(defn print-contact [contact]
  (condp = (type contact)
    :person   (println (:title contact) (:name contact))
    :business (println (:name contact) (str "(" (:owner contact) ")"))
    String    (println "Contact:" contact)
    (println "unknown format")))

(print-contact person)      ; (out) Mr John
(print-contact manning)     ; (out) Manning (Marjan)
(print-contact "Mr. Renzo") ; (out) Contact: Mr. Renzo
(print-contact nil)         ; (out) unknown format

;; Clojure offers multimethods ;;

;; (Protocols would be also an option. They are specifically designed to handle type dispatch efficiently.)

(defmulti print-contact1 type) ; dispath on type

;; person type
(defmethod print-contact1 :person
  [contact]
  (println (:title contact) (:name contact)))

;; business type
(defmethod print-contact1 :business
  [contact]
  (println (:name contact) (str "(" (:owner contact) ")")))

;; String type
(defmethod print-contact1 String
  [contact]
  (println "Contact:" contact))

;; default
(defmethod print-contact1 :default
  [_contact]
  (println "unknown format"))

;; same output as before
(print-contact1 person)      ; (out) Mr John
(print-contact1 manning)     ; (out) Manning (Marjan)
(print-contact1 "Mr. Renzo") ; (out) Contact: Mr. Renzo
(print-contact1 nil)         ; (out) unknown format

;; ;;;;;;;;;;;;;;;;;
;; Type Naming Rules
;; ;;;;;;;;;;;;;;;;;

;; Clojure's idiomatic dashes "-" need to be replaced by underscores "_" for Java

(comment
  ;; in the REPL
  (in-ns 'my-package)          ; define a new namespace and switch to it
  (clojure.core/refer-clojure) ; we want access to clojure.core (Clojure standard library)
  (type (fn q? [])))           ; my_package$eval1777$q_QMARK___1778  (Clojure transforms "?" to "_QMARK_".)

(instance? java.lang.Number (bigint 1)) ; true  (java.lang.Number is the abstract class at the base of all numbers in Java and Clojure)
(instance? java.lang.Comparable 1)      ; true  (java.lang.Comparable is an interface)

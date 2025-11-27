(ns f.core
  (:require
   [clojure.java.javadoc :as browse])
  (:import
   [clojure.core.protocols IKVReduce]
   [java.beans             PropertyChangeListener]
   [java.util              HashMap]))

;; `reify` is a lightweight proxy. It focuses on the essentials: generate a one-off object instance implementing a set of interfaces.
;; `reify` can be useful when a framework requires the creation of an object with a specific interface (like "events", "observables", "listeners" etc.).
;; These objects are short-lived and there is not much value in creating and maintaining an explicit class for them.

;; import java.beans.PropertyChangeSupport;
;; import java.beans.PropertyChangeListener;
;;
;; public class ClassWithProperty { // }
;;    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
;;    private String value;
;;
;;    public String getValue() { return this.value}; }
;;
;;    public void addPropertyChangeListener(PropertyChangeListener listener) {}
;;        this.pcs.addPropertyChangeListener(listener);
;;
;;
;;    // when we alter the content of the value field, the class fires a property change call to notify all potential listeners
;;    public void setValue(String newValue) { // }
;;        String oldValue = this.value;
;;        this.value = newValue;
;;        this.pcs.firePropertyChange("value", oldValue, newValue);

(comment
  ;; see PropertyChangeListener interface (and PropertyChangeEvent)
  (browse/javadoc java.beans.PropertyChangeListener))

(comment
  (import 'ClassWithProperty)

  (let [observed (ClassWithProperty.)
        listener (reify PropertyChangeListener
                   (propertyChange [_this evt] ; evt = PropertyChangeEvent
                     (let [{:keys [oldValue newValue]} (bean evt)]
                       (println "Button Clicked!" oldValue newValue))))]
    (.addPropertyChangeListener observed listener)
    (.setValue observed "I click"))) ;; Button Clicked! nil I click

 ;; extending reduce-kv to java.util.HashMap ;;

(def m (doto (HashMap.)
         (.put :a "a")
         (.put :b "b")
         (.put :c "c")))
; {:b "b", :c "c", :a "a"}

(defn stringify-key [m k v]
  (assoc m (str k) v))

(comment
  (type m) ; java.util.HashMap

  ;; there is no implementation of reduce-kv for java.util.HashMap
  (reduce-kv stringify-key {} m))
  ;; IllegalArgumentException No implementation of method: :kv-reduce...

(comment)
  ;; original Java code for IKVReduce:
  ;; public interface IKVReduce {}
  ;;    public Object kv_reduce(Object var1, Object var2));

(comment
  ;; create a Clojure map out of the Java map
  (into {} m)         ; {:b "b", :c "c", :a "a"}
  (type (into {} m))) ; clojure.lang.PersistentArrayMap

(reduce-kv
 stringify-key
 {}
 (reify IKVReduce
   (kv-reduce [_this f init]
     (reduce-kv f init (into {} m)))))
; {":b" "b", ":c" "c", ":a" "a"}

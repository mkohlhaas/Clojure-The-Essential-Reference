(ns f.core
  (:require
   [clojure.reflect :as r]
   [clojure.repl :refer [doc]]))

;; `type-reflect` works with classes
;; `reflect`      works with objects and classes (is based on `type-reflect`, actually)

(comment
  (doc r/type-reflect))
  ; (out) -------------------------
  ; (out) clojure.reflect/type-reflect
  ; (out) ([typeref & options])
  ; (out)   Alpha - subject to change.
  ; (out)    Reflect on a typeref, returning a map with :bases, :flags, and
  ; (out)   :members. In the discussion below, names are always Clojure symbols.
  ; (out) 
  ; (out)    :bases            a set of names of the type's bases
  ; (out)    :flags            a set of keywords naming the boolean attributes
  ; (out)                      of the type.
  ; (out)    :members          a set of the type's members. Each member is a map
  ; (out)                      and can be a constructor, method, or field.
  ; (out) 
  ; (out)    Keys common to all members:
  ; (out)    :name             name of the type 
  ; (out)    :declaring-class  name of the declarer
  ; (out)    :flags            keyword naming boolean attributes of the member
  ; (out) 
  ; (out)    Keys specific to constructors:
  ; (out)    :parameter-types  vector of parameter type names
  ; (out)    :exception-types  vector of exception type names
  ; (out) 
  ; (out)    Key specific to methods:
  ; (out)    :parameter-types  vector of parameter type names
  ; (out)    :exception-types  vector of exception type names
  ; (out)    :return-type      return type name
  ; (out) 
  ; (out)    Keys specific to fields:
  ; (out)    :type             type name
  ; (out) 
  ; (out)    Options:
  ; (out) 
  ; (out)      :ancestors     in addition to the keys described above, also
  ; (out)                     include an :ancestors key with the entire set of
  ; (out)                     ancestors, and add all ancestor members to
  ; (out)                     :members.
  ; (out)      :reflector     implementation to use. Defaults to JavaReflector,
  ; (out)                     AsmReflector is also an option.

(keys (r/reflect {}))                          ; (:bases :flags :members)
(keys (r/reflect clojure.lang.APersistentMap)) ; (:bases :flags :members)

;; :bases   -> direct super-classes or implemented interfaces
;; :flags   -> modifiers of the class (`public`, `final`, `private`, …)
;; :members -> public methods

;; also super-classes and super-interfaces with `:ancestors`
(:ancestors (r/reflect {} :ancestors true))
; #{java.lang.Object
;   clojure.lang.Associative
;   clojure.lang.IDrop
;   java.util.concurrent.Callable
;   java.util.Map
;   clojure.lang.ILookup
;   java.lang.Runnable
;   clojure.lang.IPersistentCollection
;   clojure.lang.IHashEq
;   clojure.lang.IObj
;   clojure.lang.IFn
;   clojure.lang.MapEquivalence
;   clojure.lang.IKVReduce
;   clojure.lang.IMeta
;   clojure.lang.Counted
;   clojure.lang.IPersistentMap
;   clojure.lang.Seqable
;   java.io.Serializable
;   clojure.lang.AFn
;   clojure.lang.IEditableCollection
;   clojure.lang.IMapIterable
;   clojure.lang.APersistentMap
;   java.lang.Iterable}

(count (:members (r/reflect {})))                 ;  41
(count (:members (r/reflect {} :ancestors true))) ; 201

(deftype DummyReflector []
  r/Reflector
  (do-reflect [_this _typeref]
    {:bases   #{}
     :flags   #{}
     :members #{}}))

(r/reflect java.lang.Integer :reflector (DummyReflector.)) ; {:bases #{}, :flags #{}, :members #{}}

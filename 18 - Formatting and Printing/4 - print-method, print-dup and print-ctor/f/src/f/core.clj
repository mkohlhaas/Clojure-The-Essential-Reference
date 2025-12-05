(ns f.core
  (:require
   [clojure.repl :refer [doc]]))

;; `print-method`, `print-dup` and `print-ctor` are the entry points for the Clojure printing extension mechanism

(deftype Point [x y]) ; f.core.Point

(pr (Point. 1 2))     ; (out) #object[f.core.Point 0x59fd734f "f.core.Point@59fd734f"]

(defmethod print-method Point
  [object writer]
  (let [class-name (.getName (class object))
        args       (str (.x object) " " (.y object))]
    (.append writer (format "(%s. %s)" class-name args))))

(pr (Point. 1 2))     ; (out) (f.core.Point. 1 2)

(def point         (Point. 1 2))
(def point-as-str  (pr-str point))
(def point-as-list (read-string point-as-str))
(def back-to-point (eval point-as-list))

(comment
  (pr point)          ; (out) (f.core.Point. 1 2)
  (pr point-as-str)   ; (out) "(f.core.Point. 1 2)"
  (pr point-as-list)  ; (out) (f.core.Point. 1 2)
  (pr back-to-point)) ; (out) (f.core.Point. 1 2)

[point-as-str  :type (type point-as-str)]  ; ["(f.core.Point. 1 2)" :type java.lang.String]
[point-as-list :type (type point-as-list)] ; [(f.core.Point. 1 2)   :type clojure.lang.PersistentList]
[back-to-point :type (type back-to-point)] ; [(f.core.Point. 1 2)   :type f.core.Point]

(comment
  (doc *print-dup*))
  ; (out) -------------------------
  ; (out) clojure.core/*print-dup*
  ; (out)   When set to logical true, objects will be printed in a way that preserves
  ; (out)   their type when read in later.
  ; (out) 
  ; (out)   Defaults to false.

(binding [*print-dup* true]
  (pr-str {:a 1 :b 2}))
; "#=(clojure.lang.PersistentArrayMap/create {:a 1, :b 2})"

;; for print-* functions
(defmethod print-method Point
  [object writer]
  (.append writer (format "[x=%s, y=%s]" (.x object) (.y object))))

(pr-str (Point. 1 2)) ; "[x=1, y=2]"

;; for pr-* functions (serialization)
(defmethod print-dup Point
  [object writer]
  (print-ctor
   object
   (fn print-args [object writer]
     (.append writer (str (.x object) " " (.y object))))
   writer))

(comment
  (doc print-ctor))
 ; (out) -------------------------
 ; (out) clojure.core/print-ctor
 ; (out) ([o print-args w])

;; Clojure serialization with `print-dup` is effective but vulnerable to code injection.
;; This might explain why `print-dup` is generally undocumented and `read-string` is discouraged.
;; Unless you are in total control of serialized data, a better option is to use clojure EDN.
;; However, there might be cases where it makes sense to use `print-dup`, e.g. to temporarily park data on disk.

;; `print-ctor` outputs the constructor call inside the reader eval macro (#=(…))
(binding [*print-dup* true] (pr-str (Point. 1 2)))               ; "#=(f.core.Point. 1 2)"
(binding [*print-dup* true] (read-string (pr-str (Point. 1 2)))) ; [x=1, y=2]

(ns f.core
  (:require
   [clojure.edn :as edn]))

;; EDN (Extensible Data Notation) is a subset of the Clojure syntax designed for data exchange with other languages

(alias 'core 'clojure.core)

;; reader macro is no problem (@ calls the `var` function)
(core/read-string "@#'+") ; @#'+

(comment
  ;; some reader macros throw exceptions
  (edn/read-string "@#'+"))
  ; (err) Execution error
  ; (err) Invalid leading character: @

(comment
  ;; `read-string` doesn't know anything about `#point`
  (edn/read-string
   "#point [1 2]"))
  ; (err) Execution error
  ; (err) No reader function for tag point

(edn/read-string
 {:readers {'point identity}} ; map of tag name to tag implementation
 "#point [1 2]")
; [1 2]

(edn/read-string
 {:readers {'inst (constantly "override")}}
 "#inst \"2017-08-23T10:22:22.000-00:00\"")
; "override"

(edn/read-string
 {:default #(format "[Tag '%s', Value %s]" %1 %2)}
 "[\"There is no tag for \" #point [1 2] \"or\" #line [[1 2] [3 4]]]")
; ["There is no tag for "
;  "[Tag 'point', Value [1 2]]"
;  "or"
;  "[Tag 'line', Value [[1 2] [3 4]]]"]

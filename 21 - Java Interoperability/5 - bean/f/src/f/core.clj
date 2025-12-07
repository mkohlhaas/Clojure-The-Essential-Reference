(ns f.core
  (:require
   [clojure.java.javadoc :refer [javadoc]]
   [criterium.core :refer [quick-bench]])
  (:import
   [java.awt Point]
   [javax.swing JButton]))

; `bean` takes a Java object and returns a read-only implementation of the map abstraction based upon its JavaBean properties.

;; Not all available attributes are visible, but just those exposed through the JavaBean standard.

(def point (bean (Point. 2 4)))
; {:class    java.awt.Point,
;  :location #object[java.awt.Point 0x153235f9 "java.awt.Point[x=2,y=4]"],
;  :x        2.0,
;  :y        4.0}

(keys point)            ; (:class :location :x :y)

[(:x point) (:y point)] ; [2.0 4.0]

(comment
  ;; javax.swing.JButton contains many getter methods that `bean` can use to extract a map of key-property values
  (javadoc JButton)

  ;; Object only has `getClass`
  (javadoc Object))

(count (bean (Object.)))     ; 1
; {:class java.lang.Object}

(count (bean (JButton.)))    ; 89
; {:y 0,
;  :selectedObjects nil,
;  :componentPopupMenu nil,
;  :focusable true,
;  :managingFocus false,
;  :validateRoot false,
;  :requestFocusEnabled true,
;  :containerListeners [],
;  :rolloverSelectedIcon nil,
;  :iconTextGap 4,
;  :mnemonic 0,
;  :debugGraphicsOptions 0,
;  :visibleRect
;  …
;  #object[javax.swing.plaf.InsetsUIResource 0x29a162e "javax.swing.plaf.InsetsUIResource[top=2,left=14,bottom=2,right=14]"],
;  :alignmentY 0.5,
;  :model
;  #object[javax.swing.DefaultButtonModel 0x4443e794 "javax.swing.DefaultButtonModel@4443e794"]}

(comment
  ;; hand-made map
  (let [p (Point. 2 4)]           ; (out) Execution time mean : 28.599450 ns
    (quick-bench
     {:class    java.awt.Point
      :location (.getLocation p)
      :x        (.getX p)
      :y        (.getY p)}))

  ;; `bean` uses reflection which is very slow
  (let [p (Point. 2 4)]           ; (out) Execution time mean : 2.448194 µs
    (quick-bench (bean p))))

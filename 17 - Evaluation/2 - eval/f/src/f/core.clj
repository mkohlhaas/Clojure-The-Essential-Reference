(ns f.core)

;; `eval` evaluates the form data structure (not text!) and returns the result

(eval '(+ 1 2)) ; 3

(eval  (+ 1 2)) ; 3

(eval  [+ 1 2])
; [#object[clojure.core$_PLUS_ 0x4fbae8e7 "clojure.core$_PLUS_@4fbae8e7"]
;  1
;  2]

(eval '[+ 1 2])
; [#object[clojure.core$_PLUS_ 0x686e76be "clojure.core$_PLUS_@686e76be"]
;  1
;  2]

(eval '(do (println "eval-ed"
                    (+ 1 2))))
; (out) eval-ed
; 3

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Simple DSL for a Traffic Light System
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; using `read-string` to implement an intermediate translation layer

(require '[clojure.string :refer [split-lines]])

(def rules
  "If the light is red, you should stop
   If the light is green, you can cross
   If the light is orange, it depends")

(defmacro If [light & args]
  (let [[_ _ _ color & action] args]
    `(when (= '~light '~color) '~action)))

(defn parenthesize [s]
  (->> s
       split-lines
       (remove empty?)
       (map #(str "(" % ")"))))

(defn traffic-light [color rules]
  (->> rules
       parenthesize
       (map read-string)
       (map #(list* (first %) color (rest %)))
       (some eval)))

(comment
  ;; just text
  (->> rules
       parenthesize)
  ; ("(If the light is red, you should stop)"
  ;  "(   If the light is green, you can cross)"
  ;  "(   If the light is orange, it depends)")

  ;; from text to data structure
  (->> rules
       parenthesize
       (map read-string))
  ; ((If the light is red you should stop)
  ;  (If the light is green you can cross)
  ;  (If the light is orange it depends))

  ;; manipulating the data structure
  (->> rules
       parenthesize
       (map read-string)
       (map #(list* (first %) 'red (rest %))))
  ; ((If red the light is red you should stop)
  ;  (If red the light is green you can cross)
  ;  (If red the light is orange it depends))

  ;; evaluating the data structure
  (->> rules
       parenthesize
       (map read-string)
       (map #(list* (first %) 'red (rest %)))
       (some eval)))
  ; (you should stop)

(traffic-light 'red rules)    ; (you should stop)
(traffic-light 'green rules)  ; (you can cross)
(traffic-light 'orange rules) ; (it depends)


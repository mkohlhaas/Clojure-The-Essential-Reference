(ns f.core)

;; (completing ([f]) ([f cf])))

;; `cf` is a closing function, the function that should be called at the end of the reduction process. 
;; When `cf` is not provided identity is used.

;; `completing` provides (or replaces) the single arity call in a reducing function.
;; `completing` completes or fixes the reducing function for transduce.

(transduce (map inc) -              0 (range 10)) ;  55 (wrong)
(transduce (map inc) (completing -) 0 (range 10)) ; -55 (correct)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def ^:dynamic *debug* false)

(defn- print-if [s]
  (when *debug* (print (str s " "))))

;; custom transducer (identity with some printing for tracing)
(defn- identity-xform
  ([]
   (fn [rf]
     (fn
       ([]         (print-if "#0") (rf))
       ([acc]      (print-if "#1") (rf acc))
       ([acc item] (print-if "#2") (rf acc item)))))
  ([x] x))

(defn- completing-debug [f]
  (completing f #(do (print-if "#done!") %)))

;; By executing the transducer with tracing enabled, we can follow the invocations on screen:
(binding [*debug* true]
  (transduce
   (comp (map inc) (identity-xform))
   (completing-debug +)
   [1 2 3]))
; (out) #2 #2 #2 #1 #done! 
; 9

;; same as filter example but with transducers
(def events
  (apply concat (repeat
                 [{:device "AX31F" :owner "heathrow"
                   :date "2016-11-19T14:14:35.360Z"
                   :payload {:temperature 62.0
                             :wind-speed 22
                             :solar-radiation 470.2
                             :humidity 38
                             :rain-accumulation 2}}
                  {:device "AX31F" :owner "heathrow"
                   :date "2016-11-19T14:15:38.360Z"
                   :payload {:wind-speed 17
                             :solar-radiation 200.2
                             :humidity 46
                             :rain-accumulation 12}}
                  {:device "AX31F" :owner "heathrow"
                   :date "2016-11-19T14:16:35.360Z"
                   :payload {:temperature 63.0
                             :wind-speed 18
                             :humidity 38
                             :rain-accumulation 2}}])))

(defn average [k n]
  (transduce
   (comp
    (map (comp k :payload))
    (remove nil?)
    (take n))
   (completing + #(/ % n))
   events))

(average :temperature 10)     ; 62.5
(average :solar-radiation 60) ; 335.2000000000004

;; ;;;;;;;;;;;;;;;;;;;;;;;;;
;; The Need for `completing`
;; ;;;;;;;;;;;;;;;;;;;;;;;;;

;; usual transducer chain
(def xform
  (comp (map inc)
        (partition-all 3) ; stateful transducer
        cat))             ; unwraps any inner lists

(def xform-reductor
  (xform (completing + #(do (print "#done! ") %))))

(xform-reductor 0 0) ; 0
(xform-reductor 0 0) ; 0
(xform-reductor 0)   ; 2 (out) #done! 

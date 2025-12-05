(ns f.core
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :as pretty :refer [pp pprint]]
   [clojure.repl :refer [dir-fn doc]]))

(comment
  (dir-fn 'clojure.pprint)
  ; (*print-base*
  ;  *print-miser-width*
  ;  *print-pprint-dispatch*
  ;  *print-pretty*
  ;  *print-radix*
  ;  *print-right-margin*
  ;  *print-suppress-namespaces*
  ;  cl-format
  ;  code-dispatch
  ;  formatter
  ;  formatter-out
  ;  fresh-line
  ;  get-pretty-writer
  ;  pp
  ;  pprint
  ;  pprint-indent
  ;  pprint-logical-block
  ;  pprint-newline
  ;  pprint-tab
  ;  print-length-loop
  ;  print-table
  ;  set-pprint-dispatch
  ;  simple-dispatch
  ;  with-pprint-dispatch
  ;  write
  ;  write-out)

  (doc clojure.pprint/*print-miser-width*))
  ; (out) -------------------------
  ; (out) clojure.pprint/*print-miser-width*
  ; (out)   The column at which to enter miser style. Depending on the dispatch table, 
  ; (out) miser style add newlines in more places to try to keep lines short allowing for further 
  ; (out) levels of nesting.

(def data {:a ["red" "blue" "green"]
           :b '(:north :south :east :west)
           :c {"x-axis" 1 "y-axis" 2}})

data ; {:a ["red" "blue" "green"], :b (:north :south :east :west), :c {"x-axis" 1, "y-axis" 2}}

;; pp -> pretty prints the last thing output (esp. useful in the REPL)
(pp)
; {:a ["red" "blue" "green"],
;  :b (:north :south :east :west),
;  :c {"x-axis" 1, "y-axis" 2}}

(pprint data)
; (out) {:a ["red" "blue" "green"],
; (out)  :b (:north :south :east :west),
; (out)  :c {"x-axis" 1, "y-axis" 2}}

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pretty Printing Contribution to Clojure
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(doc pretty/write)
; (out) -------------------------
; (out) clojure.pprint/write
; (out) ([object & kw-args])
; (out)   Write an object subject to the current bindings of the printer control variables.
; (out) Use the kw-args argument to override individual variables for this call (and any 
; (out) recursive calls). Returns the string result if :stream is nil or nil otherwise.
; (out) 
; (out) The following keyword arguments can be passed with values:
; (out)   Keyword              Meaning                              Default value
; (out)   :stream              Writer for output or nil             true (indicates *out*)
; (out)   :base                Base to use for writing rationals    Current value of *print-base*
; (out)   :circle*             If true, mark circular structures    Current value of *print-circle*
; (out)   :length              Maximum elements to show in sublists Current value of *print-length*
; (out)   :level               Maximum depth                        Current value of *print-level*
; (out)   :lines*              Maximum lines of output              Current value of *print-lines*
; (out)   :miser-width         Width to enter miser mode            Current value of *print-miser-width*
; (out)   :dispatch            The pretty print dispatch function   Current value of *print-pprint-dispatch*
; (out)   :pretty              If true, do pretty printing          Current value of *print-pretty*
; (out)   :radix               If true, prepend a radix specifier   Current value of *print-radix*
; (out)   :readably*           If true, print readably              Current value of *print-readably*
; (out)   :right-margin        The column for the right margin      Current value of *print-right-margin*
; (out)   :suppress-namespaces If true, no namespaces in symbols    Current value of *print-suppress-namespaces*
; (out) 
; (out)   * = not yet supported

(with-open [w (io/writer "/tmp/prettyrange.txt")]
  (pretty/write
   (map range (range 12 0 -1))
   :stream w ; in most cases this would be *out*
   :base 2
   :length 6))

(comment
  (map range (range 12 0 -1)))
; ((0 1 2 3 4 5 6 7 8 9 10 11)
;  (0 1 2 3 4 5 6 7 8 9 10)
;  (0 1 2 3 4 5 6 7 8 9)
;  (0 1 2 3 4 5 6 7 8)
;  (0 1 2 3 4 5 6 7)
;  (0 1 2 3 4 5 6)
;  (0 1 2 3 4 5)
;  (0 1 2 3 4)
;  (0 1 2 3)
;  (0 1 2)
;  (0 1)
;  (0))

; use nvim's `gf` shortcut
; /tmp/prettyrange.txt
;
; ((0 1 10 11 100 101 ...)
;  (0 1 10 11 100 101 ...)
;  (0 1 10 11 100 101 ...)
;  (0 1 10 11 100 101 ...)
;  (0 1 10 11 100 101 ...)
;  (0 1 10 11 100 101 ...)
;  ...)

;; clojure function as string
(def op-fn
  "(defn op [sel]
     (condp = sel
        \"plus\" +
        \"minus\" -
        \"mult\" *
        \"div\" /
        \"rem\" rem
        \"quot\" quot))")

(comment)
  ;; formatted by the language server:
  ;; (defn op [sel]
  ;;   (condp = sel
  ;;     \"plus \" +
  ;;     \"minus \" -
  ;;     \"mult \" *
  ;;     \"div \" /
  ;;     \"rem \" rem
  ;;     \"quot \" quot)))

(read-string op-fn)
; (defn
;  op
;  [sel]
;  (condp
;   =
;   sel
;   "plus"
;   +
;   "minus"
;   -
;   "mult"
;   *
;   "div"
;   /
;   "rem"
;   rem
;   "quot"
;   quot))

(pprint (read-string op-fn))
; (out) (defn
; (out)  op
; (out)  [sel]
; (out)  (condp
; (out)   =
; (out)   sel
; (out)   "plus"
; (out)   +
; (out)   "minus"
; (out)   -
; (out)   "mult"
; (out)   *
; (out)   "div"
; (out)   /
; (out)   "rem"
; (out)   rem
; (out)   "quot"
; (out)   quot))

;; simple-dispatch (the default; e.g. also used by `pprint`)
(pretty/with-pprint-dispatch
  pretty/simple-dispatch
  (pprint (read-string op-fn)))
; (out) (defn
; (out)  op
; (out)  [sel]
; (out)  (condp
; (out)   =
; (out)   sel
; (out)   "plus"
; (out)   +
; (out)   "minus"
; (out)   -
; (out)   "mult"
; (out)   *
; (out)   "div"
; (out)   /
; (out)   "rem"
; (out)   rem
; (out)   "quot"
; (out)   quot))

;; code-dispatch
(pretty/with-pprint-dispatch
  pretty/code-dispatch
  (pprint (read-string op-fn)))
; (out) (defn op [sel]
; (out)   (condp = sel
; (out)     "plus" +
; (out)     "minus" -
; (out)     "mult" *
; (out)     "div" /
; (out)     "rem" rem
; (out)     "quot" quot))

;; `print-table` renders Clojure maps as two-dimensional tables ;;

;; `print-table` uses the keys of the first map to define the headers for the table
(pretty/print-table (repeat 4 (zipmap (range 10) (range 100 110))))
; (out) 
; (out) |   0 |   7 |   1 |   4 |   6 |   3 |   2 |   9 |   5 |   8 |
; (out) |-----+-----+-----+-----+-----+-----+-----+-----+-----+-----|
; (out) | 100 | 107 | 101 | 104 | 106 | 103 | 102 | 109 | 105 | 108 |
; (out) | 100 | 107 | 101 | 104 | 106 | 103 | 102 | 109 | 105 | 108 |
; (out) | 100 | 107 | 101 | 104 | 106 | 103 | 102 | 109 | 105 | 108 |
; (out) | 100 | 107 | 101 | 104 | 106 | 103 | 102 | 109 | 105 | 108 |

(comment
  (zipmap (range 10) (range 100 110))
  ; {0 100, 7 107, 1 101, 4 104, 6 106, 3 103, 2 102, 9 109, 5 105, 8 108}

  (repeat 4 (zipmap (range 10) (range 100 110))))
; ({0 100, 7 107, 1 101, 4 104, 6 106, 3 103, 2 102, 9 109, 5 105, 8 108}
;  {0 100, 7 107, 1 101, 4 104, 6 106, 3 103, 2 102, 9 109, 5 105, 8 108}
;  {0 100, 7 107, 1 101, 4 104, 6 106, 3 103, 2 102, 9 109, 5 105, 8 108}
;  {0 100, 7 107, 1 101, 4 104, 6 106, 3 103, 2 102, 9 109, 5 105, 8 108})

(let [headers [7 0 9 2]]
  (pretty/print-table
   headers
   (repeat 4 (zipmap (range 10) (range 100 110)))))
; (out) 
; (out) |   7 |   0 |   9 |   2 |
; (out) |-----+-----+-----+-----|
; (out) | 107 | 100 | 109 | 102 |
; (out) | 107 | 100 | 109 | 102 |
; (out) | 107 | 100 | 109 | 102 |
; (out) | 107 | 100 | 109 | 102 |

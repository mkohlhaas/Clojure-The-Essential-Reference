(ns f.core
  (:require
   [clojure.string :as s]))

(def link "Patterson, John: 'Once Upon a Time in the West'")

(def char-map
  {\,       "_comma_"
   \space   "_space_"
   \.       "_dot_"
   \'       "_quote_"
   \:       "_colon_"
   \newline "_newline_"})

(s/escape link char-map)
; "Patterson_comma__space_John_colon__space__quote_Once_space_Upon_space_a_space_Time_space_in_space_the_space_West_quote_"

;; `char-name-string` and `char-escape-string` are two public maps in the core namespace

char-name-string
; {\newline   "newline",
;  \tab       "tab",
;  \space     "space",
;  \backspace "backspace",
;  \formfeed  "formfeed",
;  \return    "return"}

char-escape-string
; {\newline   "\\n",
;  \tab       "\\t",
;  \return    "\\r",
;  \"         "\\\"",
;  \\         "\\\\",
;  \formfeed  "\\f",
;  \backspace "\\b"}

(map #(char-name-string % %) "Hello all!\n")
; (\H \e \l \l \o "space" \a \l \l \! "newline")

(comment
  (char-name-string \newline) ; "newline"
  (char-name-string \H)       ; nil
  (char-name-string \H \H))   ; \H

(def s "Type backslash-t '\t' followed by backslash-n '\n'")

(println s)
; (out) Type backslash-t '	' followed by backslash-n '
; (out) '

(println (s/escape s char-escape-string))
; (out) Type backslash-t '\t' followed by backslash-n '\n'

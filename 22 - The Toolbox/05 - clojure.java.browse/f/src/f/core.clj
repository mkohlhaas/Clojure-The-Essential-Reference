(ns f.core
  (:require
   [clojure.java.browse :refer [*open-url-script* browse-url]]
   [clojure.java.browse-ui :as bu]))

(comment
  ;; default system browser
  (browse-url "https://www.manning.com/books/clojure-the-essential-reference")

  ;; Swing browser (would be called if there is no system browser)
  (#'bu/open-url-in-swing "http://google.com")
  (#'bu/open-url-in-swing "https://www.manning.com/books/clojure-the-essential-reference")

  ;; custom browser
  (binding [*open-url-script* (atom "wget")]       ; "custom browser" is `wget`
    (browse-url "https://tinyurl.com/wandpeace"))) ; saves wandpeace in local file system

  ;; open with nvims gf
  ;; wandpeace

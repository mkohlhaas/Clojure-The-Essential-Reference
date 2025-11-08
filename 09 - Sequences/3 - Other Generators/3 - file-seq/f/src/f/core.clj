(ns f.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]))

;; depth-first traversal

(count (file-seq (io/file "/usr/share/man"))) ; 26446

(->> (io/file "/etc")
     (file-seq)
     (map (memfn getPath))
     (take 10))
; ("/etc"
;  "/etc/passwd-"
;  "/etc/vconsole.conf"
;  "/etc/environment"
;  "/etc/ostree-mkinitcpio.conf"
;  "/etc/resolv.conf"
;  "/etc/cifs-utils"
;  "/etc/cifs-utils/idmap-plugin"
;  "/etc/gprofng.rc"
;  "/etc/depmod.d")

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def work-dir      (file-seq (java.io.File. ".")))
(def abstract-path (file-seq (java.io.File. "")))
(def non-existent  (file-seq (java.io.File. "NONE")))

(.getAbsolutePath (first work-dir))      ; "/home/schmidh/Gitrepos/Clojure-The-Essential-Reference/09 - Sequences/3 - Other Generators/3 - file-seq/f/."
(.getAbsolutePath (first abstract-path)) ; "/home/schmidh/Gitrepos/Clojure-The-Essential-Reference/09 - Sequences/3 - Other Generators/3 - file-seq/f"
(.getAbsolutePath (first non-existent))  ; "/home/schmidh/Gitrepos/Clojure-The-Essential-Reference/09 - Sequences/3 - Other Generators/3 - file-seq/f/NONE"

(count work-dir)      ; 335
(count abstract-path) ; 335
(count abstract-path) ; 335

(defn grep-by-type [query ext]
  (sequence
   (comp
    (remove (memfn isDirectory))
    (map    (memfn getAbsolutePath))
    (filter #(= ext (last (str/split % #"\."))))
    (filter #(str/includes? (slurp %) query)))
   (file-seq (java.io.File. "."))))

(grep-by-type "file-seq" "clj")
; ("/home/schmidh/Gitrepos/Clojure-The-Essential-Reference/09 - Sequences/3 - Other Generators/3 - file-seq/f/./src/f/4.clj"
;  "/home/schmidh/Gitrepos/Clojure-The-Essential-Reference/09 - Sequences/3 - Other Generators/3 - file-seq/f/./src/f/core.clj")

(ns f.core
  (:require
   [clojure.java.javadoc :refer [*remote-javadocs* add-remote-javadoc javadoc]]
   [clojure.repl :refer [dir-fn]]))

;; NOTE: updating remote javadocs doesn't work

;; Advice
;; 1. Live with the old documentation
;; 2. Use Google
;; 3. Use Zeal

(dir-fn 'clojure.java.javadoc)
; (*core-java-api*
;  *feeling-lucky*
;  *feeling-lucky-url*
;  *local-javadocs*
;  *remote-javadocs*
;  add-local-javadoc
;  add-remote-javadoc
;  javadoc)

(comment
  ;; open browser with Java documentation for "Class String"
  (javadoc "this is a string object") ; true
  (javadoc String))                   ; true

;; Unfortunately, `javadoc` opens by default old documentation.
;; `javadoc` knows about Java 8 at the time of writing.

;; Current Java version is 25!
;; $ java --version
;; openjdk 25.0.1 2025-10-21
;; OpenJDK Runtime Environment (build 25.0.1)
;; OpenJDK 64-Bit Server VM (build 25.0.1, mixed mode, sharing)

(defn java-version []
  (let [jsv (System/getProperty "java.specification.version")]
    (if-let [single-digit (last (re-find #"^\d\.(\d+).*" jsv))]
      single-digit jsv)))

(comment
  (java-version)) ; "25"

(def jdocs-template
  (format "https://docs.oracle.com/javase/%s/docs/api/" (java-version)))
; "https://docs.oracle.com/javase/25/docs/api/"

(def known-prefix ["java." "javax." "org.ietf.jgss." "org.omg." "org.w3c.dom." "org.xml.sax."])
; ["java."
;  "javax."
;  "org.ietf.jgss."
;  "org.omg."
;  "org.w3c.dom."
;  "org.xml.sax."]

(comment
  (doseq [prefix known-prefix]
    (add-remote-javadoc prefix jdocs-template))

  @*remote-javadocs*
; {"com.google.common."        "http://google.github.io/guava/releases/23.0/api/docs/",
;  "org.apache.commons.codec." "http://commons.apache.org/proper/commons-codec/apidocs/",
;  "org.apache.commons.io."    "http://commons.apache.org/proper/commons-io/javadocs/api-release/",
;  "org.apache.commons.lang."  "http://commons.apache.org/proper/commons-lang/javadocs/api-2.6/",
;  "org.apache.commons.lang3." "http://commons.apache.org/proper/commons-lang/javadocs/api-release/",
;  "java."                     "https://docs.oracle.com/javase/25/docs/api/",
;  "javax."                    "https://docs.oracle.com/javase/25/docs/api/",
;  "org.ietf.jgss."            "https://docs.oracle.com/javase/25/docs/api/",
;  "org.omg."                  "https://docs.oracle.com/javase/25/docs/api/",
;  "org.w3c.dom."              "https://docs.oracle.com/javase/25/docs/api/",
;  "org.xml.sax."              "https://docs.oracle.com/javase/25/docs/api/"}

  (javadoc "this is a string object"))

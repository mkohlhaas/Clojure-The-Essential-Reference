(ns f.core
  (:import
   [java.util LinkedHashMap]))

(reduce
 (fn [m [k v]] (assoc m k (inc v)))
 {}
 {:a 1 :b 2 :c 3})
; {:a 2, :b 3, :c 4}

;; `reduce-kv` is dedicated to associative data structures
;; no complicated destructuring needed
(reduce-kv
 (fn [m k v] (assoc m k (inc v)))
 {}
 {:a 1 :b 2 :c 3})
; {:a 2, :b 3, :c 4}

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

(def env
  {"TERM_PROGRAM" "iTerm.app"
   "SHELL"        "/bin/bash"
   "COMMAND_MODE" "Unix2003"})

(defn transform [^String s]
  (some-> s
          .toLowerCase
          (.replace "_" "-")
          keyword))

(comment
  (transform "TERM_PROGRAM")  ; :term-program
  (transform "COMMAND_MODE")) ; :command-mode

(reduce-kv
 (fn [m k v] (assoc m (transform k) v))
 {}
 env)
; {:term-program "iTerm.app",
;  :shell        "/bin/bash",
;  :command-mode "Unix2003"}

(comment
  (reduce-kv
   (fn [m k v] (assoc m (transform k) v))
   {}
   (System/getenv)))
  ;; IllegalArgumentException No implementation of method: :kv-reduce of protocol: #'clojure.core.protocols/IKVReduce found for class: java.util.Collections$UnmodifiableMap

(comment
  (type (System/getenv))) ; java.util.Collections$UnmodifiableMap

#_{:clj-kondo/ignore [:unresolved-namespace]}

(extend-protocol clojure.core.protocols/IKVReduce
  java.util.Map ; java.util.Collections$UnmodifiableMap extends java.util.Map 
  (kv-reduce [m f init]
    (let [iter (.. m entrySet iterator)]
      (loop [ret init]
        (if (.hasNext iter)
          (let [^java.util.Map$Entry kv (.next iter)]
            (recur (f ret (.getKey kv) (.getValue kv))))
          ret)))))

;; now it works
(reduce-kv
 (fn [m k v] (assoc m (transform k) v))
 {}
 (System/getenv))
; {:lein-java-cmd "java",
;  :path "/home/schmidh/.asdf/shims:/home/schmidh/.local/bin:/home/schmidh/node_modules/.bin/:/usr/local/bin:/usr/bin:/usr/local/sbin:/var/lib/flatpak/exports/bin:/usr/lib/jvm/default/bin:/usr/bin/site_perl:/usr/bin/vendor_perl:/usr/bin/core_perl:/usr/lib/rustup/bin:/home/schmidh/.local/bin:/home/schmidh/bin:/home/schmidh/.local/share/gem/ruby/3.0.0/bin:/home/schmidh/.local/bin:/home/schmidh/.local/share/nvim/mason/bin:/home/schmidh/.local/share/vcpkg",
;  :xdg-session-type "wayland",
;  :classpath "/usr/share/java/leiningen-2.11.2-standalone.jar",
;  :xdg-session-desktop "gnome",
;  …
;  :xauthority "/run/user/1000/.mutter-Xwaylandauth.JCD3F3"}

;; works also with properties
(reduce-kv
 (fn [m k v] (assoc m (transform k) v))
 {}
 (System/getProperties))
; {:java.vendor.url.bug "https://bugreport.java.com/bugreport/",
;  :jdk.debug "release",
;  :java.vm.name "OpenJDK 64-Bit Server VM",
;  :java.vm.compressedoopsmode "Zero based",
;  :java.vm.version "25.0.1",}
;  …
; }

(comment
  (type (System/getProperties))) ; java.util.Properties (implements java.util.Map)

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; `reduce-kv` and the `reduced` convention
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(reduce-kv
 (fn [m k v]
   (if (> k 2)
     (reduced m)
     (assoc m k v)))
 {}
 [:a :b :c :d :e])
; {0 :a, 1 :b, 2 :c}

(comment
  ;; `reduced` doesn't work with LinkedHashMaps
  (reduce-kv
   (fn [m k v]
     (if (= k :abort)
       (reduced m)
       (assoc m k v)))
   {}
   (LinkedHashMap. {:a 1 :abort true :c 3})))
  ; (err) Execution error (ClassCastException)

(comment
  ;; respecting the reduced convention
  #_{:clj-kondo/ignore [:unresolved-namespace]}
  (extend-protocol clojure.core.protocols/IKVReduce
    java.util.Map
    (kv-reduce [m f init]
      (let [iter (.. m entrySet iterator)]
        (loop [ret init]
          (if (.hasNext iter)
            (let [^java.util.Map$Entry kv (.next iter)
                  ret (f ret (.getKey kv) (.getValue kv))]
              (if (reduced? ret)                            ; handle an element wrapped in a reduced object
                @ret                                        ; return immediately
                (recur ret)))
            ret)))))

  (reduce-kv
   (fn [m k v]
     (if (= k :abort)
       (reduced m)
       (assoc m k v)))
   {}
   (LinkedHashMap. {:a 1 :abort true :c 3})))
    ; {:a 1}

(ns f.core
  ;; (:require [clojure.core.protocols IKVReduce])
  (:import [java.util LinkedHashMap HashMap]))

(reduce
 (fn [m [k v]] (assoc m k (inc v)))
 {}
 {:a 1 :b 2 :c 3})
; {:a 2, :b 3, :c 4}

(reduce-kv
 (fn [m k v] (assoc m k (inc v)))
 {}
 {:a 1 :b 2 :c 3})
; {:a 2, :b 3, :c 4}

(def env
  {"TERM_PROGRAM" "iTerm.app"
   "SHELL"        "/bin/bash"
   "COMMAND_MODE" "Unix2003"})

(defn transform [^String s]
  (some-> s
          .toLowerCase
          (.replace "_" "-")
          keyword))

(reduce-kv
 (fn [m k v] (assoc m (transform k) v)) {} env)
; {:term-program "iTerm.app",
;  :shell "/bin/bash",
;  :command-mode "Unix2003"}

(reduce-kv
 (fn [m k v] (assoc m (transform k) v))
 {}
 (System/getenv))
; {:lein-java-cmd "java",
;  :path "/home/schmidh/.asdf/shims:/home/schmidh/.local/bin:/home/schmidh/node_modules/.bin/:/usr/local/bin:/usr/bin:/usr/local/sbin:/var/lib/flatpak/exports/bin:/usr/lib/jvm/default/bin:/usr/bin/site_perl:/usr/bin/vendor_perl:/usr/bin/core_perl:/usr/lib/rustup/bin:/home/schmidh/.local/bin:/home/schmidh/bin:/home/schmidh/.local/share/gem/ruby/3.0.0/bin:/home/schmidh/.local/bin:/home/schmidh/.local/share/nvim/mason/bin:/home/schmidh/.local/share/vcpkg",
;  :xdg-session-type "wayland",
;  :classpath "/usr/share/java/leiningen-2.11.2-standalone.jar",
;  :xdg-session-desktop "gnome",
; …
;  :xauthority "/run/user/1000/.mutter-Xwaylandauth.JCD3F3"}

;; IllegalArgumentException No implementation of method: :kv-reduce of protocol: #'clojure.core.protocols/IKVReduce found for class: java.util.Collections$UnmodifiableMap

(comment
  (extend-protocol clojure.core.protocols/IKVReduce
    java.util.Map
    (kv-reduce [m f init]
      (let [iter (.. m entrySet iterator)]
        (loop [ret init]
          (if (.hasNext iter)
            (let [^java.util.Map$Entry kv (.next iter)]
              (recur (f ret (.getKey kv) (.getValue kv))))
            ret))))))

(reduce-kv
 (fn [m k v] (assoc m (transform k) v))
 {}
 (System/getenv))

;; {:jenv-version "oracle64-1.8.0.121",
;;  :tmux "/private/tmp/tmux-502/default,2685,2",
;;  :term-program-version "3.1.5",
;;  :github-username "reborg"
;;  ...}

(reduce-kv
 (fn [m k v] (assoc m (transform k) v))
 {}
 (System/getProperties))

;; {:java.vm.version "25.121-b13",
;;  :java.specification.name "Java Platform API Specification",
;;  :java.io.tmpdir "/var/folders/25/T/",
;;  :java.runtime.name "Java(TM) SE Runtime Environment",
;;  ...}

(reduce-kv
 (fn [m k v]
   (if (> k 2)
     (reduced m)
     (assoc m k v)))
 {}
 [:a :b :c :d :e])
; {0 :a, 1 :b, 2 :c}

(reduce-kv
 (fn [m k v]
   (if (= k :abort)
     (reduced m)
     (assoc m k v)))
 {}
 (LinkedHashMap. {:a 1 :abort true :c 3}))
; {:a 1}

;; ClassCastException clojure.lang.Reduced cannot be cast to clojure.lang.Associative

(comment
  (extend-protocol clojure.core.protocols/IKVReduce
    java.util.Map
    (kv-reduce [m f init]
      (let [iter (.. m entrySet iterator)]
        (loop [ret init]
          (if (.hasNext iter)
            (let [^java.util.Map$Entry kv (.next iter)
                  ret (f ret (.getKey kv) (.getValue kv))]
              (if (reduced? ret) ; <1>
                @ret
                (recur ret)))
            ret))))))

(reduce-kv
 (fn [m k v]
   (if (= k :abort)
     (reduced m)
     (assoc m k v)))
 {}
 (LinkedHashMap. {:a 1 :abort true :c 3}))
; {:a 1}

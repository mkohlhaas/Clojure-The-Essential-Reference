(ns f.core)

;; - there is no mechanism to coordinate different atom instances
;; - `atom` was introduced to implement the frequent use case of protecting a single reference from concurrent access 
;; - the concept of "transaction" still exists for `atom`, but it's implicit and delimited by the update function itself (`swap!`)

;; other functions in this section:
;; - `swap!`
;; - `reset!`
;; - `compare-and-set!`

(def a (atom 0))
(swap! a inc) ; 1
@a            ; 1

;; ;;;;;
;; swap!
;; ;;;;;

(def m (atom {:a 1 :b {:c 2}}))

(swap! m (fn [m] (update-in m [:b :c] inc))) ; {:a 1 :b {:c 3}}
(swap! m update-in [:b :c] inc)              ; {:a 1 :b {:c 4}} (more concise)

;; ;;;;;;
;; reset!
;; ;;;;;;

;; `reset!` always evaluates without retries

(def configuration (atom {}))

@configuration ; {}

(defn initialize []
  (reset! configuration (System/getenv)))

(initialize)                   ; {"QT_IM_MODULES" "wayland;ibus", "SHASUM_CMD" "sha256sum", "MANAGERPIDFDID" "785", "KITTY_PID" "2372", "MAIL" "/var/spool/mail/schmidh", "TRAMPOLINE_FILE" "/tmp/lein-trampoline-oBekVgAl4RSN8", "HISTSIZE" "2147450879", "CLOJURE_HOME" "/usr/share/clojure", "PATH" "/home/schmidh/.asdf/shims:/home/schmidh/.local/bin:/home/schmidh/node_modules/.bin/:/usr/local/bin:/usr/bin:/usr/local/sbin:/var/lib/flatpak/exports/bin:/usr/lib/jvm/default/bin:/usr/bin/site_perl:/usr/bin/vendor_perl:/usr/bin/core_perl:/usr/lib/rustup/bin:/home/schmidh/.local/bin:/home/schmidh/bin:/home/schmidh/.local/share/gem/ruby/3.0.0/bin:/home/schmidh/.local/bin:/home/schmidh/.local/share/nvim/mason/bin:/home/schmidh/.local/share/vcpkg", "XDG_MENU_PREFIX" "gnome-", "LOGNAME" "schmidh", "TERMINFO" "/usr/lib/kitty/terminfo", "LS_COLORS" "rs=0:di=01;34:ln=01;36:mh=00:pi=40;33:so=01;35:do=01;35:bd=40;33;01:cd=40;33;01:or=40;31;01:mi=00:su=37;41:sg=30;43:ca=00:tw=30;42:ow=34;42:st=37;44:ex=01;32:*.7z=01;31:*.ace=01;31:*.alz=01;31:*.apk=01;31:*.arc=01;31:*.arj=01;31:*.bz=01;31:*.bz2=01;31:*.cab=01;31:*.cpio=01;31:*.crate=01;31:*.deb=01;31:*.drpm=01;31:*.dwm=01;31:*.dz=01;31:*.ear=01;31:*.egg=01;31:*.esd=01;31:*.gz=01;31:*.jar=01;31:*.lha=01;31:*.lrz=01;31:*.lz=01;31:*.lz4=01;31:*.lzh=01;31:*.lzma=01;31:*.lzo=01;31:*.pyz=01;31:*.rar=01;31:*.rpm=01;31:*.rz=01;31:*.sar=01;31:*.swm=01;31:*.t7z=01;31:*.tar=01;31:*.taz=01;31:*.tbz=01;31:*.tbz2=01;31:*.tgz=01;31:*.tlz=01;31:*.txz=01;31:*.tz=01;31:*.tzo=01;31:*.tzst=01;31:*.udeb=01;31:*.war=01;31:*.whl=01;31:*.wim=01;31:*.xz=01;31:*.z=01;31:*.zip=01;31:*.zoo=01;31:*.zst=01;31:*.avif=01;35:*.jpg=01;35:*.jpeg=01;35:*.jxl=01;35:*.mjpg=01;35:*.mjpeg=01;35:*.gif=01;35:*.bmp=01;35:*.pbm=01;35:*.pgm=01;35:*.ppm=01;35:*.tga=01;35:*.xbm=01;35:*.xpm=01;35:*.tif=01;35:*.tiff=01;35:*.png=01;35:*.svg=01;35:*.svgz=01;35:*.mng=01;35:*.pcx=01;35:*.mov=01;35:*.mpg=01;35:*.mpeg=01;35:*.m2v=01;35:*.mkv=01;35:*.webm=01;35:*.webp=01;35:*.ogm=01;35:*.mp4=01;35:*.m4v=01;35:*.mp4v=01;35:*.vob=01;35:*.qt=01;35:*.nuv=01;35:*.wmv=01;35:*.asf=01;35:*.rm=01;35:*.rmvb=01;35:*.flc=01;35:*.avi=01;35:*.fli=01;35:*.flv=01;35:*.gl=01;35:*.dl=01;35:*.xcf=01;35:*.xwd=01;35:*.yuv=01;35:*.cgm=01;35:*.emf=01;35:*.ogv=01;35:*.ogx=01;35:*.aac=00;36:*.au=00;36:*.flac=00;36:*.m4a=00;36:*.mid=00;36:*.midi=00;36:*.mka=00;36:*.mp3=00;36:*.mpc=00;36:*.ogg=00;36:*.ra=00;36:*.wav=00;36:*.oga=00;36:*.opus=00;36:*.spx=00;36:*.xspf=00;36:*~=00;90:*#=00;90:*.bak=00;90:*.crdownload=00;90:*.dpkg-dist=00;90:*.dpkg-new=00;90:*.dpkg-old=00;90:*.dpkg-tmp=00;90:*.old=00;90:*.orig=00;90:*.part=00;90:*.rej=00;90:*.rpmnew=00;90:*.rpmorig=00;90:*.rpmsave=00;90:*.swp=00;90:*.tmp=00;90:*.ucf-dist=00;90:*.ucf-new=00;90:*.ucf-old=00;90:", "GDM_LANG" "en_US.UTF-8", "WAYLAND_DISPLAY" "wayland-0", "NVM_DIR" "/home/schmidh/.nvm", "XAUTHORITY" "/run/user/1000/.mutter-Xwaylandauth.R6EPG3", "LEIN_VERSION" "2.12.0", "LEIN_JVM_OPTS" "-Xbootclasspath/a:/usr/share/java/leiningen-2.12.0-standalone.jar -XX:+TieredCompilation -XX:TieredStopAtLevel=1", "LESS" "-R", "ENVMAN_LOAD" "loaded", "LSCOLORS" "Gxfxcxdxdxegedabagacad", "XMODIFIERS" "@im=ibus", "GIO_LAUNCHED_DESKTOP_FILE_PID" "2372", "MOTD_SHOWN" "pam", "KITTY_PUBLIC_KEY" "1:7Jf8Gt7D<gZJh+3L(2e+j({C0{_m7=C|qH4nM7qa", "KITTY_INSTALLATION_DIR" "/usr/lib/kitty", "XDG_SESSION_DESKTOP" "gnome", "DBUS_SESSION_BUS_ADDRESS" "unix:path=/run/user/1000/bus", "GNOME_SETUP_DISPLAY" ":1", "INVOCATION_ID" "1d13c4aa455b499e9b86f8dc51ef8588", "OSH" "/home/schmidh/.oh-my-bash", "SHLVL" "2", "USERNAME" "schmidh", "CLASSPATH" "/usr/share/java/leiningen-2.12.0-standalone.jar", "XDG_DATA_DIRS" "/home/schmidh/.local/share/flatpak/exports/share:/var/lib/flatpak/exports/share:/usr/local/share/:/usr/share/", "SHELL" "/usr/bin/bash", "VCPKG_DISABLE_METRICS" "TRUE", "COLORTERM" "truecolor", "XDG_SESSION_CLASS" "user", "DISPLAY" ":0", "HOME" "/home/schmidh", "MEMORY_PRESSURE_WATCH" "/sys/fs/cgroup/user.slice/user-1000.slice/user@1000.service/session.slice/org.gnome.SettingsDaemon.MediaKeys.service/memory.pressure", "XDG_CURRENT_DESKTOP" "GNOME", "LEIN_HOME" "/home/schmidh/.lein", "VCPKG_ROOT" "/home/schmidh/.local/share/vcpkg", "DEBUGINFOD_URLS" "https://debuginfod.archlinux.org ", "TERM" "xterm-kitty", "QT_IM_MODULE" "ibus", "LANG" "en_US.UTF-8", "GDMSESSION" "gnome", "LAUNCH_EDITOR" "editor-launcher.sh", "GNOME_KEYRING_CONTROL" "/run/user/1000/keyring", "LC_CTYPE" "en_US.UTF-8", "JVM_OPTS" "", "KITTY_WINDOW_ID" "13", "MEMORY_PRESSURE_WRITE" "c29tZSAyMDAwMDAgMjAwMDAwMAA=", "SYSTEMD_EXEC_PID" "1006", "XDG_RUNTIME_DIR" "/run/user/1000", "SSH_AUTH_SOCK" "/run/user/1000/gcr/ssh", "HISTFILESIZE" "2147450879", "LEIN_JAVA_CMD" "java", "PAGER" "less", "MANAGERPID" "784", "DESKTOP_SESSION" "gnome", "USER" "schmidh", "XDG_SESSION_TYPE" "wayland", "PWD" "/home/schmidh/Gitrepos/Clojure/Clojure-The-Essential-Reference/14 - Concurrency/05 - atom/f", "_" "/usr/bin/java", "JOURNAL_STREAM" "9:10125"}
(take 3 (keys @configuration)) ; ("QT_IM_MODULES" "SHASUM_CMD" "MANAGERPIDFDID")

;; ;;;;;;;;;;;;;;;;
;; compare-and-set!
;; ;;;;;;;;;;;;;;;;

(defn swap-or-bail! [a f & [attempts]]
  (loop [i (or attempts 3)]
    (if (zero? i)
      (println "Could not update. Bailing out.")
      (let [old      (deref a)
            success? (compare-and-set! a old (f old))]
        (when-not success?
          (println "Update failed. Retry" i)
          (recur (dec i)))))))

(defn slow-inc [x]
  (Thread/sleep 5000)
  (inc x))

#_{:clj-kondo/ignore [:redefined-var]}
(def a (atom 0))
(future (swap-or-bail! a slow-inc)) ; start a new thread
(reset! a 1)
(reset! a 2)
(reset! a 3)
; (out) Update failed. Retry 3
; (out) Update failed. Retry 2
; (out) Update failed. Retry 1
; (out) Could not update. Bailing out.

;; You should avoid calling compare-and-set! with a value that is not coming from the same atom instance you’re trying to update!!! 
(def a1 (atom 127))
(compare-and-set! a1 127 128) ; true
(compare-and-set! a1 127 128) ; false
(compare-and-set! a1 128 129) ; false

;; Long values from -127 to 127 are cached so new Long(127) == new Long(127) is true because the two numbers are effectively the same instance.
;; But new Long(128) == new Long(128) is false in Java because the two objects are effectively different instances (as there is no implicit caching).
;; Clojure wraps numerical arguments into a new java.lang.Long instance, resulting in the observed compare-and-set! behavior.

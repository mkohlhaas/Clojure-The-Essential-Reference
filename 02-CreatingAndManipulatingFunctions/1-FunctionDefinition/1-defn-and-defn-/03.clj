(macroexpand
 '(defn hello [person]
    (str "hello " person)))

;; (def hello
;;   (clojure.core/fn
;;     ([person] (str "hello " person))))

(hello "people") ; "hello people"

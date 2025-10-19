(ns f.core
  (:require
   [clojure.repl   :refer [source-fn]]
   [clojure.string :as s]))

(letfn [(square [x] (* x x))]
  (map square (range 10)))
; (0 1 4 9 16 25 36 49 64 81)

;; letrec-like behavior
;; mutual recursive functions
(letfn [(is-even? [n] (or (zero? n) #(is-odd? (dec n))))           ; is-odd? not yet defined but already usable
        (is-odd?  [n] (and (not (zero? n)) #(is-even? (dec n))))]
  (trampoline is-odd? 121))
; true

;; ;;;;;;;;;;;;;
;; Lines of Code
;; ;;;;;;;;;;;;;

(defn locs-xform [match-ns]
  (comp
   (filter (fn [ns]
             (re-find
              (re-pattern match-ns)
              (str (ns-name ns)))))
   (map ns-interns)
   (mapcat vals)
   (map meta)
   (map (fn [{:keys [ns name]}]
          (symbol (str ns) (str name))))
   (map (juxt identity
              (fn [sym]
                (count
                 (s/split-lines
                  (or (source-fn sym) ""))))))))

(comment
  (-> (source-fn 'filter)
      s/split-lines
      count)
  ; 31

  (count (s/split-lines (source-fn 'filter)))
  ; 31

  (take 5 (sequence (locs-xform "clojure.core") (all-ns))))
; ([clojure.core.rrb-vector.transients/transient-helper 267]
;  [clojure.core.server/accept-connection 26]
;  [clojure.core.server/validate-opts 6]
;  [clojure.core.server/lock 1]
;  [clojure.core.server/required 5])

;; top - by default - 10 longest functions in a matching namespace
(defn top-locs
  ([match-ns] (top-locs match-ns 10))
  ([match-ns n]
   (->>
    (all-ns)
    (sequence (locs-xform match-ns))
    (sort-by last >)
    (take n))))

(top-locs "clojure.core" 5)
; ([clojure.core.rrb-vector.rrbt/->Vector 761]
;  [clojure.core/generate-class 382]
;  [clojure.core/->Vec 323]
;  [clojure.core.rrb-vector.transients/transient-helper 267]
;  [clojure.core.rrb-vector.rrbt/->Transient 264])

;; cleaning up that mess
#_{:clj-kondo/ignore [:redefined-var]}
(defn locs-xform [match-ns]
  (letfn [(matching? [ns]
            (re-find
             (re-pattern match-ns)
             (str (ns-name ns))))
          (var->sym [{:keys [ns name]}]
            (symbol (str ns) (str name)))
          (count-lines [fsym]
            (count
             (clojure.string/split-lines
              (or (clojure.repl/source-fn fsym) ""))))]
    (comp
     (filter matching?)
     (map ns-interns)
     (mapcat vals)
     (map meta)
     (map var->sym)
     (map (juxt identity count-lines)))))

(top-locs "clojure.core" 5)
; ([clojure.core.rrb-vector.rrbt/->Vector 761]
;  [clojure.core/generate-class 382]
;  [clojure.core/->Vec 323]
;  [clojure.core.rrb-vector.transients/transient-helper 267]
;  [clojure.core.rrb-vector.rrbt/->Transient 264])

(comment
  (all-ns)
; (#object[clojure.lang.Namespace 0x37bd4ebe "clojure.spec.gen.alpha"]
;  #object[clojure.lang.Namespace 0x7f453251 "nrepl.middleware.interruptible-eval"]
;  #object[clojure.lang.Namespace 0x3668ac33 "cider.nrepl.pprint"]
;  #object[clojure.lang.Namespace 0x1b82d7a5 "clojure.stacktrace"]
;  #object[clojure.lang.Namespace 0x109cefec "clojure.core.rrb-vector.transients"]
;  #object[clojure.lang.Namespace 0x3eb58f4f "clojure.uuid"]
;  #object[clojure.lang.Namespace 0x4d4fb59e "clojure.main"]
;  #object[clojure.lang.Namespace 0x40fd6c6d "user"]
;  #object[clojure.lang.Namespace 0x37e90811 "clojure.test"]
;  #object[clojure.lang.Namespace 0x25b42654 "clojure.data"]
;  #object[clojure.lang.Namespace 0x15e78439 "nrepl.middleware.dynamic-loader"]
;  #object[clojure.lang.Namespace 0x7cab8c03 "fipp.ednize"])
;  …

  (->>
   (all-ns)
   (sequence (locs-xform "clojure.core")))

;; ([clojure.core.rrb-vector.transients/transient-helper 267]
;;  [clojure.core.server/accept-connection 26]
;;  [clojure.core.server/validate-opts 6]
;;  [clojure.core.server/lock 1]
;;  [clojure.core.server/required 5]
;;  [clojure.core.server/with-lock 8]
;;  [clojure.core.server/stop-server 13]
;;  [clojure.core.server/repl-init 5]
;;  [clojure.core.server/start-server 40]
;;  [clojure.core.server/start-servers 5]
;;  [clojure.core.server/stop-servers 6])
;;  …

  (->>
   (all-ns)
   (sequence (locs-xform "clojure.core"))
   (sort-by last >))
; ([clojure.core.rrb-vector.rrbt/->Vector 761]
;  [clojure.core/generate-class 382]
;  [clojure.core/->Vec 323]
;  [clojure.core.rrb-vector.transients/transient-helper 267]
;  [clojure.core.rrb-vector.rrbt/->Transient 264]
;  [clojure.core/generate-proxy 227]
;  [clojure.core.rrb-vector.rrbt/->VecSeq 223]
;  [clojure.core/gen-class 132]
;  [clojure.core/emit-defrecord 117]
;  [clojure.core/->VecSeq 107]
;  [clojure.core.rrb-vector.rrbt/slice-left 103]
;  [clojure.core/destructure 95]
;  [clojure.core/defrecord 92]
;  [clojure.core/for 86]
;  …

  (->>
   (all-ns)
   (sequence (locs-xform "clojure.core"))
   (sort-by last >)
   (take 10)))
; ([clojure.core.rrb-vector.rrbt/->Vector 761]
;  [clojure.core/generate-class 382]
;  [clojure.core/->Vec 323]
;  [clojure.core.rrb-vector.transients/transient-helper 267]
;  [clojure.core.rrb-vector.rrbt/->Transient 264]
;  [clojure.core/generate-proxy 227]
;  [clojure.core.rrb-vector.rrbt/->VecSeq 223]
;  [clojure.core/gen-class 132]
;  [clojure.core/emit-defrecord 117]
;  [clojure.core/->VecSeq 107])

;; ;;;;;;;;;
;; Shadowing
;; ;;;;;;;;;

;; no shadowing with let:
(let [a (fn [n] (* 2 n))]
  (let [a (fn [n] (+ 3 (a n)))] ; `(a n)` refers to 1st let
    (a 2)))                     ; refers to 2nd let
; 7

;; shadowing with letfn:
(comment
  (letfn [(a [n] (* 2 n))]
    (letfn [(a [n] (+ 3 (a n)))] ; calling itself into oblivion
      (a 2))))
  ; (err) java.lang.StackOverflowError

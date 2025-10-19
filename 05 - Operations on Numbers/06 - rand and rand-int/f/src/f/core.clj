(ns f.core
  (:import
   java.awt.image.BufferedImage
   java.io.File
   java.util.Random
   javax.imageio.ImageIO))

(rand)         ; 0.2575534079749804
(rand-int -10) ; -6

;; ;;;;;;;;;;;
;; Quesionaire
;; ;;;;;;;;;;;

(def questions
  [["What is your current Clojure flavor?" ["Clojure" "ClojureScript" "CLR"]]
   ["What language were you using before?" ["Java" "C#" "Ruby"]]
   ["What is your company size?"           ["1-10" "11-100" "100+"]]])

(defn format-options [options]
  (apply str (map-indexed #(format "[%s] %s " (inc %1) %2) options)))

(comment
  (format-options ["Java" "C#" "Ruby"])) ; "[1] Java [2] C# [3] Ruby "

(defn render [[question options] prefix]
  (str "Q" prefix question " " (format-options options)))

(comment
  (render ["What is your current Clojure flavor?" ["Clojure" "ClojureScript" "CLR"]] "(3/1): "))
  ; "Q(3/1): What is your current Clojure flavor? [1] Clojure [2] ClojureScript [3] CLR "

;; A/B Testing
(defn A-B [prob A B]
  (if (< prob (rand)) A B))

(defn progress-handler [total]
  (A-B 0.5
       (fn [progress] (format "(%s/%s): " total progress))
       (constantly ": ")))

(defn ask [questions]
  (let [progress (progress-handler (count questions))]
    (loop [[q & more-questions] questions
           answers []]
      (if q
        (do
          (println (render q (progress (inc (count answers)))))
          (recur more-questions (conj answers (read))))
        answers))))

(comment
  (ask questions))
  ;; Q(3/1): What is your current Clojure flavor? [1] Clojure [2] ClojureScript [3] CLR
  ;; 2
  ;; Q(3/2): What language were you using before? [1] Java [2] C# [3] Ruby
  ;; 1
  ;; Q(3/3): What is your company size? [1] 1-10 [2] 11-100 [3] 100+
  ;; 3
  ;; [2 1 3]

  ;; or:
  ;; Q: What is your current Clojure flavor? [1] Clojure [2] ClojureScript [3] CLR 
  ;; 1
  ;; Q: What language were you using before? [1] Java [2] C# [3] Ruby 
  ;; 2
  ;; Q: What is your company size? [1] 1-10 [2] 11-100 [3] 100+ 
  ;; 3
  ;; [1 2 3]

;; ;;;;;;;;;;;;
;; Random Image
;; ;;;;;;;;;;;;

(def ^:const width  256)
(def ^:const height 256)

(def ^:const black 0xffffff)
(def ^:const white 0x000000)

(defn coords [x y]
  (for [m (range x)
        n (range y)]
    [m n]))

(comment
  (coords 3 2)) ; ([0 0] [0 1] [1 0] [1 1] [2 0] [2 1])

(defn save! [img]
  (ImageIO/write img "PNG" (File. "/tmp/rand-noise.png")))

(defn rand-pixel [r]
  (if (== 0 (bit-and (.nextInt r) 1))
    white
    black))

(defn a []
  (let [r (Random.)
        img (BufferedImage. width height BufferedImage/TYPE_BYTE_BINARY)]
    (doseq [[x y] (coords width height)]
      (.setRGB img x y (rand-pixel r)))
    (save! img)))

;; picture shows artefacts
(a)

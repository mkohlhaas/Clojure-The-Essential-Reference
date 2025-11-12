(ns f.core
  (:require
   [clojure.walk :as w])
  (:import
   [java.util ArrayList]))

(flatten [[1 2 [2 3] '(:x :y [nil []])]]) ; (1 2 2 3 :x :y nil)

;; ;;;;;;;;
;; Examples
;; ;;;;;;;;

;; maps and sets are not flattened
(flatten [[{:a 1} #{2 3} (doto (ArrayList.) (.add 1) (.add 2))]])  ; ({:a 1} #{3 2} [1 2])

(comment
  (doto (ArrayList.) (.add 1) (.add 2))) ; [1 2]

;; extract clojure.core functions
(defn core-fns [form]
  (->> (w/macroexpand-all form)
       flatten
       (map str)
       (map #(re-find #"clojure\.core/(.*)" %))
       (keep last)
       distinct
       sort))

(core-fns
 '(for [[head & others] coll
        :while #(< i %)
        :let [a (mod i 2)]]
    (when (zero? a)
      (doseq [item others]
        (print item)))))
; ("<"
;  "chunk"
;  "chunk-append"
;  "chunk-buffer"
;  "chunk-cons"
;  "chunk-first"
;  "chunk-rest"
;  "chunked-seq?"
;  "cons"
;  "count"
;  "first"
;  "int"
;  "next"
;  "rest"
;  "seq"
;  "unchecked-inc")

;; w/macroexpand-all:                                                           ;; flatten:                    
;; (clojure.core/let                                                            ;; (let*                       
;;  [iter__6373__auto__                                                         ;;  iter__6373__auto__         
;;   (clojure.core/fn                                                           ;;  fn*                        
;;    iter__4237                                                                ;;  iter__4245                 
;;    [s__4238]                                                                 ;;  s__4246                    
;;    (clojure.core/lazy-seq                                                    ;;  new                        
;;     (clojure.core/loop                                                       ;;  clojure.lang.LazySeq       
;;      [s__4238 s__4238]                                                       ;;  fn*                        
;;      (clojure.core/when-let                                                  ;;  loop*                      
;;       [s__4238 (clojure.core/seq s__4238)]                                   ;;  s__4246                    
;;       (if                                                                    ;;  s__4246                    
;;        (clojure.core/chunked-seq? s__4238)                                   ;;  let*                       
;;        (clojure.core/let                                                     ;;  temp__5804__auto__         
;;         [c__6371__auto__                                                     ;;  clojure.core/seq           
;;          (clojure.core/chunk-first s__4238)                                  ;;  s__4246                    
;;          size__6372__auto__                                                  ;;  if                         
;;          (clojure.core/int (clojure.core/count c__6371__auto__))             ;;  temp__5804__auto__         
;;          b__4240                                                             ;;  do                         
;;          (clojure.core/chunk-buffer size__6372__auto__)]                     ;;  let*                       
;;         (if                                                                  ;;  s__4246                    
;;          (clojure.core/loop                                                  ;;  temp__5804__auto__         
;;           [i__4239 (clojure.core/int 0)]                                     ;;  if                         
;;           (if                                                                ;;  clojure.core/chunked-seq?  
;;            (clojure.core/< i__4239 size__6372__auto__)                       ;;  s__4246                    
;;            (clojure.core/let                                                 ;;  let*                       
;;             [[head & others] (.nth c__6371__auto__ i__4239)]                 ;;  c__6371__auto__            
;;             (clojure.core/when                                               ;;  clojure.core/chunk-first   
;;              (fn* [p1__4234#] (< i p1__4234#))                               ;;  s__4246                    
;;              (clojure.core/let                                               ;;  size__6372__auto__         
;;               [a (mod i 2)]                                                  ;;  clojure.core/int           
;;               (do                                                            ;;  clojure.core/count         
;;                (clojure.core/chunk-append                                    ;;  c__6371__auto__            
;;                 b__4240                                                      ;;  b__4248                    
;;                 (when (zero? a) (doseq [item others] (print item))))         ;;  clojure.core/chunk-buffer  
;;                (recur (clojure.core/unchecked-inc i__4239))))))              ;;  size__6372__auto__         
;;            true))                                                            ;;  if                         
;;          (clojure.core/chunk-cons                                            ;;  loop*                      
;;           (clojure.core/chunk b__4240)                                       ;;  i__4247                    
;;           (iter__4237 (clojure.core/chunk-rest s__4238)))                    ;;  clojure.core/int           
;;          (clojure.core/chunk-cons (clojure.core/chunk b__4240) nil)))        ;;  0                          
;;        (clojure.core/let                                                     ;;  if                         
;;         [[head & others] (clojure.core/first s__4238)]                       ;;  clojure.core/<             
;;         (clojure.core/when                                                   ;;  i__4247                    
;;          (fn* [p1__4234#] (< i p1__4234#))                                   ;;  size__6372__auto__         
;;          (clojure.core/let                                                   ;;  let*                       
;;           [a (mod i 2)]                                                      ;;  vec__4249                  
;;           (clojure.core/cons                                                 ;;  .                          
;;            (when (zero? a) (doseq [item others] (print item)))               ;;  c__6371__auto__            
;;            (iter__4237 (clojure.core/rest s__4238)))))))))))]                ;;  nth                        
;;  (iter__6373__auto__ coll))                                                  ;;  i__4247                    
                                                                                ;;  seq__4250                  
                                                                                ;;  clojure.core/seq           
                                                                                ;;  vec__4249                  
                                                                                ;;  first__4251                
                                                                                ;;  clojure.core/first         
                                                                                ;;  seq__4250                  
                                                                                ;;  seq__4250                  
                                                                                ;;  clojure.core/next          
                                                                                ;;  seq__4250                  
                                                                                ;;  head                       
                                                                                ;;  first__4251                
                                                                                ;;  others                     
                                                                                ;;  seq__4250                  
                                                                                ;;  if                         
                                                                                ;;  fn*                        
                                                                                ;;  p1__4242#                  
                                                                                ;;  <                          
                                                                                ;;  i                          
                                                                                ;;  p1__4242#                  
                                                                                ;;  do                         
                                                                                ;;  let*                       
                                                                                ;;  a                          
                                                                                ;;  mod                        
                                                                                ;;  i                          
                                                                                ;;  2                          
                                                                                ;;  do                         
                                                                                ;;  clojure.core/chunk-append  
                                                                                ;;  b__4248                    
                                                                                ;;  if                         
                                                                                ;;  zero?                      
                                                                                ;;  a                          
                                                                                ;;  do                         
                                                                                ;;  loop*                      
                                                                                ;;  seq_4252                   
                                                                                ;;  clojure.core/seq           
                                                                                ;;  others                     
                                                                                ;;  chunk_4253                 
                                                                                ;;  nil                        
                                                                                ;;  count_4254                 
                                                                                ;;  0                          
                                                                                ;;  i_4255                     
                                                                                ;;  0                          
                                                                                ;;  if                         
                                                                                ;;  clojure.core/<             
                                                                                ;;  i_4255                     
                                                                                ;;  count_4254                 
                                                                                ;;  let*                       
                                                                                ;;  item                       
                                                                                ;;  .                          
                                                                                ;;  chunk_4253                 
                                                                                ;;  nth                        
                                                                                ;;  i_4255                     
                                                                                ;;  do                         
                                                                                ;;  print                      
                                                                                ;;  item                       
                                                                                ;;  recur                      
                                                                                ;;  seq_4252                   
                                                                                ;;  chunk_4253                 
                                                                                ;;  count_4254                 
                                                                                ;;  clojure.core/unchecked-inc 
                                                                                ;;  i_4255                     
                                                                                ;;  let*                       
                                                                                ;;  temp__5804__auto__         
                                                                                ;;  clojure.core/seq           
                                                                                ;;  seq_4252                   
                                                                                ;;  if                         
                                                                                ;;  temp__5804__auto__         
                                                                                ;;  do                         
                                                                                ;;  let*                       
                                                                                ;;  seq_4252                   
                                                                                ;;  temp__5804__auto__         
                                                                                ;;  if                         
                                                                                ;;  clojure.core/chunked-seq?  
                                                                                ;;  seq_4252                   
                                                                                ;;  let*                       
                                                                                ;;  c__6065__auto__            
                                                                                ;;  clojure.core/chunk-first   
                                                                                ;;  seq_4252                   
                                                                                ;;  recur                      
                                                                                ;;  clojure.core/chunk-rest    
                                                                                ;;  seq_4252                   
                                                                                ;;  c__6065__auto__            
                                                                                ;;  clojure.core/int           
                                                                                ;;  clojure.core/count         
                                                                                ;;  c__6065__auto__            
                                                                                ;;  clojure.core/int           
                                                                                ;;  0                          
                                                                                ;;  let*                       
                                                                                ;;  item                       
                                                                                ;;  clojure.core/first         
                                                                                ;;  seq_4252                   
                                                                                ;;  do                         
                                                                                ;;  print                      
                                                                                ;;  item                       
                                                                                ;;  recur                      
                                                                                ;;  clojure.core/next          
                                                                                ;;  seq_4252                   
                                                                                ;;  nil                        
                                                                                ;;  0                          
                                                                                ;;  0                          
                                                                                ;;  recur                      
                                                                                ;;  clojure.core/unchecked-inc 
                                                                                ;;  i__4247                    
                                                                                ;;  true                       
                                                                                ;;  clojure.core/chunk-cons    
                                                                                ;;  clojure.core/chunk         
                                                                                ;;  b__4248                    
                                                                                ;;  iter__4245                 
                                                                                ;;  clojure.core/chunk-rest    
                                                                                ;;  s__4246                    
                                                                                ;;  clojure.core/chunk-cons    
                                                                                ;;  clojure.core/chunk         
                                                                                ;;  b__4248                    
                                                                                ;;  nil                        
                                                                                ;;  let*                       
                                                                                ;;  vec__4256                  
                                                                                ;;  clojure.core/first         
                                                                                ;;  s__4246                    
                                                                                ;;  seq__4257                  
                                                                                ;;  clojure.core/seq           
                                                                                ;;  vec__4256                  
                                                                                ;;  first__4258                
                                                                                ;;  clojure.core/first         
                                                                                ;;  seq__4257                  
                                                                                ;;  seq__4257                  
                                                                                ;;  clojure.core/next          
                                                                                ;;  seq__4257                  
                                                                                ;;  head                       
                                                                                ;;  first__4258                
                                                                                ;;  others                     
                                                                                ;;  seq__4257                  
                                                                                ;;  if                         
                                                                                ;;  fn*                        
                                                                                ;;  p1__4242#                  
                                                                                ;;  <                          
                                                                                ;;  i                          
                                                                                ;;  p1__4242#                  
                                                                                ;;  do                         
                                                                                ;;  let*                       
                                                                                ;;  a                          
                                                                                ;;  mod                        
                                                                                ;;  i                          
                                                                                ;;  2                          
                                                                                ;;  clojure.core/cons          
                                                                                ;;  if                         
                                                                                ;;  zero?                      
                                                                                ;;  a                          
                                                                                ;;  do                         
                                                                                ;;  loop*                      
                                                                                ;;  seq_4259                   
                                                                                ;;  clojure.core/seq           
                                                                                ;;  others                     
                                                                                ;;  chunk_4260                 
                                                                                ;;  nil                        
                                                                                ;;  count_4261                 
                                                                                ;;  0                          
                                                                                ;;  i_4262                     
                                                                                ;;  0                          
                                                                                ;;  if                         
                                                                                ;;  clojure.core/<             
                                                                                ;;  i_4262                     
                                                                                ;;  count_4261                 
                                                                                ;;  let*                       
                                                                                ;;  item                       
                                                                                ;;  .                          
                                                                                ;;  chunk_4260                 
                                                                                ;;  nth                        
                                                                                ;;  i_4262                     
                                                                                ;;  do                         
                                                                                ;;  print                      
                                                                                ;;  item                       
                                                                                ;;  recur                      
                                                                                ;;  seq_4259                   
                                                                                ;;  chunk_4260                 
                                                                                ;;  count_4261                 
                                                                                ;;  clojure.core/unchecked-inc 
                                                                                ;;  i_4262                     
                                                                                ;;  let*                       
                                                                                ;;  temp__5804__auto__         
                                                                                ;;  clojure.core/seq           
                                                                                ;;  seq_4259                   
                                                                                ;;  if                         
                                                                                ;;  temp__5804__auto__         
                                                                                ;;  do                         
                                                                                ;;  let*                       
                                                                                ;;  seq_4259                   
                                                                                ;;  temp__5804__auto__         
                                                                                ;;  if                         
                                                                                ;;  clojure.core/chunked-seq?  
                                                                                ;;  seq_4259                   
                                                                                ;;  let*                       
                                                                                ;;  c__6065__auto__            
                                                                                ;;  clojure.core/chunk-first   
                                                                                ;;  seq_4259                   
                                                                                ;;  recur                      
                                                                                ;;  clojure.core/chunk-rest    
                                                                                ;;  seq_4259                   
                                                                                ;;  c__6065__auto__            
                                                                                ;;  clojure.core/int           
                                                                                ;;  clojure.core/count         
                                                                                ;;  c__6065__auto__            
                                                                                ;;  clojure.core/int           
                                                                                ;;  0                          
                                                                                ;;  let*                       
                                                                                ;;  item                       
                                                                                ;;  clojure.core/first         
                                                                                ;;  seq_4259                   
                                                                                ;;  do                         
                                                                                ;;  print                      
                                                                                ;;  item                       
                                                                                ;;  recur                      
                                                                                ;;  clojure.core/next          
                                                                                ;;  seq_4259                   
                                                                                ;;  nil                        
                                                                                ;;  0                          
                                                                                ;;  0                          
                                                                                ;;  iter__4245                 
                                                                                ;;  clojure.core/rest          
                                                                                ;;  s__4246                    
                                                                                ;;  iter__6373__auto__         
                                                                                ;;  coll)                      

;; map str:                          ;; map #(re-find #"clojure\.core/(.*)" %):         ;; keep last:       ;; distinct:         ;; sort:                 
;; ("let*"                           ;; (nil                                            ;; ("seq"           ;; ("seq"            ;; ("<"                  
;;  "iter__6373__auto__"             ;;  nil                                            ;;  "chunked-seq?"  ;;  "chunked-seq?"   ;;  "chunk"              
;;  "fn*"                            ;;  nil                                            ;;  "chunk-first"   ;;  "chunk-first"    ;;  "chunk-append"       
;;  "iter__4267"                     ;;  nil                                            ;;  "int"           ;;  "int"            ;;  "chunk-buffer"       
;;  "s__4268"                        ;;  nil                                            ;;  "count"         ;;  "count"          ;;  "chunk-cons"         
;;  "new"                            ;;  nil                                            ;;  "chunk-buffer"  ;;  "chunk-buffer"   ;;  "chunk-first"        
;;  "clojure.lang.LazySeq"           ;;  nil                                            ;;  "int"           ;;  "<"              ;;  "chunk-rest"         
;;  "fn*"                            ;;  nil                                            ;;  "<"             ;;  "first"          ;;  "chunked-seq?"       
;;  "loop*"                          ;;  nil                                            ;;  "seq"           ;;  "next"           ;;  "cons"               
;;  "s__4268"                        ;;  nil                                            ;;  "first"         ;;  "chunk-append"   ;;  "count"              
;;  "s__4268"                        ;;  nil                                            ;;  "next"          ;;  "unchecked-inc"  ;;  "first"              
;;  "let*"                           ;;  nil                                            ;;  "chunk-append"  ;;  "chunk-rest"     ;;  "int"                
;;  "temp__5804__auto__"             ;;  nil                                            ;;  "seq"           ;;  "chunk-cons"     ;;  "next"               
;;  "clojure.core/seq"               ;;  ["clojure.core/seq" "seq"]                     ;;  "<"             ;;  "chunk"          ;;  "rest"               
;;  "s__4268"                        ;;  nil                                            ;;  "unchecked-inc" ;;  "cons"           ;;  "seq"                
;;  "if"                             ;;  nil                                            ;;  "seq"           ;;  "rest")          ;;  "unchecked-inc")     
;;  "temp__5804__auto__"             ;;  nil                                            ;;  "chunked-seq?"                                                
;;  "do"                             ;;  nil                                            ;;  "chunk-first"                                                 
;;  "let*"                           ;;  nil                                            ;;  "chunk-rest"                                                  
;;  "s__4268"                        ;;  nil                                            ;;  "int"                                                         
;;  "temp__5804__auto__"             ;;  nil                                            ;;  "count"                                                       
;;  "if"                             ;;  nil                                            ;;  "int"                                                         
;;  "clojure.core/chunked-seq?"      ;;  ["clojure.core/chunked-seq?" "chunked-seq?"]   ;;  "first"                                                       
;;  "s__4268"                        ;;  nil                                            ;;  "next"                                                        
;;  "let*"                           ;;  nil                                            ;;  "unchecked-inc"                                               
;;  "c__6371__auto__"                ;;  nil                                            ;;  "chunk-cons"                                                  
;;  "clojure.core/chunk-first"       ;;  ["clojure.core/chunk-first" "chunk-first"]     ;;  "chunk"                                                       
;;  "s__4268"                        ;;  nil                                            ;;  "chunk-rest"                                                  
;;  "size__6372__auto__"             ;;  nil                                            ;;  "chunk-cons"                                                  
;;  "clojure.core/int"               ;;  ["clojure.core/int" "int"]                     ;;  "chunk"                                                       
;;  "clojure.core/count"             ;;  ["clojure.core/count" "count"]                 ;;  "first"                                                       
;;  "c__6371__auto__"                ;;  nil                                            ;;  "seq"                                                         
;;  "b__4270"                        ;;  nil                                            ;;  "first"                                                       
;;  "clojure.core/chunk-buffer"      ;;  ["clojure.core/chunk-buffer" "chunk-buffer"]   ;;  "next"                                                        
;;  "size__6372__auto__"             ;;  nil                                            ;;  "cons"                                                        
;;  "if"                             ;;  nil                                            ;;  "seq"                                                         
;;  "loop*"                          ;;  nil                                            ;;  "<"                                                           
;;  "i__4269"                        ;;  nil                                            ;;  "unchecked-inc"                                               
;;  "clojure.core/int"               ;;  ["clojure.core/int" "int"]                     ;;  "seq"                                                         
;;  "0"                              ;;  nil                                            ;;  "chunked-seq?"                                                
;;  "if"                             ;;  nil                                            ;;  "chunk-first"                                                 
;;  "clojure.core/<"                 ;;  ["clojure.core/<" "<"]                         ;;  "chunk-rest"                                                  
;;  "i__4269"                        ;;  nil                                            ;;  "int"                                                         
;;  "size__6372__auto__"             ;;  nil                                            ;;  "count"                                                       
;;  "let*"                           ;;  nil                                            ;;  "int"                                                         
;;  "vec__4271"                      ;;  nil                                            ;;  "first"                                                       
;;  "."                              ;;  nil                                            ;;  "next"                                                        
;;  "c__6371__auto__"                ;;  nil                                            ;;  "rest")                                                       
;;  "nth"                            ;;  nil                                                                                                              
;;  "i__4269"                        ;;  nil                                                                                                              
;;  "seq__4272"                      ;;  nil                                                                                                              
;;  "clojure.core/seq"               ;;  ["clojure.core/seq" "seq"]                                                                                       
;;  "vec__4271"                      ;;  nil                                                                                                              
;;  "first__4273"                    ;;  nil                                                                                                              
;;  "clojure.core/first"             ;;  ["clojure.core/first" "first"]                                                                                   
;;  "seq__4272"                      ;;  nil                                                                                                              
;;  "seq__4272"                      ;;  nil                                                                                                              
;;  "clojure.core/next"              ;;  ["clojure.core/next" "next"]                                                                                     
;;  "seq__4272"                      ;;  nil                                                                                                              
;;  "head"                           ;;  nil                                                                                                              
;;  "first__4273"                    ;;  nil                                                                                                              
;;  "others"                         ;;  nil                                                                                                              
;;  "seq__4272"                      ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "fn*"                            ;;  nil                                                                                                              
;;  "p1__4264#"                      ;;  nil                                                                                                              
;;  "<"                              ;;  nil                                                                                                              
;;  "i"                              ;;  nil                                                                                                              
;;  "p1__4264#"                      ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "a"                              ;;  nil                                                                                                              
;;  "mod"                            ;;  nil                                                                                                              
;;  "i"                              ;;  nil                                                                                                              
;;  "2"                              ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "clojure.core/chunk-append"      ;;  ["clojure.core/chunk-append" "chunk-append"]                                                                     
;;  "b__4270"                        ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "zero?"                          ;;  nil                                                                                                              
;;  "a"                              ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "loop*"                          ;;  nil                                                                                                              
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "clojure.core/seq"               ;;  ["clojure.core/seq" "seq"]                                                                                       
;;  "others"                         ;;  nil                                                                                                              
;;  "chunk_4275"                     ;;  nil                                                                                                              
;;  ""                               ;;  nil                                                                                                              
;;  "count_4276"                     ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "i_4277"                         ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "clojure.core/<"                 ;;  ["clojure.core/<" "<"]                                                                                           
;;  "i_4277"                         ;;  nil                                                                                                              
;;  "count_4276"                     ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "."                              ;;  nil                                                                                                              
;;  "chunk_4275"                     ;;  nil                                                                                                              
;;  "nth"                            ;;  nil                                                                                                              
;;  "i_4277"                         ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "print"                          ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "recur"                          ;;  nil                                                                                                              
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "chunk_4275"                     ;;  nil                                                                                                              
;;  "count_4276"                     ;;  nil                                                                                                              
;;  "clojure.core/unchecked-inc"     ;;  ["clojure.core/unchecked-inc" "unchecked-inc"]                                                                   
;;  "i_4277"                         ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "temp__5804__auto__"             ;;  nil                                                                                                              
;;  "clojure.core/seq"               ;;  ["clojure.core/seq" "seq"]                                                                                       
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "temp__5804__auto__"             ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "temp__5804__auto__"             ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "clojure.core/chunked-seq?"      ;;  ["clojure.core/chunked-seq?" "chunked-seq?"]                                                                     
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "c__6065__auto__"                ;;  nil                                                                                                              
;;  "clojure.core/chunk-first"       ;;  ["clojure.core/chunk-first" "chunk-first"]                                                                       
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "recur"                          ;;  nil                                                                                                              
;;  "clojure.core/chunk-rest"        ;;  ["clojure.core/chunk-rest" "chunk-rest"]                                                                         
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "c__6065__auto__"                ;;  nil                                                                                                              
;;  "clojure.core/int"               ;;  ["clojure.core/int" "int"]                                                                                       
;;  "clojure.core/count"             ;;  ["clojure.core/count" "count"]                                                                                   
;;  "c__6065__auto__"                ;;  nil                                                                                                              
;;  "clojure.core/int"               ;;  ["clojure.core/int" "int"]                                                                                       
;;  "0"                              ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "clojure.core/first"             ;;  ["clojure.core/first" "first"]                                                                                   
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "print"                          ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "recur"                          ;;  nil                                                                                                              
;;  "clojure.core/next"              ;;  ["clojure.core/next" "next"]                                                                                     
;;  "seq_4274"                       ;;  nil                                                                                                              
;;  ""                               ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "recur"                          ;;  nil                                                                                                              
;;  "clojure.core/unchecked-inc"     ;;  ["clojure.core/unchecked-inc" "unchecked-inc"]                                                                   
;;  "i__4269"                        ;;  nil                                                                                                              
;;  "true"                           ;;  nil                                                                                                              
;;  "clojure.core/chunk-cons"        ;;  ["clojure.core/chunk-cons" "chunk-cons"]                                                                         
;;  "clojure.core/chunk"             ;;  ["clojure.core/chunk" "chunk"]                                                                                   
;;  "b__4270"                        ;;  nil                                                                                                              
;;  "iter__4267"                     ;;  nil                                                                                                              
;;  "clojure.core/chunk-rest"        ;;  ["clojure.core/chunk-rest" "chunk-rest"]                                                                         
;;  "s__4268"                        ;;  nil                                                                                                              
;;  "clojure.core/chunk-cons"        ;;  ["clojure.core/chunk-cons" "chunk-cons"]                                                                         
;;  "clojure.core/chunk"             ;;  ["clojure.core/chunk" "chunk"]                                                                                   
;;  "b__4270"                        ;;  nil                                                                                                              
;;  ""                               ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "vec__4278"                      ;;  nil                                                                                                              
;;  "clojure.core/first"             ;;  ["clojure.core/first" "first"]                                                                                   
;;  "s__4268"                        ;;  nil                                                                                                              
;;  "seq__4279"                      ;;  nil                                                                                                              
;;  "clojure.core/seq"               ;;  ["clojure.core/seq" "seq"]                                                                                       
;;  "vec__4278"                      ;;  nil                                                                                                              
;;  "first__4280"                    ;;  nil                                                                                                              
;;  "clojure.core/first"             ;;  ["clojure.core/first" "first"]                                                                                   
;;  "seq__4279"                      ;;  nil                                                                                                              
;;  "seq__4279"                      ;;  nil                                                                                                              
;;  "clojure.core/next"              ;;  ["clojure.core/next" "next"]                                                                                     
;;  "seq__4279"                      ;;  nil                                                                                                              
;;  "head"                           ;;  nil                                                                                                              
;;  "first__4280"                    ;;  nil                                                                                                              
;;  "others"                         ;;  nil                                                                                                              
;;  "seq__4279"                      ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "fn*"                            ;;  nil                                                                                                              
;;  "p1__4264#"                      ;;  nil                                                                                                              
;;  "<"                              ;;  nil                                                                                                              
;;  "i"                              ;;  nil                                                                                                              
;;  "p1__4264#"                      ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "a"                              ;;  nil                                                                                                              
;;  "mod"                            ;;  nil                                                                                                              
;;  "i"                              ;;  nil                                                                                                              
;;  "2"                              ;;  nil                                                                                                              
;;  "clojure.core/cons"              ;;  ["clojure.core/cons" "cons"]                                                                                     
;;  "if"                             ;;  nil                                                                                                              
;;  "zero?"                          ;;  nil                                                                                                              
;;  "a"                              ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "loop*"                          ;;  nil                                                                                                              
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "clojure.core/seq"               ;;  ["clojure.core/seq" "seq"]                                                                                       
;;  "others"                         ;;  nil                                                                                                              
;;  "chunk_4282"                     ;;  nil                                                                                                              
;;  ""                               ;;  nil                                                                                                              
;;  "count_4283"                     ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "i_4284"                         ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "clojure.core/<"                 ;;  ["clojure.core/<" "<"]                                                                                           
;;  "i_4284"                         ;;  nil                                                                                                              
;;  "count_4283"                     ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "."                              ;;  nil                                                                                                              
;;  "chunk_4282"                     ;;  nil                                                                                                              
;;  "nth"                            ;;  nil                                                                                                              
;;  "i_4284"                         ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "print"                          ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "recur"                          ;;  nil                                                                                                              
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "chunk_4282"                     ;;  nil                                                                                                              
;;  "count_4283"                     ;;  nil                                                                                                              
;;  "clojure.core/unchecked-inc"     ;;  ["clojure.core/unchecked-inc" "unchecked-inc"]                                                                   
;;  "i_4284"                         ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "temp__5804__auto__"             ;;  nil                                                                                                              
;;  "clojure.core/seq"               ;;  ["clojure.core/seq" "seq"]                                                                                       
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "temp__5804__auto__"             ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "temp__5804__auto__"             ;;  nil                                                                                                              
;;  "if"                             ;;  nil                                                                                                              
;;  "clojure.core/chunked-seq?"      ;;  ["clojure.core/chunked-seq?" "chunked-seq?"]                                                                     
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "c__6065__auto__"                ;;  nil                                                                                                              
;;  "clojure.core/chunk-first"       ;;  ["clojure.core/chunk-first" "chunk-first"]                                                                       
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "recur"                          ;;  nil                                                                                                              
;;  "clojure.core/chunk-rest"        ;;  ["clojure.core/chunk-rest" "chunk-rest"]                                                                         
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "c__6065__auto__"                ;;  nil                                                                                                              
;;  "clojure.core/int"               ;;  ["clojure.core/int" "int"]                                                                                       
;;  "clojure.core/count"             ;;  ["clojure.core/count" "count"]                                                                                   
;;  "c__6065__auto__"                ;;  nil                                                                                                              
;;  "clojure.core/int"               ;;  ["clojure.core/int" "int"]                                                                                       
;;  "0"                              ;;  nil                                                                                                              
;;  "let*"                           ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "clojure.core/first"             ;;  ["clojure.core/first" "first"]                                                                                   
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  "do"                             ;;  nil                                                                                                              
;;  "print"                          ;;  nil                                                                                                              
;;  "item"                           ;;  nil                                                                                                              
;;  "recur"                          ;;  nil                                                                                                              
;;  "clojure.core/next"              ;;  ["clojure.core/next" "next"]                                                                                     
;;  "seq_4281"                       ;;  nil                                                                                                              
;;  ""                               ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "0"                              ;;  nil                                                                                                              
;;  "iter__4267"                     ;;  nil                                                                                                              
;;  "clojure.core/rest"              ;;  ["clojure.core/rest" "rest"]                                                                                     
;;  "s__4268"                        ;;  nil                                                                                                              
;;  "iter__6373__auto__"             ;;  nil                                                                                                              
;;  "coll")                          ;;  nil)                                                                                                             

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Performance considerations
;; ;;;;;;;;;;;;;;;;;;;;;;;;;;

;; `flatten` is lazy
(->>
 (range)               ; infinite sequence (can be used safely with `flatten`)
 (map range)           ; (() (0) (0 1) (0 1 2) (0 1 2 3) (0 1 2 3 4) (0 1 2 3 4 5) (0 1 2 3 4 5 6) (0 1 2 3 4 5 6 7) (0 1 2 3 4 5 6 7 8) …)
 (map-indexed vector)  ; ([0 ()] [1 (0)] [2 (0 1)] [3 (0 1 2)] [4 (0 1 2 3)] [5 (0 1 2 3 4)] [6 (0 1 2 3 4 5)] [7 (0 1 2 3 4 5 6)] [8 (0 1 2 3 4 5 6 7)] [9 (0 1 2 3 4 5 6 7 8)] …)
 flatten               ; (0 1 0 2 0 1 3 0 1 2 …)
 (take 10))            ; (0 1 0 2 0 1 3 0 1 2)
; (0 1 0 2 0 1 3 0 1 2)

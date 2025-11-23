(ns f.core
  (:require
   [clojure.set :as s]))

;; relational algebra functions

(def users
  #{{:user-id 1 :name "john"   :age 22 :type "personal"}
    {:user-id 2 :name "jake"   :age 28 :type "company"}
    {:user-id 3 :name "amanda" :age 63 :type "personal"}})

(def accounts
  #{{:acc-id 1 :user-id 1 :amount 300.45 :type "saving"}
    {:acc-id 2 :user-id 2 :amount 1200.0 :type "saving"}
    {:acc-id 3 :user-id 1 :amount 850.1  :type "debit"}})

(s/select #(> (:age %) 30) users)  ; #{{:user-id 3, :name "amanda", :age 63, :type "personal"}}

(s/project
 (s/join users accounts {:user-id :user-id})
 [:user-id :acc-id :name])
; #{{:user-id 1, :acc-id 1, :name "john"}
;   {:user-id 1, :acc-id 3, :name "john"}
;   {:user-id 2, :acc-id 2, :name "jake"}}

(comment
  (s/join users accounts {:user-id :user-id}))
  ; #{{:user-id 2,
  ;    :name "jake",
  ;    :age 28,
  ;    :type "saving",
  ;    :acc-id 2,
  ;    :amount 1200.0}
  ;   {:user-id 1,
  ;    :name "john",
  ;    :age 22,
  ;    :type "debit",
  ;    :acc-id 3,
  ;    :amount 850.1}
  ;   {:user-id 1,
  ;    :name "john",
  ;    :age 22,
  ;    :type "saving",
  ;    :acc-id 1,
  ;    :amount 300.45}}

(s/project
 (s/join users accounts {:user-id :user-id})
 [:user-id :acc-id :type])
; #{{:user-id 1, :acc-id 1, :type "saving"}
;   {:user-id 2, :acc-id 2, :type "saving"}
;   {:user-id 1, :acc-id 3, :type "debit"}}

(s/project
 (s/join users (s/rename accounts {:type :account-type}))
 [:user-id :acc-id :type :account-type])
; #{{:user-id 1, :acc-id 3, :type "personal", :account-type "debit"}
;   {:user-id 1, :acc-id 1, :type "personal", :account-type "saving"}
;   {:user-id 2, :acc-id 2, :type "company",  :account-type "saving"}}

(comment
  (s/rename accounts {:type :account-type})
  ; #{{:acc-id 2, :user-id 2, :amount 1200.0, :account-type "saving"}
  ;   {:acc-id 3, :user-id 1, :amount 850.1,  :account-type "debit"}
  ;   {:acc-id 1, :user-id 1, :amount 300.45, :account-type "saving"}}

  (s/join users (s/rename accounts {:type :atype})))
  ; #{{:user-id 1,
  ;    :name "john",
  ;    :age 22,
  ;    :type "personal",
  ;    :acc-id 3,
  ;    :amount 850.1,
  ;    :atype "debit"}
  ;   {:user-id 2,
  ;    :name "jake",
  ;    :age 28,
  ;    :type "company",
  ;    :acc-id 2,
  ;    :amount 1200.0,
  ;    :atype "saving"}
  ;   {:user-id 1,
  ;    :name "john",
  ;    :age 22,
  ;    :type "personal",
  ;    :acc-id 1,
  ;    :amount 300.45,
  ;    :atype "saving"}}

(s/index users [:type])
; {{:type "company"}
;  #{{:user-id 2, :name "jake", :age 28, :type "company"}},
;  {:type "personal"}
;  #{{:user-id 3, :name "amanda", :age 63, :type "personal"}
;    {:user-id 1, :name "john", :age 22, :type "personal"}}}

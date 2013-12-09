(ns xiast.session
  "This namespace provides simple methods to store
  information about the current session a user has."
  (:use [xiast.authentication :as auth]))

(defn from-cookies
  [cookies]
  (let [locale (cookies "locale")
        token  (cookies "token")]
    {:locale (if locale locale "en")
     :token token
     :user (if token (auth/netid-from-token token) nil)}))

(defn to-cookies
  [session]
  {"locale" (:locale session)
   "token"  (:token session)})

;; Exclamation because this will change data
;; in the database.
(defn kill-session!
  [session]
  (assoc session :token ""))
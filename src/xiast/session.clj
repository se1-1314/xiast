(ns xiast.session
  "This namespace provides simple methods to store
  information about the current session a user has."
  (:use [xiast.authentication :as auth]))

(def ^:dynamic *session* nil)

(defn wrap-with-session
  [handler]
  (fn [{session :session :as request}]
    (binding [*session* session]
      (handler request))))

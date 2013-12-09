(ns xiast.session
  "This namespace provides simple methods to store
  information about the current session a user has."
  (:use [xiast.authentication :as auth]))

(def ^:dynamic *session* {})

(defn wrap-with-session
  [session func]
  (binding [*session* session]
    (func)))
(ns xiast.authentication
  (:require [clj-http.client :as client]))

(defn netid-from-token
  [token]
  "nvgeele")

(defn login
  [netid password]
  (let [req (client/post
              "https://idsserve.vub.ac.be/cgi-bin/vrfy-pw"
              {:body (format "username=%s&fields=username&failure=xiastfail&location=xiastsucc&password=%s&options=valid+relation"
                             netid password)})
        body (:body req)]
    ;; TODO: get locale from db
    (if (re-find #"xiastsucc" body)
      {:user netid
       :locale "en"
       ;; FIXME actually implement
       :user-type (rand-nth [:student :program-manager :titular])}
      nil)))

(defn logout
  [session]
  (apply dissoc session [:user]))

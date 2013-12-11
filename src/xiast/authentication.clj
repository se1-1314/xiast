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
    (if (re-find #"xiastsucc" body)
      {:user netid
       :locale "en"} ;;TODO: get locale from db
      nil)))

(ns xiast.authentication
  (:require [clj-http.client :as client]
            [xiast.database :as db]))

(defn login
  [netid password]
  (let [req (client/post
             "https://idsserve.vub.ac.be/cgi-bin/vrfy-pw"
             {:body (format "username=%s&fields=username&failure=xiastfail&location=xiastsucc&password=%s&options=valid+relation"
                            netid password)})
        body (:body req)]
    ;; TODO: get locale from db
    (if (re-find #"xiastsucc" body)
      (let [user (db/get-user netid)]
        (if (empty? user)
          (do
            (db/create-user netid "en")
            {:user netid
             ;; TODO: Get locale out of session
             :locale "en"
             :user-functions (take (rand-int 4)
                                   (shuffle [:student :program-manager :titular :instructor]))})
          {:user netid
           ;; TODO: Get real locale out of db
           :locale "en"
           :user-functions (take (rand-int 4)
                                 (shuffle [:student :program-manager :titular :instructor]))}))
      nil)))

(defn logout
  [session]
  (apply dissoc session [:user]))

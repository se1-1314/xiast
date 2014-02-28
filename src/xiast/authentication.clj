(ns xiast.authentication
  (:require [clj-http.client :as client]
            [xiast.database :as db]))

(def user-types
  {0 :student
   1 :program-manager
   2 :titular})

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
          (do (db/create-user netid "en" 1)
              {:user netid
               :locale "en"
               :user-type :student})
          {:user netid
           :locale (:locale user)
           :user-type (find (:type user) user-types)}))
      nil)))

(defn logout
  [session]
  (apply dissoc session [:user]))

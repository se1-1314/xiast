(ns xiast.authentication
  (:use [xiast.database :only [*db* create-person]])
  (:require [clj-http.client :as client]
            [xiast.query :as query]))

(defn login
  [netid password]
  (let [req (client/post
             "https://idsserve.vub.ac.be/cgi-bin/vrfy-pw"
             {:body (format "username=%s&fields=username&failure=xiastfail&location=xiastsucc&password=%s&options=valid+relation"
                            netid password)})
        body (:body req)]
    ;; TODO: get locale from db
    (if (re-find #"xiastsucc" body)
      (let [person (query/person-get *db* netid)]
        (if person
          (assoc person
            :user-functions (query/person-functions *db* netid)
            :user (:netid person))
          (do
            (create-person *db* netid)
            (let [person (query/person-get *db* netid)]
              (assoc
                  person
                :user-functions
                (query/person-functions *db* netid)
                :user (:netid person))))))
      nil)))

(defn logout
  [session]
  (apply dissoc session [:user]))

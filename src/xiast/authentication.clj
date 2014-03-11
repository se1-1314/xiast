(ns xiast.authentication
  (:use [xiast.database :only [*db* create-person]]
        [xiast.config :only [config]])
  (:require [clj-http.client :as client]
            [xiast.query :as query]))

(defn- login-production
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

(defn- login-debug
  [netid password]
  (case netid
    "pmanager" (assoc (query/person-get *db* "pmanager")
                        :user "pmanager"
                        :user-functions #{:program-manager})
    "titular" (assoc (query/person-get *db* "titular")
                :user "titular"
                :user-functions #{:titular})
    "instructor" (assoc (query/person-get *db* "instructor")
                   :user "instructor"
                   :user-functions #{:instructor})
    "student" (assoc (query/person-get *db* "student")
                :user "student"
                :user-functions #{:student})
    "default" (login-production netid password)))

(def login
  (if (:production? config)
    login-production
    login-debug))

(defn logout
  [session]
  (apply dissoc session [:user]))

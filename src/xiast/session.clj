(ns xiast.session
  "This namespace provides simple methods to store
  information about the current session a user has."
  (:require [clj-time.core :as time]
            [clj-time.coerce :as timec])
  (:use [ring.middleware.session.store :as rss]
        [xiast.database]
        [korma.db]
        [korma.core])
  (:import java.util.UUID))

;; We store when the session was created, this allows us to run
;; scripts to remove sessions older than X.
;; TODO: Maybe change timestamp on update?
(defn session-create
  "Creates a new session record in the database and returns its ID."
  ([] (session-create ""))
  ([data] (let [id (str (UUID/randomUUID))]
            (insert session
                    (values {:id id
                             :data data
                             :created (timec/to-sql-time (time/now))}))
            id)))

(defn session-get
  [session-id]
  "Returns session data for a given ID."
  (let [res (select session
                    (where {:id session-id}))]
    (if (empty? res)
      nil
      (:data (first res)))))

(defn session-update!
  [session-id data]
  (update session
          (set-fields {:data data})
          (where {:id session-id})))

(defn session-delete!
  [session-id]
  (delete session
          (where {:id session-id})))

(def ^:dynamic *session* nil)
(def ^:dynamic *alert* nil)

(defn wrap-with-session
  [handler]
  (fn [{session :session :as request}]
    (binding [*session* session
              *alert* (:flash request)]
      (handler request))))

(deftype XiastSessionStore []
  rss/SessionStore
  (read-session [_ key]
    (when key
      (if-let [data (session-get key)]
        (read-string data))))
  (write-session [_ key data]
    (let [key (if key key (session-create))]
      (session-update! key (pr-str data))
      key))
  (delete-session [_ key]
    (session-delete! key)))

(defn session-store
  []
  (XiastSessionStore.))

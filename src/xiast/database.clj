(ns xiast.database
  (:require [xiast.schema :as xs]
            [schema.core :as s])
  (:use [clojure.set :only [map-invert]]
        [xiast.config :only [config]]
        [korma.db]
        [korma.core]))

(def ^:dynamic *db* nil)

;; TODO: Change `database' in config to `db' so we can
;; just use the config map as argument. (nvgeele)
(defdb db
  (let [config (:database config)]
    (if (= (:type config) 'mysql)
      (mysql
       {:host (:host config)
        :db (:database config)
        :user (:user config)
        :password (:password config)})
      (sqlite3
       {:db (:database config)}))))

(def ^:dynamic course
  (-> (create-entity "course")
      (database db)))

(def ^:dynamic course-activity
  (-> (create-entity "course-activity")
      (database db)))

(def ^:dynamic course-activity-facility
  (-> (create-entity "course-activity-facility")
      (database db)))

(def ^:dynamic course-enrollment
  (-> (create-entity "course-enrollment")
      (database db)))

(def ^:dynamic course-instructor
  (-> (create-entity "course-instructor")
      (database db)))

(def ^:dynamic department
  (-> (create-entity "department")
      (database db)))

(def ^:dynamic person
  (-> (create-entity "person")
      (database db)))

(def ^:dynamic program
  (-> (create-entity "program")
      (database db)))

(def ^:dynamic program-choice-course
  (-> (create-entity "program-choice-course")
      (database db)))

(def ^:dynamic program-mandatory-course
  (-> (create-entity "program-mandatory-course")
      (database db)))

(def ^:dynamic room
  (-> (create-entity "room")
      (database db)))

(def ^:dynamic room-facility
  (-> (create-entity "room-facility")
      (database db)))

(def ^:dynamic subscription
  (-> (create-entity "subscription")
      (database db)))

(def ^:dynamic session
  (-> (create-entity "session")
      (database db)))

(def ^:dynamic schedule-block
  (-> (create-entity "schedule-block")
      (database db)))

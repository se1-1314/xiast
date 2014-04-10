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

(defentity course
  (database db))

(defentity course-activity
  (database db))

(defentity course-enrollment
  (database db))

(defentity course-instructor
  (database db))

(defentity department
  (database db))

(defentity person
  (database db))

(defentity program
  (database db))

(defentity program-choice-course
  (database db))

(defentity program-mandatory-course
  (database db))

(defentity room
  (database db))

(defentity room-facility
  (database db))

(defentity subscription
  (database db))

(defentity session
  (database db))

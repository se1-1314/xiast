(ns xiast.database
  (:require [xiast.query :as query])
  (:use [xiast.config :only [config]]
        [korma.db]
        [korma.core]))

;; TODO: Change `database' in config to `db' so we can
;; just use the config map as argument.
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
  (database db)
  (table :CourseActivity))

(defentity course-enrollment
  (database db)
  (table :CourseEnrollment))

(defentity course-instructor
  (database db)
  (table :CourseInstructor))

(defentity deparment
  (database db))

(defentity person
  (database db))

(defentity program
  (database db))

(defentity program-choice-course
  (database db)
  (table :ProgramChoiceCourse))

(defentity program-mandatory-course
  (database db)
  (table :ProgramMandatoryCourse))

(defentity room
  (database db))

(defentity room-id
  (database db)
  (table :roomid))

(defentity subscription
  (database db))

#_(defentity studyprogram-mandatorycourse
    (table :studyprogram_mandatorycourse)
    (database db))

(defn get-user
  [netid]
  (let [user (select person
                     (where {:netid netid}))]
    (if (empty? user) user (first user))))

(defn create-user
  [netid locale]
  (insert person
          (values {:netid netid
                   :locale locale})))

(defrecord Database [])

(extend-type Database
  query/Rooms
  (room-add!
    [this room]
    "Add a room")
  (room-delete!
    [this room]
    "Delete a room")
  (room-get
    [this room]
    "returns the matching")

  query/Courses
  (course-add!
    [this course])
  (course-delete!
    [this course-code])
  (course-get
    [this course-code])
  (course-list
    [this])
  (course-find
    [this kws])

  query/Programs
  (program-list
    [this])
  (program-find
    [this kws])
  (program-courses
    [this program-id])

  query/Enrollments
  (student-enrollments
    [this student-id])
  (enroll-student!
    [this student-id course-code])

  query/Schedules
  (course-schedule
    ([this course-code])
    ([this course-code timespan]))
  (student-schedule
    ([this student-id])
    ([this student-id timespan]))
  (room-schedule
    ([this room-id])
    ([this room-id timespan]))
  (program-schedule
    ([this program-id])
    ([this program-id timespan])))

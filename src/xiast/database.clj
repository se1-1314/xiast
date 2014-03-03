(ns xiast.database
  (:require [xiast.query :as query])
  (:use [clojure.set :only [map-invert]]
        [xiast.config :only [config]]
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

(defentity room-facility
  (database db)
  (table :roomfacility))

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

(def room-facilities
  {0 :beamer
   1 :overhead-projector
   2 :speakers})

(extend-type Database
  query/Rooms
  (room-add!
    [this new-room]
    (let [facilities
          (map #(% (map-invert room-facilities))
               (:facilities new-room))
          room-id
          (:id new-room)
          vals
          (merge {:capacity (:capacity new-room)}
                 room-id)
          key
          (:GENERATED_KEY
           (insert room
                   (values vals)))]
      (doseq [facility facilities]
        (insert room-facility
                (values {:room key
                         :facility facility})))))
  (room-delete!
    [this room-id]
    ;; Do we need to check if the room exists first or not?
    (delete room
            (where {:id room-id})))
  (room-get
    [this room-id]
    (let [room (select room
                       (where {:id room-id}))]
      (if (not (empty? room))
        (let [facilities
              (map #(val (find room-facilities (:facility %)))
                   (select room-facility
                           (where {:room (:id (first room))})))]
          {:id {:building (:building (first room))
                :floor (:floor (first room))
                :number (:number (first room))}
           :capacity (:capacity (first room))
           :facilities (set facilities)})
        empty)))

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

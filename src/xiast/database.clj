(ns xiast.database
  (:require [xiast.query :as query])
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
  (database db)
  (table :CourseActivity))

(defentity course-enrollment
  (database db)
  (table :CourseEnrollment))

(defentity course-instructor
  (database db)
  (table :CourseInstructor))

(defentity department
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

(def course-grades
  {0 :ba
   1 :ma})

(def course-activity-types
  {0 :HOC
   1 :WPO})

(defn person->sPerson
  [person]
  {:id (:netid person)
   :first-name (:firstname person)
   :last-name (:surname person)
   :locale (:locale person)})

(defn course-activity->sCourseActivity
  [course-activity]
  ;; TODO: fix support for multiple instructors/activity (nvgeele)
  (let [instructor-id
        ((comp :netid first)
         (select course-instructor
                 (where {:course-activity (:id course-activity)})))]
    {:type (val (find course-activity-types (:type course-activity)))
     :semester (:semester course-activity)
     :data (:date course-activity)
     :contact-time-hours (:contact-time-hours course-activity)
     :instructor instructor-id}))

(defn course->sCourse
  [course]
  (let [department
        ((comp :name first)
         (select department
                 (where {:id (:department course)})))
        titular
        ((comp person->sPerson first)
         (select person
                 (where {:netid (:titular-id course)})))
        activities
        (map course-activity->sCourseActivity
             (select course-activity
                     (where {:course-code (:course-code course)})))
        instructors
        (map :instructor activities)
        grade
        (val (find course-grades (:grade course)))]
    {:course-code (:course-code course)
     :title (:title course)
     :description (:description course)
     :titular (:titular-id course)
     :grade grade
     :department department
     :activities (set activities)
     :instructors (set instructors)}))

(defn program->sProgram
  [program]
  (let [mandatory
        (map :course-code
             (select program-mandatory-course
                     (where {:program (:id program)})))
        choice
        (map :course-code
             (select program-choice-course
                     (where {:program (:id program)})))]
    (merge program
           {:mandatory (set mandatory)
            :optional (set choice)})))

;; TODO: replace all find's with get's (nvgeele)

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
    ;; Do we need to check if the room exists first or not? (nvgeele)
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
        nil)))

  query/Persons
  (person-add!
    [this new-person]
    (insert person
            (values {:netid (:id new-person)
                     :firstname (:first-name new-person)
                     :surname (:last-name new-person)
                     :locale (:locale new-person)})))
  (person-get
    [this netid]
    (let [person
          (select person
                  (where {:netid netid}))]
      (if (not (empty? person))
        (person->sPerson (first person))
        nil)))

  query/Courses
  (course-add!
    [this new-course]
    ;; I assume the department exists prior to adding a
    ;; new course. (nvgeele)
    (let [department
          ((comp :id first)
           (select department
                   (where {:name (:department new-course)})))]
      (if (empty? (query/person-get this (:titular-id new-course)))
        ;; TODO: Standard locale (nvgeele)
        (query/person-add! this
                           {:id (:titular-id new-course)
                            :first-name ""
                            :last-name ""
                            :locale "en"}))
      (insert course
              (values (merge (dissoc new-course
                                     :instructors :department :grade :activities)
                             {:grade ((:grade new-course)
                                      (map-invert course-grades))
                              :department department})))
      (doseq [activity (:activities new-course)]
        (if (empty? (query/person-get this (:instructor activity)))
          ;; TODO: Standard locale (nvgeele)
          (query/person-add! this
                             {:id (:instructor activity)
                              :first-name ""
                              :last-name ""
                              :locale "en"}))
        (let [activity-id
              (:GENERATED_KEY
               (insert course-activity
                       (values {:course-code (:course-code new-course)
                                :type ((:type activity)
                                       (map-invert course-activity-types))
                                :semester (:semester activity)
                                :date (:date activity)
                                :contact-time-hours
                                (:contact-time-hours activity)})))]
          (insert course-instructor
                  (values {:course-activity activity-id
                           :netid (:instructor activity)}))))))
  (course-delete!
    [this course-code]
    ;; We only need to delete the course record,
    ;; enrollments, instructors, ... will cascade (nvgeele)
    (delete course
            (where {:course-code course-code})))
  (course-get
    [this course-code]
    (let [course (select course
                         (where {:course-code course-code}))]
      (if (not (empty? course))
        (course->sCourse (first course))
        empty)))
  (course-list
    [this]
    (let [courses (select course)]
      (map course->sCourse courses)))
  (course-find
    [this kws])

  query/Programs
  (program-list
    [this]
    (map program->sProgram
         (select program)))
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

(defn wrap-database
  [handler]
  (fn [request]
    (binding [*db* (Database.)]
      (handler request))))

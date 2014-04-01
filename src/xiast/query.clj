(ns xiast.query
  "This namespace provides protocols for querying and updating
  information accessible through Xiast information stores."

  (:require [schema.core :as s]
            [xiast.database :as db]
            ))


;; TODO: replace all find's with get's (nvgeele)


(defprotocol Rooms
  (room-delete!
    [this room-id]
    "Delete a Room.")
  (room-get
    [this room-id]
    "Returns the matching Room description."))

(defprotocol Courses
  (course-add! [this course])
  (course-add-activity! [this course-code activity])
  (course-delete! [this course-code])
  (course-delete-activity! [this activity-code])
  (course-get [this course-code])
  (course-list [this])
  (course-find
    [this kws]
    "Return a list of {:title s/Str :course-code CourseCode},
    optionally filtering by the strings in kws for the name of the
    course (case insensitive and in any order)."))

(defprotocol CourseRequirements
  (course-room-requirements-set!
    [this course-code course-activity-type course-activity-requirements])
  (course-room-requirements
    [this course-code course-activity-type]
    "Return the CourseActivityRequirements for a course activity."))

(defprotocol Programs
  (program-list
    [this]
    "Return a list of {:title s/Str :program-id ProgramID}")
  (program-find
    [this kws])
  (program-get
    [this program-id]
    "Return a program map.")
  (program-add!
    [this new-program]
    "Accepts a program map and inserts it into the database.")
  (program-delete!
    [this id]))

(defprotocol Persons
  (person-add!
    [this person]
    "Adds a new person to the database")
  (person-get
    [this netid]
    "Returns a single person")
  (person-functions
    [this netid]
    "Returns the person's functions"))

(defprotocol Enrollments
  (student-enrollments
    [this student-id]
    "Return a list of {:title s/Str :course-code CourseCode :program-id
  ProgramID")
  (enroll-student!
    [this student-id course-code]
    "Enroll a student for all activities in a course."))

(defprotocol Departments
  (department-list
    [this]
    "Returns a list of all departments.")
  (department-get
    [this id])
  (department-add!
    [this new-department]))


(defprotocol Schedules
  (course-schedule
    [this course-code]
    [this course-code timespan]
    "Return a list of schedule blocks for a course, optionally using a
    timespan to limit results.")
  (student-schedule
    [this student-id]
    [this student-id timespan]
    "Return a list of schedule blocks for a student, optionally using
    a timespan to limit results.")
  (room-schedule
    [this room-id]
    [this room-id timespan]
    "Return a list of schedule blocks for a room, optionally using a
    timespan to limit results.")
  (program-schedule
    [this program-id]
    [this program-id timespan]
    "Return a list of schedule blocks for a whole program, optionally
    using a timespan to limit results."))

;; TODO: Remove mockdata. (nvgeele)
(defprotocol XiastQuery
  (courses
    [this]
    [this title-kw]
    "Return a list of {:title \"Course title\" :id \"Course ID\"},
optionally using a search keyword for the name of the course (case
insensitive).")
  (course-schedule
    [this course-id]
    [this course-id timespan]
    "Return a list of schedule blocks for a course, optionally using a
timespan to limit results.")
  (student-schedule
    [this student-id]
    [this student-id timespan]
    "Return a list of schedule blocks for a student, optionally using
a timespan to limit results.")
  (room-schedule
    [this room-id]
    [this room-id timespan]
    "Return a list of schedule blocks for a room, optionally using a
timespan to limit results."))

(defn- in-range? [num range]
  (<= (first range) num (second range)))

(defn schedule-block-in-timespan? [block timespan]
  ;; FIXME ew (aleijnse)
  (and (every? true?
               (map in-range?
                    (map block [:week :day])
                    (map timespan [:weeks :days])))
       (or (in-range? (:start-time block) (:time timespan))
           (in-range? (:end-time block) (:time timespan)))))

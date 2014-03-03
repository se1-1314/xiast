(ns xiast.query
  "This namespace provides protocols for querying and updating
  information accessible through Xiast information stores.

  Data is described using the prismatic/schema schema language."
  (:require [schema.core :as s]))

(def RoomID {:building s/Str
             :floor s/Int
             :number s/Int})
(def RoomFacility (s/enum :beamer :overhead-projector))
(def Room {:id RoomID
           :capacity s/Int
           :facilities #{RoomFacility})}
(def PersonID s/Str)
(def StudyActivityType (s/enum :HOC :WPO))
(def Degree (s/enum :ma :ba :manama :schakel))
(def SessionSemester (s/enum :1 :2 :1+2))
(def CourseCode s/Str)
(def Department [(s/one s/Keyword "department, e.g. :mathematics")
                 (s/one s/Keyword "faculty, e.g. :sciences")])
(def Course {:course-code CourseCode
             :title s/Str
             :description s/Str
             :titular-id PersonID
             :instructors #{PersonID}
             :department Department
             :grade (s/enum :ba :ma)
             :activities {StudyActivityType {:contact-hours s/Int}}})
(def CourseActivityID {:course-code CourseCode
                       :study-activity-type StudyActivityType})
(def ProgramID s/Int)
(def Program {:title s/Str
              :description s/Str
              :id ProgramID
              :mandatory #{CourseActivityID}
              :optional #{CourseActivityID}})
(def Subscription {:person-id PersonID
                   :course-code CourseCode})
(def AcademicWeek (s/one s/Int "Week on the academic calendar: 1-52"))
(def DayNumber (s/one s/Int "Day of the week: 1-7"))
(def ScheduleSlot (s/one s/Int "Half-hour time slots from 07:00 through 23:30"))
(def ScheduledItem {:type StudyActivityType
                    :title s/Str
                    :id (s/one s/Str "e.g. course code")})
(def ScheduleBlock
  {:week AcademicWeek
   :day DayNumber
   :first-slot ScheduleSlot
   :last-slot ScheduleSlot
   :item ScheduledItem
   :room RoomID})
(def TimeSpan
  "These are used to filter schedule blocks in queries; weeks, days
  and slots are filtered separately."
  {:weeks (s/pair AcademicWeek "first week"
                  AcademicWeek "last week")
   :days (s/pair DayNumber "first day"
                 DayNumber "last day")
   :slots (s/pair ScheduleSlot "first slot"
                  ScheduleSlot "last slot")})
(def CourseActivityRequirements)


(defprotocol Rooms
  (room-add!
    [this room]
    "Add a Room.")
  (room-delete!
    [this room-id]
    "Delete a Room.")
  (room-get
    [this room-id]
    "Returns the matching Room description."))

(defprotocol Courses
  (course-add! [this course])
  (course-delete! [this course-code])
  (course-get [this course-code])
  (course-find
    [this]
    [this kws]
    "Return a list of {:title s/Str :course-code CourseCode},
    optionally filtering by the strings in kws for the name of the
    course (case insensitive and in any order)."))

(defprotocol CourseRequirements
  (course-room-requirements
    [this course-code course-activity-type]
    "Return the room requirements for a course activity."))

(defprotocol Programs
  (program-list
    [this]
    [this kws]
    "Return a list of {:title s/Str :program-id ProgramID}")
  (program-courses
    [this program-id]
    "Return a list of {:title s/Str :course-code CourseCode}"))

(defprotocol Enrollments
  (student-enrollments
    [this student-id]
    "Return a list of {:title s/Str :course-code CourseCode :program-id
  ProgramID")
  (enroll-student!
    [this student-id course-code]
    "Enroll a student for all activities in a course."))


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


(defn- in-range? [num range]
  (<= (first range) num (second range)))

(defn schedule-block-in-timespan? [block timespan]
  ;; FIXME ew
  (and (every? true?
               (map in-range?
                    (map block [:week :day])
                    (map timespan [:weeks :days])))
       (or (in-range? (:start-time block) (:time timespan))
           (in-range? (:end-time block) (:time timespan)))))

(ns xiast.schema
  "This namespace uses the prismatic/schema schema language to
  describe all data forms used in Xiast."

  (:require [schema.core :as s]))

(def RoomID {(s/optional-key :id) s/Int
             :building s/Str
             :floor s/Int
             :number s/Int})
(def RoomFacility (s/enum :beamer :overhead-projector))  ;; TODO: adding PANOPTO?
(def Room {:id RoomID
           :capacity s/Int
           :facilities #{RoomFacility}})
(def PersonID s/Str)
(def PersonFunctions #{(s/enum :program-manager
                               :titular
                               :instructor
                               :student)})
(def Person
  {:netid PersonID
   :first-name s/Str
   :last-name s/Str
   :locale s/Str})    ;; TODO: ?? maybe ENUM is better  (lavholsb)
(def StudyActivityType (s/enum :HOC :WPO))
(def Degree (s/enum :ma :ba :manama :schakel :voorbereiding))
(def SessionSemester (s/enum :1 :2 :1+2))
(def CourseCode s/Str)
(def DepartmentName s/Str)
(def Department {(s/optional-key :id) s/Int
                 :name DepartmentName
                 :faculty s/Str})
(def CourseActivityType (s/enum :HOC :WPO))
(def CourseActivity {(s/optional-key :id) s/Int
                     :type CourseActivityType
                     :semester s/Int   ;; TODO: ?? maybe using sessionsemester (lavholsb)
                     :week s/Int
                     :contact-time-hours s/Int
                     ;; TODO: fix support for multiple instructors/activity (nvgeele)
                     :instructor PersonID
                     :facilities #{RoomFacility}})
(def Course {:course-code CourseCode
             :title s/Str
             :description s/Str
             :titular PersonID
             (s/optional-key :instructors) #{PersonID}
             :department DepartmentName   ;; TODO: ?? maybe using DepartmentID? WE <> DINF? (lavholsb)
             :grade (s/enum :ba :ma)
             (s/optional-key :activities) #{CourseActivity}})
(def ProgramID s/Int)
(def Program {:title s/Str
              :description s/Str
              (s/optional-key :id) ProgramID
              (s/optional-key :manager) PersonID
              :mandatory [CourseCode]
              :optional [CourseCode]})
(def Enrollment {:course CourseCode
                 :netid PersonID})
(def Subscription {:person-id PersonID
                   :course-code CourseCode})
(def AcademicWeek (s/named s/Int "Week on the academic calendar: 1-52"))
(def DayNumber (s/named s/Int "Day of the week: 1-7"))
(def ScheduleSlot (s/named s/Int "Half-hour time slots from 07:00 through 23:30"))

(def ScheduledCourseActivity
  {:type CourseActivityType
   (s/optional-key :title) (s/named s/Str "Course title")
   :course-id CourseCode})
(def ScheduleBlockID s/Int)
(def ScheduleBlock
  {(s/optional-key :id) ScheduleBlockID
   :week AcademicWeek
   :day DayNumber
   :first-slot ScheduleSlot
   :last-slot ScheduleSlot
   :item ScheduledCourseActivity
   :room RoomID})
(def Schedule #{ScheduleBlock})
(def ScheduleProposal
  {:new #{ScheduleBlock}
   :moved #{ScheduleBlock}
   :deleted #{ScheduleBlockID}})

(def ScheduleCheckResult {:type (s/enum :mandatory-course-overlap
                                        :elective-course-overlap
                                        :room-overlap
                                        :instructor-unavailable
                                        :activity-more-than-once-weekly
                                        :room-capacity-unsatisfied
                                        :room-facility-unsatisfied)
                          :concerning #{ScheduleBlock}
                          s/Any s/Any})
(def TimeSpan
  "These are used to filter schedule blocks in queries; weeks, days
  and slots are filtered separately."
  {:weeks (s/pair AcademicWeek "first week"
                  AcademicWeek "last week")
   :days (s/pair DayNumber "first day"
                 DayNumber "last day")
   :slots (s/pair ScheduleSlot "first slot"
                  ScheduleSlot "last slot")})
(def CourseActivityRequirements
  {:facilities #{RoomFacility}
   :minimum-capacity s/Int})

;; Maps for conversion between DB and Schema

(def room-facilities
  {0 :beamer
   1 :overhead-projector
   2 :speakers})

(def course-grades
  {0 :ba
   1 :ma
   2 :manama
   3 :schakel
   4 :voorbereiding})

(def course-activity-types
  {0 :HOC
   1 :WPO})

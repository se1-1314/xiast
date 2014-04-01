(ns xiast.schema
  "This namespace uses the prismatic/schema schema language to
  describe all data forms used in Xiast."

  (:require [schema.core :as s]))

(def RoomID {(s/optional-key :id) s/Int
             :building s/Str
             :floor s/Int
             :number s/Int})
(def RoomFacility (s/enum :beamer :overhead-projector))
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
   :locale s/Str})
(def StudyActivityType (s/enum :HOC :WPO))
(def Degree (s/enum :ma :ba :manama :schakel :voorbereiding))
(def SessionSemester (s/enum :1 :2 :1+2))
(def CourseCode s/Str)
(def DepartmentName s/Str)
(def Department {:id s/Int
                 :name DepartmentName
                 (s/optional-key :faculty) s/Str})
(def CourseActivityType (s/enum :HOC :WPO))
(def CourseActivity {(s/optional-key :activity-id) s/Int
                     :type CourseActivityType
                     :semester s/Int
                     :week s/Int
                     :contact-time-hours s/Int
                     ;; TODO: fix support for multiple instructors/activity (nvgeele)
                     ;; TODO: course facility requirements (nvgeele)
                     :instructor PersonID})
(def Course {:course-code CourseCode
             :title s/Str
             :description s/Str
             :titular-id PersonID
             (s/optional-key :instructors) #{PersonID}
             :department DepartmentName
             :grade (s/enum :ba :ma)
             (s/optional-key :activities) #{CourseActivity}})
(def ProgramID s/Int)
(def Program {:title s/Str
              :description s/Str
              (s/optional-key :id) ProgramID
              (s/optional-key :manager) PersonID
              :mandatory [CourseCode]
              :optional [CourseCode]})
(def Subscription {:person-id PersonID
                   :course-code CourseCode})
(def AcademicWeek (s/one s/Int "Week on the academic calendar: 1-52"))
(def DayNumber (s/one s/Int "Day of the week: 1-7"))
(def ScheduleSlot (s/one s/Int "Half-hour time slots from 07:00 through 23:30"))
(def ScheduledItem {:type CourseActivityType
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
(def CourseActivityRequirements
  {:facilities #{RoomFacility}
   :minimum-capacity s/Int})

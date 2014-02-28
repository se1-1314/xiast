(ns xiast.query
  "This namespace provides protocols for querying information
  accessible through Xiast information stores.

  Timespans for schedule queries are maps structured as follows:

  {:weeks [1 52] ;; Weeks in the academic calendar
   :days [1 7]
   :time [1 34]} ;; Half-hour time slots from 07:00 through 23:30

  Schedule blocks are of the form:

  {:week 1
   :day 1
   :start-time 3
   :end-time 6
   :course {:title \"Course title\" :id \"Course ID\"}
   :room \"RoomId\"}

  Using the same semantics as timespans and (courses).")

(defprotocol XiastQuery
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
    timespan to limit results.")
  (program-schedule
    [this program-id]
    [this program-id timespan]
    "Return a list of schedule blocks for a whole program, optionally
    using a timespan to limit results."))


(defn- in-range? [num range]
  (<= (first range) num (second range)))

(defn schedule-block-in-timespan? [block timespan]
  ;; FIXME ew, possibly fixable by adding some multimethods
  (and (every? true?
               (map in-range?
                    (map block [:week :day])
                    (map timespan [:weeks :days])))
       (or (in-range? (:start-time block) (:time timespan))
           (in-range? (:end-time block) (:time timespan)))))

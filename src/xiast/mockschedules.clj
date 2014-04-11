(ns xiast.mockschedules
  "This namespace provides dummydata in the shape of prefab schedules, based on the programs defined in \"mockprograms.clj\" .
  More information on the internal structure of a schedule can be found in \"schema.clj\""
  (:require [xiast.schema :as xs]
            [xiast.mockprograms :as xmps]
            [schema.core :as s]
            [schema.macros :as sm])
  (:use [clojure.set :only [union]]))


(s/defn course-to-scheduled-course-activities ;; TODO :- #{ScheduledCourseActivity}  (lavholsb)
  "Converts CourseActivities from a course to ScheduledCourseActivities"
  [course] ;; TODO :- Course]
  (->> course
    :activities
    (map (fn [course-activity]
           {:type (:type course-activity)
            :course-id (:course-code course)}))
    set)) ;; return #{ {HOC} {WPO}}

(defn gen-activity-schedule-blocks
  "Generates schedule blocks"
  [[fromweek tillweek] day [fromslot tillslot] scheduledcourseactivity roomid]
  (set
    (for [week (range fromweek (+ tillweek 1))]
      {:week week
       :day day
       :first-slot fromslot
       :last-slot tillslot
       :item scheduledcourseactivity
       :room  roomid})))

;; FIXME: Dirty hack to create distinction between HOC <> WPO  // (first/last scheduled-course-activities) -> not longer possible  in future (until there is only one HOC/WPO per course)
(defn gen-course-schedule-blocks [course [fromweek tillweek] day-hoc [fromslot-hoc tillslot-hoc] room-hoc day-wpo [fromslot-wpo tillslot-wpo] room-wpo]
  "Generates scheduleblocks for a course given start/endweek, day, start/endslot roomid"
  (let
    [scheduled-course-activities (course-to-scheduled-course-activities course)]
    (set
      (union (gen-activity-schedule-blocks [fromweek tillweek] day-hoc [fromslot-hoc tillslot-hoc] (first scheduled-course-activities) (room-hoc :id))
        (gen-activity-schedule-blocks [fromweek tillweek] day-wpo [fromslot-wpo tillslot-wpo] (last scheduled-course-activities) (room-wpo :id))))))

;; -----------------------------------------------------------------------------------------------------------------------------------------------------

(def ba_cw1_schedule
  (union
    (gen-course-schedule-blocks xmps/linear-algebra [2 10] 5 [7 10] xmps/D0-05 1 [17 20] xmps/G1-023)
    (gen-course-schedule-blocks xmps/foundations-of-informatics1 [2 14] 2 [17 20] xmps/F5-403 2 [7 10] xmps/G1-022)
    (gen-course-schedule-blocks xmps/algorithms-and-datastructures1 [8 36] 4 [7 10] xmps/E0-04 5 [9 12] xmps/F4-412)
    (gen-course-schedule-blocks xmps/introduction-to-databases [22 36] 5 [5 8] xmps/G1-022 3 [13 16] xmps/E0-06)
    (gen-course-schedule-blocks xmps/discrete-mathematics [2 14] 1 [3 8] xmps/G1-022 1 [13 16] xmps/E0-05)))

(def ba_cw3_schedule
  (union
    (gen-course-schedule-blocks xmps/software-engineering [2 36] 3 [7 10] xmps/F4-412 2 [23 24] xmps/E0-05)
    (gen-course-schedule-blocks xmps/teleprocessing [2 14] 3 [13 19] xmps/E0-06 4 [13 20] xmps/E0-06)
    (gen-course-schedule-blocks xmps/economics-for-business [2 14] 5 [3 6] xmps/D0-05 5 [7 8] xmps/D0-05)
    (gen-course-schedule-blocks xmps/interpretation2 [2 14] ))



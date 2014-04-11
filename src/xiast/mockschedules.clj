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

;; FIXME: Dirty: needed for testcases scheduler clashes:mandatory<|> mandatory, mandatory <|> optional, optional <|> optional, no clash (more information: aleijnse) (lavholsb)
(defn gen-course-schedule-blocks-hoc-only [course [fromweek tillweek] day-hoc [fromslot-hoc tillslot-hoc] room-hoc]
  "Generates scheduleblocks for a course given start/endweek, day, start/endslot roomid, ONLY FOR THE HOC!"
  (let
    [scheduled-course-activities (course-to-scheduled-course-activities course)]
    (set
      (gen-activity-schedule-blocks [fromweek tillweek] day-hoc [fromslot-hoc tillslot-hoc] (first scheduled-course-activities) (room-hoc :id)))))

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
    (gen-course-schedule-blocks xmps/interpretation2 [2 14] 2 [13 18] xmps/E0-04 2 [19 22] xmps/E0-04)
    (gen-course-schedule-blocks xmps/social-psychology [2 6] 4 [13 16] xmps/D0-03 4 [17 20] xmps/D0-03)))


(def mandatory1 (gen-course-schedule-blocks-hoc-only xmps/introduction-to-databases [2 2] 1 [5 8] xmps/F4-412)) ;; mandatory -> overlap 1
(def mandatory2 (gen-course-schedule-blocks-hoc-only xmps/foundations-of-informatics1 [2 2] 1 [7 10] xmps/F5-403)) ;; mandatory -> overlap 1, 2
(def optional1 (gen-course-schedule-blocks-hoc-only xmps/social-psychology [2 2] 1 [9 12] xmps/D0-03)) ;; optional -> overlap 2
(def optional2 (gen-course-schedule-blocks-hoc-only xmps/algorithms-and-datastructures1 [2 2] 1 [11 14] xmps/E0-05)) ;; optional  -> no overlap
(def check-overlap-results #{{:type :mandatory-course-overlap
                              :concerning #{mandatory1 mandatory2}}
                             {:type :elective-course-overlap
                              :concerning #{mandatory2 optional1}}})
(def ba_IRCW3
  (union mandatory1 mandatory2 optional1 optional2))
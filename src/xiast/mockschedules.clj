(ns xiast.mockschedules
  "This namespace provides dummydata in the shape of prefab schedules, based on the programs defined in \"mockprograms.clj\" .
  More information on the internal structure of a schedule can be found in \"schema.clj\""
  (:require [xiast.schema :as xs]
            [xiast.mockprograms :as xmps]
            [schema.core :as s]
            [schema.macros :as sm])
  (:use [clojure.set :only [union]]))


(defn gen-course-schedule-blocks
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

(defn gen-course-activity-schedule-blocks
  "Generates scheduleblocks from courseactivities"
  [courseactivities day [fromslot tillslot] roomid]
  (set
    (for [week])))

(defn ba_cw1_schedule (union (gen-course-schedule-blocks xmps/linear-algebra)))

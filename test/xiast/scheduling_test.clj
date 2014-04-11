(ns xiast.scheduling-test
  (:use midje.sweet
        xiast.schema)
  (:require [xiast.scheduling :as sched]
            [xiast.query :as query]
            [schema.core :as s]))

(def b1 {:id 1
         :week 1 :day 1 :first-slot 1 :last-slot 2
         :room {:building "E" :floor 1 :number 1}
         :item {:type :HOC
                :course-id "c1"}})
(def b2 {:id 2
         :week 1 :day 1 :first-slot 2 :last-slot 3
         :room {:building "E" :floor 1 :number 1}
         :item {:type :HOC
                :course-id "c2"}})
(def b3 {:id 3
         :week 1 :day 1 :first-slot 3 :last-slot 4
         :room {:building "E" :floor 1 :number 1}
         :item {:type :WPO
                :course-id "c3"}})
(def pb4 {:week 2 :day 2 :first-slot 3 :last-slot 4
          :room {:building "E" :floor 1 :number 1}
          :item {:type :HOC
                 :course-id "c3"}})

(def pb2 (assoc b2 :day 4))
(def s1 #{b1 b2 b3})
(def p1  {:new #{pb4}
          :moved #{pb2}
          :deleted #{1}})

(def s1-without-p1-del&moved #{b3})
(def s1-after-p1 #{pb2 b3 pb4})


(doseq [x [b1 b2 b3 pb4]]
  (s/validate ScheduleBlock x))
(s/validate ScheduleProposal p1)
(doseq [s [s1 s1-after-p1]]
  (s/validate Schedule s))
(def s1-after-p1-timespan
  {:weeks [1 2]
   :days [1 2]
   :slots [2 4]})
(s/validate TimeSpan s1-after-p1-timespan)

(fact
 "Schedule block overlap detection"
 (sched/schedule-blocks-overlap? b1 b2) => true
 (sched/schedule-blocks-overlap? b2 b1) => true
 (sched/schedule-blocks-overlap? b2 b3) => true
 (sched/schedule-blocks-overlap? b3 b2) => true
 (sched/schedule-blocks-overlap? b1 b3) => false
 (sched/schedule-blocks-overlap? b3 b1) => false)

(fact
 "Schedule overlap detection"
 (sched/overlapping-schedule-blocks #{b1 b2} #{b3})
 => #{#{b2 b3}})

(fact
 "Sort blocks by programs"
 (sched/blocks-by-programs #{b1 b2 b3}) => {1 #{b1 b2}
                                            2 #{b2}
                                            3 #{b3}}
 (provided (query/course-programs "c1") => #{1}
           (query/course-programs "c2") => #{1 2}
           (query/course-programs "c3") => #{3}))

(fact
 "Remove deleted & moved from schedule"
 (sched/remove-deleted&moved s1 p1) => s1-without-p1-del&moved)

(fact
 "Apply proposal to schedule"
 (sched/apply-proposal-to-schedule s1 p1) => s1-after-p1)

(fact
 "Total timespan calculation for schedule blocks"
 (sched/schedule-timespan s1-after-p1) => s1-after-p1-timespan)

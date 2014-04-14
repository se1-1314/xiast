(ns xiast.scheduling-test
  (:use midje.sweet
        xiast.schema)
  (:require [xiast.scheduling :as sched]
            [xiast.query :as q]
            [schema.core :as s]
            [xiast.mockschedules :as mocksched]
            [xiast.mockprograms :as mockprog]))

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
 (sched/blocks-by-program-ids #{b1 b2 b3}) => {1 #{b1 b2}
                                               2 #{b2}
                                               3 #{b3}}
 (provided (q/course-programs "c1") => #{1}
           (q/course-programs "c2") => #{1 2}
           (q/course-programs "c3") => #{3}))

(fact
 "Remove deleted & moved from schedule"
 (sched/remove-deleted&moved s1 p1) => s1-without-p1-del&moved)

(fact
 "Apply proposal to schedule"
 (sched/apply-proposal-to-schedule s1 p1) => s1-after-p1)

(fact
 "Total timespan calculation for schedule blocks"
 (sched/schedule-timespan s1-after-p1) => s1-after-p1-timespan)


(def mandatory1
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/introduction-to-databases [2 2] 1 [5 8] mockprog/F4-412))
;; overlaps with mandatory1
(def mandatory2
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/foundations-of-informatics1 [2 2] 1 [7 10] mockprog/F5-403))
;; overlaps with mandatory2
(def optional1
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/social-psychology [2 2] 1 [9 12] mockprog/D0-03))
;; no overlaps
(def optional2
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/algorithms-and-datastructures1 [2 2] 1 [13 16] mockprog/E0-05))

(def man&opt-overlap-results #{{:type :mandatory-course-overlap
                                :concerning #{mandatory1 mandatory2}}
                               {:type :elective-course-overlap
                                :concerning #{mandatory2 optional1}}})
(def man&opt-overlap-check-schedule #{})
(def man&opt-overlap-check-proposal
  {:new (union mandatory1 mandatory2 optional1 optional2)})

(fact
 "Check mandatory courses"
 (sched/check-mandatory&optional overlap-test-proposal)
 => check-overlap-results
 (provided
  (sched/blocks-by-programs irrelevant)
  => {mockprog/ba-cw1 #{mocksched/mandatory1
                        mocksched/mandatory2
                        mocksched/optional2}
      mockprog/ba-cw3 #{mocksched/optional1}
      mockprog/ba-IRCW3 #{mocksched/mandatory1
                          mocksched/mandatory2
                          mocksched/optional2}}
  (q/program-schedule irrelevant irrelevant)
  => #{}))


(def room-overlap-test-schedule)
(fact
 "Check room overlaps"
 (sched/check-room-overlaps #{b1 b}))

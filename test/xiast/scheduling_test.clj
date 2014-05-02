(ns xiast.scheduling-test
  (:use midje.sweet
        xiast.schema
        clojure.set)
  (:require [xiast.scheduling :as sched]
            [xiast.query :as q]
            [schema.core :as s]
            [xiast.mockschedules :as mocksched]
            [xiast.mockprograms :as mockprog]))

(def b1 {:id 1
         :week 1 :day 1 :first-slot 1 :last-slot 2
         :room {:building "E" :floor 1 :number 1}
         :item {:type :HOC
                :course-activity 0
                :course-id "c1"}})
(def b2 {:id 2
         :week 1 :day 1 :first-slot 2 :last-slot 3
         :room {:building "E" :floor 1 :number 1}
         :item {:type :HOC
                :course-activity 0
                :course-id "c2"}})
(def b3 {:id 3
         :week 1 :day 1 :first-slot 3 :last-slot 4
         :room {:building "E" :floor 1 :number 1}
         :item {:type :WPO
                :course-activity 0
                :course-id "c3"}})
(def pb4 {:week 2 :day 2 :first-slot 3 :last-slot 4
          :room {:building "E" :floor 1 :number 1}
          :item {:type :HOC
                 :course-activity 0
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
 => #{#{b2 b3}}
 (sched/overlapping-schedule-blocks #{b1} #{b3 b2})
 => #{#{b1 b2} #{b2 b3}})

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


;; Course overlaps
(def introduction-to-databases
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/introduction-to-databases [2 2] 1 [1 8] mockprog/F4-412))
;; overlaps with introduction-to-databases
(def foundations-of-informatics
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/foundations-of-informatics1 [2 2] 1 [7 10] mockprog/F5-403))
;; overlaps with foundations-of-informatics
(def social-psychology
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/social-psychology [2 2] 1 [9 12] mockprog/D0-03))
;; no overlaps
(def algorithms-and-datastructures1
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/algorithms-and-datastructures1 [2 2] 1 [13 16] mockprog/E0-05))

(def man&opt-overlap-check-results
  #{;; ba-cw1
    {:type :mandatory-course-overlap
     :concerning (union introduction-to-databases foundations-of-informatics)}
    ;; ba-IRCW3
    {:type :elective-course-overlap
     :concerning (union foundations-of-informatics social-psychology)}})
(def man&opt-overlap-check-schedule #{})
(def man&opt-overlap-check-proposal
  {:new (union introduction-to-databases foundations-of-informatics social-psychology algorithms-and-datastructures1)})

(def man&opt-blocks-by-programs
  {mockprog/ba-cw1 (union introduction-to-databases
                          foundations-of-informatics
                          algorithms-and-datastructures1)
   mockprog/ba-cw3 (union social-psychology)
   mockprog/ba-IRCW3 (union introduction-to-databases
                            social-psychology
                            foundations-of-informatics)})

;; (and (sched/schedule-blocks-overlap? (first introduction-to-databases)
;;                                      (first foundations-of-informatics))
;;      (sched/schedule-blocks-overlap? (first foundations-of-informatics)
;;                                      (first social-psychology)))
;; => true

;; (map #(-> % :item :course-id)
;;      (map first [introduction-to-databases foundations-of-informatics social-psychology algorithms-and-datastructures1]))
;; => ("1007156ANR" "1000447ANR" "1018725AER" "1015259ANR")

;; (:mandatory mockprog/ba-cw1)
;; #{"1000447ANR" "1015328ANR" "1015259ANR" "1007156ANR"}
;; (:optional mockprog/ba-cw1)
;; #{"1007132ANR"}

;; (:mandatory mockprog/ba-cw3)
;; #{"1001714AER" "1001673BNR" "1004483BNR"}
;; (:optional mockprog/ba-cw3)
;; #{"1018725AER" "1005176BNR"}

;; (:mandatory mockprog/ba-IRCW3)
;; #{"1000447ANR" "1001673BNR" "1007156ANR" "1004483BNR"}
;; (:optional mockprog/ba-IRCW3)
;; #{"1015259ANR" "1018725AER"}

(fact
 "Check mandatory and optional course overlaps"
 (sched/check-mandatory&optional man&opt-overlap-check-proposal)
 => man&opt-overlap-check-results
 (provided
  (sched/blocks-by-programs irrelevant)
  => man&opt-blocks-by-programs
  (q/program-schedule irrelevant irrelevant)
  => #{}))

;; Room overlaps
(def roc1
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/social-psychology [1 1] 1 [1 2] mockprog/F5-403))
(def roc2
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/foundations-of-informatics1 [1 1] 1 [2 3] mockprog/F5-403))
(def roc3
  (mocksched/gen-course-schedule-blocks-hoc-only
   mockprog/introduction-to-databases [1 1] 1 [4 5] mockprog/F5-403))

(def room-overlap-check-schedule roc1)
(def room-overlap-check-proposal
  {:new (union roc2 roc3)})
(def room-overlap-check-results
  #{{:type :room-overlap
     :concerning (union roc1 roc2)}})

(fact
 "Check room overlaps"
 (sched/check-room-overlaps room-overlap-check-proposal)
 => room-overlap-check-results
 (provided
  (q/room-schedules irrelevant irrelevant)
  => roc1))

;; Instructor availabilities
#_(def iac1
  (mocksched/gen-course-schedule-block ))

#_(fact
 "Check instructor availabilities"
 (sched/check-instructor-availabilities instructor-availabilities-porp))

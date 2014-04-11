(ns xiast.scheduling
  (:require [xiast.schema :as xs]
            [clojure.math.combinatorics :as comb]
            [schema.core :as s]
            [xiast.query :as query])
  (:use [clojure.set :only [difference]]))

;; Database stuff
;; (s/defn room-capacity-satisfied? :- s/Bool
;;   [room-id :- xs/RoomID
;;    schedule-item :- xs/ScheduledCourse]
;;   ;; TODO: expected amount of student in course(activity)
;;   true)
;; (s/defn room-facilities-satisfied? :- s/Bool
;;   [room-id :- xs/RoomID
;;    schedule-item :- xs/ScheduledItem]
;;   (if (contains? (set :HOC :WPO)
;;                  (:type schedule-item))
;;     (let [required (query/course-activity-facilities (:id schedule-item))
;;           available (:facilities (query/room-facilities room-id))]
;;       (empty? (difference required available)))
;;     true))
;; (defn instructor-available? [instructor-id timespan]
;;   ;; TODO
;;   true)
;; (defn blocks-for-courses [course-ids timespan]
;;   ;; TODO
;;   true)
;; (defn blocks-for-rooms [room-ids timespan]
;;   ;; TODO
;;   true)
(s/defn elective-course? :- s/Bool
  [course-id :- xs/CourseCode
   program-id :- xs/ProgramID]
  (let [program (query/program-get program-id)]
    (contains? (:optional program) course-id)))
(s/defn mandatory-course? :- s/Bool
  [course-id :- xs/CourseCode
   program-id :- xs/ProgramID]
  (let [program (query/program-get program-id)]
    (contains? (:mandatory program) course-id)))

;; Work on schedule block items
(s/defn schedule-blocks-overlap? :- s/Bool
  [b1 :- xs/ScheduleBlock
   b2 :- xs/ScheduleBlock]
  (and (= (:week b1) (:week b2))
       (= (:day b1) (:day b2))
       (or (<= (:first-slot b1) (:first-slot b2) (:last-slot b1))
           (<= (:first-slot b1) (:last-slot b2) (:last-slot b1)))))
(s/defn overlapping-schedule-blocks :- [(s/pair xs/ScheduleBlock ""
                                                xs/ScheduleBlock "")]
  [sched1 :- xs/Schedule
   sched2 :- xs/Schedule]
  "Return a seq of pairs of overlapping schedule blocks between two
  schedules."
  (set (for [[b1 b2] (comb/cartesian-product sched1 sched2)
             :when (schedule-blocks-overlap? b1 b2)]
         (set [b1 b2]))))
;; Work on seqs of schedule blocks
(s/defn mandatory-blocks :- xs/Schedule
  [blocks :- xs/Schedule
   program-id :- xs/ProgramID]
  (filter #(mandatory-course? (-> % :item :course-id)
                              program-id)
          blocks))
(s/defn elective-blocks :- xs/Schedule
  [blocks :- xs/Schedule
   program-id :- xs/ProgramID]
  )
(s/defn blocks-by-programs :- {xs/ProgramID xs/Schedule}
  [schedule :- xs/Schedule]
  "Return a map of Programs to schedule blocks. The programs are those
  to which the scheduled course belongs to."
  (->> schedule
       (mapcat (fn [block]
                 (comb/cartesian-product (query/course-programs
                                          (-> block :item :course-id))
                                         [block])))
       (reduce (fn [program-id-schedule-map [program-id block]]
                 (update-in program-id-schedule-map
                            [program-id]
                            #(clojure.set/union % #{block})))
               {})))
(s/defn remove-deleted&moved :- xs/Schedule
  [schedule :- xs/Schedule
   proposal :- xs/ScheduleProposal]
  "Produce a new schedule based on schedule, with the blocks which
  share the same ids between schedule and proposal removed, as well as
  the blocks with :deleted? set in the proposal."
  (let [deleted&moved-block-ids
        (set (concat (map :id (:moved proposal)) (:deleted proposal)))]
    (set (filter #(not (contains? deleted&moved-block-ids (:id %)))
                 schedule))))
(s/defn apply-proposal-to-schedule :- xs/Schedule
  [schedule :- xs/Schedule
   proposal :- xs/ScheduleProposal]
  (clojure.set/union (remove-deleted&moved schedule proposal)
                     (:moved proposal)
                     (:new proposal)))
;; This is the best thing ever written.
(s/defn schedule-timespan :- xs/TimeSpan
  [schedule :- xs/Schedule]
  "Return the timespan covered by schedule blocks"
  (let [init-range [Float/POSITIVE_INFINITY Float/NEGATIVE_INFINITY]]
    (reduce (fn [{[w< >w] :weeks
                  [d< >d] :days
                  [s< >s] :slots
                  :as span}
                 {d :day
                  w :week
                  bs< :first-slot
                  >bs :last-slot}]
              {:weeks [(if (< w w<) w w<) (if (> w >w) w >w)]
               :days [(if (< d d<) d d<) (if (> w >w) w >w)]
               :slots [(if (< bs< s<) bs< s<) (if (> >bs >s) >bs >s)]})
            {:weeks init-range
             :days init-range
             :slots init-range}
            schedule)))
(s/defn electives-schedule :- xs/Schedule
  [program-id :- xs/ProgramID
   schedule :- xs/Schedule]
  (filter #(elective-course? (-> % :item :course-id) program-id)
          schedule))

;; Checks
;;-------

;; Check-mandatory and check-elective need to know:
;; - mandatory/optional status for each schedule block in proposal
;; - programs for each schedule block in proposal
;; - the schedules for the mandatory/optional courses in those programs
;; -> if mandatory and elective courses are checked together,
;;    the whole schedule for all programs involved could be supplied
;; Check room overlaps needs to know:
;; -> room schedules for proposal blocks
(comment
  (defn check-mandatory-courses [proposal]
   "Checks if there are overlaps in time for mandatory course
   activities."
   ;; Sort proposals by the programs they belong to
   (->> (for [[program proposal] (blocks-by-programs proposal)]
          (let [ ;; Mandatory courses in proposal
                proposal-mandatory (mandatory-blocks                                                             program proposed)]
            (-> schedule
                ;; Schedule for mandatory courses in program
                (schedule-for-courses (:mandatory program)
                                      (blocks-timespan proposed))
                (remove-moves proposal-mandatory)
                ;; Overlapping pairs between the two
                (overlapping-schedule-blocks proposal-mandatory))))
        flatten
        (map (fn [blocks]
               {:type :mandatory-course-overlap
                :concerning blocks}))))
  ;; TODO this is almost the same as check-mandatory-courses and could
  ;; be done in one go
  (defn check-elective-courses [proposed]
    (->> (for [[program-id proposed] (blocks-by-programs proposed)]
           (let [proposal-electives (filter #(elective-course?
                                              (-> % :item :course-id)
                                              program-id)
                                            proposed)]
             (-> schedule
                 (blocks-for-courses (:optional program)
                                     (blocks-timespan proposed))
                 (remove-moves proposal-electives)
                 (overlapping-schedule-blocks proposal-electives))))
         flatten
         (map (fn [blocks]
                {:type :elective-course-overlap
                 :concerning blocks}))))

  (defn check-room-overlaps [proposed]
    (->> (-> schedule
             (blocks-for-rooms (map :room proposed)
                               (blocks-timespan proposed))
             (remove-moves proposed)
             (overlapping-schedule-blocks proposed))
         (filter #(= (:room (first %))
                     (:room (second %))))
         (map (fn [block]
                {:type :room-overlap
                 :concerning [block]}))))

  (defn check-instructor-availabilities [proposed]
    (->> proposed
         (filter #(not (instructor-available? (:instructor (:item %))
                                              (:timespan %))))
         (map (fn [block]
                {:type :instructor-unavailable
                 :concerning [block]}))))
  (defn check-weekly-activity [proposed]
    "Check if activities occur more than once per week"
    (->> proposed
         (group-by #(list (:week %) (:course %) (:activity %)))
         (map second)
         (filter #(> (count %) 1))
         flatten
         (map (fn [blocks]
                {:type :activity-more-than-once-weekly
                 :concerning blocks}))))
  (defn check-room-capacities [proposed]
    (->> proposed
         (filter #(room-capacity-satisfied? (:room %) (:item %)))
         (map (fn [block]
                {:type :room-capacity-unsatisfied
                 :concerning [block]}))))
  (defn check-room-facilities [proposed]
    (->> proposed
         (filter #(room-facilities-satisfied? (:room %) (:item %)))
         (map (fn [block]
                {:type :room-facility-unsatisfied
                 :concerning [block]}))))
  (defn proposal-checks
    [check-mandatory-courses
     check-elective-courses
     check-room-overlaps
     check-instructor-available
     check-weekly-activity
     check-room-capacities
     check-room-facilities])
  (s/defn check-proposal :- [ScheduleCheckResult]
    [proposal :- Schedule]
    (->> proposal-checks
         (map #(% proposal))
         flatten))

  )

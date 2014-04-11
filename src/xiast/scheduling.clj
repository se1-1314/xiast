(ns xiast.scheduling
  (:require [clojure.math.combinatorics :as comb]
            [schema.core :as s]
            [xiast.query :as q])
  (:use [clojure.set :only [difference rename-keys]]
        [xiast.schema]))

;; Database stuff
;; (s/defn room-capacity-satisfied? :- s/Bool
;;   [room-id :- RoomID
;;    schedule-item :- ScheduledCourse]
;;   ;; TODO: expected amount of student in course(activity)
;;   true)
;; (s/defn room-facilities-satisfied? :- s/Bool
;;   [room-id :- RoomID
;;    schedule-item :- ScheduledItem]
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

;; Work on schedule block items
(s/defn schedule-blocks-overlap? :- s/Bool
  [b1 :- ScheduleBlock
   b2 :- ScheduleBlock]
  (and (= (:week b1) (:week b2))
       (= (:day b1) (:day b2))
       (or (<= (:first-slot b1) (:first-slot b2) (:last-slot b1))
           (<= (:first-slot b1) (:last-slot b2) (:last-slot b1)))))
(s/defn overlapping-schedule-blocks :- [(s/pair ScheduleBlock ""
                                                ScheduleBlock "")]
  [correct-schedule :- Schedule
   proposed-schedule :- Schedule]
  "Return a seq of pairs of overlapping schedule blocks between a
  schedule without overlaps and a proposal schedule which might
  contain overlaps."
  (->> (concat
        (comb/cartesian-product correct-schedule proposed-schedule)
        (remove = (comb/cartesian-product proposed-schedule
                                          proposed-schedule)))
       (filter #(apply schedule-blocks-overlap? %))
       (map #(set %))
       set))
;; Work on seqs of schedule blocks
(s/defn mandatory-blocks :- Schedule
  [blocks :- Schedule
   program-id :- ProgramID]
  (filter #(mandatory-course? (-> % :item :course-id)
                              program-id)
          blocks))
(s/defn blocks-by-program-ids :- {ProgramID Schedule}
  [schedule :- Schedule]
  "Return a map of ProgramIDS to Schedules. The programs are those to
  which the scheduled course belongs to."
  (->> schedule
       (mapcat (fn [block]
                 (comb/cartesian-product (q/course-programs
                                          (-> block :item :course-id))
                                         [block])))
       (reduce (fn [program-id-schedule-map [program-id block]]
                 (update-in program-id-schedule-map
                            [program-id]
                            #(clojure.set/union % #{block})))
               {})))
(s/defn blocks-by-programs :- {Program Schedule}
  [schedule :- Schedule]
  (let [bbpi (blocks-by-program-ids schedule)]
    (rename-keys bbpi
                 (zipmap (keys bbpi)
                         (map q/program-get (keys bbpi))))))

(s/defn remove-deleted&moved :- Schedule
  [schedule :- Schedule
   proposal :- ScheduleProposal]
  "Produce a new schedule based on schedule, with the blocks which
  share the same ids between schedule and proposal removed, as well as
  the blocks with :deleted? set in the proposal."
  (let [deleted&moved-block-ids
        (set (concat (map :id (:moved proposal)) (:deleted proposal)))]
    (set (filter #(not (contains? deleted&moved-block-ids (:id %)))
                 schedule))))
(s/defn proposal-new&moved :- Schedule
  [proposal :- ScheduleProposal]
  (clojure.set/union (:moved proposal) (:new proposal)))
(s/defn apply-proposal-to-schedule :- Schedule
  [schedule :- Schedule
   proposal :- ScheduleProposal]
  (clojure.set/union (remove-deleted&moved schedule proposal)
                     (proposal-new&moved proposal)))
;; This is the best thing ever written.
(s/defn schedule-timespan :- TimeSpan
  [schedule :- Schedule]
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
(s/defn electives-schedule :- Schedule
  [program-id :- ProgramID
   schedule :- Schedule]
  (filter #(elective-course? (-> % :item :course-id) program-id)
          schedule))

;; Checks
;;-------
(s/defn check-mandatory&optional :- [ScheduleCheckResult]
  [proposal :- ScheduleProposal]
  "Checks if there are overlaps in time for mandatory course
   activities."
  (->> (for [[program prop-schedule]
             (blocks-by-programs (proposal-new&moved proposal))]
         (let [mandatory-courses (:mandatory program)]
           (-> (:id program)
               (q/program-schedule (schedule-timespan prop-schedule))
               (remove-deleted&moved proposal)
               (overlapping-schedule-blocks prop-schedule)
               (->>
                (map (fn [[b1 b2 :as blocks]]
                       {:type (if (and (contains? mandatory-courses
                                                  (-> b1 :item :course-id))
                                       (contains? mandatory-courses
                                                  (-> b2 :item :course-id)))
                                :mandatory-course-overlap
                                :elective-course-overlap)
                        :concerning blocks}))))))
       (apply concat)))
(comment
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
         flatten)))

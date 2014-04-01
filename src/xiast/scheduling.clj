(ns xiast.scheduling
  (:require [clojure.math.combinatorics :as comb]
            [schema.core :as s]))

(defprotocol XiastSchedule
  (move-block [id to])
  (room-blocks [room-id timespan])
  (schedule-for-courses [schedule courses timespan]))

(def CheckResult {:type (s/enum :mandatory-course-overlap
                                :elective-course-overlap
                                :room-overlap
                                :instructor-unavailable
                                :activity-more-than-once-weekly
                                :room-capacity-unsatisfied
                                :room-facility-unsatisfied)
                  :concerning [xs/ScheduleBlock]
                  s/Any s/Any})
;; Database stuff
(defn room-capacity-satisfied? [room-id schedule-item]
  ;; TODO
  )
(defn room-facilities-satisfied? [room-id schedule-item]
  ;; TODO
  )
(defn instructor-available? [instructor-id timespan]
  ;; TODO
  )
(defn mandatory-course-in-program? [course-id program-id]
  ;; TODO
  )
(defn blocks-for-courses [schedule course-ids timespan]
  ;; TODO
  )
(defn blocks-for-rooms [schedule room-ids timespan]
  ;; TODO
  )
(defn elective-course? [course-id program-id]
  ;; TODO
  )
(defn mandatory-course? [course-id program-id]
  ;; TODO
  )
(defn course-programs [schedule course-id]
  "Return seq of program-ids for programs which contain course with
  course-id"
  ;; TODO
  )
;; Work on schedule block items
(defn course-item? [item]
  (contains? (set :HOC :WPO)
             (:type item)))
(defn schedule-blocks-overlap? [b1 b2]
  (and (= (:week b1) (:week b2))
       (= (:day b1) (:day b2))
       (or (>= (:last-slot b1) (:first-slot b2))
           (<= (:first-slot b1) (:last-slot b2)))))
;; Work on seqs of schedule blocks
(defn mandatory-blocks [blocks program-id]
  (filter #(and (course-item? (:item %))
                (mandatory-course? (-> :item :course-id %)
                                   program-id))
          blocks))
(defn elective-blocks [blocks program-id]
  (filter #(and (course-item? (:item %))
                (elective-course? (-> :item :course-id %)
                                  program-id))
          blocks))
(defn blocks-by-programs [blocks]
  "Return a map of Programs to schedule blocks. The programs are those
  to which the scheduled course belongs to."
  (let [programs-blocks (for [block blocks]
                          (let [item (:item block)]
                            (if (course-item? item)
                              [(course-programs (:course-id item))
                               block])))]
    (reduce (fn [programs-blocks-map [programs block]]
              (for [p programs]
                (update-in programs-blocks-map
                           [p]
                           #(conj % block))))
            {}
            programs-blocks)))
(defn overlapping-schedule-blocks [sched1 sched2]
  "Return a seq of pairs of overlapping schedule blocks between two
  schedules."
  (for [[b1 b2] (comb/cartesian-product sched1 sched2)
        :when #(schedule-blocks-overlap? b1 b2)]
    [b1 b2]))
(defn remove-moves [sched1 sched2]
  "Produce a new schedule based on sched1, with the blocks which share
  the same ids between sched1 and sched2 removed."
  (let [block-ids (set (map :id sched2))]
    (filter #(contains? block-ids (:id %))
            sched1)))
(defn rooms-in-blocks [blocks]
  (map :room blocks))
;; This is the best thing ever written.
(defn blocks-timespan [blocks]
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
            blocks)))
(defn elective-blocks [program-id blocks]
  (filter #(and (course-item? (:item %))
                (elective-course? (-> % :item :course-id) program-id))
          blocks))
;; Checks
(defn check-mandatory-courses [schedule proposed]
  "Checks if there are overlaps in time for mandatory course
   activities."
  ;; Sort proposals by the programs they belong to
  (->> (for [[program proposed] (blocks-by-programs proposed)]
         (let [;; Mandatory courses in proposal
               proposal-mandatory (mandatory-blocks program proposed)]
           (-> schedule
               ;; Schedule for mandatory courses in program
               (schedule-for-courses (:mandatory program)
                                     (blocks-timespan proposed))
               (remove-moves proposal-mandatory)
               ;; Overlapping pairs between the two
               (overlapping-schedule-blocks proposal-mandatory))))
       flatten
       (map (fn []
              {:type :mandatory-course-overlap
               :concerning %}))))
;; TODO this is almost the same as check-mandatory-courses and could
;; be done in one go
(defn check-elective-courses [schedule proposed]
  (->> (for [[program proposed] (blocks-by-programs proposed)]
         (let [proposal-electives (elective-blocks proposed program)]
           (-> schedule
               (blocks-for-courses (:optional program)
                                   (blocks-timespan proposed))
               (remove-moves proposal-electives)
               (overlapping-schedule-blocks proposal-electives))))
       flatten
       (map (fn [blocks]
              {:type :elective-course-overlap
               :concerning blocks}))))

(defn check-room-overlaps [schedule proposed]
  (->> (-> schedule
           (blocks-for-rooms (rooms-in-blocks proposed)
                             (blocks-timespan proposed))
           (remove-moves proposed)
           (overlapping-schedule-blocks proposed))
       (filter #(= (:room (first %))
                   (:room (second %))))
       (map (fn [block]
              {:type :room-overlap
               :concerning [block]}))))

(defn check-instructor-availabilities [schedule proposed]
  (->> proposed
       (filter #(not (instructor-available? (:instructor (:item %))
                                            (:timespan %))))
       (map (fn [block]
              {:type :instructor-unavailable
               :concerning [block]}))))
(defn check-weekly-activity [schedule proposed]
  "Check if activities occur more than once per week"
  (->> proposed
       (group-by #(list (:week %) (:course %) (:activity %)))
       (map second)
       (filter #(> (count %) 1))
       flatten
       (map (fn [blocks]
              {:type :activity-more-than-once-weekly
               :concerning blocks}))))
(defn check-room-capacities [schedule proposed]
  (->> proposed
       (filter #(room-capacity-satisfied? (:room %) (:item %)))
       (map (fn [block]
              {:type :room-capacity-unsatisfied
               :concerning [block]}))))
(defn check-room-facilities [schedule proposed]
  (->> proposed
       (filter #(room-facilities-satisfied? (:room %) (:item %)))
       (map (fn [block]
              {:type :room-facility-unsatisfied
               :concerning [block]}))))

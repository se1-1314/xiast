(ns xiast.scheduling
  (:require [clojure.math.combinatorics :as comb]
            [schema.core :as s]
            [xiast.query.core :as q])
  (:use [clojure.set :only [union difference rename-keys]]
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
        (remove (fn [[a b]] (= a b))
                (comb/cartesian-product proposed-schedule
                                        proposed-schedule)))
       (filter #(apply schedule-blocks-overlap? %))
       (map #(set %))
       set))

(def ^:dynamic *overlapping-schedule-blocks*
  overlapping-schedule-blocks)
;; Work on seqs of schedule blocks

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
;; Checks
;;-------

(s/defn check-room-overlaps :- #{ScheduleCheckResult}
  [proposal :- ScheduleProposal]
  (->> (-> (q/room-schedules
            (map :room (proposal-new&moved proposal))
            (schedule-timespan (proposal-new&moved proposal)))
           (apply-proposal-to-schedule proposal)
           (*overlapping-schedule-blocks*
            (proposal-new&moved proposal)))
       (filter #(= (:room (first %))
                   (:room (second %))))
       (map (fn [block-pair]
              {:type :room-overlap
               :concerning block-pair}))
       set))

(s/defn check-mandatory&optional :- #{ScheduleCheckResult}
  [proposal :- ScheduleProposal]
  "Checks if there are overlaps in time for mandatory or optional
   course activities."
  (->> (for [[program prop-schedule] (blocks-by-programs
                                      (proposal-new&moved proposal))]
         (-> (:id program)
             (q/program-schedule (schedule-timespan prop-schedule))
             (remove-deleted&moved proposal)
             (*overlapping-schedule-blocks* prop-schedule)
             (->>
              (map seq) ;; Hurray, no destructuring on sets
              (map (fn [[b1 b2 :as blocks]]
                     {:type
                      (if (let [mandatory-courses (:mandatory program)]
                            (and (contains? mandatory-courses
                                            (-> b1 :item :course-id))
                                 (contains? mandatory-courses
                                            (-> b2 :item :course-id))))
                        :mandatory-course-overlap
                        :elective-course-overlap),
                      :concerning
                      (set blocks)})))))
       (apply concat)
       set))

(def proposal-checks
    [check-room-overlaps
     check-mandatory&optional
     ;; check-instructor-available
     ;; check-weekly-activity
     ;; check-room-capacities
     ;; check-room-facilities
     ])

(s/defn check-proposal :- #{ScheduleCheckResult}
  [proposal :- ScheduleProposal]
  (->> proposal-checks
       (map #(% proposal))
       (apply union)))

(s/defn blocks-in-timespan :- [ScheduleBlock]
  [{[first-week last-week] :weeks
    [first-day last-day] :days
    [first-slot last-slot] :slots} :- TimeSpan
   block-length :- (s/named s/Int "Number of schedule slots needed")
   item :- ScheduledCourseActivity
   ;;room :- RoomID
   ]
  (->> (comb/cartesian-product
        (range first-week (+ last-week 1))
        (range first-day (+ last-day 1))
        (range first-slot (+ last-slot 1)))
       (remove #(> (nth % 2) (- last-slot block-length -1)))
       (map (fn [[w d s]]
              {:week w :day d
               :first-slot s
               :last-slot (+ s block-length -1)
               :item item
               ;; :room room
               ::available true}))))

(s/defn available-blocks-in-timespan :- [ScheduleBlock]
  [timespan :- TimeSpan
   block-length :- (s/named s/Int "Number of schedule slots needed")
   course-activity :- ScheduledCourseActivity
   proposal :- ScheduleProposal]
  "List the available blocks for a specific course-activity within a
  specific time span. The supplied schedule proposal will be applied
  before checking available blocks."
  (let [blocks (set (blocks-in-timespan timespan
                                        block-length
                                        course-activity))]
    (->> (merge-with union proposal {:new blocks})
         (#(binding [*overlapping-schedule-blocks*
                     (fn [old prop]
                       (overlapping-schedule-blocks
                        old
                        (set (remove ::available prop))))]
             (check-proposal %)))
         (map #(-> % :concerning))
         (apply union)
         (difference blocks)
         (map #(dissoc % ::available)))))

(s/defn filter-rooms-by-block&proposal :- [RoomID]
  [rooms :- [Room]
   block :- ScheduleBlock
   proposal :- ScheduleProposal]
  (let [relevant-rooms-in-proposal
        (->> proposal
             proposal-new&moved
             (filter #(schedule-blocks-overlap? % block))
             (map #(-> % :room))
             set)]
    (remove relevant-rooms-in-proposal
            (map #(-> % :id) rooms))))

(comment
  (defn check-instructor-availabilities :- #{ScheduleCheckResult}
    [proposed :- ScheduleProposal]
    (->> proposed
         (filter #(not (instructor-available? (:instructor (:item %))
                                              (:timespan %))))
         (map (fn [block]
                {:type :instructor-unavailable
                 :concerning #{block}}))))
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
                 :concerning [block]})))))

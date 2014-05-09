(ns xiast.query.core
  "This namespace provides protocols for querying and updating
  information accessible through Xiast information stores."

  (:require [clojure.edn :as edn]
            [schema.core :as s]
            [xiast.schema :as xs]
            [clojure.set :as cset])
  (:use [clojure.set :only [map-invert]]
        [xiast.schema :only [room-facilities course-grades course-activity-types message-status]]
        [xiast.database]
        [korma.db]
        [korma.core]
        [slingshot.slingshot :only [throw+ try+]]))

(load "core_convert")
(load "core_enrollment")

(defn- schedule-blocks-in-timespan
  ([timespan]
     (schedule-blocks-in-timespan timespan {}))
  ([timespan constraints]
     (let [blocks
           (select schedule-block
                   (where (and (merge constraints
                                      {:week [>= (first (:weeks timespan))]
                                       :day [>= (first (:days timespan))]
                                       :first-slot [>= (first (:slots timespan))]}
                                      {:week [<= (second (:weeks timespan))]
                                       :day [<= (second (:days timespan))]
                                       :last-slot [<= (second (:slots timespan))]}))))]
       blocks)))

(s/defn schedule-block-add! :- xs/ScheduleBlockID
  [block :- xs/ScheduleBlock]
  (let [course-activity (:course-activity (:item block))
        room-id (:id (first (select room (where (:room block)))))
        key (insert schedule-block
                    (values {:week (:week block)
                             :day (:day block)
                             :first-slot (:first-slot block)
                             :last-slot (:last-slot block)
                             :room room-id
                             :course-activity course-activity}))]
    (:GENERATED_KEY key)))

(s/defn schedule-block-get :- (s/maybe xs/ScheduleBlock)
  [schedule-block-id :- xs/ScheduleBlockID]
  (let [block (select schedule-block
                      (where {:id schedule-block-id}))]
    (if (empty? block)
      nil
      (schedule-block->sScheduleBlock (first block)))))

(s/defn course-schedule :- xs/Schedule
  [course-code :- xs/CourseCode
   timespan :- xs/TimeSpan]
  "Returns the schedule for a certain course in the provided timespan."
  (let [activities (select course-activity
                           (where {:course-code course-code})
                           (fields :id))
        blocks (map #(schedule-blocks-in-timespan timespan {:course-activity (:id %)})
                    activities)]
    (map schedule-block->sScheduleBlock (apply concat blocks))))

(s/defn student-schedule :- xs/Schedule
  [student-id :- xs/PersonID
   timespan :- xs/TimeSpan]
  "Returns the schedules for courses a certain student is enrolled in the
   provided timespan."
  (let [courses
        (map :course-code (enrollments-student student-id))
        schedules
        (map #(course-schedule % timespan) courses)]
    (mapcat identity schedules)))

(s/defn room-schedule :- xs/Schedule
  [room-id :- xs/RoomID
   timespan :- xs/TimeSpan]
  "Returns the schedule for a certain room in the provided timespan."
  (let [room-id (:id (first (select room (where room-id))))
        blocks (schedule-blocks-in-timespan timespan {:room room-id})]
    (map schedule-block->sScheduleBlock blocks)))

(s/defn room-schedules :- xs/Schedule
  [room-ids :- [xs/RoomID]
   timespan :- xs/TimeSpan]
  "Return the schedule for multiple rooms in the provided timespan"
  (apply clojure.set/union (map #(room-schedule % timespan) room-ids)))

(s/defn program-schedule :- xs/Schedule
  [program-id :- xs/ProgramID
   timespan :- xs/TimeSpan]
  "Returns the schedules for all courses in a certain program in the provided
   timespan."
  (let [courses (concat (map :course-code
                             (select program-choice-course
                                     (where {:program program-id})))
                        (map :course-code
                             (select program-mandatory-course
                                     (where {:program program-id}))))
        schedules (map #(course-schedule % timespan) courses)]
    (set (mapcat identity schedules))))

(s/defn instructor-schedule :- xs/Schedule
  [instructor-id :- xs/PersonID
   timespan :- xs/TimeSpan]
  (let [blocks
        (select schedule-block
                (join course-activity
                      (= :course-activity :course-activity.id))
                (join course-instructor
                      (= :course-activity.id :course-instructor.course-activity))
                (where (and {:course-instructor.netid instructor-id}
                            {:week [>= (first (:weeks timespan))]
                             :day [>= (first (:days timespan))]
                             :first-slot [>= (first (:slots timespan))]}
                            {:week [<= (second (:weeks timespan))]
                             :day [<= (second (:days timespan))]
                             :last-slot [<= (second (:slots timespan))]})))]
    (set (map schedule-block->sScheduleBlock blocks))))

(s/defn program-manager-schedule :- xs/Schedule
  [manager :- xs/PersonID
   timespan :- xs/TimeSpan]
  (let [activities
        (union
         (queries
          (subselect course-activity
                     (fields :id)
                     (join program-mandatory-course
                           (= :course-code :program-mandatory-course.course-code))
                     (join program
                           (= :program.id :program-mandatory-course.program))
                     (where {:program.manager manager})
                     (modifier "DISTINCT"))
          (subselect course-activity
                     (fields :id)
                     (join program-choice-course
                           (= :course-code :program-choice-course.course-code))
                     (join program
                           (= :program.id :program-choice-course.program))
                     (where {:program.manager manager})
                     (modifier "DISTINCT"))))
        schedule-blocks
        (map #(schedule-blocks-in-timespan timespan {:course-activity (:id %)})
             activities)]
    (map schedule-block->sScheduleBlock
         (mapcat identity schedule-blocks))))

;; TODO: Put this in schedule and refactor
(s/defn schedule-proposal-apply! :- s/Any
  [proposal :- xs/ScheduleProposal]
  (doseq [new (:new proposal)]
    (schedule-block-add! (dissoc new :id)))
  (doseq [moved (:moved proposal)]
    (let [room-id (:id (first (select room
                                      (where (:room moved)))))]
      (update schedule-block
              (set-fields (assoc (dissoc moved [:id :item :room])
                            :room room-id))
              (where {:id (:id moved)}))))
  (doseq [deleted (:deleted proposal)]
    (delete schedule-block
            (where {:id deleted}))))

(s/defn schedule-proposal-message-add! :- s/Any
  [message :- xs/ScheduleProposalMessage]
  (let [courses-affected
        (cset/union
         (set (map (comp :course-id :item)
                   (:new (:proposal message))))
         (set (map (comp :course-id :item)
                   (:moved (:proposal message))))
         (set (map #((comp :course-code :first)
                     (select schedule-block
                             (join course-activity
                                   (= :course-activity :course-activity.id))
                             (fields :course-activity.course-code)
                             (where {:id %})))
                   (:deleted (:proposal message)))))
        programs-affected
        (set
         (map :program
              (mapcat identity
                      (map #(union (queries (subselect program-choice-course
                                                       (where {:course-code %})
                                                       (fields :program))
                                            (subselect program-mandatory-course
                                                       (where {:course-code %})
                                                       (fields :program))))
                           courses-affected))))
        message-id
        (:GENERATED_KEY
         (insert schedule-proposal-message
                 (values (assoc (dissoc message [:id :proposal :status])
                           :proposal (pr-str (:proposal message))
                           :status (:inprogress (map-invert message-status))))))]
    (doseq [program programs-affected]
      (insert schedule-proposal-message-programs
              (values {:message-id message-id
                       :program-id program})))))

(s/defn schedule-proposal-message-list :- [xs/ScheduleProposalMessage]
  ([manager :- xs/PersonID]
     (schedule-proposal-message-list manager false))
  ([manager :- xs/PersonID
    status :- xs/ScheduleProposalMessageStatus]
     (let [programs
           (set (map :id (select program
                                 (where {:manager manager})
                                 (fields :id))))
           messages
           (if (empty? programs)
             []
             ;; Joins, eval, macho code
             (eval
              `(select schedule-proposal-message
                       (join schedule-proposal-message-programs
                             (~'= :schedule-proposal-message-programs.message-id :id))
                       (where
                        ~(let [or-terms
                               (map (fn [id]
                                      `{:schedule-proposal-message-programs.program-id ~id})
                                    programs)]
                           (if (and status (keyword? status))
                             `(~'and {:status ~(status (map-invert message-status))}
                                     (~'or ~@or-terms))
                             `(~'or ~@or-terms))))
                       (modifier "DISTINCT"))))]
       (map #(assoc (dissoc % [:proposal :status])
               :proposal (edn/read-string (:proposal %))
               :status (get message-status (:status %)))
            messages))))


(s/defn schedule-proposal-message-get :- xs/ScheduleProposalMessage
  [id :- s/Int]
  (let [message
        (select schedule-proposal-message
                (where {:id id}))]
    (if (empty? message)
      nil
      (#(assoc (dissoc % [:proposal :status])
          :proposal (edn/read-string (:proposal %))
          :status (get message-status (:status %)))
       (first message)))))

(s/defn schedule-proposal-message-accept! :- s/Any
  [id :- s/Int
   manager :- xs/PersonID]
  (let [message
        (schedule-proposal-message-get id)
        programs
        (select schedule-proposal-message-programs
                (join program (= :program-id :program.id))
                (where {:program.manager manager
                        :message-id id}))]
    (cond
     (not message) (throw+ {:type :not-found})
     (empty? programs) (throw+ {:type :not-authorized})
     :else (do (schedule-proposal-apply! (:proposal message))
               (update schedule-proposal-message
                       (set-fields {:status (:accepted (map-invert message-status))})
                       (where {:id id}))))))

(s/defn schedule-proposal-message-reject! :- s/Any
  [id :- s/Int
   manager :- xs/PersonID]
  (let [message
        (schedule-proposal-message-get id)
        programs
        (select schedule-proposal-message-programs
                (join program (= :program-id :program.id))
                (where {:program.manager manager
                        :message-id id}))]
    (cond
     (not message) (throw+ {:type :not-found})
     (empty? programs) (throw+ {:type :not-authorized})
     :else (update schedule-proposal-message
                   (set-fields {:status (:rejected (map-invert message-status))})
                   (where {:id id})))))

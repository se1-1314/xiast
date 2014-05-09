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

;; Functions for converting database records to Schema data.

(defn person->sPerson
  [person]
  {:netid (:netid person)
   :first-name (:firstname person)
   :last-name (:surname person)
   :locale (:locale person)})

(defn course-activity->sCourseActivity
  [course-activity]
  ;; TODO: fix support for multiple instructors/activity (nvgeele)
  (let [instructor-id
        ((comp :netid first)
         (select course-instructor
                 (where {:course-activity (:id course-activity)})))]
    {:id (:id course-activity)
     :name (:name course-activity)
     :type (val (find course-activity-types (:type course-activity)))
     :semester (:semester course-activity)
     :week (:week course-activity)
     :contact-time-hours (:contact-time-hours course-activity)
     :instructor instructor-id
     :facilities (set (map #(get room-facilities (:facility %))
                           (select course-activity-facility
                                   (where {:course-activity
                                           (:id course-activity)}))))}))

(defn course->sCourse
  [course]
  (let [department
        ((comp :name first)
         (select department
                 (where {:id (:department course)})))
        titular
        ((comp person->sPerson first)
         (select person
                 (where {:netid (:titular-id course)})))
        activities
        (map course-activity->sCourseActivity
             (select course-activity
                     (where {:course-code (:course-code course)})))
        instructors
        (map :instructor activities)
        grade
        (val (find course-grades (:grade course)))]
    {:course-code (:course-code course)
     :title (:title course)
     :description (:description course)
     :titular (:titular-id course)
     :grade grade
     :department department
     :activities (set activities)
     :instructors (set instructors)}))

(defn program->sProgram
  [program]
  (let [mandatory
        (map :course-code
             (select program-mandatory-course
                     (where {:program (:id program)})))
        choice
        (map :course-code
             (select program-choice-course
                     (where {:program (:id program)})))]
    (merge program
           {:mandatory (vec mandatory)
            :optional (vec choice)})))

(defn department->sDepartment
  [department]
  (let [dep {:id (:id department)
             :name (:name department)}]
    (if (= (:faculty department) "")
      dep
      (assoc dep :faculty (:faculty department)))))

(defn room->sRoom
  [room]
  {:id (select-keys room [:building :floor :number])
   :capacity (:capacity room)
   :facilities (set (map #(get room-facilities (:facility %))
                         (select room-facility
                                 (where {:room (:id room)}))))})

(s/defn schedule-block->sScheduleBlock
  [block]
  (assoc (dissoc block [:room :course-activity])
    :item (let [activity
                (first (select course-activity
                               (where {:id (:course-activity block)})))]
            {:title (:name activity)
             :course-activity (:id activity)
             :course-code (:course-code activity)})
    :room (first (select room
                         (where {:id (:room block)})
                         (fields :building :floor :number)))))

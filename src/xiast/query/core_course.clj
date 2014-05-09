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
(load "core_person")

(s/defn course-add-activity! :- s/Any
  ([course-code :- xs/CourseCode
    activity :- xs/CourseActivity]
     "Add a new activity to a course."
     (let [course (select course (where {:course-code course-code}))]
       (if (empty? course)
         ;; TODO: exceptions?
         "Course not found"
         (if (:name activity)
           (course-add-activity! course-code activity (:name activity))
           (let [course (first course)
                 type ((:type activity) (map-invert course-activity-types))
                 activity-count (count (select course-activity
                                               (where {:course-code course-code
                                                       :type type})))]
             (course-add-activity! course-code
                                   activity
                                   (str (:name course)
                                        " "
                                        type
                                        " "
                                        (+ count 1))))))))
  ([course-code :- xs/CourseCode
    activity :- xs/CourseActivity
    name :- s/Str]
     "Add a new activity to a course."
     (let [course (select course (where {:course-code course-code}))]
       (if (not (empty? course))
         (let [facilities
               (map #(% (map-invert room-facilities))
                    (:facilities activity))
               activity-count
               (count (select course-activity
                              (where {:course-code course-code
                                      :type type})))
               key
               (:GENERATED_KEY
                (insert course-activity
                        (values {:course-code course-code
                                 :name name
                                 :type ((:type activity)
                                        (map-invert course-activity-types))
                                 :semester (:semester activity)
                                 :week (:week activity)
                                 :contact-time-hours
                                 (:contact-time-hours activity)})))]
           (insert course-instructor
                   (values {:course-activity key
                            :netid (person-create!
                                    (:instructor activity))}))
           (doseq [facility facilities]
             (insert course-activity-facility
                     (values {:course-activity key
                              :facility facility})))
           key)
         ;; TODO: Exceptions!
         "Course does not exist"))))

(s/defn course-add! :- s/Any
  [new-course :- xs/Course]
  "Add a new course to the database."
  (let [department
        ((comp :id first)
         (select department
                 (where {:name (:department new-course)})))]
    (insert course
            (values
             (merge
              (dissoc new-course
                      :instructors :department :grade :activities :titular)
              {:grade ((:grade new-course)
                       (map-invert course-grades))
               :department department
               :titular-id (person-create! (:titular new-course))})))
    (if (:activities new-course)
      (doseq [activity (:activities new-course)]
        (course-add-activity! (:course-code new-course)
                              activity)))))

(s/defn course-delete! :- s/Any
  [course-code :- xs/CourseCode]
  "Delete a course from the database."
  ;; We only need to delete the course record,
  ;; enrollments, instructors, ... will cascade (nvgeele)
  (delete course
          (where {:course-code course-code})))

(s/defn course-delete-activity! :- s/Any
  [activity-code :- s/Int]
  "Delete a course's activity."
  ;; Facilities will be deleted by cascade.
  (delete course-activity
          (where {:id activity-code})))

(s/defn course-get :- xs/Course
  [course-code :- xs/CourseCode]
  "Fetch a course from the database."
  (let [course (select course
                       (where {:course-code course-code}))]
    (if (not (empty? course))
      (course->sCourse (first course))
      nil)))

(s/defn course-programs :- #{xs/ProgramID}
  [course-code :- xs/CourseCode]
  "Return seq of program-ids for programs which contain course with
  course-id"
  (let [m-ids (map :program
                   (select program-mandatory-course
                           (where {:course-code course-code})))
        o-ids (map :program
                   (select program-choice-course
                           (where {:course-code course-code})))]
    (set (conj m-ids o-ids))))

(s/defn course-activity-facilities :- #{xs/RoomFacility}
  [activity-code :- s/Int]
  (set (map #(get room-facilities (:facility %))
            (select course-activity-facility
                    (where {:course-activity activity-code})))))

(s/defn course-activity-get :- xs/CourseActivity
  [id :- s/Int]
  (let [result (select course-activity
                       (where {:id id}))]
    (if (empty? result)
      nil
      (course-activity->sCourseActivity (first result)))))

(s/defn course-activity-update! :- s/Int
  [activity :- xs/CourseActivity]
  (if-let [id (:id activity)]
    (let [course-code ((comp :course-code first)
                       (select course-activity
                               (where {:id id})))
          blocks (map :id
                      (select schedule-block
                              (where {:course-activity activity})))]
      (course-delete-activity! id)
      (let [id (course-add-activity! course-code activity)]
        (doseq [block blocks]
          (update schedule-block
                  (set-fields {:course-activity id})
                  (where {:id block})))
        id))
    (throw+ {:error "ID required"})))

(s/defn course-list :- [xs/Course]
  []
  "Fetch a list of all courses from the database."
  (let [courses (select course)]
    (map course->sCourse courses)))

(s/defn course-find :- [xs/Course]
  [keywords :- [s/Str]]
  "Accepts a list of keywords and returns a list of courses with one or more of
   the keywords in their name."
  (let [terms
        (map (fn [kw]
               `{:title [~'like ~(str "%" kw "%")]})
             keywords)
        results
        ((comp eval macroexpand)
         `(select course
                  (where (~'or ~@terms))))]
    (map #(select-keys % [:course-code :title])
         results)))

(s/defn course-update-description! :- s/Bool
  [course-code :- xs/CourseCode
   description :- s/Str]
  (if (empty? (select course
                      (where {:course-code course-code})))
    false
    (do (update course
                (set-fields {:description description})
                (where {:course-code course-code}))
        true)))

(s/defn program-manager-course-list :- [xs/Course]
  [manager :- xs/PersonID]
  (map course->sCourse
       (union
        (queries
         (subselect course
                    (join program-mandatory-course
                          (= :course-code :program-mandatory-course.course-code))
                    (join program
                          (= :program.id :program-mandatory-course.program))
                    (where {:program.manager manager})
                    (modifier "DISTINCT"))
         (subselect course
                    (join program-choice-course
                          (= :course-code :program-choice-course.course-code))
                    (join program
                          (= :program.id :program-choice-course.program))
                    (where {:program.manager manager})
                    (modifier "DISTINCT"))))))

(s/defn titular-course-list :- [xs/Course]
  [titular :- xs/PersonID]
  "Returns a list off all courses for which the user is a titular."
  (map course->sCourse
       (select course
               (where {:titular-id titular}))))

(s/defn instructor-course-list :- [xs/Course]
  [instructor :- xs/PersonID]
  "Returns a list off all courses for which the user is an instructor."
  (let [activity-ids (map (comp :course-activity first)
                          (select course-instructor
                                  (where {:netid instructor})))
        activities (map #(first (select course-activity
                                        (where {:id %})))
                        activity-ids)]
    (map #((comp course->sCourse first)
           (select course
                   (where {:course-code (:course-code %)}))))))

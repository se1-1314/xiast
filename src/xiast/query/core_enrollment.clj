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

(s/defn enrollments-student :- [xs/Enrollment]
  [student-id :- xs/PersonID]
  "Get a list of all enrollments from a student."
  (map (fn [en]
         {:course (:course-code en)
          :netid (:netid en)})
       (select course-enrollment
               (where {:netid student-id}))))

(s/defn enroll-student! :- s/Any
  [student-id :- xs/PersonID
   course-code :- xs/CourseCode]
  "Add a new enrollment to the database."
  (insert course-enrollment
          (values {:course-code course-code
                   :netid student-id})))

(s/defn enrollments-course :- [xs/Enrollment]
  [course-code :- xs/CourseCode]
  "Get a list of all enrollments for a certain course."
  (map (fn [en]
         {:course (:course-code en)
          :netid (:netid en)})
       (select course-enrollment
               (where {:course-code course-code}))))

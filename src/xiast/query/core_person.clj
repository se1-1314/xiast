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

(s/defn person-add! :- s/Any
  [new-person :- xs/Person]
  "Add a new person to the database."
  (insert person
          (values {:netid (:netid new-person)
                   :firstname (:first-name new-person)
                   :surname (:last-name new-person)
                   :locale (:locale new-person)})))

(s/defn person-get :- (s/maybe xs/Person)
  [netid :- xs/PersonID]
  "Fetch person associated with a certain NetID from the database."
  (let [person
        (select person
                (where {:netid netid}))]
    (if (not (empty? person))
      (person->sPerson (first person))
      nil)))

(s/defn person-functions :- xs/PersonFunctions
  [netid :- xs/PersonID]
  "Returns a set of all functions associated with the person."
  (let [program-manager?
        (not (empty?(select program
                            (where {:manager netid})
                            (limit 1))))
        instructor?
        (not (empty? (select course-instructor
                             (where {:netid netid})
                             (limit 1))))
        titular?
        (not (empty? (select course
                             (where {:titular-id netid})
                             (limit 1))))
        student?
        (not (empty? (select course-enrollment
                             (where {:netid netid})
                             (limit 1))))]
    (disj
     (set [(if program-manager? :program-manager)
           (if instructor? :instructor)
           (if titular? :titular)
           (if student? :student)])
     nil)))

(s/defn person-create! :- xs/PersonID
  "This functions checks whether a user with the given netid exists in the
  database. If not, a new record for the person will be inserted. Returns
  netid."
  [netid :- xs/PersonID]
  ;; TODO: Standard locale (nvgeele)
  (if (empty? (person-get netid))
    (person-add! {:netid netid
                  :first-name ""
                  :last-name ""
                  :locale "en"}))
  netid)

(s/defn person-locale! :- s/Any
  [netid :- xs/PersonID
   locale :- s/Str]
  (update person
          (set-fields {:locale locale})
          (where {:netid netid})))

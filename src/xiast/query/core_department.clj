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

(s/defn department-list :- [xs/Department]
  []
  "Fetch a list of all departments from the database."
  (let [deps (select department)]
    (if (empty? deps)
      []
      (map department->sDepartment deps))))

(s/defn department-get :- xs/Department
  [id :- s/Int]
  "Fetch a department from the database."
  (let [dep (select department
                    (where {:id id}))]
    (if (empty? dep)
      nil
      (department->sDepartment dep))))

(s/defn department-add! :- s/Any
  [new-department :- xs/Department]
  "Add a new department to the database."
  (insert department (values (dissoc new-department :id))))

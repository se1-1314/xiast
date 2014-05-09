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

(s/defn program-list :- [xs/Program]
  ([]
     "Returns a list of all programs."
     #_(map #(assoc (select-keys % [:course-code :title]) :program-id (:id %))
            (select program))
     (map program->sProgram (select program)))
  ([manager :- xs/PersonID]
     "Returns a list of all programs the manager is manager of."
     #_(map #(assoc (select-keys % [:course-code :title]) :program-id (:id %))
            (select program (where {:manager manager})))
     (map program->sProgram (select program
                                    (where {:manager manager})))))

(s/defn program-find :- [xs/Program]
  [keywords :- [s/Str]]
  "Accepts a list of keywords and returns a list of programs with one or more of
   the keywords in their name."
  (let [terms
        (map (fn [kw]
               `{:title [~'like ~(str "%" kw "%")]})
             keywords)
        results
        ((comp eval macroexpand)
         `(select program
                  (where (~'or ~@terms))))]
    (map #(assoc (select-keys % [:course-code :title]) :program-id (:id %))
         results)))

(s/defn program-get :- xs/Program
  [program-id :- xs/ProgramID]
  "Fetch a program from the database."
  (let [result
        (select program
                (where {:id program-id}))]
    (if (not (empty? result))
      (program->sProgram (first result))
      nil)))

(s/defn program-add! :- s/Any
  [new-program :- xs/Program]
  "Add a new program to the database."
  (let [id
        (:GENERATED_KEY
         (insert program
                 (values {:title (:title new-program)
                          :description (:description new-program)
                          :manager (:manager new-program)})))]
    (doseq [course-code (:mandatory new-program)]
      (insert program-mandatory-course
              (values {:program id
                       :course-code course-code})))
    (doseq [course-code (:optional new-program)]
      (insert program-choice-course
              (values {:program id
                       :course-code course-code})))
    (assoc new-program :id id)))

(s/defn program-delete! :- s/Any
  [id :- xs/ProgramID]
  "Delete a program from the database."
  (delete program
          (where {:id id})))

(s/defn program-add-mandatory! :- s/Any
  [id :- xs/ProgramID
   course :- xs/CourseCode]
  (insert program-mandatory-course
          (values {:program id
                   :course-code course})))

(s/defn program-add-optional! :- s/Any
  [id :- xs/ProgramID
   course :- xs/CourseCode]
  (insert program-choice-course
          (values {:program id
                   :course-code course})))

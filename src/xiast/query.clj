(ns xiast.query
  "This namespace provides protocols for querying and updating
  information accessible through Xiast information stores."

  (:require [clojure.edn :as edn]
            [schema.core :as s]
            [xiast.schema :as xs])
  (:use [clojure.set :only [map-invert]]
        [xiast.schema :only [room-facilities course-grades course-activity-types]]
        [xiast.database]
        [korma.db]
        [korma.core]
        [slingshot.slingshot :only [throw+ try+]]))


;; TODO: replace all find's with get's (nvgeele)
;; TODO: replace-keys can make code cleaner (nvgeele)

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
  {:id (select-keys room [:id :building :floor :number])
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
            {:type (get course-activity-types (:type activity))
             :course-activity (:id activity)
             :course-code (:course-code activity)})
    :room (first (select room
                         (where {:id (:room block)})
                         (fields :building :floor :number)))))

;; PUBLIC API

(s/defn room-list :- [xs/Room]
  ([]
     "Get a list of all of rooms"
     (map room->sRoom (select room)))
  ([building]
     "Get a list of all rooms in a building"
     (map room->sRoom (select room
                              (where {:building building}))))
  ([building floor]
     "Get a list of all rooms on a floor in a building"
     (map room->sRoom (select room
                              (where {:building building
                                      :floor floor})))))

(s/defn room-add! :- s/Any
  [new-room :- xs/Room]
  "Add a Room."
  (let [facilities
        (map #(% (map-invert room-facilities))
             (:facilities new-room))
        room-id
        (dissoc (:id new-room) :id)
        vals
        (merge {:capacity (:capacity new-room)}
               room-id)
        key
        (:GENERATED_KEY
         (insert room
                 (values vals)))]
    (doseq [facility facilities]
      (insert room-facility
              (values {:room key
                       :facility facility})))))

(s/defn room-delete! :- s/Any
  [room-id :- xs/RoomID]
  "Delete a room from the database."
  ;; Do we need to check if the room exists first or not? (nvgeele)
  (delete room
          (where room-id)))

(s/defn room-get :- xs/Room
  [room-id :- xs/RoomID]
  "Fetch information about a room."
  (let [room (if (:id room-id)
               (select room
                       (where {:id room-id}))
               (select room
                       (where room-id)))]
    (if (not (empty? room))
      (let [facilities
            (map #(val (find room-facilities (:facility %)))
                 (select room-facility
                         (where {:room (:id (first room))})))]
        {:id {:building (:building (first room))
              :floor (:floor (first room))
              :number (:number (first room))}
         :capacity (:capacity (first room))
         :facilities (set facilities)})
      nil)))

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

(s/defn course-add-activity! :- s/Any
  [course-code :- xs/CourseCode
   activity :- xs/CourseActivity]
  "Add a new activity to a course."
  (let [course (select course (where {:course-code course-code}))]
    (if (not (empty? course))
      (let [facilities
            (map #(% (map-invert room-facilities))
                 (:facilities activity))
            key
            (:GENERATED_KEY
             (insert course-activity
                     (values {:course-code course-code
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
        key))))

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
                               (where {:id id})))]
      (course-delete-activity! id)
      (course-add-activity! course-code activity))
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

;; Schedule queries

(defn- schedule-blocks-in-timespan
  ([timespan]
     (schedule-blocks-in-timespan timespan {}))
  ([timespan constraints]
     (select schedule-block
             (where (merge constraints
                           {:course-activity course-activity
                            :week [>= (first (:weeks timespan)) (second (:weeks timespan))]
                            ;;:week [<= (second (:weeks timespan))]
                            :days [>= (first (:days timespan)) (second (:days timespan))]
                            ;;:days [<= (second (:days timespan))]
                            :first-slot [<= (first (:slots timespan))]
                            :last-slot [<= (second (:slots timespan))]})))))

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
        blocks (map #(schedule-blocks-in-timespan timespan {:course-activity %})
                    activities)]
    (mapcat schedule-block->sScheduleBlock blocks)))

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
    blocks))

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
    (mapcat identity schedules)))

(s/defn instructor-schedule :- xs/Schedule
  [instructor-id :- xs/PersonID
   timespan :- xs/TimeSpan]
  (let [activities (map :course-activity
                        (select course-instructor
                                (where {:netid instructor-id})
                                (fields :course-activity)))
        blocks (map #(schedule-blocks-in-timespan timespan {:course-activity %})
                    activities)]
    (mapcat identity blocks)))

(s/defn schedule-proposal-message-add! :- s/Any
  [titular :- xs/PersonID
   program :- xs/ProgramID
   proposal :- xs/ScheduleProposal]
  (insert schedule-proposal-message
          (values {:titular titular
                   :program program
                   :content (pr-str proposal)})))

(s/defn schedule-proposal-message-get :- [xs/ScheduleProposalMessage]
  [to :- xs/ProgramID]
  (let [proposals (select schedule-proposal-message
                          (where {:program to}))]
    (map (fn [proposal]
           (assoc (dissoc proposal :content)
             :proposal (edn/read-string (:content proposal))))
         proposals)))

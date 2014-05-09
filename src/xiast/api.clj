(ns xiast.api
  (:use compojure.core
        [xiast.database :only [*db*]]
        [xiast.session :only [*session*]]
        [clojure.data.json :only [read-str write-str]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [xiast.query.core :as query]
            [xiast.schema :as xs]
            [xiast.scheduling :as scheduling]
            [clojure.data.json :as json]
            [schema.core :as s]
            [schema.utils :as utils]
            [schema.coerce :as coerce]))

;; TODO: Define standard error message + HTTP error code (nvgeele)
;; TODO: Return correct json on empty returns (nvgeele)

(defn coerce-as
  [schema str]
  (let [json (read-str str :key-fn keyword)
        coercer (coerce/coercer schema coerce/json-coercion-matcher)
        res (coercer json)]
    (if (schema.utils/error? res)
      #_(do (println res)
            (throw+ {:type :coercion-error}))
      (throw+ {:type :coercion-error})
      res)))

(defn read-json
  [str]
  (read-str str :key-fn keyword))

(defn wrap-api-function
  [func]
  (fn [& args]
    (let [result (if args (apply func args) (func))]
      (write-str result))))

;; Request Schema's

(def FindQuery
  {:keywords [s/Str]})

(def CourseSetDescriptionQuery
  {:description s/Str})

(def CourseActivityAPI
  ;; Schema can not (yet) coerce a JSON array (clojure vector) as a set...
  (assoc (dissoc xs/CourseActivity :facilities)
    :facilities [xs/RoomFacility]))

(def CourseAddQuery
  {:program xs/ProgramID
   :course xs/CourseCode})

(def ScheduleProposal
  ;; See CourseActivityAPI...
  {(s/optional-key :new) [xs/ScheduleBlock]
   (s/optional-key :moved) [xs/ScheduleBlock]
   (s/optional-key :deleted) [xs/ScheduleBlockID]})

(def ScheduleProposalMessage
  {:sender xs/PersonID
   :proposal ScheduleProposal
   :message s/Str})

;; Course API

(defn course-list
  []
  {:courses (query/course-list)})

(defn course-get
  [course-code]
  (let [result (query/course-get course-code)]
    (if result
      result
      "[]")))

(defn course-delete
  [course-code]
  (if-let [course (query/course-get course-code)]
    (if (or (some #{:program-manager} (:user-functions *session*))
            (= (:titular course) (:user *session*)))
      (do (query/course-delete! course-code)
          {:result "OK"})
      {:result "Not authorized"})
    {:result "Course not found"}))

(defn course-find
  [body]
  (try+ (let [request (coerce-as FindQuery body)
              result (query/course-find (:keywords request))]
          {:result result})
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

(defn course-add
  [body]
  (println body)
  (try+ (let [request (coerce-as xs/Course body)]
          (if (some #{:program-manager} (:user-functions *session*))
            (do (query/course-add! request)
                {:result "OK"})
            {:result "Not authorized"}))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

(defn course-activity-get
  [id]
  (if-let [activity (query/course-activity-get id)]
    activity
    []))

;; TODO: security; check if program manager! (nvgeele)
(defn course-activity-put
  [id body]
  (try+ (let [request (coerce-as CourseActivityAPI body)
              activity (assoc (dissoc request :facilities)
                         :facilities (set (:facilities request)))]
          (let [new-id (query/course-activity-update! (assoc activity :id id))]
            {:id new-id}))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

(defn course-description-update!
  [id body]
  (try+ (let [request (coerce-as CourseSetDescriptionQuery body)
              course (query/course-get id)]
          (cond
           (nil? course)
           {:result "Course does not exist"}
           (or (some #{:program-manager} (:user-functions *session*))
               (= (:titular course) (:user *session*)))
           (do (query/course-update-description! id (:description body))
               {:result "OK"})
           :else
           {:result "Not authorized"}))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

(defroutes course-routes
  (GET "/" []
       "Invalid request")
  ;; Returns a map with courses currently in the database:
  ;; { {"course-code":"1000428ANR","title":"Structuur 1\r","description":null,"titular":"0006101","grade":"ba","department":"INFORMAT","activities":[{"id":20,"type":"WPO","semester":1,"week":0,"contact-time-hours":39,"facilities":[],"instructor":"0075773"},{"id":19,"type":"HOC","semester":1,"week":0,"contact-time-hours":39,"facilities":[],"instructor":null}],"instructors":[null,"0075773"]}, {...} }
  (GET "/list" []
       ((wrap-api-function course-list)))
  ;; /get/course-code -- Returns the corrseponding course given a course-code
  (GET "/get/:course-code" [course-code]
       ((wrap-api-function course-get) course-code))
  ;; /del/course-code -- if authorized, removes the corrseponding course given a course-code
  (DELETE "/del/:course-code" [course-code]
          ((wrap-api-function course-delete) course-code))
  ;; /find -- expects raw JSON: searches for course-names given a keyword -- p.e.: { "keywords":["Structuur"]} -> returns all courses containing "structuur" in their name
  (POST "/find" {body :body}
        ((wrap-api-function course-find) (slurp body)))
  ;; /add -- expects raw JSON : adds a course to the database, given the structure defined in schema.clj -- p.e.: {"course-code":".." "title":".." "description":".." "titular":".." "department":".." "grade":".."}
  (POST "/add" {body :body}
        ((wrap-api-function course-add) (slurp body)))
  ;; TODO: add activity by API, maybe
  ;;  /activity/get/id -- return a course activity given its id -- p.e. http://localhost:3000/api/course/activity/get/10 -> {"id":10,"type":"WPO","semester":1,"week":0,"contact-time-hours":32,"facilities":[],"instructor":null}
  (GET "/activity/get/:id" [id]
       ((wrap-api-function course-activity-get) id))
  (PUT "/activity/:id" [id :as {body :body}]
       ((wrap-api-function course-activity-put) id (slurp body)))
  (PUT "/description" [id :as {body :body}]
       ((wrap-api-function course-description-update!) id (slurp body))))

;; Program API

(defn program-list
  []
  {:programs (query/program-list)})

(defn program-get
  [id]
  (let [result (query/program-get id)]
    (if result
      result
      "[]")))

(defn program-delete
  [id]
  (if-let [program (query/program-get id)]
    (if (= (:manager program) (:user *session*))
      (do (query/program-delete! id)
          {:result "OK"})
      {:result "Not authorized"})
    {:result "Program not found"}))

(defn program-find
  [body]
  (try+ (let [request (coerce-as FindQuery body)
              result (query/program-find (:keywords request))]
          {:result result})
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

;; TODO: everyone can add programs now! We need some kind of admin role!
(defn program-add
  [body]
  (try+ (let [request (coerce-as xs/Program body)]
          (query/program-add! request)
          {:result "OK"})
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

(defroutes program-routes
  (GET "/" []
       "Invalid request")
  ;; /list -- returns a map containing all programs available in the database -- p.e. {"programs":[{"program-id":1,"title":"ultrices"},{"program-id":2,"title":"eget lacus."},...]}
  (GET "/list" []
       ((wrap-api-function program-list)))
  ;; /get/id -- returns a course given a program-id -- p.e. /get/2 ->  {"optional":["1015259ANR","1018725AER"],"mandatory":["1000447ANR","1001673BNR","1007156ANR","1004483BNR"],"manager":"pmanager","description": "bbbb",,"title":"3e Bachelor Ingenieurswetenschappen - Computerwetenschappen","id":3}
  (GET "/get/:id" [id]
       ((wrap-api-function program-get) id))
  ;; /del/id -- deletes a course given its id
  (DELETE "/del/:id" [id]
          ((wrap-api-function program-delete) id))
  ;; /find -- expects raw JSON -- returns a complete program (like get) given a keyword -- p.e. {"keywords":["lorem"]} -> returns program with title "scelerisque, lorem"
  (POST "/find" {body :body}
        ((wrap-api-function program-find) (slurp body)))
  ;; /add -- expects raw JSON -- creates a program given Program attributes (title, description, (id), (manager), [mandatory], [optional]), defined in "schema.clj"
  (POST "/add" {body :body}
        ((wrap-api-function program-add) (slurp body))))

;; Room API

(defn room-list
  ([building floor]
     {:rooms (query/room-list building floor)})
  ([building]
     {:rooms (query/room-list building)})
  ([]
     {:rooms (query/room-list)}))

(defn room-get
  [building floor number]
  (query/room-get {:building building
                   :floor floor
                   :number number}))

(defn room-building-list
  []
  (query/room-building-list))

(defn room-list-free
  [timespan]
  (query/free-rooms-in-timespan timespan))

(defn room-list-free-for-block
  [block body]
  (try+ (let [proposal (coerce-as ScheduleProposal body)]
          (query/free-rooms-for-block block proposal))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result (str "Unexpected error: " (.getMessage e))})))

(defroutes room-routes
  (GET "/" []
       "Invalid request")
  ;; /list -> lists all rooms in a specific building on a specific floor in the database
  (GET "/list/:building/:floor" [building floor]
       ((wrap-api-function room-list) building floor))
  ;; /list/building -> lists all rooms in the given building
  (GET "/list/:building" [building]
       ((wrap-api-function room-list) building))
  ;; /list/building -- lists all rooms in a specific building
  (GET "/list" []
       ((wrap-api-function room-list)))
  ;; /get/building/floor/number -- returns room in building on floor with number
  (GET "/get/:building/:floor/:number" [building floor number]
       ((wrap-api-function room-get) building floor number))
  (GET "/building/list" []
       ((wrap-api-function room-building-list)))
  (GET "/free/:w1/:w2/:d1/:d2/:s1/:s2"
       [w1 w2 d1 d2 s1 s2]
       ((wrap-api-function room-list-free)
        {:weeks [w1 w2]
         :days [d1 d2]
         :slots [s1 s2]}))
  (POST "/free/:w/:d/:fs/:ls"
        [w d fs ls :as {body :body}]
        ((wrap-api-function room-list-free-for-block)
         {:week w
          :day d
          :first-slot fs
          :last-slot ls}
         (slurp body))))

;; Enrollment API

(defn enrollment-student
  ([]
     (if (:user *session*)
       {:enrollments (query/enrollments-student (:user *session*))}
       []))
  ([id]
     {:enrollments (query/enrollments-student id)}))

(defn enrollment-course
  [id]
  {:enrollments (query/enrollments-course id)})

(defroutes enrollment-routes
  (GET "/" []
       "Invalid request")
  ;; /student -- returns a map with all enrollments of the student currently logged in
  (GET "/student" []
       ((wrap-api-function enrollment-student)))
  ;; /student/id -- returns a map with all enrollments of Student with id = id
  (GET "/student/:id" [id]
       ((wrap-api-function enrollment-student) id))
  ;; /course/id -- returns map with all enrollments of course with id = id
  (GET "/course/:id" [id]
       ((wrap-api-function enrollment-course) id)))

(defn program-manager-programs
  []
  (if (some #{:program-manager} (:user-functions *session*))
    {:programs (query/program-list (:user *session*))}
    {:programs []}))

(defn program-manager-add-optional
  [body]
  (try+ (let [request (coerce-as CourseAddQuery body)]
          ;; TODO: Check if user is manager of program itself or not? (nvgeele)
          (if (some #{:program-manager} (:user-functions *session*))
            (do (query/program-add-optional! (:program request) (:course request))
                {:result "OK"})
            {:result "Not authorized"}))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

(defn program-manager-add-mandatory
  [body]
  (try+ (let [request (coerce-as CourseAddQuery body)]
          ;; TODO: Check if user is manager of program itself or not? (nvgeele)
          (if (some #{:program-manager} (:user-functions *session*))
            (do (query/program-add-mandatory! (:program request) (:course request))
                {:result "OK"})
            {:result "Not authorized"}))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result "Error"})))

(defroutes program-manager-routes
  (GET "/" []
       "Invalid request")
  (GET "/programs" []
       ((wrap-api-function program-manager-programs)))
  (POST "/program/optional" {body :body}
        ((wrap-api-function program-manager-add-optional) (slurp body)))
  (POST "/program/mandatory" {body :body}
        ((wrap-api-function program-manager-add-mandatory) (slurp body))))

(defn titular-courses
  []
  (cond
   (some #{:titular} (:user-functions *session*))
   {:courses (query/titular-course-list (:user *session*))}
   (some #{:program-manager} (:user-functions *session*))
   {:courses (query/program-manager-course-list (:user *session*))}
   :else
   {:courses []}))

(defroutes titular-routes
  (GET "/" []
       "Invalid request")
  (GET "/courses" []
       ((wrap-api-function titular-courses))))

(defn instructor-courses
  []
  (if (some #{:instructor} (:user-functions *session*))
    {:courses (query/instructor-course-list (:user *session*))}
    {:courses []}))

(defroutes instructor-routes
  (GET "/" []
       "Invalid request")
  (GET "/courses" []
       ((wrap-api-function instructor-courses))))

(defn schedule-get
  [timespan]
  (cond
   (some #{:instructor} (:user-functions *session*))
   {:schedule (query/instructor-schedule (:user *session*) timespan)}
   (some #{:student} (:user-functions *session*))
   {:schedule (query/student-schedule (:user *session*) timespan)}
   (some #{:program-manager} (:user-functions *session*))
   {:schedule (query/program-manager-schedule (:user *session*) timespan)}
   :else
   {:schedule []}))

(defn schedule-student-get
  [timespan]
  (if (:user *session*)
    {:schedule (query/student-schedule (:user *session*) timespan)}
    {:error "Not logged in"}))

(defn schedule-instructor-get
  [timespan]
  (cond
   (not (:user *session*))
   {:error "Not logged in"}
   (not (some #{:instructor} (:user-functions *session*)))
   {:error "Not an instructor"}
   :else
   {:schedule (query/instructor-schedule (:user *session*) timespan)}))

(defn schedule-course-get
  [course-code timespan]
  {:schedule (query/course-schedule course-code timespan)})

(defn schedule-program-get
  [program-id timespan]
  {:schedule (query/program-schedule program-id timespan)})

;; TODO: Check whether the sender is the titular of all activities he's scheduling?
(defn schedule-proposal-message-add!
  [body]
  (try+ (if (some #{:program-manager :titular} (:user-functions *session*))
          (let [request (coerce-as ScheduleProposalMessage body)
                proposal (:proposal request)
                message (assoc (dissoc request :proposal)
                          :proposal {:new (set (:new proposal))
                                     :moved (set (:moved proposal))
                                     :deleted (set (:deleted proposal))})]
            (do (query/schedule-proposal-message-add! message)
                {:result "OK"}))
          {:result "Not authorized"})
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result (str "Unexpected error: " (.getMessage e))})))

(defn schedule-proposal-message-list
  []
  (if (some #{:program-manager} (:user-functions *session*))
    (select-keys (query/schedule-proposal-message-list (:user *session*)
                                                       :inprogress)
                 [:id :sender])
    {:result "Not authorized"}))

;; TODO: Check if user is program manager of a program that is linked to the msg
(defn schedule-proposal-message-get
  [id]
  (if (some #{:program-manager} (:user-functions *session*))
    (query/schedule-proposal-message-get id)
    {:result "Not authorized"}))

(defn schedule-proposal-message-accept!
  [id]
  (if (some #{:program-manager} (:user-functions *session*))
    (try+ (do (query/schedule-proposal-message-accept! id (:user *session*))
              {:result "OK"})
          (catch [:type :not-found] e
              {:result "Message not found"})
          (catch [:type :not-authorized] e
              {:result "Not authorized"})
          (catch Exception e
            {:result (str "Unexpected error: " (.getMessage e))}))
    {:result "Not authorized"}))

(defn schedule-proposal-message-reject!
  [id]
  (if (some #{:program-manager} (:user-functions *session*))
    (try+ (do (query/schedule-proposal-message-reject! id (:user *session*))
              {:result "OK"})
          (catch [:type :not-found] e
              {:result "Message not found"})
          (catch [:type :not-authorized] e
              {:result "Not authorized"})
          (catch Exception e
            {:result (str "Unexpected error: " (.getMessage e))}))
    {:result "Not authorized"}))

(defn schedule-proposal-check
  [body]
  (try+ (let [request (coerce-as ScheduleProposal body)
              proposal {:new (set (:new request))
                        :moved (set (:moved request))
                        :deleted (set (:deleted request))}]
          (if (some #{:program-manager :titular} (:user-functions *session*))
            (scheduling/check-proposal proposal)
            {:result "Not authorized"}))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result (str "Unexpected error: " (.getMessage e))})))

(defn schedule-proposal-apply!
  [body]
  (try+ (let [request (coerce-as ScheduleProposal body)
              proposal {:new (set (:new request))
                        :moved (set (:moved request))
                        :deleted (set (:deleted request))}
              check (scheduling/check-proposal proposal)]
          (if (empty? check)
            (do (query/schedule-proposal-apply! proposal)
                {:result "OK"})
            check))
        (catch [:type :coercion-error] e
          {:result "Invalid JSON"})
        (catch Exception e
          {:result (str "Unexpected error: " (.getMessage e))})))

(defroutes schedule-routes
  (GET "/" []
       "Invalid request")
  (GET "/:w1/:w2/:d1/:d2/:s1/:s2"
       [w1 w2 d1 d2 s1 s2]
       ((wrap-api-function schedule-get)
        {:weeks [w1 w2]
         :days [d1 d2]
         :slots [s1 s2]}))
  (GET "/student/:w1/:w2/:d1/:d2/:s1/:s2"
       [w1 w2 d1 d2 s1 s2]
       ((wrap-api-function schedule-student-get)
        {:weeks [w1 w2]
         :days [d1 d2]
         :slots [s1 s2]}))
  (GET "/instructor/:w1/:w2/:d1/:d2/:s1/:s2"
       [w1 w2 d1 d2 s1 s2]
       ((wrap-api-function schedule-instructor-get)
        {:weeks [w1 w2]
         :days [d1 d2]
         :slots [s1 s2]}))
  (GET "/course/:course-code/:w1/:w2/:d1/:d2/:s1/:s2"
       [course-code w1 w2 d1 d2 s1 s2]
       ((wrap-api-function schedule-course-get)
        course-code
        {:weeks [w1 w2]
         :days [d1 d2]
         :slots [s1 s2]}))
  (GET "/program/:id/:w1/:w2/:d1/:d2/:s1/:s2"
       [id w1 w2 d1 d2 s1 s2]
       ((wrap-api-function schedule-program-get)
        id
        {:weeks [w1 w2]
         :days [d1 d2]
         :slots [s1 s2]}))
  (POST "/message" {body :body}
        ((wrap-api-function schedule-proposal-message-add! (slurp body))))
  (GET "/message/list" []
       ((wrap-api-function schedule-proposal-message-list)))
  (GET "/message/:id" [id]
       ((wrap-api-function schedule-proposal-message-get id)))
  (GET "/message/accept/:id" [id]
       ((wrap-api-function schedule-proposal-message-accept! id)))
  (GET "/message/reject/:id" [id]
       ((wrap-api-function schedule-proposal-message-reject! id)))
  (POST "/proposal/check" {body :body}
        ((wrap-api-function schedule-proposal-check (slurp body))))
  (POST "/proposal/apply" {body :body}
        ((wrap-api-function schedule-proposal-apply! (slurp body)))))

(defn department-list
  []
  {:departments (map #(dissoc % :id)
                     (query/department-list))})

(defroutes department-routes
  (GET "/" []
       "Invalid request")
  (GET "/list" []
       ((wrap-api-function department-list))))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes)
  (context "/room" [] room-routes)
  (context "/enrollment" [] enrollment-routes)
  (context "/program-manager" [] program-manager-routes)
  (context "/titular" [] titular-routes)
  (context "/instructor" [] instructor-routes)
  (context "/schedule" [] schedule-routes)
  (context "/my-schedule" [] schedule-routes) ;nieuw?
  (context "/department" [] department-routes))

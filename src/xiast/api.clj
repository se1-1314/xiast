(ns xiast.api
  (:use compojure.core
        [xiast.database :only [*db*]]
        [xiast.session :only [*session*]]
        [clojure.data.json :only [read-str write-str]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [xiast.query :as query]
            [xiast.schema :as xs]
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

(def CourseActivityAPI
  ;; Schema can not (yet) coerce a JSON array (clojure vector) as a set...
  (assoc (dissoc xs/CourseActivity [:facilities :instructor])
    :facilities [xs/RoomFacility]))

(def CourseAddQuery
  {:program xs/ProgramID
   :course xs/CourseCode})

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
    (if (= (:titular course) (:user *session*))
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
       ((wrap-api-function course-activity-put) id (slurp body))))

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
  ;; /get/id -- return a course given its id -- p.e. /get/28 ->  {"optional":["4018804DNR","1001720BNR"],"mandtory":["1007131ANW","4012722ENR"],"manager":"pmanager","description":"La citudin","title":"euismod","id":28}
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
       ((wrap-api-function room-get) building floor number)))

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
  (GET "/programs" []
       ((wrap-api-function program-manager-programs)))
  (POST "/program/optional" {body :body}
       ((wrap-api-function program-manager-add-optional) (slurp body)))
  (POST "/program/mandatory" {body :body}
       ((wrap-api-function program-manager-add-mandatory) (slurp body))))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes)
  (context "/room" [] room-routes)
  (context "/enrollment" [] enrollment-routes)
  (context "/program-manager" [] program-manager-routes))

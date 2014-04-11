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

;; TODO: Write some API docs (nvgeele)
;; TODO: Define standard error message + HTTP error code (nvgeele)
;; TODO: Return correct json on empty returns (nvgeele)

(defn coerce-as
  [schema str]
  (println str)
  (let [json (read-str str :key-fn keyword)
        coercer (coerce/coercer schema coerce/json-coercion-matcher)
        res (coercer json)]
    (if (schema.utils/error? res)
      (throw+ {:type :coercion-error})
      res)))

(defn read-json
  [str]
  (read-str str :key-fn keyword))

(def FindQuery
  {:keywords [s/Str]})

(def parse-find-query
  (coerce/coercer FindQuery coerce/json-coercion-matcher))

(defn wrap-api-function
  [func]
  (fn [& args]
    (let [result (if args (apply func args) (func))]
      (write-str result))))

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
  ;; /add --   expects raw : adds a course to the database, given the structure defined in schema.clj -- p.e.: {"course-code":".." "title":".." "description":".." "titular":".." "department":".." "grade":".."}
  (POST "/add" {body :body}
        ((wrap-api-function course-add) (slurp body)))
  ;; TODO: add activity by API, maybe
  ;;  /activity/get/id -- return a course activity given its id -- p.e. http://localhost:3000/api/course/activity/get/10 -> {"id":10,"type":"WPO","semester":1,"week":0,"contact-time-hours":32,"facilities":[],"instructor":null}
  (GET "/activity/get/:id" [id]
       ((wrap-api-function course-activity-get) id)))

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
  (GET "/list" []
       ((wrap-api-function program-list)))
  (GET "/get/:id" [id]
       ((wrap-api-function program-get) id))
  (DELETE "/del/:id" [id]
          ((wrap-api-function program-delete) id))
  (POST "/find" {body :body}
        ((wrap-api-function program-find) (slurp body)))
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
  (GET "/list/:building/:floor" [building floor]
       ((wrap-api-function room-list) building floor))
  (GET "/list/:building" [building]
       ((wrap-api-function room-list) building))
  (GET "/list" []
       ((wrap-api-function room-list)))
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
  (GET "/student" []
       ((wrap-api-function enrollment-student)))
  (GET "/student/:id" [id]
       ((wrap-api-function enrollment-student) id))
  (GET "/course/:id" [id]
       ((wrap-api-function enrollment-course) id)))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes)
  (context "/room" [] room-routes)
  (context "/enrollment" [] enrollment-routes))

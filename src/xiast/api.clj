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

(defroutes course-routes
  (GET "/" [] "Invalid request")
  (GET "/list" []
       (write-str {:courses (query/course-list *db*)}))
  (GET "/get/:course-code" [course-code]
       (let [result (query/course-get course-code)]
         (if result
           (write-str result)
           "[]")))
  (DELETE "/del/:course-code" [course-code]
          (if (query/course-get course-code)
            (if-let [course (query/course-delete! course-code)]
              (if (= (:titular-id course) (:user *session*))
                (do (query/course-delete! course-code)
                    (write-str {:result "OK"}))
                (write-str {:result "Not authorized"}))
              (write-str {:result "Course not found"}))))
  (POST "/find" {body :body}
        (try+ (let [request (coerce-as FindQuery (slurp body))
                    result (query/course-find (:keywords request))]
                (write-str {:result result}))
              (catch [:type :coercion-error] e
                "Malformed request")))
  (POST "/add" {body :body}
        (if (some #{:program-manager} (:user-functions *session*))
          (try+ (let [request (coerce-as xs/Course (slurp body))]
                  (query/course-add! request)
                  (write-str {:result "OK"}))
                (catch [:type :coercion-error] e
                  "Malformed request")
                (catch Exception e
                  (write-str {:result "ERROR"})))
          (write-str {:result "Not authorized"}))))

(defroutes program-routes
  (GET "/" [] "Invalid request")
  (GET "/list" []
       (write-str {:programs (query/program-list)}))
  (GET "/get/:id" [id]
       (let [result (query/program-get id)]
         (if result
           (write-str result)
           "[]")))
  (DELETE "/del/:id" [id]
          (if (query/program-get id)
            (do (query/program-delete! id)
                (write-str {:result "OK"}))
            (write-str {:result "ERROR"})))
  (POST "/find" {body :body}
        (try+ (let [request (coerce-as FindQuery (slurp body))
                    result (query/program-find (:keywords request))]
                (write-str {:result result}))
              (catch [:type :coercion-error] e
                "Malformed request")))
  (POST "/add" {body :body}
        (try+ (let [request (coerce-as xs/Program (slurp body))]
                (query/program-add! request)
                (write-str {:result "OK"}))
              (catch [:type :coercion-error] e
                "Malformed request")
              (catch Exception e
                (write-str {:result "ERROR"})))))

(defroutes room-routes
  (GET "/" [] "Invalid request")
  (GET "/list" []
       (write-str {:rooms (query/room-list)})))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes)
  (context "/room" [] room-routes))

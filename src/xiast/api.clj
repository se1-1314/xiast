(ns xiast.api
  (:use compojure.core
        [xiast.database :only [*db*]]
        [clojure.data.json :only [read-str write-str]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [xiast.query :as query]
            [clojure.data.json :as json]
            [schema.core :as s]
            [schema.utils :as utils]
            [schema.coerce :as coerce]))

;; TODO: Write some API docs (nvgeele)
;; TODO: Define standard error message + HTTP error code (nvgeele)
;; TODO: Return correct json on empty returns (nvgeele)

(defn coerce-as
  [schema str]
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
       (let [result (query/course-get *db* course-code)]
         (if result
           (write-str result)
           "[]")))
  (DELETE "/del/:course-code" [course-code]
          (if (query/course-get *db* course-code)
            (do (query/course-delete! *db* course-code)
                (write-str {:result "OK"}))
            (write-str {:result "ERROR"})))
  (POST "/find" {body :body}
        (try+ (let [request (coerce-as FindQuery (slurp body))
                    result (query/course-find *db* (:keywords request))]
                (write-str {:result result}))
              (catch [:type :coercion-error] e
                "Malformed request")))
  (POST "/add" {body :body}
        (try+ (let [request (coerce-as query/Course (slurp body))]
                (query/course-add! *db* request)
                (write-str {:result "OK"}))
              (catch [:type :coercion-error] e
                "Malformed request")
              (catch Exception e
                (write-str {:result "ERROR"})))))

(defroutes program-routes
  (GET "/" [] "Invalid request")
  (GET "/list" []
       (write-str {:programs (query/program-list *db*)}))
  (GET "/get/:id" [id]
       (let [result (query/program-get *db* id)]
         (if result
           (write-str result)
           "[]")))
  (DELETE "/del/:id" [id]
          (if (query/program-get *db* id)
            (do (query/program-delete! *db* id)
                (write-str {:result "OK"}))
            (write-str {:result "ERROR"})))
  (POST "/find" {body :body}
        (try+ (let [request (coerce-as FindQuery (slurp body))
                    result (query/program-find *db* (:keywords request))]
                (write-str {:result result}))
              (catch [:type :coercion-error] e
                "Malformed request")))
  (POST "/add" {body :body}
        (try+ (let [request (coerce-as query/Program (slurp body))]
                (query/program-add! *db* request)
                (write-str {:result "OK"}))
              (catch [:type :coercion-error] e
                "Malformed request")
              (catch Exception e
                (write-str {:result "ERROR"})))))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes))

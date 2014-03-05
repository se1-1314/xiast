(ns xiast.api
  (:use compojure.core
        [xiast.database :only [*db*]]
        [clojure.data.json :only [read-str write-str]])
  (:require [xiast.query :as query]
            [clojure.data.json :as json]
            [schema.core :as s]
            [schema.coerce :as coerce]))

;; TODO: Write some API docs (nvgeele)
;; TODO: Define standard error message + HTTP error code (nvgeele)
;; TODO: Return correct json on empty returns (nvgeele)

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
  (POST "/find" [data]
        (let [request (parse-find-query (read-json data))
              result (query/course-find *db* (:keywords request))]
          (write-str {:result result}))))

(defroutes program-routes
  (GET "/" [] "Invalid request")
  (GET "/list" []
       (write-str {:programs (query/program-list *db*)}))
  (GET "/get/:id" [id]
       (let [result (query/program-get *db* id)]
         (if result
           (write-str result)
           "[]")))
  (POST "/find" [data]
        (let [request (parse-find-query (read-json data))
              result (query/program-find *db* (:keywords request))]
          (write-str {:result result}))))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes))

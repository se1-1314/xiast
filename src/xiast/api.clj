(ns xiast.api
  (:use compojure.core
        [xiast.database :only [*db*]]
        [clojure.data.json :only [read-str write-str]])
  (:require [xiast.query :as query]
            [clojure.data.json :as json]
            [schema.core :as s]
            [schema.coerce :as coerce]))

;; TODO: Define standard error message + HTTP error code (nvgeele)

(defn read-json
  [str]
  (read-str str :key-fn keyword))

(def CourseFindQuery
  {:keywords [s/Str]})

(def parse-course-find-query
  (coerce/coercer CourseFindQuery coerce/json-coercion-matcher))

(defroutes course-routes
  (GET "/" [] "Invalid request")
  (GET "/list" []
       (write-str {:courses (query/course-list *db*)}))
  (GET "/get/:course-code" [course-code]
       (write-str (query/course-get *db* course-code)))
  (POST "/find" [data]
        (let [request (parse-course-find-query (read-json data))
              result (query/course-find *db* (:keywords request))]
          (write-str {:result result}))))

(defroutes program-routes
  (GET "/" [] "Invalid request"))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes))


(def test-data
  "{ \"keywords\" : [] }")

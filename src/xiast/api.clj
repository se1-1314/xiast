(ns xiast.api
  (:use compojure.core
        [xiast.database :only [*db*]])
  (:require [xiast.query :as query]))

;; TODO: Define standard error message + HTTP error code (nvgeele)

(defroutes course-routes
  (GET "/" [] "Invalid request")
  (GET "/list" []
       (reduce str (query/course-list *db*))))

(defroutes program-routes
  (GET "/" [] "Invalid request"))

(defroutes api-routes
  (GET "/" [] "Invalid request")
  (context "/course" [] course-routes)
  (context "/program" [] program-routes))

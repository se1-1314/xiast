(ns xiast.core
  (:use compojure.core
        net.cgrand.enlive-html
        [xiast.mock :only [*mock-data*]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [xiast.query :as query]))

(deftemplate main-page "templates/index.html"
  [courses]
  [:ul#course-list :li] (clone-for [course courses]
                                   (content (:title course))))

;;; Read: https://github.com/weavejester/compojure/wiki
(defroutes main-routes
  (GET "/" [] (main-page (query/courses *mock-data*)))
  (route/not-found "Not found!"))

(def app
  (-> (handler/site main-routes)))

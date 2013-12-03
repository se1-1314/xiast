(ns xiast.core
  (:use compojure.core
        net.cgrand.enlive-html
        [xiast.mock :only [*mock-data*]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [xiast.query :as query]
            [taoensso.tower :as tower
             :refer (with-locale with-tscope t *locale*)]))

(def tconfig
  {:dev-mode? true
   :fallback-locale :en
   ;; TODO Write function to load dictionaries
   :dictionary {:en (load-file "resources/dictionaries/en.clj")
                :nl (load-file "resources/dictionaries/nl.clj")}})

(defsnippet index-page-body "templates/index.html" [:#page-content] []
  identity)

(defsnippet about-page-body "templates/about.html" [:#page-content] []
  identity)

(defsnippet login-page-body "templates/login.html" [:#page-content] []
  identity)

(deftemplate index-page "templates/layout.html"
  [courses index]
  [:div#page-content] (content index)
  [:ul#course-list :li] (clone-for [course courses]
                                   (content (:title course))))

(deftemplate about-page "templates/layout.html"
  [about]
  [:div#page-content] (content about))

(deftemplate login-page "templates/layout.html"
  [login]
  [:div#page-content] (content login))

;;; Read: https://github.com/weavejester/compojure/wiki
(defroutes main-routes
  (GET "/" [] (index-page (query/courses *mock-data*) (index-page-body)))
  (GET "/about" [] (about-page (about-page-body)))
  (GET "/login" [] (login-page (login-page-body)))
  (route/not-found "Not found!"))

(def app
  (-> (handler/site main-routes)))

(ns xiast.core
  (:use compojure.core
        [xiast.mock :only [*mock-data*]]
        [xiast.session :as session]
        [xiast.authentication :as auth]
        [ring.middleware.file-info :only [wrap-file-info]]
        [ring.middleware.resource :only [wrap-resource]]
        [ring.handler.dump :only [handle-dump]]
        [ring.util.response :as resp]
        net.cgrand.enlive-html)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [xiast.query :as query]
            [taoensso.tower :as tower
             :refer (with-locale with-tscope t *locale*)]
            [taoensso.tower.ring :as tower.ring]
            [xiast.translate :as translate]))


(deftemplate base "templates/layout.html"
  [body]
  [:div#page-content] (content body))

(defsnippet index-body "templates/index.html" [:div#page-content]
  []
  [:ul#course-list :li] (clone-for [course (query/courses *mock-data*)]
                                   (content (:title (val course)))))

(defroutes index-routes
  (GET "/" [] (base (-> (index-body)
                        (translate/translate-nodes
                         [:index/welcome "Guest"])))))

(defsnippet about-body "templates/about.html" [:div#page-content]
  []
  identity)

(defroutes about-routes
  (GET "/about" [] (base (-> (about-body)
                             (translate/translate-nodes)))))

(defsnippet login-body "templates/login.html" [:div#page-content]
  []
  identity)
(defroutes login-routes
  (GET "/login" [] (base (-> (login-body)
                             (translate/translate-nodes))))
  (POST "/login" {cookies :cookies params :params}
    (if-let [res (auth/login (:user params) (:pwd params))]
      (assoc (resp/redirect "/") :cookies (session/to-cookies res))
      (base (login-body))))
  (GET "/logout" {cookies :cookies}
    (let [session (session/from-cookies cookies)]
      (assoc (resp/redirect "/") :cookies (session/to-cookies (session/kill-session! session))))))

;;; Read: https://github.com/weavejester/compojure/wiki
(defroutes main-routes
  index-routes
  about-routes
  login-routes
  (route/not-found "Not found!"))


(def app
  (-> (handler/site main-routes)
      (tower.ring/wrap-tower-middleware :fallback-locale :en :tconfig translate/tower-config)
      (wrap-resource "public")
      (wrap-file-info)))

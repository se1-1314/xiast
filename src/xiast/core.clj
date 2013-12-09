(ns xiast.core
  (:use compojure.core
        [xiast.mock :only [*mock-data*]]
        [xiast.session :as session]
        [xiast.authentication :as auth]
        [ring.middleware.file-info :only [wrap-file-info]]
        [ring.middleware.resource :only [wrap-resource]]
        [ring.handler.dump :only [handle-dump]]
        [ring.util.response :as resp]
        [ring.middleware.session.cookie :only [cookie-store]]
        net.cgrand.enlive-html)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [xiast.query :as query]
            [taoensso.tower :as tower
             :refer (with-locale with-tscope t *locale*)]
            [taoensso.tower.ring :as tower.ring]
            [xiast.translate :as t]))


(deftemplate base "templates/layout.html"
  [body & {:keys [title]}]
  [:html :> :head :> :title] (content title)
  [:div#page-content] (content body))

(defsnippet index-body "templates/index.html" [:div#page-content]
  []
  [:ul#course-list :li] (clone-for [course (query/courses *mock-data*)]
                                   (content (:title (val course)))))

(defroutes index-routes
  (GET "/" {session :session}
    (base (-> (index-body)
            (t/translate-nodes
              [:index/welcome (if-let [user (:user session)]
                                user "Guest")]))
          :title (t/translate :index/title))))

(defsnippet about-body "templates/about.html" [:div#page-content]
  []
  identity)

(defroutes about-routes
  (GET "/about" [] (base (-> (about-body)
                             (t/translate-nodes)))))

(defsnippet login-body "templates/login.html" [:div#page-content]
  []
  identity)

(defroutes login-routes
  (GET "/login" {session :session}
    (if (:user session)
      ;;TODO: flash message that user is already logged in
      (resp/redirect "/")
      ;;TODO: flash message that login was succesful
      (base (-> (login-body)
                (t/translate-nodes)))))
  (POST "/login" {session :session params :params}
    (if-let [res (auth/login (:user params) (:pwd params))]
      (assoc (resp/redirect "/") :session (conj session res))
      (base (login-body)))) ;;TODO: flash message that username/password is incorrect
  (GET "/logout" {session :session}
    (if (:user session)
      (assoc (resp/redirect "/") :session {:locale (:locale session)})
      (assoc (resp/redirect "/")))))

(defn- block-time->time-str [t]
  (str (+ 7 (quot (- t 1) 2))
       ":"
       (if (= 0 (mod (- t 1) 2))
         "00"
         "30")))

(defsnippet schedule-body "templates/schedule.html" [:div#page-content]
  [schedule-blocks]
  [:ul#schedule :li] (clone-for [sb schedule-blocks]
                                (content (str "W" (:week sb)
                                              " D" (:day sb)
                                              " " (block-time->time-str (:start-time sb))
                                              "-" (block-time->time-str (:end-time sb))
                                              ": " (-> sb :course :title)
                                              " in " (:room sb)))))

;; FIXME, hack?
(defn- schedule-page [schedule-blocks]
  (base (-> (schedule-body schedule-blocks)
            (t/translate-nodes))))
(defroutes schedule-routes
  (GET "/schedule/student/:student-id" [student-id]
       (schedule-page (query/student-schedule *mock-data* student-id)))
  (GET "/schedule/room/:room-id" [room-id]
       (schedule-page (query/room-schedule *mock-data* room-id)))
  (GET "/schedule/course/:course-id" [course-id]
       (schedule-page (query/course-schedule *mock-data* course-id))))

(defroutes language-routes
  (GET "/lang/:locale" [cookies :as {session :cookies}]
       (assoc (resp/redirect "/") {}))) ;; TODO

;;; Read: https://github.com/weavejester/compojure/wiki
(defroutes main-routes
  index-routes
  about-routes
  login-routes
  schedule-routes
  language-routes
  (route/not-found "Not found!"))


(def app
  ;; TODO: get cookie-store secret key out of a config file or something
  (-> (handler/site main-routes {:session {:store (cookie-store {:key "Kn4pHR5jxnuo3Bmc"})}})
      (tower.ring/wrap-tower-middleware :fallback-locale :en :tconfig t/tower-config)
      (wrap-resource "public")
      (wrap-file-info)))

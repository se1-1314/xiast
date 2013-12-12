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
        [ring.middleware params
         keyword-params
         nested-params
         multipart-params
         cookies
         session
         flash]
        net.cgrand.enlive-html)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [xiast.query :as query]
            [taoensso.tower :as tower
             :refer (with-locale with-tscope t *locale*)]
            [taoensso.tower.ring :as tower.ring]
            [xiast.translate :as t]))


(def login-link "<a href=\"/login\" msg=\"header/login\">Login</a>")
(defn logged-in-link [user]
  (str "<a href=\"/logout\">" user "</a>"))

(deftemplate base "templates/layout.html"
  [body & {:keys [title]}]
  [:html :> :head :> :title] (content title)
  [:div#page-content] (content body)
  ;;[:li#login-out] (html-content loginout)
  )

(defsnippet index-body "templates/index.html" [:div#page-content]
  []
  [:div#course-list :div] (clone-for [course (query/courses *mock-data*)]
                                     (content (:title  course))))

(defroutes index-routes
  (GET "/" {session :session}
    (base (-> (index-body)
            (t/translate-nodes
              [:index/welcome (if-let [user (:user session)]
                                user "Guest")]))
          (if-let [user (:user session)]
            (logged-in-link user) login-link)
          :title (t/translate :index/title))))

(defsnippet about-body "templates/about.html" [:div#page-content]
  []
  identity)

(defroutes about-routes
  (GET "/about" {session :session}
       (base (-> (about-body)
                 (t/translate-nodes))
             (if-let [user (:user session)]
               (logged-in-link user) login-link))))

(defsnippet login-body "templates/login.html" [:div#page-content]
  []
  identity)


(defroutes login-routes
  (GET "/login" []
    (if (:user session)
      ;;TODO: flash message that user is already logged in
      (resp/redirect "/")
      ;;TODO: flash message that login was succesful
      (base (-> (login-body)
                (t/translate-nodes))
            (if-let [user (:user session)]
              (logged-in-link user) login-link))))
  (POST "/login" {session :session params :params}
    (if-let [res (auth/login (:user params) (:pwd params))]
      (assoc (resp/redirect "/") :session (conj session res))
      (base (login-body)
            (if-let [user (:user session)]
              (logged-in-link user) login-link)))) ;;TODO: flash message that username/password is incorrect
  (GET "/logout" []
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
  [:div#schedule :div] (clone-for [sb schedule-blocks]
                                  (html-content (str "<div class=\"panel panel-success\">"
                                                     "<div class=\"panel-heading\">"
                                                     "W" (:week sb)
                                                     " D" (:day sb)
                                                     " - "
                                                     (-> sb :course :title)
                                                     "</div>"
                                                     "<div class=\"panel-body row\">"
                                                     "<div class=\"col-md-6 pull-left\">"
                                                     " " (block-time->time-str (:start-time sb))
                                                     "</div>"
                                                     "<div class=\"col-md-6 pull-right\">"
                                                     (block-time->time-str (:end-time sb))
                                                     "</div>"
                                                      "<div class=\"col-md-12\">"
                                                     "<a href=\"/schedule/room/"
                                                     (:room sb)
                                                     "\" >"
                                                     (:room sb)
                                                     "</a>"
                                                     "</div>"
                                                     "</div>"))))

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

(defsnippet course-body "templates/courses.html" [:div#page-content]
  [courses]
  [:div#course-list :div]
  (clone-for [course courses]
             (html-content (str "<a href=\"/schedule/course/"
                                (:id course) "\">"
                                (:title course)
                                "</a>"))))
  
(defroutes course-routes
  (GET "/courses" [key]
       (base (-> (course-body (if (:key params)
                                (query/courses *mock-data* (:key params))
                                      (query/courses *mock-data*)))
                       (t/translate-nodes))
                   (if-let [user (:user session)]
                     (logged-in-link user) login-link))))
  
(defroutes language-routes
  (GET "/lang/:locale" [locale]
    (assoc (resp/redirect "/")
           :session (assoc session :locale locale))))

;;; Read: https://github.com/weavejester/compojure/wiki
(defroutes main-routes
  index-routes
  about-routes
  login-routes
  schedule-routes
  language-routes
  course-routes
  (route/not-found "Not found!"))


(def app
  ;; TODO: get cookie-store secret key out of a config file or something
  (-> main-routes
      session/wrap-with-session
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      wrap-multipart-params
      wrap-flash
      (tower.ring/wrap-tower-middleware :fallback-locale :en :tconfig t/tower-config)
      (wrap-session {:store (cookie-store {:key "Kn4pHR5jxnuo3Bmc"})})
      (wrap-resource "public")
      (wrap-file-info)))

(ns xiast.core
  (:use compojure.core
        [xiast.mock :only [*mock-data*]]
        [xiast.session :only [*alert* *session* wrap-with-session]]
        [xiast.authentication :as auth]
        [xiast.config :only [config]]
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


;; (def login-link "<a href=\"/login\" msg=\"header/login\">Login</a>")
(def login-link
  (str "<li class=\"dropdown\">"
       "<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">"
       "Login <strong class=\"caret\"></strong>"
       "</a>"
       "<div class=\"dropdown-menu login\">"
       "<form action=\"/login\" method=\"post\" accept-charset=\"UTF-8\" role=\"form\">"
       "<fieldset>"
       "<div class=\"form-group\">"
       "<input class=\"form-control\" id=\"user\" name=\"user\" placeholder=\"User\" type=\"text\">"
       "</div>"
       "<div class=\"form-group\">"
       "<input class=\"form-control\" id=\"pwd\" name=\"pwd\" placeholder=\"Password\" type=\"password\">"
       "</div>"
       "<input class=\"btn btn-lg bt-succes btn-block\" type=\"submit\" value=\"Log in\">"
       "</fieldset>"
       "</div>"
       "</li>"
       "</form>"))

(defn logged-in-link [user]
  (str "<li class=\"dropdown\">"
       "<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">"
       user
       "<span class=\"caret\"></span>"
       "</a>"
       "<ul class=\"dropdown-menu\">"
       "<li> <a href=\"/schedule/student/" user "\">Schedule</li>"
       "<li><a href=\"/logout\">Logout</a></li>"
       "</ul>"
       "</li>"))

(deftemplate base "templates/layout.html"
  [body & {:keys [title alert]}]
  [:html :> :head :> :title] (content title)
  [:div#page-content] (content body)
  [:li#login-out] (html-content (if-let [user (:user *session*)]
                                  (logged-in-link user)
                                  login-link))
 [:div#alert] (if-let [alert (or *alert* alert)]
                 (do-> (add-class (str "alert-" (name (:type alert))))
                       (content (t/translate (:message alert)))))
  ;; FIXME, this prefixes absolute URLs witha string. Needs to be read
  ;; from configuration file.
  ;; [:a] (fn [nodes]
  ;;       (update-in nodes [:attrs :href] #(if (= (first %) \/)
  ;;                                          (str nil %))))
)

(defsnippet index-body "templates/index.html" [:div#page-content]
  []
  [:div#course-list :div] (clone-for [course (query/courses *mock-data*)]
                                     (content (:title  course))))

(defroutes index-routes
  (GET "/" []
    (base (-> (index-body)
            (t/translate-nodes
              [:index/welcome (if-let [user (:user *session*)]
                                user
                                "Guest")]))
          :title (t/translate :index/title))))

(defsnippet about-body "templates/about.html" [:div#page-content]
  []
  identity)

(defroutes about-routes
  (GET "/about" []
       (base (-> (about-body)
                 (t/translate-nodes)))))

(defsnippet login-body "templates/login.html" [:div#page-content]
  []
  identity)


(defroutes login-routes
  (GET "/login" []
    (if (:user *session*)
      (assoc (resp/redirect "/") :flash
             {:message :authentication/already-logged-in :type "info"})
      (base (-> (login-body)
                (t/translate-nodes)))))
  (POST "/login" [user pwd]
    (if-let [res (auth/login user pwd)]
      (assoc (resp/redirect "/")
             :session (conj *session* res)
             :flash {:message :authentication/logged-in-successful :type "success"})
      (base (-> (login-body)
                (t/translate-nodes))
            :alert {:message :authentication/incorrect-credentials
                    :type "danger"})))
  (GET "/logout" []
    (if (:user *session*)
      (assoc (resp/redirect "/") :session {:locale (:locale *session*)})
      (assoc (resp/redirect "/")))))

(defn- block-time->time-str [t]
  (str (+ 7 (quot (- t 1) 2))
       ":"
       (if (= 0 (mod (- t 1) 2))
         "00"
         "30")))

(defsnippet schedule-body "templates/schedule.html" [:div#page-content]
  [schedule-blocks]
  [:div#schedule :div]
  (clone-for [sb schedule-blocks]
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
             (html-content (str "<a class=\"btn course-btn\" href=\"/schedule/course/"
                                (:id course) "\">"
                                (:title course)
                                "</a>"))))

(defroutes course-routes
  (GET "/courses" [key]
       (base (-> (course-body (if key
                                (query/courses *mock-data* key)
                                (query/courses *mock-data*)))
                 (t/translate-nodes)))))

(defroutes language-routes
  (GET "/lang/:locale" [locale]
    (assoc (resp/redirect "/")
           :session (assoc *session* :locale locale))))

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
      wrap-with-session
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      wrap-multipart-params
      wrap-flash
      (tower.ring/wrap-tower-middleware :fallback-locale :en :tconfig t/tower-config)
      (wrap-session {:store (cookie-store {:key "Kn4pHR5jxnuo3Bmc"})})
      (wrap-resource "public")
      (wrap-file-info)))

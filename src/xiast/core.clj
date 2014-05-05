(ns xiast.core
  (:use compojure.core
        [xiast.session :only [*session* *alert* session-store wrap-with-session]]
        [xiast.authentication :as auth]
        [xiast.config :only [config]]
        [xiast.api :only [api-routes]]
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
            [xiast.translate :as t]
            [xiast.query :as query]))


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
       "<li><a href=\"/logout\">Logout</a></li>"
       "<li><a href=\"/profile\">Profile</a></li>"
       "</ul>"
       "</li>"))

(deftemplate base "templates/layout.html"
  [body & {:keys [title alert]}]
  [:html :> :head :> :title] (content title)
  [:li#my-schedule] (cond
                      (contains? (set (:user-functions *session*)) :program-manager) identity
                      (contains? (set (:user-functions *session*)) :student) identity
                      (contains? (set (:user-functions *session*)) :titular) identity
                      :else nil)
  [:li#curriculum-info] (if (contains? (set (:user-functions *session*)) :student)
                          identity
                          nil)
  [:li#semi-scheduling] (if (contains? (set (:user-functions *session*)) :titular)
                          identity
                          nil)
  [:li#schedule-nav] (if (contains? (set (:user-functions *session*)) :program-manager)
                   identity
                   nil)
  [:li#classroom-edit] (if (contains? (set (:user-functions *session*)) :program-manager)
                         identity
                         nil)
  [:li#program-edit] (if (contains? (set (:user-functions *session*)) :program-manager)
                       identity
                       nil)

  [:div#page-content] (content body)
  [:li#login-out] (html-content (if-let [user (:user *session*)]
                                  (logged-in-link user)
                                  login-link))
  [:div#alert] (if-let [alert (or *alert* alert)]
                 (do-> (add-class (str "alert-" (name (:type alert))))
                       (append (t/translate (:message alert))))))

 ;; What this actually did was deleting all hyperlinkgs by having it referring to nothing
 ;; please do not reinclude this piece of code unless you are certain you are not reintrodicing that bug.
 ;; We NEED those hyperlinks!
 ;; This prefixes absolute URLs with a string.
 ;;[:a] (fn [nodes]
 ;;       (update-in nodes [:attrs :href]
 ;;                  #(if (= (first %) \/)
 ;;                     (str (:url-prefix config) %)))))

(defsnippet index-body "templates/index.html" [:div#page-content]
  []
  identity)

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

(defsnippet curriculum-info-body "templates/curriculum-info.html" [:div#page-content]
  []
  identity)

(defroutes curriculum-info-routes
  (GET "/curriculum-info" []
       (base (-> (curriculum-info-body)
                 (t/translate-nodes)))))

(defsnippet semi-scheduling-body "templates/semi-scheduling.html" [:div#page-content]
  []
  identity)

(defroutes semi-scheduling-routes
  (GET "/semi-scheduling" []
       (base (-> (semi-scheduling-body)
                 (t/translate-nodes)))))

(defsnippet program-edit-body "templates/program-edit.html" [:div#page-content]
  []
  identity)

(defroutes program-edit-routes
  (GET "/program-edit" []
       (base (-> (program-edit-body)
                 (t/translate-nodes)))))

(defsnippet classroom-edit-body "templates/classroom-edit.html" [:div#page-content]
  []
  identity)

(defroutes classroom-edit-routes
  (GET "/classroom-edit" []
       (base (-> (classroom-edit-body)
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
      (assoc (resp/redirect "/") :session (auth/logout *session*))
      (assoc (resp/redirect "/")))))

(defn- block-time->time-str [t]
  (str (+ 7 (quot (- t 1) 2))
       ":"
       (if (= 0 (mod (- t 1) 2))
         "00"
         "30")))

(defsnippet schedule-body "templates/schedule.html" [:div#page-content] []
  identity
  )

;; FIXME, hack?
(defn- schedule-page [schedule-blocks]
  (base (-> (schedule-body schedule-blocks)
            (t/translate-nodes))))

(defroutes schedule-routes
  (GET "/timetables" []
       (base (-> (schedule-body)
                 (t/translate-nodes))))
  (GET "/schedule" []
       (base (-> (schedule-body)
                 (t/translate-nodes)))))


(defsnippet profile-student-body "templates/profile-student.html" [:div#page-content]
  []
  identity)
(defsnippet profile-titular-body "templates/profile-titular.html" [:div#page-content]
  []
  identity)
(defsnippet profile-program-manager-body "templates/profile-program-manager.html" [:div#page-content]
  []
  identity)
      
(defroutes profile-routes
  (GET "/profile" []
       (base (-> 
                 (cond 
                   (contains? (set (:user-functions *session*)) :student) (profile-student-body)
                   (contains? (set (:user-functions *session*)) :titular) (profile-titular-body)
                   (contains? (set (:user-functions *session*)) :program-manager) (profile-program-manager-body)
                   :else nil)
                 (t/translate-nodes)))))

(defsnippet my-schedule-program-manager-body "templates/my-schedule.html" [:div.my-schedule-program-manager] [])
(defsnippet my-schedule-titular-body "templates/my-schedule.html" [:div.my-schedule-titular] [])
(defsnippet my-schedule-student-body "templates/my-schedule.html" [:div.my-schedule-student] [])

(defroutes my-schedule-routes
  (GET "/my-schedule" []
       (base (-> 
               (cond
                      (contains? (set (:user-functions *session*)) :program-manager) (my-schedule-program-manager-body)
                      (contains? (set (:user-functions *session*)) :student) (my-schedule-student-body)
                      (contains? (set (:user-functions *session*)) :titular) (my-schedule-titular-body)
                      :else nil)
                 (t/translate-nodes)))))

(defroutes language-routes
  (GET "/lang/:locale" [locale]
    (assoc (resp/redirect "/")
           :session (assoc *session* :locale locale))))

;;; Read: https://github.com/weavejester/compojure/wiki
(defroutes main-routes
  (context "/api" [] api-routes)
  index-routes
  about-routes
  login-routes
  schedule-routes
  language-routes
  curriculum-info-routes
  semi-scheduling-routes
  program-edit-routes
  classroom-edit-routes
  profile-routes
  my-schedule-routes
  (route/not-found "Not found!"))


(def app
  (-> main-routes
      wrap-with-session
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      wrap-multipart-params
      wrap-flash
      (tower.ring/wrap-tower-middleware :fallback-locale :en :tconfig t/tower-config)
      (wrap-session {:store (session-store)})
      (wrap-resource "public")
      (wrap-file-info)))

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
            [xiast.query.core :as query]
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
       "<li><a href=\"/logout\">Logout</a></li>"
       "<li><a href=\"/profile\">Profile</a></li>"
       "</ul>"
       "</li>"))

(deftemplate base "templates/layout.html"
  [body & {:keys [title alert]}]
  [:html :> :head :> :title] (content title)
  [:.guest] (cond
            (contains? (set (:user-functions *session*)) :program-manager) nil
            (contains? (set (:user-functions *session*)) :student) nil
            (contains? (set (:user-functions *session*)) :titular) nil
            :else identity)
  [:div#menu] #(t/translate-nodes %)
  [:.non-guest] (cond
                 (contains? (set (:user-functions *session*)) :program-manager) identity
                 (contains? (set (:user-functions *session*)) :student) identity
                 (contains? (set (:user-functions *session*)) :titular) identity
                 :else nil)
  [:.student] (if (contains? (set (:user-functions *session*)) :student)
           identity
           nil)
  [:.titular] (if (contains? (set (:user-functions *session*)) :titular)
            identity
            nil)
  [:.program-manager] (if (contains? (set (:user-functions *session*)) :program-manager)
          identity
          nil)
  [:div#page-content] (content body)
  [:li#login-out] (html-content (if-let [user (:user *session*)]
                                  (logged-in-link user)
                                  login-link))
  [:div#alert] (if-let [alert (or *alert* alert)]
                 (do-> (add-class (str "alert-" (name (:type alert))))
                       (append (t/translate (:message alert)))))
  [:script#initscript] (html-content
                        (str "var current_user = \""
                             (let [user-functions (:user-functions *session*)]
                               (cond
                                (contains? user-functions :student)
                                "student"
                                (contains? user-functions :titular)
                                "titular"
                                (contains? user-functions :program-manager)
                                "program-manager"
                                :else
                                "guest"))
                             "\";")))

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

(defsnippet schedules-body "templates/schedules.html" [:div#page-content] [] identity)

(defroutes schedules-routes
  (GET "/schedules" []
       (base (->
              (schedules-body)
              (t/translate-nodes)))))

(defsnippet my-schedule-program-manager-body "templates/my-schedule.html" [:.program-manager] [])
(defsnippet my-schedule-titular-body "templates/my-schedule.html" [:.titular] [])
(defsnippet my-schedule-student-body "templates/my-schedule.html" [:.student] [])

(defroutes my-schedule-routes
  (GET "/my-schedule" []
       (base (->
              (cond
               (contains? (set (:user-functions *session*)) :program-manager) (my-schedule-program-manager-body)
               (contains? (set (:user-functions *session*)) :student) (my-schedule-student-body)
               (contains? (set (:user-functions *session*)) :titular) (my-schedule-titular-body)
               :else nil)
              (t/translate-nodes)))))

(defsnippet curriculum-info-body "templates/curriculum-info.html" [:div#page-content]
  []
  identity)

(defroutes curriculum-info-routes
  (GET "/curriculum-info" []
       (base (-> (curriculum-info-body)
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

(defn- block-time->time-str [t]
  (str (+ 7 (quot (- t 1) 2))
       ":"
       (if (= 0 (mod (- t 1) 2))
         "00"
         "30")))

(defsnippet profile-student-body "templates/profile-student.html" [:div#page-content] [] identity)
(defsnippet profile-titular-body "templates/profile-titular.html" [:div#page-content] [] identity)
(defsnippet profile-program-manager-body "templates/profile-program-manager.html" [:div#page-content] [] identity)

(defroutes profile-routes
  (GET "/profile" []
       (base (->
              (cond
               (contains? (set (:user-functions *session*)) :student) (profile-student-body)
               (contains? (set (:user-functions *session*)) :titular) (profile-titular-body)
               (contains? (set (:user-functions *session*)) :program-manager) (profile-program-manager-body)
               :else nil)
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

(defroutes language-routes
  (GET "/lang/:locale" [locale]
       (if (:user *session*)
         (query/person-locale! (:user *session*) locale))
       (assoc (resp/redirect "/")
         :session (assoc *session* :locale locale))))

;;; Read: https://github.com/weavejester/compojure/wiki
(defroutes main-routes
  (context "/api" [] api-routes)
  index-routes
  about-routes
  schedules-routes
  my-schedule-routes
  curriculum-info-routes
  program-edit-routes
  classroom-edit-routes
  profile-routes
  login-routes
  language-routes
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

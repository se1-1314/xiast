(ns xiast.database
  (:use [xiast.config :only [config]]
        [korma.db]
        [korma.core]))

;; TODO: Change `database' in config to `db' so we can
;; just use the config map as argument.
(defdb db
  (let [config (:database config)]
    (if (= (:type config) 'mysql)
      (mysql
       {:host (:host config)
        :db (:database config)
        :user (:user config)
        :password (:password config)})
      (sqlite3
       {:db (:database config)}))))

(defentity student
  (database db)
  (pk :studentRoleNumber))

(defentity course
  (database db)
  (pk :courseCode))

(defentity department
  (database db)
  (pk :departmentID))

(defentity instructor
  (database db)
  (pk :instructorID))

(defentity course-titular
  (table :course_titular)
  (database db))

(defentity course-enrollment
  (table :course_enrollment)
  (database db))

(defentity course-session-period
  (table :course_session_period)
  (database db))

(defentity courseactivity
  (database db))

(defentity studyprogram
  (database db))

(defentity studyprogram-choicecourse
  (table :studyprogram_choicecourse)
  (database db))

(defentity studyprogram-mandatorycourse
  (table :studyprogram_mandatorycourse)
  (database db))

(defn get-user
  [netid]
  (select student
          (where {:netId netid})))

(defn create-user
  [netid locale type]
  (insert student
          (values {:netId netid
                   :locale locale
                   :type type})))

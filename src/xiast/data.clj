(ns xiast.data
  "Data structures.

  ")

;; TODO case insensitive searches

(defn room [building floor number capacity
            {:keys [beamer overhead-projector]
             :as facilities}]
  {:room-id [building floor number]
   :facilities facilities})

;; TODO needs exceptions for destructive ops
(defprotocol Rooms
  (room-add!
    [this room]
    "Add a room to the data store.")
  (room-delete!
    [this building floor number]
    "Delete a room from the data store.")
  (room-get
    [this building floor number]
    "Returns the matching room description."))

(defn course
  [{:keys [id description elements semester titular instructors degree faculty department]
    :as c}]
  "Semester is either 1, 2 or [1 2].

  Elements are :HOC, :WPO + contacturen.

  Intructors is a set of netids.

  Degree is either :master, :bachelor, :manama, :schakel,
  :voorbereidingsprogramma :postgraduaat

  Faculty is :wetenschappen, TODO

  Department is :wiskunde TODO"
  c)

(defprotocol Courses
  (course-add! [this course])
  (course-delete! [this course-id])
  (course-get [this course-id])
  (course-find
    [this]
    [this title-kw]
    "Return a list of {:title \"Course title\" :id \"Course ID\"},
    optionally using a search keyword for the name of the course (case
    insensitive)."))

(defn enrollment
  [{:keys [student-id course-id program element]
    :as e}]
  e)

(defn student
  [& {netid first-name last-name}])

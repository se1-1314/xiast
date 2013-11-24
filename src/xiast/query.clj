(ns xiast.query
  "This namespace provides protocols for querying information
  accessible through Xiast.

  Timespans are maps structured as follows:

  {:weeks [1 52] ;; Weeks in the academic calendar
   :days [1 7]
   :time [1 34]} ;; Half-hour time slots from 07:00 through 23:30

  Weeks, days and time can also be a single integer instead of a range.")

(defprotocol CourseList
  (courses
    [this]
    [this title-kw]
    "Return a list of {:title \"Course title\" :id \"Course ID\"},
    optionally using a search keyword for the name of the course."))

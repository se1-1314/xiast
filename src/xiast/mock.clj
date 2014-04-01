(ns xiast.mock
  (:use xiast.query))

(defrecord MockData [courses schedules enrollment])

(def *mock-data*
  (MockData. {"course1" {:title "Mathematics"}
              "course2" {:title "Physics"}
              "course3" {:title "Scheme"}}
             {"course1" {1 {1 [{:time [3 6]
                                :room "E1.01"}]
                            2 [{:time [7 12]
                                :room "E1.02"}]}}}
             {"student1" #{"course1"}}))

(defn- in-range? [num range]
  (<= (first range) num (second range)))

(defn- schedule-block-in-timespan? [block timespan]
  ;; FIXME ew (aleijnse)
  (and (every? true?
               (map in-range?
                    (map block [:week :day])
                    (map timespan [:weeks :days])))
       (or (in-range? (:start-time block) (:time timespan))
           (in-range? (:end-time block) (:time timespan)))))

(defn- schedule-blocks [{schedules :schedules
                         courses :courses}
                        course-id]
  (let [course {:id course-id
                :title (-> courses (get course-id) :title)}]
    (for [week (get schedules course-id)
          day (val week)
          block (val day)]
      {:week (key week)
       :day (key day)
       :start-time (first (:time block))
       :end-time (second (:time block))
       :course course
       :room (:room block)})))

(defn- filter-timespan [timespan schedule-blocks]
  (filter #(schedule-block-in-timespan? % timespan)
          schedule-blocks))

(extend-type MockData
  xiast.query/XiastQuery
  (courses
    ([this]
       (courses this ""))
    ([this title-kw]
       (map  (fn [%]
               {:id (key %)
                :title (:title (val %))})
            (filter #(.contains (.toLowerCase (:title (val %)))
                                (.toLowerCase title-kw))
                    (:courses this)))))
  (course-schedule
    ([this course-id]
       (schedule-blocks this course-id))
    ([this course-id timespan]
       (filter-timespan timespan (course-schedule this course-id))))
  (student-schedule
    ([this student-id]
       (mapcat #(schedule-blocks this %)
               (get-in *mock-data* [:enrollment student-id])))
    ([this student-id timespan]
       (filter-timespan timespan (student-schedule this student-id))))
  (room-schedule
    ([this room-id]
       (->> (keys (:schedules this))
            (mapcat #(schedule-blocks this %))
            (filter #(= (:room %) room-id))))
    ([this room-id timespan]
       (filter-timespan timespan (room-schedule this room-id)))))

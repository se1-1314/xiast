(ns xiast.mock
  (:use xiast.query))

(defrecord MockData [courses])

(def *mock-data*
  (MockData. [{:id "1" :title "Mathematics"}
              {:id "2" :title "Fysics"}
              {:id "3" :title "Scheme"}]))

(extend-type MockData
  xiast.query/CourseList
  (courses
    ([this]
       (:courses this))
    ([this title-kw]
       (filter #(.contains (:title %) title-kw) (:courses this)))))

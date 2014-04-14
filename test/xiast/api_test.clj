(ns xiast.api-test
  (:use [xiast.session :only [*session*]]
        [clojure.data.json :only [write-str]])
  (:require [midje.sweet :refer :all]
            [xiast.api :as api]
            [xiast.query :as query]))

(def course-add-request
  ;; Let's make this a bit easier for ourselves.
  (write-str
   {:course-code "testcourse"
    :title "A test course"
    :description "This is only a test"
    :titular "testuser"
    :department "DINF"
    :grade :ba}))

(def course-activity-put-request
  (write-str
   {:type "HOC"
    :semester 2
    :week 0
    :contact-time-hours 26
    :instructor "testuser"
    :facilities []}))

(def program-add-request
  (write-str
   {:title "Test program"
    :description "Nothing but a test program"
    :mandatory []
    :optional []}))

(def program-add-course-request
  (write-str
   {:program 0
    :course "test"}))

(def timespan
  {:weeks [0 1]
   :days [0 1]
   :slots [0 1]})

(facts "Course API functions work"
       (fact "course-get works"
             (api/course-get nil) => []
             (provided
              (query/course-get nil) => []))
       (facts "course-delete works"
              (fact (api/course-delete nil) => {:result "Course not found"}
                    (provided
                     (query/course-get nil) => nil))
              (fact (binding [*session* {:user "testuser"}]
                      (api/course-delete nil)) => {:result "Not authorized"}
                      (provided
                       (query/course-get nil) => {:titular "not-testuser"}))
              (fact (binding [*session* {:user "testuser"}]
                      (api/course-delete nil)) => {:result "OK"}
                      (provided
                       (query/course-get nil) => {:titular "testuser"}
                       (query/course-delete! nil) => nil)))
       (fact "course-find works"
             (api/course-find "fubar") => {:result "Error"}
             (api/course-find "{\"keywrds\":[\"test\"]}") => {:result "Invalid JSON"}
             (api/course-find "{\"keywords\":[\"test\"]}") => {:result []}
             (provided
              (query/course-find ["test"]) => []))
       (facts "course-add works"
              (api/course-add "test") => {:result "Error"}
              (api/course-add "{\"test\":0}") => {:result "Invalid JSON"}
              (api/course-add course-add-request) => {:result "Not authorized"}
              (fact (binding [*session* {:user-functions #{:program-manager}}]
                      (api/course-add course-add-request)) => {:result "OK"}
                      (provided
                       (query/course-add! irrelevant) => nil)))
       (facts "course-activity-put"
              (api/course-activity-put 0 "") => {:result "Error"}
              (api/course-activity-put 0 "[]") => {:result "Invalid JSON"}
              (fact (api/course-activity-put 0 course-activity-put-request)
                    => {:id 1}
                    (provided
                     (query/course-activity-update! irrelevant) => 1))))

(facts "Program API functions work"
       (fact "program-get"
             (api/program-get nil) => []
             (provided
              (query/program-get nil) => []))
       (fact "program-delete works"
             (fact (api/program-delete nil) => {:result "Program not found"}
                   (provided
                    (query/program-get nil) => nil))
             (fact (binding [*session* {:user "testuser"}]
                     (api/program-delete nil)) => {:result "Not authorized"}
                     (provided
                      (query/program-get nil) => {:program "not-testuser"}))
             (fact (binding [*session* {:user "testuser"}]
                     (api/program-delete nil)) => {:result "OK"}
                     (provided
                      (query/program-get nil) => {:manager "testuser"}
                      (query/program-delete! nil) => nil)))
       (fact "program-find works"
             (api/program-find "fubar") => {:result "Error"}
             (api/program-find "{\"keywrds\":[\"test\"]}") => {:result "Invalid JSON"}
             (api/program-find "{\"keywords\":[\"test\"]}") => {:result []}
             (provided
              (query/program-find ["test"]) => []))
       (fact "program-add works"
             (api/program-add "test") => {:result "Error"}
             (api/program-add "{\"test\":0}") => {:result "Invalid JSON"}
             (fact (api/program-add program-add-request) => {:result "OK"}
                   (provided
                    (query/program-add! irrelevant) => nil))))

(fact "Enrollment API"
      (fact (binding [*session* {:user "testuser"}]
              (api/enrollment-student)) => {:enrollments nil}
              (provided
               (query/enrollments-student irrelevant) => nil)))

(facts "Program-manager API"
       (fact (api/program-manager-programs) => {:programs []})
       (fact (binding [*session* {:user-functions #{:program-manager}}]
               (api/program-manager-programs)) => {:programs :ok}
               (provided
                (query/program-list irrelevant) => :ok))
       (fact "program-manager-add-optional"
             (api/program-manager-add-optional "test")
             => {:result "Error"}
             (api/program-manager-add-optional "[]")
             => {:result "Invalid JSON"}
             (api/program-manager-add-optional program-add-course-request)
             => {:result "Not authorized"}
             (binding [*session* {:user-functions #{:program-manager}}]
               (api/program-manager-add-optional program-add-course-request))
             => {:result "OK"}
             (provided
              (query/program-add-optional! irrelevant irrelevant) => []))
       (fact "program-manager-add-mandatory"
             (api/program-manager-add-mandatory "test")
             => {:result "Error"}
             (api/program-manager-add-mandatory "[]")
             => {:result "Invalid JSON"}
             (api/program-manager-add-mandatory program-add-course-request)
             => {:result "Not authorized"}
             (binding [*session* {:user-functions #{:program-manager}}]
               (api/program-manager-add-mandatory program-add-course-request))
             => {:result "OK"}
             (provided
              (query/program-add-mandatory! irrelevant irrelevant) => [])))

(facts "Schedule API"
       (facts "student schedule"
              (api/schedule-student-get timespan) => {:error "Not logged in"}
              (fact (binding [*session* {:user "testuser"}]
                      (api/schedule-student-get timespan)) => {:schedule []}
                      (provided
                       (query/student-schedule irrelevant irrelevant) => [])))
       (facts "instructor schedule"
              (api/schedule-instructor-get timespan)
              => {:error "Not logged in"}
              (binding [*session* {:user "testuser"}]
                (api/schedule-instructor-get timespan))
              => {:error "Not an instructor"}
              (fact (binding [*session* {:user "testuser"
                                         :user-functions #{:instructor}}]
                      (api/schedule-instructor-get timespan))
                    => {:schedule []}
                    (provided
                     (query/instructor-schedule irrelevant irrelevant) => []))))

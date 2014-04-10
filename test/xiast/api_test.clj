(ns xiast.api-test
  (:use [xiast.session :only [*session*]]
        [clojure.data.json :only [write-str]])
  (:require [midje.sweet :refer :all]
            [xiast.api :as api]
            [xiast.query :as query])
  (:import java.util.UUID))

(def course-add-request
  ;; Let's make this a bit easier for ourselves.
  (write-str
   {:course-code "testcourse"
    :title "A test course"
    :description "This is only a test"
    :titular "testuser"
    :department "DINF"
    :grade :ba}))

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
                     (query/course-add! irrelevant) => nil))))

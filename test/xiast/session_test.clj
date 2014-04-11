(ns xiast.session-test
  (:require [midje.sweet :refer :all]
            [xiast.session :as s]
            [ring.middleware.session.store :as rss])
  (:import java.util.UUID))

(def session-id
  (s/session-create))

(def session-store
  (s/session-store))

(fact "Session database operations can read and write"
      (s/session-update! session-id "{:test 1}") => irrelevant
      (s/session-get session-id) => "{:test 1}")

(fact "XiastSessionStore is working"
      (rss/read-session session-store session-id) => {:test 1}
      (rss/write-session session-store session-id {:test 2 :a 'b}) => session-id
      (rss/read-session session-store session-id) => {:test 2 :a 'b})

(fact "Session database operations can delete sessions"
      (s/session-delete! session-id) => irrelevant
      (s/session-get session-id) => nil)

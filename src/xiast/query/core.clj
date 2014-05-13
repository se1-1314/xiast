(ns xiast.query.core
  "This namespace provides protocols for querying and updating
  information accessible through Xiast information stores."

  (:require [clojure.edn :as edn]
            [schema.core :as s]
            [xiast.schema :as xs]
            [clojure.set :as cset])
  (:use [clojure.set :only [map-invert]]
        [xiast.schema :only [room-facilities course-grades course-activity-types message-status]]
        [xiast.database]
        [korma.db]
        [korma.core]
        [slingshot.slingshot :only [throw+ try+]]))

(load "core_room")
(load "core_convert")
(load "core_person")
(load "core_course")
(load "core_program")
(load "core_enrollment")
(load "core_schedule")
(load "core_department")

;; TODO: replace all find's with get's (nvgeele)
;; TODO: replace-keys can make code cleaner (nvgeele)

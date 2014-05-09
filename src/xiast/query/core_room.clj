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

(load "core_convert")

(s/defn room-list :- [xs/Room]
  ([]
     "Get a list of all of rooms"
     (map room->sRoom (select room)))
  ([building]
     "Get a list of all rooms in a building"
     (map room->sRoom (select room
                              (where {:building building}))))
  ([building floor]
     "Get a list of all rooms on a floor in a building"
     (map room->sRoom (select room
                              (where {:building building
                                      :floor floor})))))

(s/defn room-building-list :- [s/Str]
  []
  (map :building
       (select room
               (fields :building)
               (modifier "DISTINCT"))))

(s/defn room-add! :- s/Any
  [new-room :- xs/Room]
  "Add a Room."
  (let [facilities
        (map #(% (map-invert room-facilities))
             (:facilities new-room))
        room-id
        (dissoc (:id new-room) :id)
        vals
        (merge {:capacity (:capacity new-room)}
               room-id)
        key
        (:GENERATED_KEY
         (insert room
                 (values vals)))]
    (doseq [facility facilities]
      (insert room-facility
              (values {:room key
                       :facility facility})))))

(s/defn room-delete! :- s/Any
  [room-id :- xs/RoomID]
  "Delete a room from the database."
  ;; Do we need to check if the room exists first or not? (nvgeele)
  (delete room
          (where room-id)))

(s/defn room-get :- xs/Room
  [room-id :- xs/RoomID]
  "Fetch information about a room."
  (let [room (if (:id room-id)
               (select room
                       (where {:id room-id}))
               (select room
                       (where room-id)))]
    (if (not (empty? room))
      (let [facilities
            (map #(val (find room-facilities (:facility %)))
                 (select room-facility
                         (where {:room (:id (first room))})))]
        {:id {:building (:building (first room))
              :floor (:floor (first room))
              :number (:number (first room))}
         :capacity (:capacity (first room))
         :facilities (set facilities)})
      nil)))

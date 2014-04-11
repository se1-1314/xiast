(ns xiast.query-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [xiast.query :as query]
            [xiast.schema :as xs]
            [schema.test :as s]
            [xiast.mockprograms :as xmp])
  (:use [korma.db]
        [korma.core]
        [xiast.config :only [config]]
        [xiast.database]))

(defdb test-db
  (let [config (:database config)]
    (mysql {:db (str (:database config) "-test")
            :user (:user config)
            :password (:password config)
            :host (:host config)})))

(defmacro bind-entities
  [f & ents]
  `(binding [~@(mapcat identity
                       (for [ent ents]
                         [ent `(-> (create-entity ~(name ent))
                                   (database test-db))]))]
     (~f)))

(defn wrap-with-test-database
  [f]
  (bind-entities
   f room room-facility course course-activity course-activity-facility
   course-enrollment course-instructor department person program
   program-choice-course program-mandatory-course room room-facility
   subscription session))

(defn reset-schema
  []
  (doseq [sql [ "DELETE FROM `room-facility`;"
                "DELETE FROM `room`;"]]
    (exec-raw test-db sql)))

(def test-rooms
  (list xmp/F5-403 xmp/F4-412 xmp/E0-04 xmp/G1-022))

;; TODO: remove :id's from room-ids outside of database;
;; They can still be used as primary keys for relations
;; in the database, but (building, floor, number) is
;; also unique.
(s/deftest room-test
  (fact "room-add"
        ;; Fails if Schema is not validating
        (is (thrown? Exception (query/room-add! {:test 0}))) => irrelevant
        ;; Add some rooms for testing to the database
        (doseq [test-room test-rooms]
          (query/room-add! test-room)) => irrelevant)
  (facts "room-list"
         (fact "List all rooms"
               (map #(assoc % :id (dissoc (:id %) :id))
                    (query/room-list))
               => test-rooms)
         (fact "List all rooms in building F"
               (set (map #(assoc % :id (dissoc (:id %) :id))
                         (query/room-list "F")))
               => (set (list xmp/F5-403 xmp/F4-412)))
         (fact "List all rooms in building F, floor 4"
               (let [room (first (query/room-list "F" 4))]
                 (assoc room :id (dissoc (:id room) :id)))
               => xmp/F4-412))
  (fact "room-get"
        (let [room (query/room-get (:id (first test-rooms)))]
          (assoc room :id (dissoc (:id room) :id)))
        => (first test-rooms))
  (fact "room-delete!"
        (doseq [test-room test-rooms]
          (query/room-delete! (:id test-room))) => irrelevant
          (query/room-list) => []))

(s/deftest person-test
  (fact 1 => 1))

(use-fixtures :each wrap-with-test-database)
(use-fixtures :once s/validate-schemas)

(reset-schema)

(ns xiast.query-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [xiast.query :as query]
            [xiast.schema :as xs]
            [schema.test :as s])
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

(defn wrap-with-test-database
  [f]
  (binding [room
            (-> (create-entity "room")
                (database test-db))
            room-facility
            (-> (create-entity "room-facility")
                (database test-db))]
    (f)))

(defn reset-schema
  []
  (doseq [sql [ "DELETE FROM `room-facility`;"
                "DELETE FROM `room`;"]]
    (exec-raw test-db sql)))

(def test-rooms
  [{:id {:building "F" :floor 1 :number 1}
    :capacity 100
    :facilities #{}}
   {:id {:building "F" :floor 2 :number 1}
    :capacity 100
    :facilities #{}}
   {:id {:building "G" :floor 1 :number 1}
    :capacity 100
    :facilities #{}}
   {:id {:building "B" :floor 2 :number 1}
    :capacity 100
    :facilities #{}}])

(s/deftest room-test
  (fact "room-add"
        ;; Fails if Schema is not validating
        (is (thrown? Exception (query/room-add! {:test 0}))) => irrelevant
        ;; Add some rooms for testing to the database
        (doseq [test-room test-rooms]
          (query/room-add! test-room)) => irrelevant)
  (fact "room-list"
        (vec (map #(assoc % :id (dissoc (:id %) :id))
                  (query/room-list))) => test-rooms)
  (facts "Room queries"
         1 => 1))

(use-fixtures :each wrap-with-test-database)
(use-fixtures :once s/validate-schemas)

(reset-schema)

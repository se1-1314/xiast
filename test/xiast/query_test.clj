(ns xiast.query-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [xiast.query.core :as query]
            [xiast.schema :as xs]
            [schema.test :as s]
            [xiast.mockprograms :as xmp]
            [xiast.mockschedules :as xms])
  (:use [korma.db]
        [korma.core]
        [xiast.config :only [config]]
        [clojure.set :only [map-invert]]
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
                       (for [ent (eval (first ents))]
                         [ent `(-> (create-entity ~(name ent))
                                   (database test-db))]))]
     (~f)))

(def tables
  '(room room-facility course course-activity course-activity-facility
         course-enrollment course-instructor department program person
         program-choice-course program-mandatory-course room room-facility
         subscription session schedule-block schedule-proposal-message
         schedule-proposal-message-programs))

(defn wrap-with-test-database
  [f]
  (bind-entities f tables))

(defn reset-schema
  []
  (doseq [sql (map #(format "DELETE FROM `%s`;" %) tables)]
    (exec-raw test-db sql)))

(use-fixtures :each wrap-with-test-database)
(use-fixtures :once s/validate-schemas)

(reset-schema)

;; TEST DATA

(def test-rooms
  (list xmp/D0-05 xmp/D0-03
        xmp/G1-022 xmp/G1-023
        xmp/F5-403 xmp/F4-412
        xmp/E0-04 xmp/E0-05 xmp/E0-06))

(def test-persons
  (list xmp/ejespers xmp/dthumas xmp/odetroyer xmp/chdebruyne
        xmp/wdemeuter xmp/ephilips xmp/bsigner xmp/phcara
        xmp/rvanderstraeten xmp/ksteenhaut xmp/fdominguez
        xmp/mpuwase xmp/ischeerlinck xmp/thdhondt
        xmp/jdekoster xmp/fvanoverwalle
        {:netid "testuser"
         :first-name "Test"
         :last-name "User"
         :locale "en"}))

(def test-courses
  (list xmp/linear-algebra xmp/foundations-of-informatics1
        xmp/algorithms-and-datastructures1 xmp/introduction-to-databases
        xmp/discrete-mathematics xmp/software-engineering xmp/teleprocessing
        xmp/economics-for-business xmp/interpretation2 xmp/social-psychology))

(def test-programs
  (map #(assoc (dissoc % [:mandatory :optional])
          :mandatory (vec (:mandatory %))
          :optional (vec (:optional %)))
       (list xmp/ba-cw1 xmp/ba-cw3 xmp/ba-IRCW3)))

(def test-departments
  (list xmp/DINF xmp/DWIS xmp/ETRO xmp/BEDR xmp/EXTO))

(def test-schedules
  (list xms/ba_cw1_schedule
        xms/ba_cw3_schedule))

;; TESTS

;; TODO: remove :id's from room-ids outside of database;
;; They can still be used as primary keys for relations
;; in the database, but (building, floor, number) is
;; also unique.
(defn room-test []
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
  ;; TODO: put delete tests somewhere else (nvgeele)
  #_(fact "room-delete!"
          (doseq [test-room test-rooms]
            (query/room-delete! (:id test-room))) => irrelevant
            (query/room-list) => []))

(defn person-test1 []
  (fact "person-add!"
        (is (thrown? Exception (query/person-add! {:test 0}))) => irrelevant
        (doseq [test-person test-persons]
          (query/person-add! test-person)) => irrelevant)
  (let [person (rand-nth test-persons)]
    (fact "person-get with random person"
          (query/person-get (:netid person)) => person)))

(defn department-test []
  (fact "department-add!"
        (is (thrown? Exception (query/department-add! {:test 1})))
        (doseq [test-department test-departments]
          (query/department-add! test-department)) => irrelevant)
  (fact "department-list"
        (map #(dissoc % :id) (query/department-list)) => test-departments))

(defn course-test []
  (fact "course-add! (also tests course-add-activity!)"
        (is (thrown? Exception (query/course-add! {:test 0}))) => irrelevant
        (is (thrown? Exception (query/course-add-activity! 0 0))) => irrelevant
        (doseq [test-course test-courses]
          (query/course-add! test-course)) => irrelevant))

(defn program-test []
  (fact "program-add!"
        (is (thrown? Exception (query/program-add! {:test 0}))) => irrelevant
        (doseq [test-program test-programs]
          (query/program-add! test-program)(flush)) => irrelevant))

(defn schedule-test [test-schedule-blocks]
  (fact "schedule-block-add!"
        (is (thrown? Exception (query/schedule-block-add! {:test 1}))) => irrelevant
        (doseq [schedule-block test-schedule-blocks]
          (query/schedule-block-add! schedule-block)) => irrelevant))

(defn schedule-proposal-message-test
  [test-proposal
   test-message]
  (let [program-id (rand-nth (map :id (query/program-list)))
        manager ((comp :manager first)
                 (select program
                         (where {:id program-id})))]
    (fact "schedule-proposal-message-add!"
          (is (thrown? Exception
                       (query/schedule-proposal-message-add! 0)))
          => irrelevant
          (query/schedule-proposal-message-add! test-message)
          => irrelevant
          (map #(dissoc % :id)
               (query/schedule-proposal-message-list manager))
          => (list (assoc test-message :status :inprogress)))))

(s/deftest query-tests
  (room-test)
  (person-test1)
  (department-test)
  (course-test)
  (program-test)
  (schedule-test
   (map (fn [sb]
          (let [cc (:course-id (:item sb))
                type ((:type (:item sb)) (map-invert xs/course-activity-types))
                id ((comp :id first)
                    (select course-activity
                            (fields :id)
                            (where {:course-code cc
                                    :type type})))]
            (assoc (dissoc sb :item)
              :item {:course-id cc
                     :course-activity id})))
        (mapcat identity test-schedules)))
  (let [ca
        (first (select course-activity))
        test-proposal
        {:new #{{:week 1
                 :day 1
                 :first-slot 0
                 :last-slot 1
                 :item {:course-activity (:id ca)
                        :course-id (:course-code ca)}
                 :room {:building "F" :floor 1 :number 1}}}}
        test-message
        {:sender "testuser"
         :proposal test-proposal
         :message "Hey you"}]
    (schedule-proposal-message-test test-proposal
                                    test-message)))

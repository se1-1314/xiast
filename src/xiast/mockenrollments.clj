(ns xiast.mockenrollments
  "This namespace provides dummydata in the shape of programs, given at the VUB university.
  More information on the internal structure of a studyprogram can be found in schema.clj"
  (:require [xiast.schema :as xs]
            [schema.core :as s]
            [xiast.mockprograms :as mprgs])
  (:use [clojure.set :only [union]]))


;; Persons
;; =======
;; Lars
;; ----------
(def person1
  {:netid "lavholsb"
   :first-name "Lars"
   :last-name "Van Holsbeeke"
   :locale "nl"})

;; Anders
;; ----------
(def person2
  {:netid "adeliens"
   :first-name "Anders"
   :last-name "Deliens"
   :locale "nl"})

;; Nils
;; ----------
(def person3
  {:netid "nvgeele"
   :first-name "Nils"
   :last-name "Van Geele"
   :locale "en"})

;; Enrollments
;; ===========
(defn take-random-sample [seq]
  (take (rand-int (count seq))
    (shuffle seq)))
(defn generate-enrollments [person program]
  (->> (concat (program :mandatory)
               (take-random-sample (program :optional)))
    (map (fn [course-id]
           {:course course-id
            :netid (person :netid)}))
    set))

(def person1-enrollments (generate-enrollments person1 mprgs/ba-cw3))
(def person2-enrollments (generate-enrollments person2 mprgs/ba-IRCW3))
(def person3-enrollments (generate-enrollments person3 mprgs/ba-cw3))


;; Previously generated enrollments
(def p1-old-enrl
  #{{:course "1018725AER" :netid "lavholsb"}
    {:course "1004483BNR" :netid "lavholsb"}
    {:course "1001714AER" :netid "lavholsb"}
    {:course "1001673BNR" :netid "lavholsb"}})

(def p2-old-enrl
  #{{:course "1007156ANR" :netid "adeliens"}
    {:course "1004483BNR" :netid "adeliens"}
    {:course "1001673BNR" :netid "adeliens"}
    {:course "1015259ANR" :netid "adeliens"}
    {:course "1000447ANR" :netid "adeliens"}})

(def p3-old-enrl
  #{{:course "1004483BNR" :netid "nvgeele"}
    {:course "1001673BNR" :netid "nvgeele"}
    {:course "1001714AER" :netid "nvgeele"}})

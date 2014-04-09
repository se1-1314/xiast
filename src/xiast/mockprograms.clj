

(ns xiast.mockprograms
    "This namespace provides dummydata in the shape of programs, given at the VUB university.
    More information on the internal structure of a studyprogram can be found in schema.clj"
  (:require [xiast.schema :as xs]))

;; Courses
(def linear_algebra
  {:course-code "1015328ANR"
   :title "Lineaire Algebra"
   :description "De hoofdbedoeling van deze cursus is het aanleren en gebruiken van de
lineaire algebra  technieken die nodig zijn in andere opleidingsonderdelen,
in diverse  curricula van studenten in de Wetenschappen, waar deze materie wordt toegepast, zoals
o.a. fysisca, statistiek, data-analyse, biologie.
Het is niet de bedoeling om de methodes en aangehaalde resultaten  en stellingen
in deze cursus te bewijzen. Aangezien het voor vele studenten de eerste
wiskundecursus in hun curriculum is, heeft deze cursus naaste de specifieke
wiskunde inhoud als extra pedagogisch doel het vertrouwd raken met en leren
gebruiken van  wiskundig formalisme."
   :titular-id "1000127" ;; Jespers
   :instructors #{"1000127", "0084047"} ;; Jespers, Thumas
   :department "WE"
   :grade :ba
   :activities #{{:type :HOC
                  :semester 1
                  :week 3
                  :contact-time-hours 2
                  :instructor "100127"}
                 {                 :semester 1
                 :week 3
                 :contact-time-hours 2
                 :instructor "0084047"}}
  })


;; Programs
(def ba_cw1
  ;; xs/Program
  {:title "1e bachelor Computerwetenschappen"
   :description "Alle studenten die de bacheloropleiding in de Computerwetenschappen aanvatten starten met de module eerste bachelor Computerwetenschappen. Deze module komt overeen met het eerste jaar van het modeltraject. Bij een eerste inschrijving in de bacheloropleiding mag de student enkel verplichte studiedelen eerste bachelor opnemen, met uitzondering van het voorbereidend keuzestudiedeel \"Basisvaardigheden Wiskunde\"."
   ;; :id
   ;; :manager
   :mandatory ["1015328ANR", "1000447ANR"]

   })

;; Persons


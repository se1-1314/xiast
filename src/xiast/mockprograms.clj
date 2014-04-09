(ns xiast.mockprograms
  "This namespace provides dummydata in the shape of programs, given at the VUB university.
  More information on the internal structure of a studyprogram can be found in schema.clj"
  (:require [xiast.schema :as xs]
            [schema.core :as s]
            [schema.macros :as sm])
  (:use [clojure.set :only [union]]))

;; misc functions
;; **************
(defmacro gencourseactivities
  "Generates course activities given the type of activity (:HOC/:WPO), semester, range of weeks, instructor, contact-time"
  [type semester [from till] contacthours instructor]
  (loop [week from res #{}]
    (if-not (> week till)
      (recur (inc week) (conj res {:type type :semester semester :week week :contact-time-hours contacthours :instructor instructor}))
      res
      )))

;; Courses
;; =======
;; 1e bach CW
;; ----------
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
   :activities (union (gencourseactivities :HOC 1 [3 14] 2 "100127") (gencourseactivities :WPO 1 [3 14] 2 "0084047"))
   })
(def foundations_of_informatics1
  {:course-code "1000447ANR"
   :title "Grondslagen van de informatica I"
   :description "Dit opleidingsonderdeel bestaat uit drie delen:
Een eerste deel van de cursus herhaalt kort de belangrijkste wiskundige bewijstechnieken die later in de cursus gebruikt zullen worden: bewijs door contrapositie, bewijs uit het ongerijmde, bewijs per inductie.
Het tweede deel van deze cursus bestaat uit een inleiding tot de formele logica, waarbij ook de nadruk gelegd wordt op de aspecten die relevant zijn voor de informatica.
De volgende onderwerpen komen daarbij aan bod:
- Motivatie: logica en informatica.
- Propositielogica: syntaxis en semantiek, logisch gevolg en semantische tableaus, afleidingen (natuurlijke deductie, axiomatisch afleiden), adequaatheid en volledigheid.
- Predicaatlogica: syntaxis en semantiek, logisch gevolg en semantische tableaus, afleidingen (natuurlijke deductie, axiomatisch afleiden), adequaatheid en volledigheid theorieën en normaalvormen.
Het derde gedeelte is een inleiding tot de lambda-calculus. Hier ligt de nadruk op het verband met de functionele programmeertaal Scheme (die gebruikt wordt als inleidende programmeertaal in de bachelor opleiding Computerwetenschappen): oorsprong, lambda-expressies, currying, vrije en gebonden variabelen, substitutie, reductie, Church-getallen, lambda-defineerbaarheid, fixpunten en recursieve definities. lambda-calculus als een programmeertaal."
   :titular-id "0008275" ;; De Troyer
   :instructors #{"0008275", "0081560"} ;; De Troyer, Debruyne
   :department "WE"
   :grade :ba
   :activities (union (gencourseactivities :HOC 1 [3 14] 2 "0008275") (gencourseactivities :WPO 1 [3 14] 2 "0081560"))
   })
(def algorithms_and_datastructures1
  {:course-code "1015259ANR"
   :title "Algoritmen & Datastructuren I"
   :description "De vakkenreeks \"Algoritmen en Datastructuren 1&2\" presenteert de algoritmen en datastructuren die tot het basisvocabularium van een informaticus behoren. In principe staan al deze algoritmen en datastructuren los van een specifieke programmeertaal maar omwille van de wetenschappelijke precisie wordt Scheme als formele taal gebruikt.
   Volgende onderwerpen worden in detail besproken, verspreid over beide vakken \"Algoritmen en Datastructuren 1&2\":
- Data abstractie en procedurele abstractie: Data constructoren, Abstracte Data Types (ADT's), Genericiteit, Uitvoeringstijd van algoritmen (grote O, Omega en Theta notatie).
- Patroonherkenning in Strings: het Brute-kracht algoritme, het Knutt-Morris-Pratt algoritme, het Sunday Quicksearch algoritme.
- Lineaire datastructuren: Vectoriële lijsten, enkel gelinkte lijsten, dubbel gelinkte lijsten, Zoeken in lineaire datastructuren, Ringen.
- Lineaire ADT's: Stacks, Queues, Prioriteitenqueues, Heaps.
- Sorteren: Taxonomie van sorteeralgoritmen, Simpele sorteeralgoritmen (bubble sort, insertion sort en selection sort), Geavanceerde algoritmen (Quicksort, Heapsort, Mergesort) en Lineaire algoritmen (Radix Sort, Counting Sort, Bucket Sort). Ondergrens op sorteren.
- Bomen en Toepassingen: Definitie van dictionaries, Het doorlopen van bomen recursief en iteratief, Binaire zoekbomen, AVL Bomen en balanceringstechnieken.
- Hashing: Opstellen van hashfuncties, Primaire en secondaire clustering, botsingsoplossingsstrategieën (external chaining, open addressing) en herhashen (lineair, kwadratisch, dubbel).
- De geheugenhiërarchie: intern geheugen, extern geheugen, soorten files, caching
- Externe datastructuren: Indexering, B+-Bomen
- Extern sorteren (multiway balanced mergesort, polyphase sort).
- Geamortisseerde Analyse: disjuncte verzamelingen, het union-find probleem, uptrees, padcompressie
- Grafen: voorstelling van grafen met adjacencystructuren
- Graaftraversals: DFS, BFS, karakteriseringen van DFS en BFS door classificatie van de bogen
- Ongerichte graafproblemen: samenhangendheid, boogsamenhangendheid, biconnectiviteit, en spanningsbomen (Prim, Kruskal,Boruvka).
- Gerichte Graafproblemen: Topologisch Sorteren van DAGs, Sterk samenhangendheid, Bereikbaarheid en kortste paden in Grafen (Bellman-Ford, Dijkstra, Floyd-Warshall, Lawler)
- Geheugenbeheertechnieken: taxonomie van de problematiek, first-fit systemen, best-fit systemen, buddy allocators
- Garbagecollectie: stop-and-copy, mark-and-sweep, Deutsch-Schorr-Waite algoritme voor blokken van vaste lengte en blokken van variabele lengte."
   :titular-id "0033825"
   :instructors #{"0033825", "0075773"} ;; De Meuter, Philips (no other assistants could be found)
   :department "WE"
   :grade :ba
   :activities (union (gencourseactivities :HOC 3 [8 36] 3 "0033825") (gencourseactivities :WPO 3 [9 36] 4 "0075773"))
   })
(def introduction_to_databases
  {:course-code "1007156ANR"
   :title "Inleiding Databases"
   :description "De cursus bestaat uit de volgende onderwerpen:
- Entiteit-Relationship model (E-R-model)
- Relationeel model en relationele algebra
- Relationeel databankontwerp
- Structured Query Language (SQL)
- Transactiemanagement
- Concurrency control
- Opslag- en toegangsstructuren
- Alternatieve gegevensmodellen"
   :titular-id "1000454"
   :instructors #{"1000454", "0081560"} ;; Signer, Debruyne
   :department "WE"
   :grade :ba
   :activities (union (gencourseactivities :HOC 2 [22 36] 2 "1000454") (gencourseactivities :WPO 2 [23 36] 2 "0081560"))
   })
(def discrete_mathematics
  {:course-code "1007132ANR"
   :title "Discrete Wiskunde"
   :description "Basisbegrippen wiskunde
Eenvoudige teltechnieken
Natuurlijke getallen en inductie
Gehele getallen, deelbaarheid en grootste gemene deler
Priemgetallen, factorisatie en modulaire rekenkunde
Graffen en bomen
Genererende functies
Recurrentievergelijkingen"
   :titular-id "0040941"
   :instructors #{"0040941", "0084047"} ;; Cara, Thumas
   :department "WE"
   :grade :ba
   :activities (union (gencourseactivities :HOC 1 [2 14] 3 "0040941") (gencourseactivities :WPO 1 [2 14] 2 "0084047"))
   })
;; 3e bach CW
;; ----------


;; Programs
;; ========
(def ba_cw1
  ;; xs/Program
  {:title "1e bachelor Computerwetenschappen"
   :description "Alle studenten die de bacheloropleiding in de Computerwetenschappen aanvatten starten met de module eerste bachelor Computerwetenschappen. Deze module komt overeen met het eerste jaar van het modeltraject. Bij een eerste inschrijving in de bacheloropleiding mag de student enkel verplichte studiedelen eerste bachelor opnemen, met uitzondering van het voorbereidend keuzestudiedeel \"Basisvaardigheden Wiskunde\"."
   ;; :id
   ;; :manager
   :mandatory ["1015328ANR", "1000447ANR", "1015259ANR", "1007156ANR"]
   :optional ["1007132ANR"]
   })
(def ba_cw3 "")   ;; TODO (lavholsb)
(def ba_IR3 "")   ;; TODO (lavholsb)

;; Persons
;; =======
(def ejespers {:netid "100127"
               :first-name "Eric"
               :last-name "Jespers"
               :locale "Dutch"
               })
(def dthumas {:netid "0084047"
              :first-name "Dorien"
              :last-name "Thumas"
              :locale "Dutch"
              })
(def odetroyer {:netid "0008275"
                :first-name "Olga"
                :last-name "De Troyer"
                :locale "Dutch"
                })
(def chdebruyne {:netid "0008275"
                 :first-name "Christophe"
                 :last-name "Debruyne"
                 :locale "Dutch"
                 })
(def wdemeuter {:netid "0033825"
                :first-name "Wolfgang"
                :last-name "De Meuter"
                :locale "Dutch"
                })
(def ephilips {:netid "0075773"
               :first-name "Eline"
               :last-name "Philips"
               :locale "Dutch"
               })
(def bsigner {:netid "1000454"
              :first-name "Beat"
              :last-name "Signer"
              :locale "English"
              })
(def phcara {:netid "0040941"
             :first-name "Philippe"
             :last-name "Cara"
             :locale "Dutch"
             })


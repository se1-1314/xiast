(ns xiast.mockprograms
  "This namespace provides dummydata in the shape of programs, given at the VUB university.
  More information on the internal structure of a studyprogram can be found in schema.clj"
  (:require [xiast.schema :as xs]
            [schema.core :as s])
  (:use [clojure.set :only [union]]))

;; misc functions
;; **************
;;  Not longer in use
(defn gencourseactivities
  "Generates course activities given the type of activity (:HOC/:WPO), semester, range of weeks, instructor, contact-time"
  [type semester [from till] contacthours instructor]
  (loop [week from res #{}]
    (if-not (> week till)
      (recur (inc week) (conj res {:type type :semester semester :week week :contact-time-hours contacthours :instructor instructor}))
      res)))

;; Courses
;; =======
;; 1e bach CW
;; ----------
(def linear-algebra
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
   :titular "1000127" ;; Jespers
   :instructors #{"1000127", "0084047"} ;; Jespers, Thumas
   :department "DINF"
   :grade :ba
   :activities #{{:type :HOC :semester 1 :week 0 :contact-time-hours 2 :instructor "100127" :facilities #{}}
                 {:type :WPO :semester 1 :week 0 :contact-time-hours 2 :instructor "0084047" :facilities #{}}}
   })
(def foundations-of-informatics1
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
   :titular "0008275" ;; De Troyer
   :instructors #{"0008275", "0081560"} ;; De Troyer, Debruyne
   :department "DINF"
   :grade :ba
   :activities #{{:type :HOC :semester 1 :week 0 :contact-time-hours 2 :instructor "0008275" :facilities #{}}
                 {:type :WPO :semester 1 :week 0 :contact-time-hours 2 :instructor "0081560" :facilities #{}}}
   })
(def algorithms-and-datastructures1
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
   :titular "0033825"
   :instructors #{"0033825", "0075773"} ;; De Meuter, Philips (no other assistants could be found)
   :department "DINF"
   :grade :ba
   :activities #{{:type :HOC :semester 3 :week 0 :contact-time-hours 3 :instructor "0033825" :facilities #{}}
                 { :type :WPO :semester 3 :week 0 :contact-time-hours 4 :instructor "0075773" :facilities #{}}}
   })
(def introduction-to-databases
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
   :titular "1000454"
   :instructors #{"1000454", "0081560"} ;; Signer, Debruyne
   :department "DINF"
   :grade :ba
   :activities #{{:type :HOC :semester 2 :week 0 :contact-time-hours 2 :instructor "1000454" :facilities #{}}
                 {:type :WPO :semester 2 :week 0 :contact-time-hours 2 :instructor "0081560" :facilities #{}}}
   })
(def discrete-mathematics
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
   :titular "0040941"
   :instructors #{"0040941", "0084047"} ;; Cara, Thumas
   :department "DWIS"
   :grade :ba
   :activities #{{:type :HOC :semester 1 :week 0 :contact-time-hours 3 :instructor "0040941" :facilities #{}}
                 {:type :WPO :semester 1 :week 0 :contact-time-hours 2 :instructor "0084047" :facilities #{}}}
   })
;; 3e bach CW
;; ----------
(def software-engineering
  {:course-code "1004483BNR"
   :title "Software Engineering"
   :description "Naast een kort theoretisch gedeelte bestaat dit vak hoofdzakelijk uit een
groepsproject. De docent stelt de groepen samen. Elke groep bestaat uit circa 8
studenten, die onderling de diverse rollen (projectleider, configuratieleider,
ontwerpleider, kwaliteitscontrole etc). verdelen. Het doel van het project is
hetzelfde voor alle groepen: het ontwikkelen van een softwaresysteem waarvoor
de docent de rol van klant waarneemt. Alle project documentatie en code, incl.
statistieken zoals gespendeerde tijd, moeten publiek beschikbaar zijn op de
website van de groep.
De inhoud van het theoretisch gedeelte:
- software engineering activiteiten: procesdefinitie, management, requirements
  en specificatie opstellen, ontwerp, implementatie, testing, integratie,
  onderhoud
- het software ontwikkelingsproces: alternatieven
- configuratie management
- requirements management
- software ontwerp
- kwaliteit: metrieken, formele methoden, inspecties, testen, CMM
- management: haalbaarheid, kostenraming, planning, risicobeheer, rapportering
- documentbeheer"
   :titular "0062333"
   :instructors #{"0062333"} ;; Van der Straeten
   :department "DINF"
   :grade :ba
   :activities #{{:type :HOC :semester 3 :week 0 :contact-time-hours 2 :instructor "0062333" :facilities #{}}}
   })
(def teleprocessing
  {:course-code  "1001673BNR"
   :title "Tele-Informatica"
   :description "In a first part, the basic concepts of datacommunications and their evolution are introduced. In a second and a third part, the concepts introduced previously are illustrated by means of descriptions of actual circuit switching and packet switching networks. In the fourth part, finally, it is shown how the described networks can be combined into a single internet.
    The lab sessions are subdivided in two parts: the first demonstrates the basic concepts of Wireless transmission by means of the deployment of  Wireless Sensor Nodes. With these nodes  several small network topologies are built. The second part consists in building and interconnecting small local area networks based upon switches and routers."
   :titular "1234567"
   :instructors #{"1234567", "5389644", "4264924"} ;; Steenhaut(random), Dominguez (random), Uwase (random)
   :department "ETRO"
   :grade :ba
   :activities #{{:type :HOC :semester 1 :week 0 :contact-time-hours 3 :instructor "1234567" :facilities #{}}
                 {:type :WPO :semester 1 :week 0 :contact-time-hours 4 :instructor "5389644" :facilities #{}}
                 {:type :WPO :semester 1 :week 0 :contact-time-hours 4 :instructor "4264924" :facilities #{}}}
   })
(def economics-for-business
  {:course-code "1001714AER"
   :title "Economie en Bedrijfsleven"
   :description "Het doel van dit onderdeel is inzicht te verschaffen in de beginselen van de economie. Met het oog op het voorbereiden van de student op de toetreding tot de arbeidsmarkt, wordt uitvoerig aandacht besteed aan de micro-economische aspecten van de economie. Wat de macro-economie betreft, legt de cursus vooral de nadruk op economische indicatoren; geaggregeerde vraag en geaggregeerd aanbod; het meten en interpreteren van de macro-economische activiteit; en het belang van economische politiek en institutionele aspecten voor het bedrijfsleven. De topics zijn:
        De beginselen van de economie
        De markt: vraag en aanbod
        Elasticiteit
        Toepassingen van vraag en aanbod
        Kosten en opbrengsten van de onderneming
        Marktvormen
        Marktverstoringen en overheidsbeleid
        Inleiding tot de macro-economie
        Meten van economische activiteit
        Output-bestedingsmodel en fiscaal beleid
        Geld, bankwezen en monetair beleid
        Geaggregeerde vraag, geaggregeerd aanbod en inflatie
        Wisselkoersen"
   :titular "0025867"
   :instructors #{"0025867"} ;; Scheerlick (random)
   :department "BEDR"
   :grade :ba
   :activities #{{:type :HOC :semester 1 :week 0 :contact-time-hours 2 :instructor "0025867" :facilities #{}}}
   })
(def interpretation2
  {:course-code "1005176BNR"
   :title "Interpretatie van computerprogramma's II"
   :description "Metacirculaire specificatie van Pico
Geheugenbeheer in Pico
Een stackmachine voor Pico
De Pico-evaluator
Het parsen en printen van Picoprogramma's
Ingebouwde functies voor Pico
De API van Pico
Threads in Pico
Optimalisatie van de virtuele machine"
   :titular "0000585"
   :instructors #{"0000585","1568634" } ; D'Hondt, De Koster (random)
   :department "DINF"
   :grade :ba
   :activities #{{:type :HOC :semester 1 :week 0 :contact-time-hours 2 :instructor "0000585" :facilities #{}}
                 {:type :WPO :semester 1 :week 0 :contact-time-hours 2 :instructor "1568634" :facilities #{}}}
   })
(def social-psychology
  {:course-code "1018725AER"
   :title "Sociale Psychologie"
   :description "Les 1: Hoofdstuk 1 - Introduction to Social Psychology
Les 2: Hoofdstuk 2 - Methodology: How Social Psychologists do Research
Les 3: Hoofdstuk 3 - Social Cognition: Automatic & Controlled
Les 4: Hoofdstuk 4 - Social Perception: Non-verbal
Les 5: Hoofdstuk 4 - Social Perception: Attribution & Accuracy
Les 6: Hoofdstuk 5 - Self-Knowledge
Les 7: Hoofdstuk 6 - Self-justification
Les 8: Hoofdstuk 7 - Attitudes: Nature & Change of Attitudes
Les 9: Hoofdstuk 7 - Attitudes: Resistance, Behavior & Advertising"
   :titular "3596346"
   :instructors #{"3596346"} ;; Van Overwalle (random)
   :department "EXTO"
   :grade :ba
   :activities #{{:type :HOC :semester 1 :weel 0 :contact-time-hours 3 :instructor "3596346" :facilities #{}}}
   })

;; Programs
;; ========
(def ba-cw1
  ;; xs/Program
  {:title "1e bachelor Computerwetenschappen"
   :description "Alle studenten die de bacheloropleiding in de Computerwetenschappen aanvatten starten met de module eerste bachelor Computerwetenschappen. Deze module komt overeen met het eerste jaar van het modeltraject. Bij een eerste inschrijving in de bacheloropleiding mag de student enkel verplichte studiedelen eerste bachelor opnemen, met uitzondering van het voorbereidend keuzestudiedeel \"Basisvaardigheden Wiskunde\"."
   ;; :id
   ;; :manager
   :mandatory ["1015328ANR", "1000447ANR", "1015259ANR", "1007156ANR"]
   :optional ["1007132ANR"]})
(def ba-cw3
  {:title "3e bachelor computerwetenschappen"
   :description "In combinatie met de verplichte studiedelen derde bachelor neemt de student bij voorkeur voor 30 studiepunten aan keuzestudiedelen op.
Inschrijven voor de bachelorproef kan indien het een inschrijving betreft waarbij met de andere gekozen studiedelen het volledige bachelortraject van minstens 180 studiepunten wordt ingevuld."
   :mandatory ["1004483BNR", "1001673BNR", "1001714AER"]
   :optional ["1005176BNR", "1018725AER"]})
(def ba-IRCW3
  {:title "3e Bachelor Ingenieurswetenschappen - Computerwetenschappen"
   :description "Deze module (60 SP) is specifiek voor de Afstudeerrichting Elektronica en informatietechnologie, met het profiel Computerwetenschappen. Ze bestaat uit een submodule die gemeenschappelijk is voor alle studenten die de afstudeerrichting Elektronica en informatietechnologie hebben gekozen en een submodule specifiek voor het profiel Computerwetenschappen. De studenten moeten alle studiedelen uit beide modules verplicht voltooien. Deze studiedelen behoren tot het derde jaar van het voltijds modeltraject bachelor (Bachelor 3). Bij een eerste inschrijving in de bacheloropleiding is het niet toegelaten reeds in te schrijven voor studiedelen uit deze module. Studenten mogen pas inschrijven voor studiedelen uit 'Jaar 3 van het voltijds modeltraject BA IR – EIT Computerwetenschappen' indien zij reeds de credits verworven hebben voor het technologieproject 'Informatie en communicatietechnologie' en ten minste één van de 3 andere technologieprojecten (Leefmilieu en duurzame materialen of Werktuigkunde en Elektrotechniek of Informatie en communicatietechnologie) uit de module ‘Technologieprojecten in opleidingsateliers' van 'Jaar 2 van het modeltraject BA IR' of voor deze 2 technologieprojecten inschrijven samen met de studiedelen uit de afstudeerrichtingsmodule. Studenten moeten voldoen aan de aan elk van de studiedelen verbonden specifieke inschrijvingsvereisten."
   :mandatory ["1004483BNR", "1007156ANR", "1000447ANR", "1001673BNR", "1015259ANR"]
   :optional [""]})

;; Persons
;; =======
(def ejespers {:netid "1000127"
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
(def chdebruyne {:netid "0081560"
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
(def rvanderstraeten {:netid "0062333"
                      :first-name "Ragnhild"
                      :last-name "Van der Straeten"
                      :locale "Dutch"})
(def ksteenhaut {:netid "1234567"
                 :first-name "Kris"
                 :last-name "Steenhaut"
                 :locale "Dutch"})
(def fdominguez {:netid "5389644"
                 :first-name "Frederico"
                 :last-name "Dominguez"
                 :locale "English"})
(def mpuwase {:netid "4264924"
              :first-name "Marie-Paule"
              :last-name "Uwase"
              :locale "English"})
(def ischeerlinck {:netid "0025867"
                   :first-name "Ilse"
                   :last-name "Scheerlinck"
                   :locale "Dutch"})
(def thdhondt {:netid "0000585"
               :first-name "Theo"
               :last-name "D'Hondt"
               :locale "Dutch"})
(def jdekoster {:netid "1568634"
                :first-name "Joeri"
                :last-name "De Koster"
                :locale "Dutch"})
(def fvanoverwalle {:netid "3596346"
                    :first-name "Frank"
                    :last-name "Van Overwalle"
                    :locale "Dutch"})

;; Rooms
;; =====
(def D0-05 {:id {:building "D", :floor 0, :number 05}
            :capacity 200
            :facilities #{:beamer :overhead-projector}
            })
(def D0-03 {:id {:building "D", :floor 0, :number 03}
            :capacity 200
            :facilities #{:beamer :overhead-projector}
            })
(def G1-022 {:id {:building "G", :floor 1, :number 022}
             :capacity 100
             :facilities #{:beamer :overhead-projector}
             })
(def G1-023 {:id {:building "G", :floor 1, :number 023}
             :capacity 150
             :facilities #{:beamer :overhead-projector}
             })
(def F5-403 {:id {:building "F", :floor 5, :number 403}
             :capacity 70
             :facilities #{:beamer :overhead-projector}
             })
(def F4-412 {:id {:building "F", :floor 4, :number 412}
             :capacity 20
             :facilities #{:beamer :overhead-projector}
             })
(def E0-04 {:id {:building "E", :floor 0, :number 04}
             :capacity 100
             :facilities #{:beamer :overhead-projector}
             })
(def E0-05 {:id {:building "E", :floor 0, :number 05}
            :capacity 80
            :facilities #{:beamer :overhead-projector}
            })
(def E0-06 {:id {:building "E", :floor 0, :number 06}
            :capacity 80
            :facilities #{:beamer :overhead-projector}
            })

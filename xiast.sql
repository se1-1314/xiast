-- MySQL dump 10.13  Distrib 5.6.16, for osx10.9 (x86_64)
--
-- Host: localhost    Database: xiast
-- ------------------------------------------------------
-- Server version	5.6.16

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course` (
  `course-code` varchar(32) NOT NULL DEFAULT '',
  `title` varchar(126) DEFAULT NULL,
  `description` text,
  `titular-id` varchar(12) DEFAULT NULL,
  `department` int(11) unsigned NOT NULL,
  `grade` int(11) DEFAULT NULL,
  PRIMARY KEY (`course-code`),
  KEY `department` (`department`),
  KEY `titular-id` (`titular-id`),
  CONSTRAINT `course_ibfk_1` FOREIGN KEY (`department`) REFERENCES `department` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `course_ibfk_2` FOREIGN KEY (`titular-id`) REFERENCES `person` (`netid`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES ('1000447ANR','Grondslagen vd informatica I',NULL,'titular',37,0),('1001673BNR','Tele-Informatica','In a first part, the basic concepts of datacommunications and their evolution are introduced. In a second and a third part, the concepts introduced previously are illustrated by means of descriptions of actual circuit switching and packet switching networks. In the fourth part, finally, it is shown how the described networks can be combined into a single internet.\n    The lab sessions are subdivided in two parts: the first demonstrates the basic concepts of Wireless transmission by means of the deployment of  Wireless Sensor Nodes. With these nodes  several small network topologies are built. The second part consists in building and interconnecting small local area networks based upon switches and routers.','titular',40,0),('1001714AER','Economie en Bedrijfsleven','Het doel van dit onderdeel is inzicht te verschaffen in de beginselen van de economie. Met het oog op het voorbereiden van de student op de toetreding tot de arbeidsmarkt, wordt uitvoerig aandacht besteed aan de micro-economische aspecten van de economie. Wat de macro-economie betreft, legt de cursus vooral de nadruk op economische indicatoren; geaggregeerde vraag en geaggregeerd aanbod; het meten en interpreteren van de macro-economische activiteit; en het belang van economische politiek en institutionele aspecten voor het bedrijfsleven. De topics zijn:\n        De beginselen van de economie\n        De markt: vraag en aanbod\n        Elasticiteit\n        Toepassingen van vraag en aanbod\n        Kosten en opbrengsten van de onderneming\n        Marktvormen\n        Marktverstoringen en overheidsbeleid\n        Inleiding tot de macro-economie\n        Meten van economische activiteit\n        Output-bestedingsmodel en fiscaal beleid\n        Geld, bankwezen en monetair beleid\n        Geaggregeerde vraag, geaggregeerd aanbod en inflatie\n        Wisselkoersen','titular',41,0),('1004483BNR','Software Engineering',NULL,'titular',37,0),('1005176BNR','Interpret v compprogramma\'s II',NULL,'titular',37,0),('1007132ANR','Discrete wiskunde',NULL,'titular',21,0),('1007156ANR','Inleiding databases',NULL,'titular',37,0),('1015259ANR','Algoritmen en datastructuren 1',NULL,'wdemeuter',37,0),('1015328ANR','Lineaire algebra',NULL,'titular',21,0),('1018725AER','Sociale Psychologie','Les 1: Hoofdstuk 1 - Introduction to Social Psychology\nLes 2: Hoofdstuk 2 - Methodology: How Social Psychologists do Research\nLes 3: Hoofdstuk 3 - Social Cognition: Automatic & Controlled\nLes 4: Hoofdstuk 4 - Social Perception: Non-verbal\nLes 5: Hoofdstuk 4 - Social Perception: Attribution & Accuracy\nLes 6: Hoofdstuk 5 - Self-Knowledge\nLes 7: Hoofdstuk 6 - Self-justification\nLes 8: Hoofdstuk 7 - Attitudes: Nature & Change of Attitudes\nLes 9: Hoofdstuk 7 - Attitudes: Resistance, Behavior & Advertising','titular',42,0);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course-activity`
--

DROP TABLE IF EXISTS `course-activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course-activity` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `course-code` varchar(32) NOT NULL DEFAULT '',
  `name` varchar(128) DEFAULT NULL,
  `type` int(11) unsigned NOT NULL,
  `semester` int(11) unsigned NOT NULL,
  `week` int(11) DEFAULT NULL,
  `contact-time-hours` int(11) unsigned NOT NULL,
  `contact-time-days` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `course-code` (`course-code`),
  CONSTRAINT `course-activity_ibfk_1` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3194 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course-activity`
--

LOCK TABLES `course-activity` WRITE;
/*!40000 ALTER TABLE `course-activity` DISABLE KEYS */;
INSERT INTO `course-activity` VALUES (1626,'1000447ANR','HOC 1',0,1,0,26,0),(1627,'1000447ANR','HOC 2',0,1,0,26,0),(1628,'1000447ANR','WPO 1',1,1,0,26,0),(1629,'1000447ANR','WPO 2',1,1,0,26,0),(1687,'1004483BNR','HOC 1',0,3,0,13,0),(1688,'1004483BNR','WPO 1',1,3,0,75,0),(1699,'1005176BNR','HOC 1',0,1,0,26,0),(1700,'1005176BNR','HOC 2',0,1,0,26,0),(1701,'1005176BNR','WPO 1',1,1,0,26,0),(1702,'1005176BNR','WPO 2',1,1,0,26,0),(1722,'1007132ANR','HOC 1',0,1,0,26,0),(1723,'1007132ANR','HOC 2',0,1,0,26,0),(1724,'1007132ANR','WPO 1',1,1,0,26,0),(1725,'1007132ANR','WPO 2',1,1,0,26,0),(1740,'1007156ANR','HOC 1',0,2,0,26,0),(1741,'1007156ANR','HOC 2',0,2,0,26,0),(1742,'1007156ANR','WPO 1',1,2,0,26,0),(1743,'1007156ANR','WPO 2',1,2,0,26,0),(1867,'1015259ANR','HOC 1',0,3,0,39,0),(1868,'1015259ANR','WPO 1',1,3,0,39,0),(1900,'1015328ANR','HOC 1',0,1,0,18,0),(1901,'1015328ANR','HOC 2',0,1,0,18,0),(1902,'1015328ANR','WPO 1',1,1,0,22,0),(1903,'1015328ANR','WPO 2',1,1,0,22,0),(3187,'1001673BNR','WPO 1',1,1,0,4,NULL),(3188,'1001673BNR','HOC 1',0,1,0,3,NULL),(3189,'1001673BNR','WPO 2',1,1,0,4,NULL),(3190,'1001714AER','HOC 1',0,1,0,2,NULL),(3191,'1018725AER','HOC 1',0,1,0,3,NULL);
/*!40000 ALTER TABLE `course-activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course-activity-facility`
--

DROP TABLE IF EXISTS `course-activity-facility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course-activity-facility` (
  `course-activity` int(11) unsigned NOT NULL,
  `facility` int(11) unsigned NOT NULL,
  KEY `course-activity` (`course-activity`),
  CONSTRAINT `course-activity-facility_ibfk_1` FOREIGN KEY (`course-activity`) REFERENCES `course-activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course-activity-facility`
--

LOCK TABLES `course-activity-facility` WRITE;
/*!40000 ALTER TABLE `course-activity-facility` DISABLE KEYS */;
/*!40000 ALTER TABLE `course-activity-facility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course-enrollment`
--

DROP TABLE IF EXISTS `course-enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course-enrollment` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `course-code` varchar(32) DEFAULT NULL,
  `netid` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `course-code` (`course-code`),
  KEY `netid` (`netid`),
  CONSTRAINT `course-enrollment_ibfk_1` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `course-enrollment_ibfk_2` FOREIGN KEY (`netid`) REFERENCES `person` (`netid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course-enrollment`
--

LOCK TABLES `course-enrollment` WRITE;
/*!40000 ALTER TABLE `course-enrollment` DISABLE KEYS */;
INSERT INTO `course-enrollment` VALUES (1,'1018725AER','lavholsb'),(2,'1004483BNR','lavholsb'),(3,'1001714AER','lavholsb'),(4,'1001673BNR','lavholsb'),(5,'1007156ANR','adeliens'),(6,'1004483BNR','adeliens'),(7,'1001673BNR','adeliens'),(8,'1015259ANR','adeliens'),(9,'1000447ANR','adeliens'),(10,'1004483BNR','nvgeele'),(11,'1001673BNR','nvgeele'),(12,'1001714AER','nvgeele');
/*!40000 ALTER TABLE `course-enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course-instructor`
--

DROP TABLE IF EXISTS `course-instructor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `course-instructor` (
  `course-activity` int(11) unsigned DEFAULT NULL,
  `netid` varchar(12) DEFAULT NULL,
  KEY `netid` (`netid`,`course-activity`),
  KEY `course-activity` (`course-activity`),
  CONSTRAINT `course-instructor_ibfk_1` FOREIGN KEY (`course-activity`) REFERENCES `course-activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `course-instructor_ibfk_2` FOREIGN KEY (`netid`) REFERENCES `person` (`netid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course-instructor`
--

LOCK TABLES `course-instructor` WRITE;
/*!40000 ALTER TABLE `course-instructor` DISABLE KEYS */;
INSERT INTO `course-instructor` VALUES (1626,'0008275'),(1627,'0008275'),(3190,'0025867'),(1722,'0040941'),(1723,'0040941'),(1687,'0062333'),(1742,'0080698'),(1743,'0080698'),(1628,'0081560'),(1629,'0081560'),(1900,'1000127'),(1901,'1000127'),(1740,'1000454'),(1741,'1000454'),(3188,'1234567'),(3191,'3596346'),(3187,'4264924'),(3189,'5389644'),(1699,'tjdhondt'),(1700,'tjdhondt'),(1867,'wdemeuter');
/*!40000 ALTER TABLE `course-instructor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `department`
--

DROP TABLE IF EXISTS `department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `department` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `faculty` varchar(64) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `department`
--

LOCK TABLES `department` WRITE;
/*!40000 ALTER TABLE `department` DISABLE KEYS */;
INSERT INTO `department` VALUES (20,'WE','GEOGRAF'),(21,'WE','WISKUNDE'),(22,'WE','FYSICA'),(23,'WE','STEDRUIM'),(24,'WE','ONDRZOEK'),(25,'WE','ONDRWIJS'),(26,'WE','CHEMIE'),(27,'WE','WETENSCH'),(28,'WE','INDUSWET'),(29,'WE','TOERISME'),(30,'WE','GEOLOGIE'),(31,'WE','FARMACIE'),(32,'WE','INGRWET'),(33,'WE','ECONOMIE'),(34,'WE','BIOINGR'),(35,'WE','BIOLOGIE'),(36,'WE','AGOGIWET'),(37,'WE','INFORMAT'),(38,'WE','DINF'),(39,'WE','DWIS'),(40,'IR','ETRO'),(41,'ES','BEDR'),(42,'PE','EXTO');
/*!40000 ALTER TABLE `department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `person` (
  `netid` varchar(12) NOT NULL DEFAULT '',
  `firstname` varchar(64) DEFAULT NULL,
  `surname` varchar(64) DEFAULT NULL,
  `locale` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`netid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `person`
--

LOCK TABLES `person` WRITE;
/*!40000 ALTER TABLE `person` DISABLE KEYS */;
INSERT INTO `person` VALUES ('0000015','Florimond','DE SMEDT','en'),('0000033','Georges','EISENDRATH','en'),('0000038','Gino','BARON','en'),('0000595','Marc','VAN MOLLE','en'),('0000599','Robert','LUYPAERT','en'),('0000604','Walter','VAN DONINCK','en'),('0000616','Georges','VAUQUELIN','en'),('0000621','Frank','DEHAIRS','en'),('0000629','Willy','BAEYENS','en'),('0000631','Daniel','CHARLIER','en'),('0000694','Sonia','BEECKMANS','en'),('0000780','Jacques','DE RUYCK','en'),('0000949','BRUNO','VAN MELE','en'),('0001008','Robert','MEERSMAN','en'),('0001010','PAUL','GEERLINGS','en'),('0001235','Georges','LAUS','en'),('0001271','Jean-Pierre','HERNALSTEENS','en'),('0001365','Paul','VAN GOETHEM','en'),('0001382','Gustaaf','VAN TENDELOO','en'),('0001393','Paul','COPPENS','en'),('0001408','Roland','HAUSPIE','en'),('0002305','Robert','LOWEN','en'),('0002320','Frank','PLASTRIA','en'),('0002327','Franklin','LAMBERT','en'),('0002754','Monique','BIESEMANS','en'),('0002770','Dirk','TOURWE','en'),('0002955','Raymond','CUNIN','en'),('0003022','DANY','VANBEVEREN','en'),('0003164','Rene','VAN DOOREN','en'),('0003286','Ingrid','DAUBECHIES','en'),('0003638','Patrick','DE BAETSELIER','en'),('0003751','Leo','GOEYENS','en'),('0003768','Jean','DE GREVE','en'),('0003814','Walther','DE LANNOY','en'),('0005058','Henri','DE GREVE','en'),('0005281','Patrick','ROMBAUTS','en'),('0005446','Edilbert','VAN DRIESSCHE','en'),('0005553','Mona','GRINWIS PLAAT STULTJES','en'),('0005631','Gaston','MOENS','en'),('0005692','Erik','STIJNS','en'),('0005725','Stefaan','TAVERNIER','en'),('0005759','Jean','VAN CRAEN','en'),('0005760','Edward','KEPPENS','en'),('0006101','Viviane','JONCKERS','en'),('0006288','Serge','MUYLDERMANS','en'),('0006371','MARNIX','GOOSSENS','en'),('0006603','Guido','VAN OOST','en'),('0006604','Jacques','VERNIERS','en'),('0006620','Robert','FINSY','en'),('0007555','Catherine','DE CLERCQ','en'),('0007631','Christian','LACOR','en'),('0008275','OLGA','DE TROYER','en'),('0008289','Harry','VERELST','en'),('0008290','Henri','PEPERMANS','en'),('0008982','WILLY','BAUWENS','en'),('0009424','Nico','KOEDAM','en'),('0009581','Rudger','KIEBOOM','en'),('0009607','HERMAN','TERRYN','en'),('0009748','Jacques','TIBERGHIEN','en'),('0010139','Christiaan','STERKEN','en'),('0011312','CHRISTIAN','VAN DEN BROECK','en'),('0011392','Robert','ROOSEN','en'),('0012716','Alex','HERMANNE','en'),('0013005','FRANK','CANTERS','en'),('0013556','Annick','HUBIN','en'),('0013739','Ludwig','TRIEST','en'),('0013886','Rudi','WILLEMS','en'),('0014978','Erik','DIRKX','en'),('0015336','Hilde','REVETS','en'),('0015592','Hugo','THIENPONT','en'),('0015938','Jan','VANHELLEMONT','en'),('0016336','LUDO','HOLSBEEK','en'),('0016796','Jozef','MESKENS Ad','en'),('0017419','Philippe','HUYBRECHTS','en'),('0017613','Patrick','VANDERHEYDEN','en'),('0019308','Michel','DEFRISE','en'),('0019554','STEFAAN','CAENEPEEL','en'),('0019690','Danny','VAN HEMELRIJCK','en'),('0019885','Jozef','HUS','en'),('0020338','JAN','STEYAERT','en'),('0020360','Rudi','Wilms','en'),('0020623','PAUL','VAN HUMMELEN','en'),('0020706','Ronnie','WILLAERT','en'),('0022831','Dominique','MAES','en'),('0023049','Thierry','VANDENDRIESSCHE','en'),('0023409','Hubert','RAHIER','en'),('0025010','Remy','LORIS','en'),('0025867','Ilse','Scheerlinck','Dutch'),('0026266','Bernard','MANDERICK','en'),('0026379','Patrick','MEIRE','en'),('0026602','Gert','DESMET','en'),('0027008','Eddy','CARETTE','en'),('0028347','Jan','DANCKAERT','en'),('0028592','Dirk','VAN BERLAER','en'),('0028766','Stefan','MAGEZ','en'),('0031121','Bruno','GODDEERIS','en'),('0031777','Gert','SONCK','en'),('0031817','Guy','VAN ASSCHE','en'),('0031919','Frank','DE PROFT','en'),('0032281','Martine','LEERMAKERS','en'),('0032411','Peter','BRUYNDONCKX','en'),('0032698','Sylvia','BURSSENS','en'),('0033601','Ann','NOWÉ','en'),('0033956','Steven','DEWITTE','en'),('0034008','Geert','RAES','en'),('0034308','HENK','FORIERS','en'),('0035985','Eric','CORIJN','en'),('0036357','Luc','VAN KEMPEN','en'),('0036736','Joeri','DENAYER','en'),('0038953','Frederic','ROUSSEAU','en'),('0039472','Ignace','LORIS','en'),('0039489','Griselda','DEELSTRA','en'),('0039928','Jo','VAN GINDERACHTER','en'),('0040088','Christophe','CROUX','en'),('0040116','Farid','DAHDOUH-GUEBAS','en'),('0040553','Martine','DE MAZIERE','en'),('0040941','Philippe','CARA','en'),('0041745','Guy','VERSCHAFFELT','en'),('0042295','Katja','VERBEECK','en'),('0042296','Peter','SCHELKENS','en'),('0044961','Olivier','DEVROEDE','en'),('0045156','Abdellah','TOUHAFI','en'),('0045215','Joost','SCHYMKOWITZ','en'),('0047113','Tom','LENAERTS','en'),('0047932','TOM','TOURWE','en'),('0047940','Maja','D\'HONDT','en'),('0047944','Wim','VERSEES','en'),('0049892','Ilse','DECORDIER','en'),('0051332','Tony','BELPAEME','en'),('0052412','Margaret','CHEN','en'),('0052478','Caroline','Verhoeven','en'),('0052480','Johan','Brichau','en'),('0052544','Didier','DESES','en'),('0052770','Jorgen','D\'HONDT','en'),('0052777','Sven','CASTELEYN','en'),('0055206','Guy','VAN DER SANDE','en'),('0055483','Anja','DECOSTER','en'),('0055514','Ann','DOOMS','en'),('0056196','Kim','ROELANTS','en'),('0057666','Adrian','MUNTEANU','en'),('0057757','Bart','DE BOER','en'),('0057891','Steven','LOWETTE','en'),('0058101','Anne','WINTER','en'),('0058967','Wendy','LOWEN','en'),('0059362','Steven','BALLET','en'),('0059901','Anton','VAN ROMPAEY','en'),('0060073','Franky','BOSSUYT','en'),('0061744','Pieter','DE LEENHEER','en'),('0061887','Tomas','EVERAERT','en'),('0062333','Ragnhild','VAN DER STRAETEN','en'),('0062407','Frederic','LEROY','en'),('0062601','Marc','ELSKENS','en'),('0063067','Wim','DE MALSCHE','en'),('0063146','Audrey','BAEYENS','en'),('0063584','Steven','BOUILLON','en'),('0063626','Mylene','D\'HAESELEER','en'),('0063804','Andy','KELLENS','en'),('0064737','An','Gerlo','en'),('0065122','Kim','EVERAERT','en'),('0066799','Peter','VRANCX','en'),('0067395','Tom','VAN CUTSEM','en'),('0067408','Coen','DE ROOVER','en'),('0068388','Sander','DERIDDER','en'),('0069291','Nathalie','BRION','en'),('0069705','Lendert','GELENS','en'),('0070060','Eefje','VLOEBERGHS','en'),('0070505','Ines','VAN BOCXLAER','en'),('0071080','Ken','BROECKHOVEN','en'),('0071138','Melissa','FERRE','en'),('0072241','Joost','VERCRUYSSE','en'),('0072468','Petra','VAN MULDERS','en'),('0072475','Sofie','VERMEULEN','en'),('0072534','Yann-Michaël','DE HAUWERE','en'),('0075066','Carlos Francisco','NOGUERA GARCIA','en'),('0075773','Eline','PHILIPS','en'),('0075976','Lars','KEUNINCKX','en'),('0075981','Nicki','MENNEKENS','en'),('0076508','Sarah','COUCK','en'),('0078299','Michael','MAES','en'),('0078386','Nele','VANBEKBERGEN','en'),('0078387','Tom','DORISSEN','en'),('0079386','Stijn','BLYWEERT','en'),('0080680','Lode','HOSTE','en'),('0080698','Reinout','ROELS','en'),('0081005','Karen','VAN DE WATER','en'),('0081237','Gerrit','VAN ONSEM','en'),('0081560','Christophe','DEBRUYNE','en'),('0081770','Elisa','GONZALEZ BOIX','en'),('0082591','Lieve','LAMBRECHTS','en'),('0083387','Stijn','VAN DER PERRE','en'),('0083506','Tim','BRUYLANTS','en'),('0084047','Dorien','THUMAS','en'),('0084634','Egon','DEYAERT','en'),('0085022','Peter','DE SCHEPPER','en'),('0088624','Yannick','VERBELEN','en'),('0089618','Tom','VAN ASSCHE','en'),('0090521','Mattias','DE WAEL','en'),('0091187','Steven','ODONGO','en'),('0092848','Bram','VANCOMPERNOLLE','en'),('0093116','Erik','Smets','en'),('0095731','Dave','GEERARDYN','en'),('0095901','Katrien','BEULS','en'),('0099002','Dennis','LENAERTS','en'),('0100637','Rosa','VAN DER VEN','en'),('0105519','Jordy','VANPOUCKE','en'),('0503068','David','BASSENS','en'),('0504607','Harry','OLDE VENTERINK','en'),('1000011','Marjolene','CRABEEL','en'),('1000012','Lode','WYNS','en'),('1000024','Peter','DE GROEN','en'),('1000025','LEO','VAN BIESEN','en'),('1000026','EVA','COLEBUNDERS','en'),('1000030','Marc','ZABEAU','en'),('1000032','Hugo','DECLEIR','en'),('1000035','Luc','STEELS','en'),('1000036','Jean-Marie','FRERE','en'),('1000043','Paul','NIEUWENHUYSEN','en'),('1000055','CECILE','BAETEMAN','en'),('1000056','Edmond','TORFS','en'),('1000068','Carlos','HEIP','en'),('1000069','Eduard','SOMERS','en'),('1000070','Pierre','CORNELIS','en'),('1000095','José','MARTINS','en'),('1000099','Noel','VERAVERBEKE','en'),('1000101','Dirk','VERMEIR','en'),('1000103','Luc','DE VUYST','en'),('1000104','MICHEL','VAN DEN BERGH','en'),('1000106','Cornelis','KONING','en'),('1000108','Alexandre','SEVRIN','en'),('1000120','UWE','EINMAHL','en'),('1000127','Eric','JESPERS','en'),('1000130','ALAIN','VERSCHOREN','en'),('1000134','Luc','LEYNS','en'),('1000150','Guy','SMAGGHE','en'),('1000158','AXEL','CLEEREMANS','en'),('1000161','GEERT','ANGENON','en'),('1000170','Philippe','CLAEYS','en'),('1000172','BRUNO','POT','en'),('1000175','Mark','SIOEN','en'),('1000176','Kourosch','ABBASPOUR TEHRANI','en'),('1000189','Joris','MESSENS','en'),('1000192','Karel','IN\'T HOUT','en'),('1000193','Johannes','VAN CASTEREN','en'),('1000194','Guido','VAN STEEN','en'),('1000195','Wim','VANROOSE','en'),('1000197','Freddy','VAN OYSTAEYEN','en'),('1000198','Maarten','LOOPMANS','en'),('1000199','Christophe','DETAVERNIER','en'),('1000200','Dirk','POELMAN','en'),('1000202','Peter','GOOS','en'),('1000203','Jacques','TEMPERE','en'),('1000204','Rudi','PENNE','en'),('1000205','Annie','CUYT','en'),('1000208','Lieven','BERVOETS','en'),('1000209','Nico','VAN NULAND','en'),('1000210','Crist','AMELYNCK','en'),('1000211','Johan','VAN DER EYCKEN','en'),('1000212','Dirk','VERSCHUREN','en'),('1000213','Zeger','HENS','en'),('1000214','Pascal','VAN DER VOORT','en'),('1000215','Francis','VERPOORT','en'),('1000216','Patrick','BULTINCK','en'),('1000217','Isabel','VAN DRIESSCHE','en'),('1000218','Katrien','STRUBBE','en'),('1000219','Rik','VAN DEUN','en'),('1000222','Filip','DU PREZ','en'),('1000223','Richard','HOOGENBOOM','en'),('1000224','Peter','DUBRUEL','en'),('1000225','Annemie','ADRIAENS','en'),('1000226','Frank','VANHAECKE','en'),('1000227','David','EELBODE','en'),('1000228','Gianfranco','GENTILE','en'),('1000229','Freddy','CALLENS','en'),('1000230','Bartel','VAN WAEYENBERGE','en'),('1000231','Piet','TERMONIA','en'),('1000232','Theodoros','NAKOS','en'),('1000233','Veronique','VAN SPEYBROECK','en'),('1000234','Sven','DE RIJCKE','en'),('1000235','Willy','SARLET','en'),('1000236','Natalie','JACHOWICZ','en'),('1000237','Luc','VAN HOOREBEKE','en'),('1000238','Klaus','BACHER','en'),('1000239','Jan','RYCKEBUSCH','en'),('1000240','Christophe','LEYS','en'),('1000241','Herwig','DEJONGHE','en'),('1000242','Frans','CANTRIJN','en'),('1000243','Dimitri','VAN NECK','en'),('1000244','David','DUDAL','en'),('1000245','Stefaan','COTTENIER','en'),('1000246','Marc','DE BATIST','en'),('1000247','Geert','HUYS','en'),('1000248','Wim','VYVERMAN','en'),('1000249','David','VAN ROOIJ','en'),('1000250','Sofie','DERYCKE','en'),('1000251','Annemieke','MADDER','en'),('1000252','Jan','VANAVERBEKE','en'),('1000253','Frank','MAES','en'),('1000254','Vera','VAN LANCKER','en'),('1000279','Hugues','GOOSSE','en'),('1000293','Johan','BRANTS','en'),('1000295','Frederic','KLEINERMANN','en'),('1000305','Geert-Jan','HOUBEN','en'),('1000309','Pascal','COSTANZA','en'),('1000323','Andreas','CHRISTMANN','en'),('1000324','Ben','CRAPS','en'),('1000332','Marleen','DE TROCH','en'),('1000334','Ann','VANREUSEL','en'),('1000335','Olivier','DE CLERCK','en'),('1000336','Steven','DEGRAER','en'),('1000337','Gudrun','DE BOECK','en'),('1000338','Koen','SABBE','en'),('1000355','Kevin','RUDDICK','en'),('1000361','Bart','DIERICKX','en'),('1000372','Filip','MEYSMAN','en'),('1000379','Claude','JOIRIS','en'),('1000380','Jean','VEREECKEN','en'),('1000381','Henri','EISENDRATH','en'),('1000387','Walter','VAN RENSBERGEN','en'),('1000389','Irina','VERETENNICOFF','en'),('1000391','Josee','CHARLIER','en'),('1000392','Nicolas','GLANSDORFF','en'),('1000394','Francine','GRANDSARD','en'),('1000396','Charles','SUSANNE','en'),('1000401','Alfons','BUEKENS','en'),('1000406','Marie-Hermande','DARO','en'),('1000414','Micheline','VOLDERS','en'),('1000423','Luit','SLOOTEN','en'),('1000425','Ivan','CNOP','en'),('1000429','Gert','VERSTRAETEN','en'),('1000430','Christian','KESTELOOT','en'),('1000431','Gerard','GOVERS','en'),('1000432','Etienne','VAN HECKE','en'),('1000433','Dominique','VANNESTE','en'),('1000434','Nicole','VAN LIPZIG','en'),('1000435','Jean','POESEN','en'),('1000442','Han Karel','REMAUT','en'),('1000454','Beat','SIGNER','en'),('1000455','Joost','WEYLER','en'),('1000456','Kayawe Valentine','MUBIANA','en'),('1000462','Els','LAENENS','en'),('1000463','Sebastiaan','EELTINK','en'),('1000464','Nicolaas','VAN EIJNDHOVEN','en'),('1000465','Jeroen','RAES','en'),('1000468','Frederik','BUYLAERT','en'),('1000470','Tetyana','KADANKOVA','en'),('1000473','Marc','KOCHZIUS','en'),('1000480','Freya','BLEKMAN','en'),('1000486','Matthieu','KERVYN DE MEERENDRE','en'),('1000489','Guido','VERNIEST','en'),('1000498','Walter','KEMPENAERS','en'),('1000522','Stephen','LOUWYE','en'),('1000640','Robert','VERLAAK','en'),('1000650','Stijn','TEMMERMAN','en'),('1000683','Marinee','CHUAH','en'),('1000688','Danny','SEGERS','en'),('1000690','Elke','VANEMPTEN','en'),('1000731','Bruno','DUMAS','en'),('1000737','Petra','VAN DEN BORRE','en'),('1000895','Peter','TOMPA','en'),('1000937','Tim','DEPREZ','en'),('1000960','An','CLIQUET','en'),('1001047','Stefan','WECKX','en'),('1001055','Oleksandr','VOLKOV','en'),('1001179','Hubert','THIERENS','en'),('1001214','Bas','VAN HEUR','en'),('1001303','Anna-Karina','SEGERS','en'),('1001499','Robert','DE WULF','en'),('1001521','Gholamreza','HASSANZADEH GHASSABEH','en'),('1001552','Tjeerd','BOUMA','en'),('1001570','Ludwig','CARDON','en'),('1001619','Magda','VINCX','en'),('1001679','Dirk','RYCKBOSCH','en'),('1001729','Karline','SOETAERT','en'),('1001761','Philippe','KOK','en'),('1001803','Gustavo','GUTIERREZ GONZALEZ','en'),('1001821','Wim','VRANKEN','en'),('1001963','Ronny','BLUST','en'),('1002002','Joris','BLOMMAERT','en'),('1002026','Andreas','BÄCHLE','en'),('1002098','Bart','WINDELS','en'),('1002114','Hans','VANDE SANDE','en'),('1002143','Michael','RYCKEWAERT','en'),('1002147','David John','GOWER','en'),('1002148','Mark','WILKINSON','en'),('1002149','Miguel','VENCES','en'),('1002150','Frank','PASMANS','en'),('1002151','Darrel','FROST','en'),('1002152','Jan Willem','ARNTZEN','en'),('1002153','Raoul','VAN DAMME','en'),('1002159','Nele','VANDAELE','en'),('1002162','Paul','VAN DER MEEREN','en'),('1002164','Jean-Pierre','VAN GEERTRUYDEN','en'),('1002166','Wim','VAN HUL','en'),('1002167','Bruno','CAMMUE','en'),('1002168','Eric','TOLLENS','en'),('1002171','Erik','MATHIJS','en'),('1002176','Mainak','GUHA ROY','en'),('1002177','Lionel','SIESS','en'),('1002180','Christoffel','WAELKENS','en'),('1002181','Antoine','VAN PROEYEN','en'),('1002183','Henk','VRIELINCK','en'),('1002184','Alexander','PANFILOV','en'),('1002225','Peter','VANDENABEELE','en'),('1002234','Paul','BLONDEEL','en'),('1002235','Frédérique','Borguet','en'),('1002238','Nadia','CASABELLA','en'),('1002239','Stefan','De Corte','en'),('1002244','Luc','Lehouck','en'),('1002246','Thomas','Moens','en'),('1002247','Jan','Parys','en'),('1002262','Jens','AERTS','en'),('1002263','Tom','DE WAELE','en'),('1234567','Kris','Steenhaut','Dutch'),('1568634','Joeri','De Koster','Dutch'),('2000000','Promotor','.','en'),('2000005','Decaan','WE','en'),('2000007','Decaan','IR','en'),('3596346','Frank','Van Overwalle','Dutch'),('4264924','Marie-Paule','Uwase','English'),('5389644','Frederico','Dominguez','English'),('adeliens','Anders','Deliens','nl'),('lavholsb','Lars','Van Holsbeeke','nl'),('nvgeele','Nils','Van Geele','en'),('pmanager','Program','Manager','en'),('testuser','','','en'),('titular','Random','Titular','en'),('tjdhondt','Theo','D\'HONDT','en'),('wdemeuter','Wolfgang','DE MEUTER','en');
/*!40000 ALTER TABLE `person` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program`
--

DROP TABLE IF EXISTS `program`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(126) NOT NULL DEFAULT '',
  `description` text,
  `manager` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `manager` (`manager`),
  CONSTRAINT `program_ibfk_1` FOREIGN KEY (`manager`) REFERENCES `person` (`netid`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program`
--

LOCK TABLES `program` WRITE;
/*!40000 ALTER TABLE `program` DISABLE KEYS */;
INSERT INTO `program` VALUES (1,'1e bachelor Computerwetenschappen','Alle studenten die de bacheloropleiding in de Computerwetenschappen aanvatten starten met de module eerste bachelor Computerwetenschappen. Deze module komt overeen met het eerste jaar van het modeltraject. Bij een eerste inschrijving in de bacheloropleiding mag de student enkel verplichte studiedelen eerste bachelor opnemen, met uitzondering van het voorbereidend keuzestudiedeel \"Basisvaardigheden Wiskunde\".','tjdhondt'),(2,'3e bachelor computerwetenschappen','In combinatie met de verplichte studiedelen derde bachelor neemt de student bij voorkeur voor 30 studiepunten aan keuzestudiedelen op.\nInschrijven voor de bachelorproef kan indien het een inschrijving betreft waarbij met de andere gekozen studiedelen het volledige bachelortraject van minstens 180 studiepunten wordt ingevuld.','tjdhondt'),(3,'3e Bachelor Ingenieurswetenschappen - Computerwetenschappen','Deze module (60 SP) is specifiek voor de Afstudeerrichting Elektronica en informatietechnologie, met het profiel Computerwetenschappen. Ze bestaat uit een submodule die gemeenschappelijk is voor alle studenten die de afstudeerrichting Elektronica en informatietechnologie hebben gekozen en een submodule specifiek voor het profiel Computerwetenschappen. De studenten moeten alle studiedelen uit beide modules verplicht voltooien. Deze studiedelen behoren tot het derde jaar van het voltijds modeltraject bachelor (Bachelor 3). Bij een eerste inschrijving in de bacheloropleiding is het niet toegelaten reeds in te schrijven voor studiedelen uit deze module. Studenten mogen pas inschrijven voor studiedelen uit \'Jaar 3 van het voltijds modeltraject BA IR – EIT Computerwetenschappen\' indien zij reeds de credits verworven hebben voor het technologieproject \'Informatie en communicatietechnologie\' en ten minste één van de 3 andere technologieprojecten (Leefmilieu en duurzame materialen of Werktuigkunde en Elektrotechniek of Informatie en communicatietechnologie) uit de module ‘Technologieprojecten in opleidingsateliers\' van \'Jaar 2 van het modeltraject BA IR\' of voor deze 2 technologieprojecten inschrijven samen met de studiedelen uit de afstudeerrichtingsmodule. Studenten moeten voldoen aan de aan elk van de studiedelen verbonden specifieke inschrijvingsvereisten.','pmanager');
/*!40000 ALTER TABLE `program` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program-choice-course`
--

DROP TABLE IF EXISTS `program-choice-course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program-choice-course` (
  `program` int(11) unsigned NOT NULL,
  `course-code` varchar(32) NOT NULL DEFAULT '',
  KEY `program` (`program`),
  KEY `course-code` (`course-code`),
  CONSTRAINT `program-choice-course_ibfk_1` FOREIGN KEY (`program`) REFERENCES `program` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `program-choice-course_ibfk_2` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program-choice-course`
--

LOCK TABLES `program-choice-course` WRITE;
/*!40000 ALTER TABLE `program-choice-course` DISABLE KEYS */;
INSERT INTO `program-choice-course` VALUES (1,'1007132ANR'),(2,'1018725AER'),(2,'1005176BNR'),(3,'1015259ANR'),(3,'1018725AER');
/*!40000 ALTER TABLE `program-choice-course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `program-mandatory-course`
--

DROP TABLE IF EXISTS `program-mandatory-course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `program-mandatory-course` (
  `program` int(11) unsigned NOT NULL,
  `course-code` varchar(32) NOT NULL DEFAULT '',
  KEY `program` (`program`),
  KEY `course-code` (`course-code`),
  CONSTRAINT `program-mandatory-course_ibfk_1` FOREIGN KEY (`program`) REFERENCES `program` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `program-mandatory-course_ibfk_2` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `program-mandatory-course`
--

LOCK TABLES `program-mandatory-course` WRITE;
/*!40000 ALTER TABLE `program-mandatory-course` DISABLE KEYS */;
INSERT INTO `program-mandatory-course` VALUES (1,'1000447ANR'),(1,'1015328ANR'),(1,'1015259ANR'),(1,'1007156ANR'),(2,'1001714AER'),(2,'1001673BNR'),(2,'1004483BNR'),(3,'1000447ANR'),(3,'1001673BNR'),(3,'1007156ANR'),(3,'1004483BNR');
/*!40000 ALTER TABLE `program-mandatory-course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room`
--

DROP TABLE IF EXISTS `room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `room` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `capacity` int(11) unsigned NOT NULL,
  `building` varchar(5) NOT NULL DEFAULT '',
  `floor` int(11) unsigned NOT NULL,
  `number` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room`
--

LOCK TABLES `room` WRITE;
/*!40000 ALTER TABLE `room` DISABLE KEYS */;
INSERT INTO `room` VALUES (1,200,'D',0,5),(2,200,'D',0,3),(3,100,'G',1,18),(4,150,'G',1,19),(5,70,'F',5,403),(6,20,'F',4,412),(7,100,'E',0,4),(8,80,'E',0,5),(9,80,'E',0,6);
/*!40000 ALTER TABLE `room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `room-facility`
--

DROP TABLE IF EXISTS `room-facility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `room-facility` (
  `room` int(11) unsigned NOT NULL,
  `facility` int(11) unsigned NOT NULL,
  KEY `room` (`room`),
  CONSTRAINT `room-facility_ibfk_1` FOREIGN KEY (`room`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `room-facility`
--

LOCK TABLES `room-facility` WRITE;
/*!40000 ALTER TABLE `room-facility` DISABLE KEYS */;
INSERT INTO `room-facility` VALUES (1,1),(1,0),(2,1),(2,0),(3,1),(3,0),(4,1),(4,0),(5,1),(5,0),(6,1),(6,0),(7,1),(7,0),(8,1),(8,0),(9,1),(9,0);
/*!40000 ALTER TABLE `room-facility` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule-block`
--

DROP TABLE IF EXISTS `schedule-block`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schedule-block` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `week` int(11) DEFAULT NULL,
  `day` int(11) DEFAULT NULL,
  `first-slot` int(11) DEFAULT NULL,
  `last-slot` int(11) DEFAULT NULL,
  `room` int(11) unsigned DEFAULT NULL,
  `course-activity` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `course-activity` (`course-activity`),
  KEY `schedule-block_ibfk_2` (`room`),
  CONSTRAINT `schedule-block_ibfk_1` FOREIGN KEY (`course-activity`) REFERENCES `course-activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `schedule-block_ibfk_2` FOREIGN KEY (`room`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=548 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule-block`
--

LOCK TABLES `schedule-block` WRITE;
/*!40000 ALTER TABLE `schedule-block` DISABLE KEYS */;
INSERT INTO `schedule-block` VALUES (6,6,2,7,10,3,1626),(7,22,4,7,10,7,1868),(8,3,1,3,8,3,1722),(9,14,5,9,12,6,1867),(10,31,5,5,8,3,1740),(11,5,2,7,10,3,1626),(12,21,4,7,10,7,1868),(13,2,1,3,8,3,1722),(14,13,5,9,12,6,1867),(15,30,5,5,8,3,1740),(16,4,2,7,10,3,1626),(17,20,4,7,10,7,1868),(18,12,5,9,12,6,1867),(19,29,5,5,8,3,1740),(20,23,3,13,16,9,1742),(21,11,4,7,10,7,1868),(22,35,5,9,12,6,1867),(23,28,5,5,8,3,1740),(24,22,3,13,16,9,1742),(25,10,4,7,10,7,1868),(26,34,5,9,12,6,1867),(27,7,1,3,8,3,1722),(28,9,4,7,10,7,1868),(29,33,5,9,12,6,1867),(30,6,1,3,8,3,1722),(31,10,5,7,10,1,1902),(32,11,1,13,16,8,1724),(33,8,4,7,10,7,1868),(34,32,5,9,12,6,1867),(35,5,1,3,8,3,1722),(36,9,5,7,10,1,1902),(37,10,1,13,16,8,1724),(38,15,4,7,10,7,1868),(39,4,1,3,8,3,1722),(40,8,5,7,10,1,1902),(41,9,1,13,16,8,1724),(42,10,1,17,20,4,1900),(43,14,4,7,10,7,1868),(44,23,5,5,8,3,1740),(45,8,1,13,16,8,1724),(46,9,1,17,20,4,1900),(47,13,4,7,10,7,1868),(48,22,5,5,8,3,1740),(49,8,1,17,20,4,1900),(50,12,4,7,10,7,1868),(51,36,5,9,12,6,1867),(52,14,1,13,16,8,1724),(53,35,4,7,10,7,1868),(54,27,5,9,12,6,1867),(55,13,1,13,16,8,1724),(56,34,4,7,10,7,1868),(57,26,5,9,12,6,1867),(58,3,5,7,10,1,1902),(59,12,1,13,16,8,1724),(60,33,4,7,10,7,1868),(61,25,5,9,12,6,1867),(62,2,5,7,10,1,1902),(63,3,1,13,16,8,1724),(64,32,4,7,10,7,1868),(65,24,5,9,12,6,1867),(66,11,2,17,20,5,1628),(67,2,1,13,16,8,1724),(68,3,1,17,20,4,1900),(69,35,3,13,16,9,1742),(70,31,5,9,12,6,1867),(71,10,2,17,20,5,1628),(72,2,1,17,20,4,1900),(73,34,3,13,16,9,1742),(74,30,5,9,12,6,1867),(75,7,5,7,10,1,1902),(76,9,2,17,20,5,1628),(77,33,3,13,16,9,1742),(78,29,5,9,12,6,1867),(79,6,5,7,10,1,1902),(80,8,2,17,20,5,1628),(81,7,1,13,16,8,1724),(82,32,3,13,16,9,1742),(83,36,4,7,10,7,1868),(84,28,5,9,12,6,1867),(85,11,2,7,10,3,1626),(86,5,5,7,10,1,1902),(87,6,1,13,16,8,1724),(88,7,1,17,20,4,1900),(89,27,4,7,10,7,1868),(90,19,5,9,12,6,1867),(91,10,2,7,10,3,1626),(92,4,5,7,10,1,1902),(93,14,2,17,20,5,1628),(94,5,1,13,16,8,1724),(95,6,1,17,20,4,1900),(96,26,4,7,10,7,1868),(97,18,5,9,12,6,1867),(98,35,5,5,8,3,1740),(99,9,2,7,10,3,1626),(100,13,2,17,20,5,1628),(101,4,1,13,16,8,1724),(102,5,1,17,20,4,1900),(103,25,4,7,10,7,1868),(104,17,5,9,12,6,1867),(105,34,5,5,8,3,1740),(106,8,2,7,10,3,1626),(107,12,2,17,20,5,1628),(108,4,1,17,20,4,1900),(109,36,3,13,16,9,1742),(110,24,4,7,10,7,1868),(111,16,5,9,12,6,1867),(112,33,5,5,8,3,1740),(113,3,2,17,20,5,1628),(114,27,3,13,16,9,1742),(115,31,4,7,10,7,1868),(116,23,5,9,12,6,1867),(117,32,5,5,8,3,1740),(118,14,2,7,10,3,1626),(119,2,2,17,20,5,1628),(120,26,3,13,16,9,1742),(121,30,4,7,10,7,1868),(122,11,1,3,8,3,1722),(123,22,5,9,12,6,1867),(124,13,2,7,10,3,1626),(125,25,3,13,16,9,1742),(126,29,4,7,10,7,1868),(127,10,1,3,8,3,1722),(128,21,5,9,12,6,1867),(129,12,2,7,10,3,1626),(130,24,3,13,16,9,1742),(131,28,4,7,10,7,1868),(132,9,1,3,8,3,1722),(133,20,5,9,12,6,1867),(134,3,2,7,10,3,1626),(135,7,2,17,20,5,1628),(136,31,3,13,16,9,1742),(137,19,4,7,10,7,1868),(138,8,1,3,8,3,1722),(139,11,5,9,12,6,1867),(140,36,5,5,8,3,1740),(141,2,2,7,10,3,1626),(142,6,2,17,20,5,1628),(143,30,3,13,16,9,1742),(144,18,4,7,10,7,1868),(145,10,5,9,12,6,1867),(146,27,5,5,8,3,1740),(147,5,2,17,20,5,1628),(148,29,3,13,16,9,1742),(149,17,4,7,10,7,1868),(150,14,1,3,8,3,1722),(151,9,5,9,12,6,1867),(152,26,5,5,8,3,1740),(153,4,2,17,20,5,1628),(154,28,3,13,16,9,1742),(155,16,4,7,10,7,1868),(156,13,1,3,8,3,1722),(157,8,5,9,12,6,1867),(158,25,5,5,8,3,1740),(159,7,2,7,10,3,1626),(160,23,4,7,10,7,1868),(161,12,1,3,8,3,1722),(162,15,5,9,12,6,1867),(163,24,5,5,8,3,1740),(164,6,2,13,18,7,1699),(165,32,3,7,10,6,1687),(166,2,4,13,20,9,3188),(167,11,2,23,24,8,1687),(168,12,2,19,22,7,1701),(169,5,2,13,18,7,1699),(170,7,3,7,10,6,1687),(171,10,2,23,24,8,1687),(172,3,2,19,22,7,1701),(173,6,4,17,20,2,3191),(174,4,2,13,18,7,1699),(175,6,3,7,10,6,1687),(176,9,2,23,24,8,1687),(177,2,2,19,22,7,1701),(178,5,4,17,20,2,3191),(179,5,3,7,10,6,1687),(180,7,4,13,20,9,3188),(181,8,2,23,24,8,1687),(182,4,4,17,20,2,3191),(183,36,3,7,10,6,1687),(184,4,3,7,10,6,1687),(185,6,4,13,20,9,3188),(186,15,2,23,24,8,1687),(187,27,3,7,10,6,1687),(188,5,4,13,20,9,3188),(189,14,2,23,24,8,1687),(190,7,2,19,22,7,1701),(191,26,3,7,10,6,1687),(192,4,4,13,20,9,3188),(193,13,2,23,24,8,1687),(194,6,2,19,22,7,1701),(195,25,3,7,10,6,1687),(196,12,2,23,24,8,1687),(197,5,2,19,22,7,1701),(198,11,5,3,6,1,3190),(199,24,3,7,10,6,1687),(200,35,2,23,24,8,1687),(201,3,2,23,24,8,1687),(202,4,2,19,22,7,1701),(203,10,5,3,6,1,3190),(204,31,3,7,10,6,1687),(205,34,2,23,24,8,1687),(206,2,2,23,24,8,1687),(207,9,5,3,6,1,3190),(208,30,3,7,10,6,1687),(209,33,2,23,24,8,1687),(210,8,5,3,6,1,3190),(211,29,3,7,10,6,1687),(212,32,2,23,24,8,1687),(213,28,3,7,10,6,1687),(214,7,2,23,24,8,1687),(215,14,5,3,6,1,3190),(216,19,3,7,10,6,1687),(217,6,2,23,24,8,1687),(218,13,5,3,6,1,3190),(219,11,5,7,8,1,3190),(220,18,3,7,10,6,1687),(221,5,2,23,24,8,1687),(222,11,3,13,19,9,3187),(223,12,5,3,6,1,3190),(224,10,5,7,8,1,3190),(225,17,3,7,10,6,1687),(226,36,2,23,24,8,1687),(227,4,2,23,24,8,1687),(228,10,3,13,19,9,3187),(229,3,5,3,6,1,3190),(230,9,5,7,8,1,3190),(231,16,3,7,10,6,1687),(232,27,2,23,24,8,1687),(233,9,3,13,19,9,3187),(234,2,5,3,6,1,3190),(235,8,5,7,8,1,3190),(236,23,3,7,10,6,1687),(237,26,2,23,24,8,1687),(238,8,3,13,19,9,3187),(239,22,3,7,10,6,1687),(240,25,2,23,24,8,1687),(241,11,2,13,18,7,1699),(242,14,5,7,8,1,3190),(243,21,3,7,10,6,1687),(244,24,2,23,24,8,1687),(245,14,3,13,19,9,3187),(246,10,2,13,18,7,1699),(247,7,5,3,6,1,3190),(248,13,5,7,8,1,3190),(249,20,3,7,10,6,1687),(250,31,2,23,24,8,1687),(251,3,4,13,16,2,3191),(252,13,3,13,19,9,3187),(253,9,2,13,18,7,1699),(254,6,5,3,6,1,3190),(255,12,5,7,8,1,3190),(256,11,3,7,10,6,1687),(257,30,2,23,24,8,1687),(258,2,4,13,16,2,3191),(259,12,3,13,19,9,3187),(260,8,2,13,18,7,1699),(261,5,5,3,6,1,3190),(262,3,5,7,8,1,3190),(263,10,3,7,10,6,1687),(264,29,2,23,24,8,1687),(265,3,3,13,19,9,3187),(266,4,5,3,6,1,3190),(267,2,5,7,8,1,3190),(268,9,3,7,10,6,1687),(269,11,4,13,20,9,3188),(270,28,2,23,24,8,1687),(271,2,3,13,19,9,3187),(272,14,2,13,18,7,1699),(273,8,3,7,10,6,1687),(274,10,4,13,20,9,3188),(275,19,2,23,24,8,1687),(276,13,2,13,18,7,1699),(277,15,3,7,10,6,1687),(278,9,4,13,20,9,3188),(279,18,2,23,24,8,1687),(280,11,2,19,22,7,1701),(281,6,4,13,16,2,3191),(282,12,2,13,18,7,1699),(283,7,5,7,8,1,3190),(284,14,3,7,10,6,1687),(285,8,4,13,20,9,3188),(286,17,2,23,24,8,1687),(287,10,2,19,22,7,1701),(288,5,4,13,16,2,3191),(289,7,3,13,19,9,3187),(290,3,2,13,18,7,1699),(291,6,5,7,8,1,3190),(292,13,3,7,10,6,1687),(293,16,2,23,24,8,1687),(294,9,2,19,22,7,1701),(295,4,4,13,16,2,3191),(296,6,3,13,19,9,3187),(297,2,2,13,18,7,1699),(298,5,5,7,8,1,3190),(299,12,3,7,10,6,1687),(300,14,4,13,20,9,3188),(301,23,2,23,24,8,1687),(302,8,2,19,22,7,1701),(303,3,4,17,20,2,3191),(304,5,3,13,19,9,3187),(305,4,5,7,8,1,3190),(306,35,3,7,10,6,1687),(307,3,3,7,10,6,1687),(308,13,4,13,20,9,3188),(309,22,2,23,24,8,1687),(310,2,4,17,20,2,3191),(311,4,3,13,19,9,3187),(312,34,3,7,10,6,1687),(313,2,3,7,10,6,1687),(314,12,4,13,20,9,3188),(315,21,2,23,24,8,1687),(316,14,2,19,22,7,1701),(317,7,2,13,18,7,1699),(318,33,3,7,10,6,1687),(319,3,4,13,20,9,3188),(320,20,2,23,24,8,1687),(321,13,2,19,22,7,1701),(322,26,2,7,10,3,1626),(323,23,1,3,8,3,1722),(324,34,5,9,12,6,1867),(325,25,2,7,10,3,1626),(326,41,4,7,10,7,1868),(327,22,1,3,8,3,1722),(328,33,5,9,12,6,1867),(329,24,2,7,10,3,1626),(330,40,4,7,10,7,1868),(331,32,5,9,12,6,1867),(332,31,4,7,10,7,1868),(333,30,4,7,10,7,1868),(334,27,1,3,8,3,1722),(335,29,4,7,10,7,1868),(336,26,1,3,8,3,1722),(337,30,5,7,10,1,1902),(338,31,1,13,16,8,1724),(339,28,4,7,10,7,1868),(340,25,1,3,8,3,1722),(341,29,5,7,10,1,1902),(342,30,1,13,16,8,1724),(343,35,4,7,10,7,1868),(344,24,1,3,8,3,1722),(345,28,5,7,10,1,1902),(346,29,1,13,16,8,1724),(347,30,1,17,20,4,1900),(348,34,4,7,10,7,1868),(349,28,1,13,16,8,1724),(350,29,1,17,20,4,1900),(351,33,4,7,10,7,1868),(352,28,1,17,20,4,1900),(353,32,4,7,10,7,1868),(354,34,1,13,16,8,1724),(355,33,1,13,16,8,1724),(356,23,5,7,10,1,1902),(357,32,1,13,16,8,1724),(358,22,5,7,10,1,1902),(359,23,1,13,16,8,1724),(360,31,2,17,20,5,1628),(361,22,1,13,16,8,1724),(362,23,1,17,20,4,1900),(363,30,2,17,20,5,1628),(364,22,1,17,20,4,1900),(365,27,5,7,10,1,1902),(366,29,2,17,20,5,1628),(367,26,5,7,10,1,1902),(368,28,2,17,20,5,1628),(369,27,1,13,16,8,1724),(370,31,2,7,10,3,1626),(371,25,5,7,10,1,1902),(372,26,1,13,16,8,1724),(373,27,1,17,20,4,1900),(374,39,5,9,12,6,1867),(375,30,2,7,10,3,1626),(376,24,5,7,10,1,1902),(377,34,2,17,20,5,1628),(378,25,1,13,16,8,1724),(379,26,1,17,20,4,1900),(380,38,5,9,12,6,1867),(381,29,2,7,10,3,1626),(382,33,2,17,20,5,1628),(383,24,1,13,16,8,1724),(384,25,1,17,20,4,1900),(385,37,5,9,12,6,1867),(386,28,2,7,10,3,1626),(387,32,2,17,20,5,1628),(388,24,1,17,20,4,1900),(389,36,5,9,12,6,1867),(390,23,2,17,20,5,1628),(391,34,2,7,10,3,1626),(392,22,2,17,20,5,1628),(393,31,1,3,8,3,1722),(394,33,2,7,10,3,1626),(395,30,1,3,8,3,1722),(396,41,5,9,12,6,1867),(397,32,2,7,10,3,1626),(398,29,1,3,8,3,1722),(399,40,5,9,12,6,1867),(400,23,2,7,10,3,1626),(401,27,2,17,20,5,1628),(402,39,4,7,10,7,1868),(403,28,1,3,8,3,1722),(404,31,5,9,12,6,1867),(405,22,2,7,10,3,1626),(406,26,2,17,20,5,1628),(407,38,4,7,10,7,1868),(408,30,5,9,12,6,1867),(409,25,2,17,20,5,1628),(410,37,4,7,10,7,1868),(411,34,1,3,8,3,1722),(412,29,5,9,12,6,1867),(413,24,2,17,20,5,1628),(414,36,4,7,10,7,1868),(415,33,1,3,8,3,1722),(416,28,5,9,12,6,1867),(417,27,2,7,10,3,1626),(418,32,1,3,8,3,1722),(419,35,5,9,12,6,1867),(420,26,2,13,18,7,1699),(421,22,4,13,20,9,3188),(422,31,2,23,24,8,1687),(423,32,2,19,22,7,1701),(424,25,2,13,18,7,1699),(425,27,3,7,10,6,1687),(426,30,2,23,24,8,1687),(427,23,2,19,22,7,1701),(428,26,4,17,20,2,3191),(429,24,2,13,18,7,1699),(430,26,3,7,10,6,1687),(431,29,2,23,24,8,1687),(432,22,2,19,22,7,1701),(433,25,4,17,20,2,3191),(434,25,3,7,10,6,1687),(435,27,4,13,20,9,3188),(436,28,2,23,24,8,1687),(437,24,4,17,20,2,3191),(438,24,3,7,10,6,1687),(439,26,4,13,20,9,3188),(440,35,2,23,24,8,1687),(441,25,4,13,20,9,3188),(442,34,2,23,24,8,1687),(443,27,2,19,22,7,1701),(444,24,4,13,20,9,3188),(445,33,2,23,24,8,1687),(446,26,2,19,22,7,1701),(447,32,2,23,24,8,1687),(448,25,2,19,22,7,1701),(449,31,5,3,6,1,3190),(450,23,2,23,24,8,1687),(451,24,2,19,22,7,1701),(452,30,5,3,6,1,3190),(453,22,2,23,24,8,1687),(454,29,5,3,6,1,3190),(455,28,5,3,6,1,3190),(456,27,2,23,24,8,1687),(457,34,5,3,6,1,3190),(458,39,3,7,10,6,1687),(459,26,2,23,24,8,1687),(460,33,5,3,6,1,3190),(461,31,5,7,8,1,3190),(462,38,3,7,10,6,1687),(463,25,2,23,24,8,1687),(464,31,3,13,19,9,3187),(465,32,5,3,6,1,3190),(466,30,5,7,8,1,3190),(467,37,3,7,10,6,1687),(468,24,2,23,24,8,1687),(469,30,3,13,19,9,3187),(470,23,5,3,6,1,3190),(471,29,5,7,8,1,3190),(472,36,3,7,10,6,1687),(473,29,3,13,19,9,3187),(474,22,5,3,6,1,3190),(475,28,5,7,8,1,3190),(476,28,3,13,19,9,3187),(477,31,2,13,18,7,1699),(478,34,5,7,8,1,3190),(479,41,3,7,10,6,1687),(480,34,3,13,19,9,3187),(481,30,2,13,18,7,1699),(482,27,5,3,6,1,3190),(483,33,5,7,8,1,3190),(484,40,3,7,10,6,1687),(485,23,4,13,16,2,3191),(486,33,3,13,19,9,3187),(487,29,2,13,18,7,1699),(488,26,5,3,6,1,3190),(489,32,5,7,8,1,3190),(490,31,3,7,10,6,1687),(491,22,4,13,16,2,3191),(492,32,3,13,19,9,3187),(493,28,2,13,18,7,1699),(494,25,5,3,6,1,3190),(495,23,5,7,8,1,3190),(496,30,3,7,10,6,1687),(497,23,3,13,19,9,3187),(498,24,5,3,6,1,3190),(499,22,5,7,8,1,3190),(500,29,3,7,10,6,1687),(501,31,4,13,20,9,3188),(502,22,3,13,19,9,3187),(503,34,2,13,18,7,1699),(504,28,3,7,10,6,1687),(505,30,4,13,20,9,3188),(506,39,2,23,24,8,1687),(507,33,2,13,18,7,1699),(508,35,3,7,10,6,1687),(509,29,4,13,20,9,3188),(510,38,2,23,24,8,1687),(511,31,2,19,22,7,1701),(512,26,4,13,16,2,3191),(513,32,2,13,18,7,1699),(514,27,5,7,8,1,3190),(515,34,3,7,10,6,1687),(516,28,4,13,20,9,3188),(517,37,2,23,24,8,1687),(518,30,2,19,22,7,1701),(519,25,4,13,16,2,3191),(520,27,3,13,19,9,3187),(521,23,2,13,18,7,1699),(522,26,5,7,8,1,3190),(523,33,3,7,10,6,1687),(524,36,2,23,24,8,1687),(525,29,2,19,22,7,1701),(526,24,4,13,16,2,3191),(527,26,3,13,19,9,3187),(528,22,2,13,18,7,1699),(529,25,5,7,8,1,3190),(530,32,3,7,10,6,1687),(531,34,4,13,20,9,3188),(532,28,2,19,22,7,1701),(533,23,4,17,20,2,3191),(534,25,3,13,19,9,3187),(535,24,5,7,8,1,3190),(536,23,3,7,10,6,1687),(537,33,4,13,20,9,3188),(538,22,4,17,20,2,3191),(539,24,3,13,19,9,3187),(540,22,3,7,10,6,1687),(541,32,4,13,20,9,3188),(542,41,2,23,24,8,1687),(543,34,2,19,22,7,1701),(544,27,2,13,18,7,1699),(545,23,4,13,20,9,3188),(546,40,2,23,24,8,1687),(547,33,2,19,22,7,1701);
/*!40000 ALTER TABLE `schedule-block` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule-proposal-message`
--

DROP TABLE IF EXISTS `schedule-proposal-message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schedule-proposal-message` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `sender` varchar(12) NOT NULL DEFAULT '',
  `proposal` text NOT NULL,
  `message` text,
  `status` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `titular` (`sender`),
  CONSTRAINT `schedule-proposal-message_ibfk_1` FOREIGN KEY (`sender`) REFERENCES `person` (`netid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule-proposal-message`
--

LOCK TABLES `schedule-proposal-message` WRITE;
/*!40000 ALTER TABLE `schedule-proposal-message` DISABLE KEYS */;
/*!40000 ALTER TABLE `schedule-proposal-message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `schedule-proposal-message-programs`
--

DROP TABLE IF EXISTS `schedule-proposal-message-programs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schedule-proposal-message-programs` (
  `message-id` int(11) unsigned NOT NULL,
  `program-id` int(11) unsigned NOT NULL,
  KEY `message-id` (`message-id`),
  KEY `schedule-proposal-message-programs_ibfk_2` (`program-id`),
  CONSTRAINT `schedule-proposal-message-programs_ibfk_1` FOREIGN KEY (`message-id`) REFERENCES `schedule-proposal-message` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `schedule-proposal-message-programs_ibfk_2` FOREIGN KEY (`program-id`) REFERENCES `program` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `schedule-proposal-message-programs`
--

LOCK TABLES `schedule-proposal-message-programs` WRITE;
/*!40000 ALTER TABLE `schedule-proposal-message-programs` DISABLE KEYS */;
/*!40000 ALTER TABLE `schedule-proposal-message-programs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session`
--

DROP TABLE IF EXISTS `session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `session` (
  `id` varchar(36) NOT NULL DEFAULT '',
  `data` text,
  `created` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session`
--

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;
INSERT INTO `session` VALUES ('109e0b61-cdfc-4cc2-9ff5-51ea2fcd5a55','{:locale \"en\", :last-name \"DE MEUTER\", :first-name \"Wolfgang\", :netid \"wdemeuter\", :user \"wdemeuter\", :user-functions #{:instructor :titular}}','2014-05-15 11:23:28');
/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscription` (
  `course-code` varchar(32) NOT NULL DEFAULT '',
  `netid` varchar(12) NOT NULL DEFAULT '',
  KEY `course-code` (`course-code`),
  KEY `netid` (`netid`),
  CONSTRAINT `subscription_ibfk_1` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `subscription_ibfk_2` FOREIGN KEY (`netid`) REFERENCES `person` (`netid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-05-20 16:01:36

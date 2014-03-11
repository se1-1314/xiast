# Dump of table room
# ------------------------------------------------------------

DROP TABLE IF EXISTS `room`;

CREATE TABLE `room` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `capacity` int(11) unsigned NOT NULL,
  `building` varchar(5) NOT NULL DEFAULT '',
  `floor` int(11) unsigned NOT NULL,
  `number` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table department
# ------------------------------------------------------------

DROP TABLE IF EXISTS `department`;

CREATE TABLE `department` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `faculty` varchar(64) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table person
# ------------------------------------------------------------

DROP TABLE IF EXISTS `person`;

CREATE TABLE `person` (
  `netid` varchar(12) NOT NULL DEFAULT '',
  `firstname` varchar(64) DEFAULT NULL,
  `surname` varchar(64) DEFAULT NULL,
  `locale` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`netid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `person` (`netid`, `firstname`, `surname`, `locale`)
VALUES
('instructor','Instruc','Tor','en'),
('pmanager','Program','Manager','en'),
('student','Stu','Dent','en'),
('titular','Ti','Tular','en');

# Dump of table course
# ------------------------------------------------------------

DROP TABLE IF EXISTS `course`;

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
  CONSTRAINT `course_ibfk_1` FOREIGN KEY (`titular-id`) REFERENCES `person` (`netid`) ON UPDATE CASCADE,
  CONSTRAINT `course_ibfk_2` FOREIGN KEY (`department`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table course-activity
# ------------------------------------------------------------

DROP TABLE IF EXISTS `course-activity`;

CREATE TABLE `course-activity` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `course-code` varchar(32) NOT NULL DEFAULT '',
  `type` int(11) unsigned NOT NULL,
  `semester` int(11) unsigned NOT NULL,
  `week` int(11) DEFAULT NULL,
  `contact-time-hours` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `course-code` (`course-code`),
  CONSTRAINT `course-activity_ibfk_1` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table course-activity-facility
# ------------------------------------------------------------

DROP TABLE IF EXISTS `course-activity-facility`;

CREATE TABLE `course-activity-facility` (
  `course-activity` int(11) unsigned NOT NULL,
  `facility` int(11) unsigned NOT NULL,
  KEY `course-activity` (`course-activity`),
  CONSTRAINT `course-activity-facility_ibfk_1` FOREIGN KEY (`course-activity`) REFERENCES `course-activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table course-enrollment
# ------------------------------------------------------------

DROP TABLE IF EXISTS `course-enrollment`;

CREATE TABLE `course-enrollment` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `course-code` varchar(32) DEFAULT NULL,
  `netid` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `course-code` (`course-code`),
  KEY `netid` (`netid`),
  CONSTRAINT `course-enrollment_ibfk_1` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `course-enrollment_ibfk_2` FOREIGN KEY (`netid`) REFERENCES `person` (`netid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table course-instructor
# ------------------------------------------------------------

DROP TABLE IF EXISTS `course-instructor`;

CREATE TABLE `course-instructor` (
  `course-activity` int(11) unsigned DEFAULT NULL,
  `netid` varchar(12) DEFAULT NULL,
  KEY `netid` (`netid`,`course-activity`),
  KEY `course-activity` (`course-activity`),
  CONSTRAINT `course-instructor_ibfk_1` FOREIGN KEY (`course-activity`) REFERENCES `course-activity` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `course-instructor_ibfk_2` FOREIGN KEY (`netid`) REFERENCES `person` (`netid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table program
# ------------------------------------------------------------

DROP TABLE IF EXISTS `program`;

CREATE TABLE `program` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(126) NOT NULL DEFAULT '',
  `description` text,
  `manager` varchar(12) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `manager` (`manager`),
  CONSTRAINT `program_ibfk_1` FOREIGN KEY (`manager`) REFERENCES `person` (`netid`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table program-choice-course
# ------------------------------------------------------------

DROP TABLE IF EXISTS `program-choice-course`;

CREATE TABLE `program-choice-course` (
  `program` int(11) unsigned NOT NULL,
  `course-code` varchar(32) NOT NULL DEFAULT '',
  KEY `program` (`program`),
  KEY `course-code` (`course-code`),
  CONSTRAINT `program-choice-course_ibfk_1` FOREIGN KEY (`program`) REFERENCES `program` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `program-choice-course_ibfk_2` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table program-mandatory-course
# ------------------------------------------------------------

DROP TABLE IF EXISTS `program-mandatory-course`;

CREATE TABLE `program-mandatory-course` (
  `program` int(11) unsigned NOT NULL,
  `course-code` varchar(32) NOT NULL DEFAULT '',
  KEY `program` (`program`),
  KEY `course-code` (`course-code`),
  CONSTRAINT `program-mandatory-course_ibfk_1` FOREIGN KEY (`program`) REFERENCES `program` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `program-mandatory-course_ibfk_2` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table room-facility
# ------------------------------------------------------------

DROP TABLE IF EXISTS `room-facility`;

CREATE TABLE `room-facility` (
  `room` int(11) unsigned NOT NULL,
  `facility` int(11) unsigned NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table subscription
# ------------------------------------------------------------

DROP TABLE IF EXISTS `subscription`;

CREATE TABLE `subscription` (
  `course-code` varchar(32) NOT NULL DEFAULT '',
  `netid` varchar(12) NOT NULL DEFAULT '',
  KEY `course-code` (`course-code`),
  KEY `netid` (`netid`),
  CONSTRAINT `subscription_ibfk_1` FOREIGN KEY (`course-code`) REFERENCES `course` (`course-code`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `subscription_ibfk_2` FOREIGN KEY (`netid`) REFERENCES `person` (`netid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

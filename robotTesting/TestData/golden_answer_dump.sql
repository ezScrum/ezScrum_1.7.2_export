DROP DATABASE IF EXISTS `ezscrum_172`;
CREATE DATABASE IF NOT EXISTS `ezscrum_172`;
USE `ezscrum_172`;

DROP TABLE IF EXISTS `account`;
CREATE TABLE IF NOT EXISTS `account` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(255) NOT NULL,
  `nick_name` varchar(255) DEFAULT NULL,
  `email` text,
  `password` varchar(255) NOT NULL,
  `enable` tinyint(4) NOT NULL DEFAULT '1',
  `create_time` bigint(20) unsigned NOT NULL,
  `update_time` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `account_UNIQUE` (`account`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

DELETE FROM `account`;
INSERT INTO `account` (`ID`, `account`, `nick_name`, `email`, `password`, `enable`, `create_time`, `update_time`) VALUES
	(1, 'admin', 'admin', 'example@ezScrum.tw', '63a9f0ea7bb98050796b649e85481845', 1, 1379910191599, 1379910191599);

DROP TABLE IF EXISTS `ezscrum_story_relation`;
CREATE TABLE IF NOT EXISTS `ezscrum_story_relation` (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `storyID` int(10) unsigned NOT NULL,
  `projectID` int(10) unsigned NOT NULL,
  `releaseID` int(10) DEFAULT NULL,
  `sprintID` int(10) DEFAULT NULL,
  `estimation` int(8) DEFAULT NULL,
  `importance` int(8) DEFAULT NULL,
  `updateDate` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `estimation` (`estimation`,`importance`),
  KEY `updateDate` (`sprintID`,`projectID`,`storyID`,`updateDate`)
) ENGINE=MyISAM AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

DELETE FROM `ezscrum_story_relation`;
INSERT INTO `ezscrum_story_relation` (`id`, `storyID`, `projectID`, `releaseID`, `sprintID`, `estimation`, `importance`, `updateDate`) VALUES
	(1, 1, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:08:15'),
	(2, 1, 1, -1, 1, NULL, NULL, '2015-12-22 16:08:15'),
	(3, 1, 1, 1, 1, NULL, NULL, '2015-12-22 16:08:15'),
	(4, 2, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:08:50'),
	(5, 2, 1, -1, 1, NULL, NULL, '2015-12-22 16:08:50'),
	(6, 2, 1, 1, 1, NULL, NULL, '2015-12-22 16:08:50'),
	(7, 2, 1, 1, 0, NULL, NULL, '2015-12-22 16:20:17'),
	(8, 2, 1, -1, 0, NULL, NULL, '2015-12-22 16:20:17'),
	(9, 7, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:20:43'),
	(10, 8, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:21:00'),
	(11, 9, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:21:20'),
	(12, 10, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:21:41'),
	(13, 11, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:22:22'),
	(14, 11, 1, -1, 1, NULL, NULL, '2015-12-22 16:22:22'),
	(15, 12, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:22:42'),
	(16, 12, 1, -1, 1, NULL, NULL, '2015-12-22 16:22:42'),
	(17, 13, 1, NULL, NULL, NULL, NULL, '2015-12-22 16:23:04'),
	(18, 13, 1, -1, 2, NULL, NULL, '2015-12-22 16:23:04'),
	(19, 13, 1, NULL, 2, 0, NULL, '2015-12-22 16:23:13');


DROP TABLE IF EXISTS `ezscrum_tag_relation`;
CREATE TABLE IF NOT EXISTS `ezscrum_tag_relation` (
  `tag_id` int(10) NOT NULL,
  `story_id` int(10) NOT NULL,
  KEY `tag_id` (`tag_id`),
  KEY `story_id` (`story_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DELETE FROM `ezscrum_tag_relation`;
INSERT INTO `ezscrum_tag_relation` (`tag_id`, `story_id`) VALUES
	(1, 1),
	(2, 2);

DROP TABLE IF EXISTS `ezscrum_tag_table`;
CREATE TABLE IF NOT EXISTS `ezscrum_tag_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `project_id` int(10) NOT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`,`project_id`)
) ENGINE=MyISAM AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

DELETE FROM `ezscrum_tag_table`;
INSERT INTO `ezscrum_tag_table` (`id`, `project_id`, `name`) VALUES
	(1, 1, 'tag01'),
	(2, 1, 'tag02'),
	(3, 1, 'tag03');

DROP TABLE IF EXISTS `mantis_bugnote_table`;
CREATE TABLE IF NOT EXISTS `mantis_bugnote_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `bug_id` int(10) unsigned NOT NULL DEFAULT '0',
  `reporter_id` int(10) unsigned NOT NULL DEFAULT '0',
  `bugnote_text_id` int(10) unsigned NOT NULL DEFAULT '0',
  `view_state` smallint(6) NOT NULL DEFAULT '10',
  `date_submitted` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `last_modified` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `note_type` int(11) DEFAULT '0',
  `note_attr` varchar(250) DEFAULT '',
  `time_tracking` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_bug` (`bug_id`),
  KEY `idx_last_mod` (`last_modified`)
) ENGINE=MyISAM AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_bugnote_table`;
INSERT INTO `mantis_bugnote_table` (`id`, `bug_id`, `reporter_id`, `bugnote_text_id`, `view_state`, `date_submitted`, `last_modified`, `note_type`, `note_attr`, `time_tracking`) VALUES
	(1, 1, 1, 1, 10, '2015-12-22 16:08:15', '2015-12-22 16:08:15', 0, '', 0),
	(2, 2, 1, 2, 10, '2015-12-22 16:08:50', '2015-12-22 16:08:50', 0, '', 0),
	(3, 3, 1, 3, 10, '2015-12-22 16:09:21', '2015-12-22 16:09:21', 0, '', 0),
	(4, 4, 1, 4, 10, '2015-12-22 16:09:34', '2015-12-22 16:09:34', 0, '', 0),
	(5, 5, 1, 5, 10, '2015-12-22 16:09:52', '2015-12-22 16:09:52', 0, '', 0),
	(6, 6, 1, 6, 10, '2015-12-22 16:10:05', '2015-12-22 16:10:05', 0, '', 0),
	(7, 3, 1, 7, 10, '2015-12-22 16:18:28', '2015-12-22 16:18:28', 0, '', 0),
	(8, 4, 1, 8, 10, '2015-12-22 16:18:52', '2015-12-22 16:18:52', 0, '', 0),
	(9, 7, 1, 9, 10, '2015-12-22 16:20:43', '2015-12-22 16:20:43', 0, '', 0),
	(10, 8, 1, 10, 10, '2015-12-22 16:21:00', '2015-12-22 16:21:00', 0, '', 0),
	(11, 9, 1, 11, 10, '2015-12-22 16:21:20', '2015-12-22 16:21:20', 0, '', 0),
	(12, 10, 1, 12, 10, '2015-12-22 16:21:41', '2015-12-22 16:21:41', 0, '', 0),
	(13, 11, 1, 13, 10, '2015-12-22 16:22:22', '2015-12-22 16:22:22', 0, '', 0),
	(14, 12, 1, 14, 10, '2015-12-22 16:22:42', '2015-12-22 16:22:42', 0, '', 0),
	(15, 13, 1, 15, 10, '2015-12-22 16:23:04', '2015-12-22 16:23:04', 0, '', 0);

DROP TABLE IF EXISTS `mantis_bugnote_text_table`;
CREATE TABLE IF NOT EXISTS `mantis_bugnote_text_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `note` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_bugnote_text_table`;
INSERT INTO `mantis_bugnote_text_table` (`id`, `note`) VALUES
	(1, '<JCIS id="20151222160815">\r\n  <Importance>90</Importance>\r\n  <Estimation>8</Estimation>\r\n  <Value>0</Value>\r\n  <HowToDemo>How to demo in Story01</HowToDemo>\r\n  <Notes>notes in Story01</Notes>\r\n</JCIS>\r\n<JCIS id="20151222160815">\r\n  <Iteration>1</Iteration>\r\n</JCIS>\r\n<JCIS id="20151222160815">\r\n  <Release>1</Release>\r\n</JCIS>'),
	(2, '<JCIS id="20151222160850">\r\n  <Importance>80</Importance>\r\n  <Estimation>3</Estimation>\r\n  <Value>0</Value>\r\n  <HowToDemo>how to demo in Story02</HowToDemo>\r\n  <Notes>notes in Story02</Notes>\r\n</JCIS>\r\n<JCIS id="20151222160850">\r\n  <Iteration>1</Iteration>\r\n</JCIS>\r\n<JCIS id="20151222160850">\r\n  <Release>1</Release>\r\n</JCIS>\r\n<JCIS id="20151222162017">\r\n  <Iteration>0</Iteration>\r\n</JCIS>\r\n<JCIS id="20151222162017">\r\n  <Release>0</Release>\r\n</JCIS>'),
	(3, '<JCIS id="20151222000000">\r\n  <Estimation>3</Estimation>\r\n  <Remains>3</Remains>\r\n  <Notes>notes in Task01</Notes>\r\n</JCIS>\r\n<JCIS id="20151222161836">\r\n  <Remains>0</Remains>\r\n</JCIS>'),
	(4, '<JCIS id="20151222000000">\r\n  <Estimation>8</Estimation>\r\n  <Remains>8</Remains>\r\n  <Notes>notes in Task02</Notes>\r\n</JCIS>\r\n<JCIS id="20151222161852">\r\n  <Partners>account3</Partners>\r\n</JCIS>\r\n<JCIS id="20151222000000">\r\n  <Partners />\r\n</JCIS>'),
	(5, '<JCIS id="20151222000000">\r\n  <Estimation>2</Estimation>\r\n  <Remains>2</Remains>\r\n  <Notes>notes in Task03</Notes>\r\n</JCIS>'),
	(6, '<JCIS id="20151222000000">\r\n  <Estimation>13</Estimation>\r\n  <Remains>13</Remains>\r\n  <Notes>notes in Task04</Notes>\r\n</JCIS>'),
	(7, 'notes in Task01'),
	(8, 'notes in Task02'),
	(9, '<JCIS id="20151222162043">\r\n  <Iteration>1</Iteration>\r\n</JCIS>'),
	(10, '<JCIS id="20151222162100">\r\n  <Iteration>1</Iteration>\r\n</JCIS>'),
	(11, '<JCIS id="20151222162120">\r\n  <Iteration>2</Iteration>\r\n</JCIS>'),
	(12, '<JCIS id="20151222162141">\r\n  <Iteration>2</Iteration>\r\n</JCIS>'),
	(13, '<JCIS id="20151222162222">\r\n  <Estimation>3</Estimation>\r\n  <Notes>notes in Unplan01</Notes>\r\n  <Iteration>1</Iteration>\r\n</JCIS>'),
	(14, '<JCIS id="20151222162242">\r\n  <Estimation>5</Estimation>\r\n  <Notes>notes in Unplan02</Notes>\r\n  <Iteration>1</Iteration>\r\n</JCIS>'),
	(15, '<JCIS id="20151222162304">\r\n  <Partners>account3</Partners>\r\n  <Iteration>2</Iteration>\r\n</JCIS>');

DROP TABLE IF EXISTS `mantis_bug_file_table`;
CREATE TABLE IF NOT EXISTS `mantis_bug_file_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `bug_id` int(10) unsigned NOT NULL DEFAULT '0',
  `title` varchar(250) NOT NULL DEFAULT '',
  `description` varchar(250) NOT NULL DEFAULT '',
  `diskfile` varchar(250) NOT NULL DEFAULT '',
  `filename` varchar(250) NOT NULL DEFAULT '',
  `folder` varchar(250) NOT NULL DEFAULT '',
  `filesize` int(11) NOT NULL DEFAULT '0',
  `file_type` varchar(250) NOT NULL DEFAULT '',
  `date_added` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `content` longblob,
  PRIMARY KEY (`id`),
  KEY `idx_bug_file_bug_id` (`bug_id`),
  KEY `idx_diskfile` (`diskfile`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_bug_file_table`;
INSERT INTO `mantis_bug_file_table` (`id`, `bug_id`, `title`, `description`, `diskfile`, `filename`, `folder`, `filesize`, `file_type`, `date_added`, `content`) VALUES
	(1, 1, '', '', 'bf1d7d25ecc14ffe7a633dbe27163fc7', 'Story01.txt', '', 7, 'application/octet-stream', '2015-12-22 16:17:03', _binary 0x53746F72793031),
	(2, 3, '', '', '53383dc83288252adcc93ea54727912d', 'Task01.txt', '', 6, 'application/octet-stream', '2015-12-22 16:17:12', _binary 0x5461736B3031),
	(3, 4, '', '', '0d6fc81383e0ebbb39167ee2ac99361a', 'Task02.txt', '', 6, 'application/octet-stream', '2015-12-22 16:17:21', _binary 0x5461736B3032),
	(4, 2, '', '', '0b8734a13af9fd9d61fb685a8a609848', 'Story02.txt', '', 7, 'application/octet-stream', '2015-12-22 16:17:37', _binary 0x53746F72793032);

DROP TABLE IF EXISTS `mantis_bug_history_table`;
CREATE TABLE IF NOT EXISTS `mantis_bug_history_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int(10) unsigned NOT NULL DEFAULT '0',
  `bug_id` int(10) unsigned NOT NULL DEFAULT '0',
  `date_modified` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `field_name` varchar(64) NOT NULL,
  `old_value` varchar(255) NOT NULL,
  `new_value` varchar(255) NOT NULL,
  `type` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_bug_history_bug_id` (`bug_id`),
  KEY `idx_history_user_id` (`user_id`)
) ENGINE=MyISAM AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_bug_history_table`;
INSERT INTO `mantis_bug_history_table` (`id`, `user_id`, `bug_id`, `date_modified`, `field_name`, `old_value`, `new_value`, `type`) VALUES
	(1, 1, 1, '2015-12-22 16:08:15', 'null', '0', '0', 1),
	(2, 1, 1, '2015-12-22 16:08:15', 'Sprint', '-1', '1', 0),
	(3, 1, 2, '2015-12-22 16:08:50', 'null', '0', '0', 1),
	(4, 1, 2, '2015-12-22 16:08:50', 'Sprint', '-1', '1', 0),
	(5, 1, 3, '2015-12-22 16:09:21', 'null', '0', '0', 1),
	(6, 1, 1, '2015-12-22 00:00:00', 'null', '2', '3', 18),
	(7, 1, 3, '2015-12-22 00:00:00', 'null', '3', '1', 18),
	(8, 1, 4, '2015-12-22 16:09:34', 'null', '0', '0', 1),
	(9, 1, 1, '2015-12-22 00:00:00', 'null', '2', '4', 18),
	(10, 1, 4, '2015-12-22 00:00:00', 'null', '3', '1', 18),
	(11, 1, 5, '2015-12-22 16:09:52', 'null', '0', '0', 1),
	(12, 1, 2, '2015-12-22 00:00:00', 'null', '2', '5', 18),
	(13, 1, 5, '2015-12-22 00:00:00', 'null', '3', '2', 18),
	(14, 1, 6, '2015-12-22 16:10:05', 'null', '0', '0', 1),
	(15, 1, 2, '2015-12-22 00:00:00', 'null', '2', '6', 18),
	(16, 1, 6, '2015-12-22 00:00:00', 'null', '3', '2', 18),
	(17, 1, 3, '2015-12-22 16:18:28', 'handler_id', '0', '4', 0),
	(18, 1, 3, '2015-12-22 16:18:28', 'status', '10', '50', 0),
	(19, 1, 3, '2015-12-22 16:18:28', 'null', '7', '0', 2),
	(20, 1, 3, '2015-12-22 16:18:37', 'status', '50', '90', 0),
	(21, 1, 4, '2015-12-22 16:18:52', 'handler_id', '0', '5', 0),
	(22, 1, 4, '2015-12-22 16:18:52', 'status', '10', '50', 0),
	(23, 1, 4, '2015-12-22 16:18:52', 'null', '8', '0', 2),
	(24, 1, 4, '2015-12-22 16:20:12', 'status', '50', '10', 0),
	(25, 1, 1, '2015-12-22 16:20:12', 'null', '2', '4', 19),
	(26, 1, 4, '2015-12-22 16:20:12', 'null', '3', '1', 19),
	(27, 1, 7, '2015-12-22 16:20:43', 'null', '0', '0', 1),
	(28, 1, 8, '2015-12-22 16:21:00', 'null', '0', '0', 1),
	(29, 1, 9, '2015-12-22 16:21:20', 'null', '0', '0', 1),
	(30, 1, 10, '2015-12-22 16:21:41', 'null', '0', '0', 1),
	(31, 1, 11, '2015-12-22 16:22:22', 'null', '0', '0', 1),
	(32, 1, 11, '2015-12-22 16:22:22', 'handler_id', '0', '4', 0),
	(33, 1, 11, '2015-12-22 16:22:22', 'status', '10', '50', 0),
	(34, 1, 12, '2015-12-22 16:22:42', 'null', '0', '0', 1),
	(35, 1, 13, '2015-12-22 16:23:04', 'null', '0', '0', 1),
	(36, 1, 13, '2015-12-22 16:23:04', 'handler_id', '0', '5', 0),
	(37, 1, 13, '2015-12-22 16:23:04', 'status', '10', '50', 0),
	(38, 1, 13, '2015-12-22 16:23:13', 'status', '50', '90', 0);

DROP TABLE IF EXISTS `mantis_bug_relationship_table`;
CREATE TABLE IF NOT EXISTS `mantis_bug_relationship_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `source_bug_id` int(10) unsigned NOT NULL DEFAULT '0',
  `destination_bug_id` int(10) unsigned NOT NULL DEFAULT '0',
  `relationship_type` smallint(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_relationship_source` (`source_bug_id`),
  KEY `idx_relationship_destination` (`destination_bug_id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_bug_relationship_table`;
INSERT INTO `mantis_bug_relationship_table` (`id`, `source_bug_id`, `destination_bug_id`, `relationship_type`) VALUES
	(1, 1, 3, 2),
	(3, 2, 5, 2),
	(4, 2, 6, 2);

DROP TABLE IF EXISTS `mantis_bug_table`;
CREATE TABLE IF NOT EXISTS `mantis_bug_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `project_id` int(10) unsigned NOT NULL DEFAULT '0',
  `reporter_id` int(10) unsigned NOT NULL DEFAULT '0',
  `handler_id` int(10) unsigned NOT NULL DEFAULT '0',
  `duplicate_id` int(10) unsigned NOT NULL DEFAULT '0',
  `priority` smallint(6) NOT NULL DEFAULT '30',
  `severity` smallint(6) NOT NULL DEFAULT '50',
  `reproducibility` smallint(6) NOT NULL DEFAULT '10',
  `status` smallint(6) NOT NULL DEFAULT '10',
  `resolution` smallint(6) NOT NULL DEFAULT '10',
  `projection` smallint(6) NOT NULL DEFAULT '10',
  `category` varchar(64) NOT NULL DEFAULT '',
  `date_submitted` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `last_updated` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `eta` smallint(6) NOT NULL DEFAULT '10',
  `bug_text_id` int(10) unsigned NOT NULL DEFAULT '0',
  `os` varchar(32) NOT NULL DEFAULT '',
  `os_build` varchar(32) NOT NULL DEFAULT '',
  `platform` varchar(32) NOT NULL DEFAULT '',
  `version` varchar(64) NOT NULL DEFAULT '',
  `fixed_in_version` varchar(64) NOT NULL DEFAULT '',
  `build` varchar(32) NOT NULL DEFAULT '',
  `profile_id` int(10) unsigned NOT NULL DEFAULT '0',
  `view_state` smallint(6) NOT NULL DEFAULT '10',
  `summary` varchar(128) NOT NULL DEFAULT '',
  `sponsorship_total` int(11) NOT NULL DEFAULT '0',
  `sticky` tinyint(4) NOT NULL DEFAULT '0',
  `target_version` varchar(64) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  KEY `idx_bug_sponsorship_total` (`sponsorship_total`),
  KEY `idx_bug_fixed_in_version` (`fixed_in_version`),
  KEY `idx_bug_status` (`status`),
  KEY `idx_project` (`project_id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_bug_table`;
INSERT INTO `mantis_bug_table` (`id`, `project_id`, `reporter_id`, `handler_id`, `duplicate_id`, `priority`, `severity`, `reproducibility`, `status`, `resolution`, `projection`, `category`, `date_submitted`, `last_updated`, `eta`, `bug_text_id`, `os`, `os_build`, `platform`, `version`, `fixed_in_version`, `build`, `profile_id`, `view_state`, `summary`, `sponsorship_total`, `sticky`, `target_version`) VALUES
	(1, 1, 1, 0, 0, 30, 50, 10, 10, 10, 10, 'Story', '2015-12-22 16:08:15', '2015-12-22 16:08:15', 10, 1, '', '', '', '', '', '', 0, 10, 'Story01', 0, 0, ''),
	(2, 1, 1, 0, 0, 30, 50, 10, 10, 10, 10, 'Story', '2015-12-22 16:08:50', '2015-12-22 16:08:50', 10, 2, '', '', '', '', '', '', 0, 10, 'Story02', 0, 0, ''),
	(3, 1, 1, 4, 0, 30, 50, 10, 90, 20, 10, 'Task', '2015-12-22 16:09:21', '2015-12-22 16:09:21', 10, 3, '', '', '', '', '', '', 0, 10, 'Task01', 0, 0, ''),
	(4, 1, 1, 0, 0, 30, 50, 10, 10, 10, 10, 'Task', '2015-12-22 16:09:34', '2015-12-22 16:09:34', 10, 4, '', '', '', '', '', '', 0, 10, 'Task02', 0, 0, ''),
	(5, 1, 1, 0, 0, 30, 50, 10, 10, 10, 10, 'Task', '2015-12-22 16:09:52', '2015-12-22 16:09:52', 10, 5, '', '', '', '', '', '', 0, 10, 'Task03', 0, 0, ''),
	(6, 1, 1, 0, 0, 30, 50, 10, 10, 10, 10, 'Task', '2015-12-22 16:10:05', '2015-12-22 16:10:05', 10, 6, '', '', '', '', '', '', 0, 10, 'Task04', 0, 0, ''),
	(7, 1, 1, 0, 0, 30, 50, 10, 10, 10, 10, 'Good', '2015-12-22 16:20:43', '2015-12-22 16:20:43', 10, 7, '', '', '', '', '', '', 0, 10, 'Good01', 0, 0, ''),
	(8, 1, 1, 0, 0, 30, 50, 10, 90, 10, 10, 'Improvement', '2015-12-22 16:21:00', '2015-12-22 16:21:00', 10, 8, '', '', '', '', '', '', 0, 10, 'Improvment01', 0, 0, ''),
	(9, 1, 1, 0, 0, 30, 50, 10, 80, 10, 10, 'Good', '2015-12-22 16:21:20', '2015-12-22 16:21:20', 10, 9, '', '', '', '', '', '', 0, 10, 'Good02', 0, 0, ''),
	(10, 1, 1, 0, 0, 30, 50, 10, 50, 10, 10, 'Improvement', '2015-12-22 16:21:41', '2015-12-22 16:21:41', 10, 10, '', '', '', '', '', '', 0, 10, 'Improvment02', 0, 0, ''),
	(11, 1, 1, 4, 0, 30, 50, 10, 50, 10, 10, 'UnplannedItem', '2015-12-22 16:22:22', '2015-12-22 16:22:22', 10, 11, '', '', '', '', '', '', 0, 10, 'Unplan01', 0, 0, ''),
	(12, 1, 1, 0, 0, 30, 50, 10, 10, 10, 10, 'UnplannedItem', '2015-12-22 16:22:42', '2015-12-22 16:22:42', 10, 12, '', '', '', '', '', '', 0, 10, 'Unplan02', 0, 0, ''),
	(13, 1, 1, 5, 0, 30, 50, 10, 90, 20, 10, 'UnplannedItem', '2015-12-22 16:23:04', '2015-12-22 16:23:04', 10, 13, '', '', '', '', '', '', 0, 10, 'Unplan03', 0, 0, '');

DROP TABLE IF EXISTS `mantis_bug_text_table`;
CREATE TABLE IF NOT EXISTS `mantis_bug_text_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `description` longtext NOT NULL,
  `steps_to_reproduce` longtext NOT NULL,
  `additional_information` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_bug_text_table`;
INSERT INTO `mantis_bug_text_table` (`id`, `description`, `steps_to_reproduce`, `additional_information`) VALUES
	(1, '', '', ''),
	(2, '', '', ''),
	(3, 'notes in Task01', '', ''),
	(4, 'notes in Task02', '', ''),
	(5, 'notes in Task03', '', ''),
	(6, 'notes in Task04', '', ''),
	(7, 'Description', '', ''),
	(8, 'Description', '', ''),
	(9, 'Description', '', ''),
	(10, 'Description', '', ''),
	(11, '', '', ''),
	(12, '', '', ''),
	(13, '', '', '');

DROP TABLE IF EXISTS `mantis_config_table`;
CREATE TABLE IF NOT EXISTS `mantis_config_table` (
  `config_id` varchar(64) NOT NULL,
  `project_id` int(11) NOT NULL DEFAULT '0',
  `user_id` int(11) NOT NULL DEFAULT '0',
  `access_reqd` int(11) DEFAULT '0',
  `type` int(11) DEFAULT '90',
  `value` longtext NOT NULL,
  PRIMARY KEY (`config_id`,`project_id`,`user_id`),
  KEY `idx_config` (`config_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DELETE FROM `mantis_config_table`;
INSERT INTO `mantis_config_table` (`config_id`, `project_id`, `user_id`, `access_reqd`, `type`, `value`) VALUES
	('database_version', 0, 0, 90, 1, '63');

DROP TABLE IF EXISTS `mantis_project_table`;
CREATE TABLE IF NOT EXISTS `mantis_project_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL DEFAULT '',
  `status` smallint(6) NOT NULL DEFAULT '10',
  `enabled` tinyint(4) NOT NULL DEFAULT '1',
  `view_state` smallint(6) NOT NULL DEFAULT '10',
  `access_min` smallint(6) NOT NULL DEFAULT '10',
  `file_path` varchar(250) NOT NULL DEFAULT '',
  `description` longtext NOT NULL,
  `baseLine_velocity` int(10) NOT NULL DEFAULT '50',
  `baseLine_cost_per_storyPoint` int(10) NOT NULL DEFAULT '3',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_project_name` (`name`),
  KEY `idx_project_id` (`id`),
  KEY `idx_project_view` (`view_state`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_project_table`;
INSERT INTO `mantis_project_table` (`id`, `name`, `status`, `enabled`, `view_state`, `access_min`, `file_path`, `description`, `baseLine_velocity`, `baseLine_cost_per_storyPoint`) VALUES
	(1, 'Project01', 10, 1, 50, 10, '', '', 50, 3),
	(2, 'Project02', 10, 1, 50, 10, '', '', 50, 3);

DROP TABLE IF EXISTS `mantis_project_user_list_table`;
CREATE TABLE IF NOT EXISTS `mantis_project_user_list_table` (
  `project_id` int(10) unsigned NOT NULL DEFAULT '0',
  `user_id` int(10) unsigned NOT NULL DEFAULT '0',
  `access_level` smallint(6) NOT NULL DEFAULT '10',
  PRIMARY KEY (`project_id`,`user_id`),
  KEY `idx_project_user` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DELETE FROM `mantis_project_user_list_table`;
INSERT INTO `mantis_project_user_list_table` (`project_id`, `user_id`, `access_level`) VALUES
	(1, 2, 70),
	(1, 3, 70),
	(2, 3, 70),
	(1, 4, 70),
	(2, 4, 70),
	(1, 5, 70),
	(2, 5, 70),
	(2, 6, 70);

DROP TABLE IF EXISTS `mantis_tokens_table`;
CREATE TABLE IF NOT EXISTS `mantis_tokens_table` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `owner` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `timestamp` datetime NOT NULL,
  `expiry` datetime DEFAULT NULL,
  `value` longtext NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_typeowner` (`type`,`owner`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_tokens_table`;
INSERT INTO `mantis_tokens_table` (`id`, `owner`, `type`, `timestamp`, `expiry`, `value`) VALUES
	(1, 1, 4, '2010-01-19 05:41:31', '2010-01-19 05:46:31', '1');

DROP TABLE IF EXISTS `mantis_user_table`;
CREATE TABLE IF NOT EXISTS `mantis_user_table` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(32) NOT NULL DEFAULT '',
  `realname` varchar(64) NOT NULL DEFAULT '',
  `email` varchar(64) NOT NULL DEFAULT '',
  `password` varchar(32) NOT NULL DEFAULT '',
  `date_created` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `last_visit` datetime NOT NULL DEFAULT '1970-01-01 00:00:01',
  `enabled` tinyint(4) NOT NULL DEFAULT '1',
  `protected` tinyint(4) NOT NULL DEFAULT '0',
  `access_level` smallint(6) NOT NULL DEFAULT '10',
  `login_count` int(11) NOT NULL DEFAULT '0',
  `lost_password_request_count` smallint(6) NOT NULL DEFAULT '0',
  `failed_login_count` smallint(6) NOT NULL DEFAULT '0',
  `cookie_string` varchar(64) NOT NULL DEFAULT '',
  `Baseline_Velocity` int(11) NOT NULL DEFAULT '50',
  `Baseline_Cost_Per_StoryPoint` int(11) NOT NULL DEFAULT '50',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_cookie_string` (`cookie_string`),
  UNIQUE KEY `idx_user_username` (`username`),
  KEY `idx_enable` (`enabled`),
  KEY `idx_access` (`access_level`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

DELETE FROM `mantis_user_table`;
INSERT INTO `mantis_user_table` (`id`, `username`, `realname`, `email`, `password`, `date_created`, `last_visit`, `enabled`, `protected`, `access_level`, `login_count`, `lost_password_request_count`, `failed_login_count`, `cookie_string`, `Baseline_Velocity`, `Baseline_Cost_Per_StoryPoint`) VALUES
	(1, 'admin', '', 'root@localhost', '63a9f0ea7bb98050796b649e85481845', '2010-01-19 05:41:23', '2010-01-19 05:41:35', 1, 0, 90, 4, 0, 0, 'b9bec1c98360692f7ae7baecd9736deaa511ea87cfda0be2ddac035e208e1069', 50, 50),
	(2, 'account1', 'account1', 'account1@gmail.com', '809d7aea9eacf339b2e35e3c8ae0a57c', '2015-12-22 16:15:31', '2015-12-22 16:15:31', 1, 0, 25, 0, 0, 0, '107af4d833d57de65c717aa74d7e23ddd98ea88b0998cbfb425791392a83a7d4', 50, 50),
	(3, 'account2', 'account2', 'account2@gmail.com', '93189e2c4c7b1a2c7b16a24d5daa98a9', '2015-12-22 16:15:44', '2015-12-22 16:15:44', 1, 0, 25, 0, 0, 0, 'c1b8da014d9f0c329dd92e0907bdf4a85aa7dda7b9a5a5d6e84b483581ea674a', 50, 50),
	(4, 'account3', 'account3', 'account3@gmail.com', 'fbcd0ff0529a3dd9b733884b30941297', '2015-12-22 16:16:03', '2015-12-22 16:16:03', 1, 0, 25, 0, 0, 0, '3d4d0f4620655539c1a9936d2f22067416e35ca1837f6ff0543f10b3be38ace5', 50, 50),
	(5, 'account4', 'account4', 'account4@gmail.com', '201e9991afe90c65e13b08b53fb695de', '2015-12-22 16:16:19', '2015-12-22 16:16:19', 1, 0, 25, 0, 0, 0, 'aa436a729989b1179fa0601de4db8318a39cd1e43ec54c22a4a66e5cf8248188', 50, 50),
	(6, 'account5', 'account5', 'account5@gmail.com', '7ea4950f63c983360c863bd5d1608944', '2015-12-22 16:16:34', '2015-12-22 16:16:34', 1, 0, 25, 0, 0, 0, 'abd6c0f2b8861ba44a8316f0e43f208e0ea4df1bf04be48fb495924e874bc326', 50, 50);
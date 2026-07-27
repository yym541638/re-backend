-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: ai_shenji_v2
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `soc_control_test`
--

DROP TABLE IF EXISTS `soc_control_test`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_control_test` (
  `test_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `risk_level` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `risk_description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `coso_principle` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `control_procedure` text COLLATE utf8mb4_general_ci,
  `result_status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `current_version` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'V1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`test_id`),
  KEY `idx_control_test_project` (`project_id`),
  CONSTRAINT `fk_control_test_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='控制测试表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_control_test_version`
--

DROP TABLE IF EXISTS `soc_control_test_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_control_test_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `test_id` bigint unsigned NOT NULL,
  `version_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `snapshot_json` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `change_summary` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_control_test_version_test` (`test_id`),
  CONSTRAINT `fk_control_test_version_test` FOREIGN KEY (`test_id`) REFERENCES `soc_control_test` (`test_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='控制测试版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_gap_analysis`
--

DROP TABLE IF EXISTS `soc_gap_analysis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_gap_analysis` (
  `gap_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `source_test_id` bigint unsigned DEFAULT NULL,
  `control_title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `gap_level` varchar(40) COLLATE utf8mb4_general_ci NOT NULL,
  `status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'OPEN',
  `gap_description` text COLLATE utf8mb4_general_ci,
  `remediation_suggestion` text COLLATE utf8mb4_general_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`gap_id`),
  KEY `idx_gap_analysis_project` (`project_id`),
  CONSTRAINT `fk_gap_analysis_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='差距分析表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_operation_log`
--

DROP TABLE IF EXISTS `soc_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_operation_log` (
  `log_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `username` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `module_name` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `action_type` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `resource_type` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `resource_id` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `resource_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `project_id` bigint unsigned DEFAULT NULL,
  `action_detail` text COLLATE utf8mb4_general_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_operation_log_project` (`project_id`),
  KEY `idx_operation_log_module` (`module_name`)
) ENGINE=InnoDB AUTO_INCREMENT=548 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_project`
--

DROP TABLE IF EXISTS `soc_project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_project` (
  `project_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `company_id` int unsigned NOT NULL,
  `project_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL,
  `project_info` text COLLATE utf8mb4_general_ci COMMENT '项目描述',
  `start_date` datetime DEFAULT NULL COMMENT '项目开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '项目结束时间',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`project_id`),
  KEY `idx_project_company` (`company_id`),
  KEY `fk_project_created_by` (`created_by`),
  KEY `fk_project_updated_by` (`updated_by`),
  CONSTRAINT `fk_project_company` FOREIGN KEY (`company_id`) REFERENCES `sys_company` (`company_id`),
  CONSTRAINT `fk_project_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`user_id`),
  CONSTRAINT `fk_project_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_project_member`
--

DROP TABLE IF EXISTS `soc_project_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_project_member` (
  `member_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `user_id` int unsigned DEFAULT NULL,
  `member_role` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `display_name` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `email` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  KEY `idx_project_member_project` (`project_id`),
  CONSTRAINT `fk_project_member_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_rcm`
--

DROP TABLE IF EXISTS `soc_rcm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_rcm` (
  `rcm_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `control_code` varchar(80) COLLATE utf8mb4_general_ci NOT NULL,
  `control_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `description` text COLLATE utf8mb4_general_ci,
  `category` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `module_name` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `risk_description` text COLLATE utf8mb4_general_ci,
  `status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `stage` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MANUAL',
  `ai_generated` tinyint NOT NULL DEFAULT '0',
  `source_request_id` bigint unsigned DEFAULT NULL,
  `source_rcm_id` bigint unsigned DEFAULT NULL,
  `control_objective` text COLLATE utf8mb4_general_ci,
  `implementation_method` text COLLATE utf8mb4_general_ci,
  `evidence_requirement` text COLLATE utf8mb4_general_ci,
  `control_performer` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `control_reviewer` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `additional_owner` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `control_risk_rating` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `current_version` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'V1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`rcm_id`),
  KEY `idx_rcm_project` (`project_id`),
  KEY `idx_rcm_status` (`status`),
  KEY `idx_rcm_stage` (`stage`),
  KEY `idx_rcm_source_request` (`source_request_id`),
  KEY `idx_rcm_code` (`control_code`),
  CONSTRAINT `fk_rcm_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=255 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='RCM 主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_rcm_version`
--

DROP TABLE IF EXISTS `soc_rcm_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_rcm_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `rcm_id` bigint unsigned NOT NULL,
  `version_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `snapshot_json` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `change_summary` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_rcm_version_rcm` (`rcm_id`),
  CONSTRAINT `fk_rcm_version_rcm` FOREIGN KEY (`rcm_id`) REFERENCES `soc_rcm` (`rcm_id`)
) ENGINE=InnoDB AUTO_INCREMENT=255 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='RCM 版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_report_task`
--

DROP TABLE IF EXISTS `soc_report_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_report_task` (
  `task_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `report_type` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `format` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `include_sections_json` text COLLATE utf8mb4_general_ci,
  `language` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `progress` int NOT NULL DEFAULT '0',
  `file_path` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `error_message` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`),
  KEY `idx_report_task_project` (`project_id`),
  CONSTRAINT `fk_report_task_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_request`
--

DROP TABLE IF EXISTS `soc_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_request` (
  `request_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `request_master_id` bigint unsigned DEFAULT NULL,
  `catalog_id` bigint unsigned DEFAULT NULL,
  `request_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `cc_criteria` varchar(80) COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `request_description` text COLLATE utf8mb4_general_ci,
  `points_of_focus` text COLLATE utf8mb4_general_ci,
  `document_status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `evidence_manual_status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `document_owner` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `request_assignee` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `document_owner_user_id` int unsigned DEFAULT NULL,
  `implementation_date` date DEFAULT NULL,
  `last_update_at` datetime DEFAULT NULL,
  `request_send_date` datetime DEFAULT NULL,
  `ai_review_status` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `ai_review_comment` text COLLATE utf8mb4_general_ci,
  `user_comment` text COLLATE utf8mb4_general_ci,
  `notes` text COLLATE utf8mb4_general_ci,
  `requestor` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `comments` text COLLATE utf8mb4_general_ci,
  `current_version` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'V1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uk_request_code` (`request_code`),
  KEY `idx_request_project` (`project_id`),
  KEY `idx_request_status` (`document_status`),
  KEY `idx_request_master` (`request_master_id`),
  KEY `idx_request_master_catalog` (`request_master_id`,`catalog_id`),
  CONSTRAINT `fk_request_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=270 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目请求表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_request_attachment`
--

DROP TABLE IF EXISTS `soc_request_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_request_attachment` (
  `attachment_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_id` bigint unsigned NOT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  `file_type` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `content_type` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `file_size` bigint NOT NULL DEFAULT '0',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`attachment_id`),
  KEY `idx_request_attachment_request` (`request_id`),
  CONSTRAINT `fk_request_attachment_request` FOREIGN KEY (`request_id`) REFERENCES `soc_request` (`request_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目请求附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_request_criteria_catalog`
--

DROP TABLE IF EXISTS `soc_request_criteria_catalog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_request_criteria_catalog` (
  `catalog_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `criteria_code` varchar(40) NOT NULL,
  `module_name` varchar(64) NOT NULL,
  `requirement` text,
  `points_of_focus` text,
  `document_description` text,
  `sort_order` int NOT NULL DEFAULT '0',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`catalog_id`),
  KEY `idx_catalog_module_sort` (`module_name`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=243 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_request_master`
--

DROP TABLE IF EXISTS `soc_request_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_request_master` (
  `request_master_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `request_master_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
  `request_master_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL,
  `status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'INACTIVE',
  `current_version_id` bigint unsigned DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_master_id`),
  UNIQUE KEY `uk_request_master_code` (`request_master_code`),
  KEY `idx_request_master_project` (`project_id`),
  KEY `idx_request_master_status` (`status`),
  CONSTRAINT `fk_request_master_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Request Master 表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_request_master_template_file`
--

DROP TABLE IF EXISTS `soc_request_master_template_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_request_master_template_file` (
  `template_file_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_master_id` bigint unsigned NOT NULL,
  `file_no` int NOT NULL DEFAULT '1',
  `file_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_general_ci NOT NULL,
  `relevant_criteria` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_file_id`),
  KEY `idx_rm_template_master` (`request_master_id`),
  CONSTRAINT `fk_rm_template_master` FOREIGN KEY (`request_master_id`) REFERENCES `soc_request_master` (`request_master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Request Master 模板文件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_request_master_version`
--

DROP TABLE IF EXISTS `soc_request_master_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_request_master_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_master_id` bigint unsigned NOT NULL,
  `version_label` varchar(40) COLLATE utf8mb4_general_ci NOT NULL,
  `snapshot_json` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `is_latest` tinyint NOT NULL DEFAULT '1',
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_rm_version_master` (`request_master_id`),
  CONSTRAINT `fk_rm_version_master` FOREIGN KEY (`request_master_id`) REFERENCES `soc_request_master` (`request_master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Request Master 版本快照';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_request_version`
--

DROP TABLE IF EXISTS `soc_request_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_request_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_id` bigint unsigned NOT NULL,
  `version_no` varchar(20) COLLATE utf8mb4_general_ci NOT NULL,
  `snapshot_json` longtext COLLATE utf8mb4_general_ci NOT NULL,
  `change_summary` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_request_version_request` (`request_id`),
  CONSTRAINT `fk_request_version_request` FOREIGN KEY (`request_id`) REFERENCES `soc_request` (`request_id`)
) ENGINE=InnoDB AUTO_INCREMENT=270 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目请求版本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `soc_score_snapshot`
--

DROP TABLE IF EXISTS `soc_score_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `soc_score_snapshot` (
  `snapshot_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `snapshot_date` date NOT NULL,
  `total_count` bigint NOT NULL DEFAULT '0',
  `passed_count` bigint NOT NULL DEFAULT '0',
  `failed_count` bigint NOT NULL DEFAULT '0',
  `pending_count` bigint NOT NULL DEFAULT '0',
  `gap_count` bigint NOT NULL DEFAULT '0',
  `pass_rate` decimal(8,2) NOT NULL DEFAULT '0.00',
  `assessment` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`snapshot_id`),
  KEY `idx_score_snapshot_project` (`project_id`),
  KEY `idx_score_snapshot_date` (`snapshot_date`),
  CONSTRAINT `fk_score_snapshot_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评分快照表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_company`
--

DROP TABLE IF EXISTS `sys_company`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_company` (
  `company_id` int unsigned NOT NULL AUTO_INCREMENT,
  `company_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL,
  `company_code` varchar(60) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `industry` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `website` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `contact_name` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `contact_phone` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`company_id`),
  UNIQUE KEY `uk_company_name` (`company_name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='企业表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_invitation_code`
--

DROP TABLE IF EXISTS `sys_invitation_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_invitation_code` (
  `invitation_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `invitation_type` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PROJECT',
  `company_id` int unsigned NOT NULL,
  `project_id` bigint unsigned DEFAULT NULL,
  `member_role` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE',
  `max_uses` int NOT NULL DEFAULT '1',
  `used_count` int NOT NULL DEFAULT '0',
  `expires_at` datetime DEFAULT NULL,
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `used_by` int unsigned DEFAULT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`invitation_id`),
  UNIQUE KEY `uk_invitation_code` (`code`),
  KEY `idx_invitation_project` (`project_id`),
  KEY `idx_invitation_status` (`status`),
  KEY `fk_invitation_company` (`company_id`),
  CONSTRAINT `fk_invitation_company` FOREIGN KEY (`company_id`) REFERENCES `sys_company` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='邀请码表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_order`
--

DROP TABLE IF EXISTS `sys_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_order` (
  `order_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` int unsigned NOT NULL,
  `product_id` int unsigned NOT NULL,
  `package_id` int unsigned NOT NULL,
  `product_name` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `package_name` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `audit_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `amount` int NOT NULL,
  `payment_method` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING',
  `transaction_id` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `return_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `notify_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_product`
--

DROP TABLE IF EXISTS `sys_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_product` (
  `product_id` int unsigned NOT NULL AUTO_INCREMENT,
  `product_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL,
  `product_code` varchar(60) COLLATE utf8mb4_general_ci NOT NULL,
  `introduction_title` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `introduction_text` text COLLATE utf8mb4_general_ci,
  `logo_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `trust_principles` text COLLATE utf8mb4_general_ci,
  `all_features` text COLLATE utf8mb4_general_ci,
  `status` tinyint NOT NULL DEFAULT '1',
  `sort_no` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_product_code` (`product_code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_product_package`
--

DROP TABLE IF EXISTS `sys_product_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_product_package` (
  `package_id` int unsigned NOT NULL AUTO_INCREMENT,
  `product_id` int unsigned NOT NULL,
  `package_name` varchar(120) COLLATE utf8mb4_general_ci NOT NULL,
  `annual_price` int NOT NULL,
  `type1_price` int NOT NULL,
  `type2_price` int NOT NULL,
  `included_features` text COLLATE utf8mb4_general_ci,
  `supported_types` text COLLATE utf8mb4_general_ci,
  `default_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT 'Type1',
  `status` tinyint NOT NULL DEFAULT '1',
  `sort_no` int NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`package_id`),
  KEY `idx_product_package_product` (`product_id`),
  CONSTRAINT `fk_product_package_product` FOREIGN KEY (`product_id`) REFERENCES `sys_product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品套餐表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `user_id` int unsigned NOT NULL AUTO_INCREMENT,
  `company_id` int unsigned NOT NULL,
  `display_name` varchar(80) COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(120) COLLATE utf8mb4_general_ci NOT NULL,
  `phone` varchar(40) COLLATE utf8mb4_general_ci NOT NULL,
  `avatar_url` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `job_title` varchar(80) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `user_type` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'CLIENT',
  `password_hash` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `role_code` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'USER',
  `status` tinyint NOT NULL DEFAULT '1',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_email` (`email`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  KEY `idx_user_company` (`company_id`),
  CONSTRAINT `fk_user_company` FOREIGN KEY (`company_id`) REFERENCES `sys_company` (`company_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sys_user_product`
--

DROP TABLE IF EXISTS `sys_user_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_product` (
  `user_product_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `product_id` int unsigned NOT NULL,
  `product_name` varchar(120) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `package_id` int unsigned DEFAULT NULL,
  `included_features` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `audit_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `source_order_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL,
  `status` varchar(40) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'ACTIVE',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_product_id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`,`audit_type`),
  KEY `idx_user_product_user` (`user_id`),
  CONSTRAINT `fk_user_product_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户已购产品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'ai_shenji_v2'
--

--
-- Dumping routines for database 'ai_shenji_v2'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-27 23:59:22

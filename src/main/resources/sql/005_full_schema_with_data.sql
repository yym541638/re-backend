CREATE DATABASE IF NOT EXISTS `ai_shenji_v2`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `ai_shenji_v2`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `soc_rcm_version`;
DROP TABLE IF EXISTS `soc_rcm`;
DROP TABLE IF EXISTS `soc_report_task`;
DROP TABLE IF EXISTS `sys_invitation_code`;
DROP TABLE IF EXISTS `sys_user_product`;
DROP TABLE IF EXISTS `sys_order`;
DROP TABLE IF EXISTS `sys_product_package`;
DROP TABLE IF EXISTS `sys_product`;
DROP TABLE IF EXISTS `soc_score_snapshot`;
DROP TABLE IF EXISTS `soc_gap_analysis`;
DROP TABLE IF EXISTS `soc_control_test_version`;
DROP TABLE IF EXISTS `soc_control_test`;
DROP TABLE IF EXISTS `soc_operation_log`;
DROP TABLE IF EXISTS `soc_request_attachment`;
DROP TABLE IF EXISTS `soc_request_version`;
DROP TABLE IF EXISTS `soc_request`;
DROP TABLE IF EXISTS `soc_project_member`;
DROP TABLE IF EXISTS `soc_project`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_company`;

CREATE TABLE `sys_company` (
  `company_id` int unsigned NOT NULL AUTO_INCREMENT,
  `company_name` varchar(120) NOT NULL,
  `company_code` varchar(60) DEFAULT NULL,
  `industry` varchar(80) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `contact_name` varchar(80) DEFAULT NULL,
  `contact_phone` varchar(40) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`company_id`),
  UNIQUE KEY `uk_company_name` (`company_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `sys_user` (
  `user_id` int unsigned NOT NULL AUTO_INCREMENT,
  `company_id` int unsigned NOT NULL,
  `display_name` varchar(80) NOT NULL,
  `email` varchar(120) NOT NULL,
  `phone` varchar(40) NOT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `job_title` varchar(80) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role_code` varchar(40) NOT NULL DEFAULT 'USER',
  `status` tinyint NOT NULL DEFAULT 1,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_email` (`email`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  KEY `idx_user_company` (`company_id`),
  CONSTRAINT `fk_user_company` FOREIGN KEY (`company_id`) REFERENCES `sys_company` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `sys_product` (
  `product_id` int unsigned NOT NULL AUTO_INCREMENT,
  `product_name` varchar(120) NOT NULL,
  `product_code` varchar(60) NOT NULL,
  `introduction_title` varchar(200) DEFAULT NULL,
  `introduction_text` text,
  `logo_url` varchar(500) DEFAULT NULL,
  `trust_principles` text,
  `all_features` text,
  `status` tinyint NOT NULL DEFAULT 1,
  `sort_no` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `sys_product_package` (
  `package_id` int unsigned NOT NULL AUTO_INCREMENT,
  `product_id` int unsigned NOT NULL,
  `package_name` varchar(120) NOT NULL,
  `annual_price` int NOT NULL,
  `type1_price` int NOT NULL,
  `type2_price` int NOT NULL,
  `included_features` text,
  `supported_types` text,
  `default_type` varchar(30) DEFAULT 'Type1',
  `status` tinyint NOT NULL DEFAULT 1,
  `sort_no` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`package_id`),
  KEY `idx_product_package_product` (`product_id`),
  CONSTRAINT `fk_product_package_product` FOREIGN KEY (`product_id`) REFERENCES `sys_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `sys_order` (
  `order_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) NOT NULL,
  `user_id` int unsigned NOT NULL,
  `product_id` int unsigned NOT NULL,
  `package_id` int unsigned NOT NULL,
  `product_name` varchar(120) DEFAULT NULL,
  `package_name` varchar(120) DEFAULT NULL,
  `audit_type` varchar(30) DEFAULT NULL,
  `amount` int NOT NULL,
  `payment_method` varchar(40) DEFAULT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'PENDING',
  `transaction_id` varchar(120) DEFAULT NULL,
  `return_url` varchar(500) DEFAULT NULL,
  `notify_url` varchar(500) DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `sys_user_product` (
  `user_product_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `product_id` int unsigned NOT NULL,
  `product_name` varchar(120) DEFAULT NULL,
  `package_id` int unsigned DEFAULT NULL,
  `package_name` varchar(120) DEFAULT NULL,
  `audit_type` varchar(30) DEFAULT NULL,
  `source_order_no` varchar(64) NOT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'ACTIVE',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_product_id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`,`audit_type`),
  KEY `idx_user_product_user` (`user_id`),
  CONSTRAINT `fk_user_product_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `sys_invitation_code` (
  `invitation_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `invitation_type` varchar(40) NOT NULL DEFAULT 'PROJECT',
  `company_id` int unsigned NOT NULL,
  `project_id` bigint unsigned DEFAULT NULL,
  `member_role` varchar(50) DEFAULT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'ACTIVE',
  `max_uses` int NOT NULL DEFAULT 1,
  `used_count` int NOT NULL DEFAULT 0,
  `expires_at` datetime DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `used_by` int unsigned DEFAULT NULL,
  `used_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`invitation_id`),
  UNIQUE KEY `uk_invitation_code` (`code`),
  KEY `idx_invitation_project` (`project_id`),
  KEY `idx_invitation_status` (`status`),
  CONSTRAINT `fk_invitation_company` FOREIGN KEY (`company_id`) REFERENCES `sys_company` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_project` (
  `project_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `company_id` int unsigned NOT NULL,
  `project_code` varchar(50) NOT NULL,
  `project_name` varchar(120) NOT NULL,
  `compliance_type` varchar(40) NOT NULL,
  `audit_type` varchar(40) NOT NULL,
  `current_version` varchar(20) NOT NULL DEFAULT 'V1',
  `gap_count` int NOT NULL DEFAULT 0,
  `status` varchar(40) NOT NULL DEFAULT 'IN_PROGRESS',
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`project_id`),
  UNIQUE KEY `uk_project_code` (`project_code`),
  KEY `idx_project_company` (`company_id`),
  KEY `idx_project_status` (`status`),
  CONSTRAINT `fk_project_company` FOREIGN KEY (`company_id`) REFERENCES `sys_company` (`company_id`),
  CONSTRAINT `fk_project_created_by` FOREIGN KEY (`created_by`) REFERENCES `sys_user` (`user_id`),
  CONSTRAINT `fk_project_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `sys_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_project_member` (
  `member_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `user_id` int unsigned DEFAULT NULL,
  `member_role` varchar(50) NOT NULL,
  `display_name` varchar(80) DEFAULT NULL,
  `email` varchar(120) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  KEY `idx_project_member_project` (`project_id`),
  CONSTRAINT `fk_project_member_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_request` (
  `request_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `request_code` varchar(50) NOT NULL,
  `cc_criteria` varchar(80) NOT NULL,
  `title` varchar(200) NOT NULL,
  `request_description` text,
  `points_of_focus` text,
  `document_status` varchar(40) NOT NULL DEFAULT 'PENDING',
  `document_owner` varchar(120) DEFAULT NULL,
  `implementation_date` date DEFAULT NULL,
  `last_update_at` datetime DEFAULT NULL,
  `notes` text,
  `requestor` varchar(120) DEFAULT NULL,
  `comments` text,
  `current_version` varchar(20) NOT NULL DEFAULT 'V1',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_id`),
  UNIQUE KEY `uk_request_code` (`request_code`),
  KEY `idx_request_project` (`project_id`),
  KEY `idx_request_status` (`document_status`),
  CONSTRAINT `fk_request_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_request_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_id` bigint unsigned NOT NULL,
  `version_no` varchar(20) NOT NULL,
  `snapshot_json` longtext NOT NULL,
  `change_summary` varchar(255) DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_request_version_request` (`request_id`),
  CONSTRAINT `fk_request_version_request` FOREIGN KEY (`request_id`) REFERENCES `soc_request` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_request_attachment` (
  `attachment_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_id` bigint unsigned NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_type` varchar(40) DEFAULT NULL,
  `content_type` varchar(120) DEFAULT NULL,
  `file_size` bigint NOT NULL DEFAULT 0,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`attachment_id`),
  KEY `idx_request_attachment_request` (`request_id`),
  CONSTRAINT `fk_request_attachment_request` FOREIGN KEY (`request_id`) REFERENCES `soc_request` (`request_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_rcm` (
  `rcm_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `control_code` varchar(80) NOT NULL,
  `control_name` varchar(200) NOT NULL,
  `description` text,
  `category` varchar(80) DEFAULT NULL,
  `module_name` varchar(120) DEFAULT NULL,
  `risk_description` varchar(255) DEFAULT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'PENDING',
  `stage` varchar(40) NOT NULL DEFAULT 'MANUAL',
  `ai_generated` tinyint NOT NULL DEFAULT 0,
  `source_request_id` bigint unsigned DEFAULT NULL,
  `source_rcm_id` bigint unsigned DEFAULT NULL,
  `control_objective` text,
  `implementation_method` text,
  `evidence_requirement` text,
  `control_performer` varchar(120) DEFAULT NULL,
  `control_reviewer` varchar(120) DEFAULT NULL,
  `additional_owner` varchar(120) DEFAULT NULL,
  `control_risk_rating` varchar(40) DEFAULT NULL,
  `current_version` varchar(20) NOT NULL DEFAULT 'V1',
  `deleted` tinyint NOT NULL DEFAULT 0,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_rcm_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `rcm_id` bigint unsigned NOT NULL,
  `version_no` varchar(20) NOT NULL,
  `snapshot_json` longtext NOT NULL,
  `change_summary` varchar(255) DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_rcm_version_rcm` (`rcm_id`),
  CONSTRAINT `fk_rcm_version_rcm` FOREIGN KEY (`rcm_id`) REFERENCES `soc_rcm` (`rcm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_control_test` (
  `test_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text,
  `risk_level` varchar(40) DEFAULT NULL,
  `risk_description` varchar(255) DEFAULT NULL,
  `coso_principle` varchar(120) DEFAULT NULL,
  `control_procedure` text,
  `result_status` varchar(40) NOT NULL DEFAULT 'PENDING',
  `current_version` varchar(20) NOT NULL DEFAULT 'V1',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`test_id`),
  KEY `idx_control_test_project` (`project_id`),
  CONSTRAINT `fk_control_test_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_control_test_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `test_id` bigint unsigned NOT NULL,
  `version_no` varchar(20) NOT NULL,
  `snapshot_json` longtext NOT NULL,
  `change_summary` varchar(255) DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_control_test_version_test` (`test_id`),
  CONSTRAINT `fk_control_test_version_test` FOREIGN KEY (`test_id`) REFERENCES `soc_control_test` (`test_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_gap_analysis` (
  `gap_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `source_test_id` bigint unsigned DEFAULT NULL,
  `control_title` varchar(200) NOT NULL,
  `gap_level` varchar(40) NOT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'OPEN',
  `gap_description` text,
  `remediation_suggestion` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`gap_id`),
  KEY `idx_gap_analysis_project` (`project_id`),
  CONSTRAINT `fk_gap_analysis_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_score_snapshot` (
  `snapshot_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `snapshot_date` date NOT NULL,
  `total_count` bigint NOT NULL DEFAULT 0,
  `passed_count` bigint NOT NULL DEFAULT 0,
  `failed_count` bigint NOT NULL DEFAULT 0,
  `pending_count` bigint NOT NULL DEFAULT 0,
  `gap_count` bigint NOT NULL DEFAULT 0,
  `pass_rate` decimal(8,2) NOT NULL DEFAULT 0.00,
  `assessment` varchar(120) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`snapshot_id`),
  KEY `idx_score_snapshot_project` (`project_id`),
  KEY `idx_score_snapshot_date` (`snapshot_date`),
  CONSTRAINT `fk_score_snapshot_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_report_task` (
  `task_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `report_type` varchar(80) DEFAULT NULL,
  `format` varchar(20) DEFAULT NULL,
  `include_sections_json` text,
  `language` varchar(20) DEFAULT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'PENDING',
  `progress` int NOT NULL DEFAULT 0,
  `file_path` varchar(500) DEFAULT NULL,
  `error_message` varchar(500) DEFAULT NULL,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`),
  KEY `idx_report_task_project` (`project_id`),
  CONSTRAINT `fk_report_task_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `soc_operation_log` (
  `log_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `username` varchar(120) DEFAULT NULL,
  `module_name` varchar(60) NOT NULL,
  `action_type` varchar(60) NOT NULL,
  `resource_type` varchar(60) NOT NULL,
  `resource_id` varchar(120) DEFAULT NULL,
  `resource_name` varchar(255) DEFAULT NULL,
  `project_id` bigint unsigned DEFAULT NULL,
  `action_detail` text,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_operation_log_project` (`project_id`),
  KEY `idx_operation_log_module` (`module_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `sys_product`
(`product_id`, `product_name`, `product_code`, `introduction_title`, `introduction_text`, `logo_url`, `trust_principles`, `all_features`, `status`, `sort_no`)
VALUES
(1, 'SOC 2', 'soc2', 'Introduction of SOC2', 'SOC 2 trust service framework', '/assets/product/soc2.png', '["Security","Availability","Privacy"]', '["Security","Availability","Privacy","Processing Integrity","Confidentiality"]', 1, 10),
(2, 'ISO27001', 'iso27001', 'Introduction of ISO27001', 'ISO27001 information security framework', '/assets/product/iso27001.png', '["Security","Processing Integrity","Availability"]', '["Security","Availability","Privacy","Processing Integrity","Confidentiality"]', 1, 20)
ON DUPLICATE KEY UPDATE
`product_name` = VALUES(`product_name`),
`introduction_title` = VALUES(`introduction_title`),
`introduction_text` = VALUES(`introduction_text`),
`logo_url` = VALUES(`logo_url`),
`trust_principles` = VALUES(`trust_principles`),
`all_features` = VALUES(`all_features`),
`status` = VALUES(`status`),
`sort_no` = VALUES(`sort_no`);

INSERT INTO `sys_product_package`
(`package_id`, `product_id`, `package_name`, `annual_price`, `type1_price`, `type2_price`, `included_features`, `supported_types`, `default_type`, `status`, `sort_no`)
VALUES
(1, 1, 'Basic 3', 5999, 5999, 7999, '["Security"]', '["Type1","Type2"]', 'Type1', 1, 10),
(2, 1, 'Basic 4', 6999, 6999, 8999, '["Security","Availability"]', '["Type1","Type2"]', 'Type1', 1, 20),
(3, 1, 'Product Suite', 7999, 7999, 9999, '["Security","Availability","Privacy"]', '["Type1","Type2"]', 'Type1', 1, 30),
(4, 2, 'Basic 3', 5999, 5999, 7999, '["Security"]', '["Type1","Type2"]', 'Type1', 1, 10),
(5, 2, 'Basic 4', 6999, 6999, 8999, '["Security","Availability"]', '["Type1","Type2"]', 'Type1', 1, 20),
(6, 2, 'Product Suite', 7999, 7999, 9999, '["Security","Availability","Privacy"]', '["Type1","Type2"]', 'Type1', 1, 30)
ON DUPLICATE KEY UPDATE
`annual_price` = VALUES(`annual_price`),
`type1_price` = VALUES(`type1_price`),
`type2_price` = VALUES(`type2_price`),
`included_features` = VALUES(`included_features`),
`supported_types` = VALUES(`supported_types`),
`default_type` = VALUES(`default_type`),
`status` = VALUES(`status`),
`sort_no` = VALUES(`sort_no`);

INSERT INTO `sys_company`
(`company_id`, `company_name`, `company_code`, `industry`, `website`, `contact_name`, `contact_phone`, `address`, `created_at`, `updated_at`)
VALUES
(1, 'Demo Company', 'COMP001', 'SaaS', 'https://example.com', 'George', '13800000000', 'Shanghai', '2026-03-27 09:00:00', '2026-03-27 09:00:00');

INSERT INTO `sys_user`
(`user_id`, `company_id`, `display_name`, `email`, `phone`, `avatar_url`, `job_title`, `password_hash`, `role_code`, `status`, `deleted`, `created_at`, `updated_at`)
VALUES
(1, 1, 'George Yao', 'admin@test.com', '13800000000', '', 'Admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36iYuZXoD0XQJea7e6vQ5.q', 'COMP_ADMIN', 1, 0, '2026-03-27 09:00:00', '2026-03-27 09:00:00'),
(2, 1, 'Alice Chen', 'user@test.com', '13800000001', '', 'Auditor', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36iYuZXoD0XQJea7e6vQ5.q', 'GENERAL_USER', 1, 0, '2026-03-27 09:05:00', '2026-03-27 09:05:00');

INSERT INTO `soc_project`
(`project_id`, `company_id`, `project_code`, `project_name`, `compliance_type`, `audit_type`, `current_version`, `gap_count`, `status`, `start_date`, `end_date`, `deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(1, 1, 'PRJ202603270001', 'SOC2 Project A', 'SOC2', 'Type1', 'V1', 0, 'IN_PROGRESS', '2026-03-27', '2026-06-30', 0, 1, 1, '2026-03-27 09:10:00', '2026-03-27 09:10:00'),
(2, 1, 'PRJ202603270002', 'SOC2 Project B', 'SOC2', 'Type2', 'V1', 0, 'IN_PROGRESS', '2026-03-27', '2026-06-30', 0, 1, 1, '2026-03-27 09:12:00', '2026-03-27 09:12:00'),
(3, 1, 'PRJ202603270003', 'ISO27001 Project', 'ISO27001', 'Type1', 'V1', 0, 'IN_PROGRESS', '2026-03-27', '2026-06-30', 0, 1, 1, '2026-03-27 09:15:00', '2026-03-27 09:15:00');

INSERT INTO `soc_project_member`
(`member_id`, `project_id`, `user_id`, `member_role`, `display_name`, `email`, `deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(1, 1, 1, 'PROJECT_OWNER', 'George Yao', 'admin@test.com', 0, 1, 1, '2026-03-27 09:20:00', '2026-03-27 09:20:00'),
(2, 1, 2, 'GENERAL_USER', 'Alice Chen', 'user@test.com', 0, 1, 1, '2026-03-27 09:21:00', '2026-03-27 09:21:00');

INSERT INTO `sys_invitation_code`
(`invitation_id`, `code`, `invitation_type`, `company_id`, `project_id`, `member_role`, `status`, `max_uses`, `used_count`, `expires_at`, `remark`, `created_by`, `used_by`, `used_at`, `created_at`, `updated_at`)
VALUES
(1, 'INV-GEN-001', 'PROJECT', 1, 1, 'GENERAL_USER', 'ACTIVE', 1, 0, '2026-06-30 23:59:59', 'General user invitation', 1, NULL, NULL, '2026-03-27 09:30:00', '2026-03-27 09:30:00'),
(2, 'INV-GEN-002', 'PROJECT', 1, 2, 'GENERAL_USER', 'ACTIVE', 1, 0, '2026-06-30 23:59:59', 'General user invitation', 1, NULL, NULL, '2026-03-27 09:31:00', '2026-03-27 09:31:00'),
(3, 'INV-GEN-003', 'PROJECT', 1, 3, 'GENERAL_USER', 'ACTIVE', 1, 0, '2026-06-30 23:59:59', 'General user invitation', 1, NULL, NULL, '2026-03-27 09:32:00', '2026-03-27 09:32:00');

INSERT INTO `soc_request`
(`request_id`, `project_id`, `request_code`, `cc_criteria`, `title`, `request_description`, `points_of_focus`, `document_status`, `document_owner`, `implementation_date`, `last_update_at`, `notes`, `requestor`, `comments`, `current_version`, `deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(1, 1, 'REQ202603270001', 'CC1.1', 'Access review evidence', 'Provide quarterly access review evidence', 'Privileged users', 'PENDING', 'IT Manager', '2026-03-27', '2026-03-27 10:00:00', '', 'Auditor', '', 'V1', 0, 1, 1, '2026-03-27 10:00:00', '2026-03-27 10:00:00');

INSERT INTO `soc_request_version`
(`version_id`, `request_id`, `version_no`, `snapshot_json`, `change_summary`, `created_by`, `created_at`)
VALUES
(1, 1, 'V1', '{}', 'Initial version', 1, '2026-03-27 10:00:00');

INSERT INTO `soc_request_attachment`
(`attachment_id`, `request_id`, `file_name`, `file_path`, `file_type`, `content_type`, `file_size`, `deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(1, 1, 'evidence.pdf', 'request/1/2026-03-27/uuid.pdf', 'pdf', 'application/pdf', 123456, 0, 1, 1, '2026-03-27 10:05:00', '2026-03-27 10:05:00');

INSERT INTO `soc_rcm`
(`rcm_id`, `project_id`, `control_code`, `control_name`, `description`, `category`, `module_name`, `risk_description`, `status`, `stage`, `ai_generated`, `source_request_id`, `source_rcm_id`, `control_objective`, `implementation_method`, `evidence_requirement`, `control_performer`, `control_reviewer`, `additional_owner`, `control_risk_rating`, `current_version`, `deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(1, 1, 'RCM-001', 'Access Review Control', 'Review privileged access quarterly', 'CC1.1', 'Security', 'Privilege not reviewed', 'DRAFT', 'AI_GENERATED', 1, 1, NULL, 'Ensure access review', 'Quarterly review', 'Review records', 'IT Manager', 'Compliance', '', 'HIGH', 'V1', 0, 1, 1, '2026-03-27 10:10:00', '2026-03-27 10:10:00');

INSERT INTO `soc_rcm_version`
(`version_id`, `rcm_id`, `version_no`, `snapshot_json`, `change_summary`, `created_by`, `created_at`)
VALUES
(1, 1, 'V1', '{}', 'Initial version', 1, '2026-03-27 10:10:00');

INSERT INTO `soc_control_test`
(`test_id`, `project_id`, `title`, `description`, `risk_level`, `risk_description`, `coso_principle`, `control_procedure`, `result_status`, `current_version`, `deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES
(1, 1, 'Access Control Test', 'Verify access review', 'HIGH', 'Unauthorized access', 'Control Activities', 'Quarterly access review', 'FAIL', 'V1', 0, 1, 1, '2026-03-27 10:20:00', '2026-03-27 10:20:00');

INSERT INTO `soc_control_test_version`
(`version_id`, `test_id`, `version_no`, `snapshot_json`, `change_summary`, `created_by`, `created_at`)
VALUES
(1, 1, 'V1', '{}', 'Initial version', 1, '2026-03-27 10:20:00');

INSERT INTO `soc_gap_analysis`
(`gap_id`, `project_id`, `source_test_id`, `control_title`, `gap_level`, `status`, `gap_description`, `remediation_suggestion`, `created_at`, `updated_at`)
VALUES
(1, 1, 1, 'Access Control Test', 'HIGH', 'OPEN', 'Access review evidence missing', 'Upload review logs', '2026-03-27 10:30:00', '2026-03-27 10:30:00');

INSERT INTO `soc_score_snapshot`
(`snapshot_id`, `project_id`, `snapshot_date`, `total_count`, `passed_count`, `failed_count`, `pending_count`, `gap_count`, `pass_rate`, `assessment`, `created_at`)
VALUES
(1, 1, '2026-03-27', 10, 6, 2, 2, 3, 60.00, 'MEDIUM_RISK', '2026-03-27 10:35:00');

INSERT INTO `soc_report_task`
(`task_id`, `project_id`, `report_type`, `format`, `include_sections_json`, `language`, `status`, `progress`, `file_path`, `error_message`, `created_by`, `created_at`, `updated_at`)
VALUES
(1, 1, 'summary', 'md', '["score","gap","logs"]', 'en-US', 'SUCCESS', 100, 'reports/report-1.md', NULL, 1, '2026-03-27 10:40:00', '2026-03-27 10:45:00');

INSERT INTO `soc_operation_log`
(`log_id`, `user_id`, `username`, `module_name`, `action_type`, `resource_type`, `resource_id`, `resource_name`, `project_id`, `action_detail`, `created_at`)
VALUES
(1, 1, 'George Yao', 'RCM', 'UPDATE', 'RCM', '1', 'Access Review Control', 1, 'Fill single RCM by AI', '2026-03-27 10:50:00');

INSERT INTO `sys_order`
(`order_id`, `order_no`, `user_id`, `product_id`, `package_id`, `product_name`, `package_name`, `audit_type`, `amount`, `payment_method`, `status`, `transaction_id`, `return_url`, `notify_url`, `pay_time`, `created_at`, `updated_at`)
VALUES
(1, 'ORD202603270001', 2, 1, 1, 'SOC 2', 'Basic 3', 'Type1', 5999, 'PAYPAL', 'PAID', 'MOCK123', 'http://localhost:8080/order', 'http://localhost:18081/api/payment/notify', '2026-03-27 11:00:00', '2026-03-27 10:55:00', '2026-03-27 11:00:00');

INSERT INTO `sys_user_product`
(`user_product_id`, `user_id`, `product_id`, `product_name`, `package_id`, `package_name`, `audit_type`, `source_order_no`, `status`, `start_time`, `end_time`, `created_at`, `updated_at`)
VALUES
(1, 2, 1, 'SOC 2', 1, 'Basic 3', 'Type1', 'ORD202603270001', 'ACTIVE', '2026-03-27 11:00:00', '2027-03-26 23:59:59', '2026-03-27 11:00:00', '2026-03-27 11:00:00');

SET FOREIGN_KEY_CHECKS = 1;

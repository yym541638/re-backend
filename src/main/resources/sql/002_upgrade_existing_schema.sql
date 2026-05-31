SET NAMES utf8mb4;

/*
  说明：
  1. 这是给当前重写后端库做“增量补丁”的 SQL，不是全量重建脚本。
  2. 适用于已经存在基础表（如 sys_company、sys_user、soc_project、soc_request、soc_rcm）的库。
  3. 如果你是新库，直接执行 001_init_core_schema.sql 更简单。
*/

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER $$

CREATE PROCEDURE add_column_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND COLUMN_NAME = p_column_name
    ) THEN
        SET @ddl_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

CREATE PROCEDURE add_index_if_missing(
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND INDEX_NAME = p_index_name
    ) THEN
        SET @ddl_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD INDEX `', p_index_name, '` ', p_index_definition);
        PREPARE stmt FROM @ddl_sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

/*
  一、给现有表补字段
*/

CALL add_column_if_missing('sys_company', 'company_code', '`company_code` varchar(60) DEFAULT NULL AFTER `company_name`');
CALL add_column_if_missing('sys_company', 'industry', '`industry` varchar(80) DEFAULT NULL AFTER `company_code`');
CALL add_column_if_missing('sys_company', 'website', '`website` varchar(255) DEFAULT NULL AFTER `industry`');
CALL add_column_if_missing('sys_company', 'contact_name', '`contact_name` varchar(80) DEFAULT NULL AFTER `website`');
CALL add_column_if_missing('sys_company', 'contact_phone', '`contact_phone` varchar(40) DEFAULT NULL AFTER `contact_name`');
CALL add_column_if_missing('sys_company', 'address', '`address` varchar(255) DEFAULT NULL AFTER `contact_phone`');

CALL add_column_if_missing('sys_user', 'avatar_url', '`avatar_url` varchar(500) DEFAULT NULL AFTER `phone`');
CALL add_column_if_missing('sys_user', 'job_title', '`job_title` varchar(80) DEFAULT NULL AFTER `avatar_url`');

CALL add_column_if_missing('soc_rcm', 'stage', '`stage` varchar(40) NOT NULL DEFAULT ''MANUAL'' AFTER `status`');
CALL add_column_if_missing('soc_rcm', 'source_request_id', '`source_request_id` bigint unsigned DEFAULT NULL AFTER `ai_generated`');
CALL add_column_if_missing('soc_rcm', 'source_rcm_id', '`source_rcm_id` bigint unsigned DEFAULT NULL AFTER `source_request_id`');
CALL add_column_if_missing('sys_product_package', 'type1_price', '`type1_price` int NOT NULL DEFAULT 0 AFTER `annual_price`');
CALL add_column_if_missing('sys_product_package', 'type2_price', '`type2_price` int NOT NULL DEFAULT 0 AFTER `type1_price`');

CALL add_index_if_missing('soc_rcm', 'idx_rcm_stage', '(`stage`)');
CALL add_index_if_missing('soc_rcm', 'idx_rcm_source_request', '(`source_request_id`)');

/*
  二、补产品、订单、已购产品表
*/

CREATE TABLE IF NOT EXISTS `sys_product` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品表';

CREATE TABLE IF NOT EXISTS `sys_product_package` (
  `package_id` int unsigned NOT NULL AUTO_INCREMENT,
  `product_id` int unsigned NOT NULL,
  `package_name` varchar(120) NOT NULL,
  `annual_price` int NOT NULL,
  `type1_price` int NOT NULL DEFAULT 0,
  `type2_price` int NOT NULL DEFAULT 0,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品套餐表';

CREATE TABLE IF NOT EXISTS `sys_order` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `sys_user_product` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户已购产品表';

/*
  三、补控制测试、差距分析、评分、报表、操作日志表
*/

CREATE TABLE IF NOT EXISTS `soc_control_test` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='控制测试表';

CREATE TABLE IF NOT EXISTS `soc_control_test_version` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='控制测试版本表';

CREATE TABLE IF NOT EXISTS `soc_gap_analysis` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='差距分析表';

CREATE TABLE IF NOT EXISTS `soc_score_snapshot` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评分快照表';

CREATE TABLE IF NOT EXISTS `soc_report_task` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报表任务表';

CREATE TABLE IF NOT EXISTS `soc_operation_log` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

/*
  四、补初始化产品数据
*/

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

UPDATE `sys_product_package`
SET `type1_price` = CASE
        WHEN `type1_price` IS NULL OR `type1_price` <= 0 THEN `annual_price`
        ELSE `type1_price`
    END,
    `type2_price` = CASE
        WHEN `type2_price` IS NULL OR `type2_price` <= 0 THEN `annual_price`
        ELSE `type2_price`
    END,
    `annual_price` = CASE
        WHEN `default_type` = 'Type2' THEN `type2_price`
        ELSE `type1_price`
    END;

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

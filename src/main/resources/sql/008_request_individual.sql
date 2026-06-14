SET NAMES utf8mb4;

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
        SELECT 1 FROM information_schema.COLUMNS
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
        SELECT 1 FROM information_schema.STATISTICS
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

CALL add_column_if_missing('soc_request', 'request_master_id',
    '`request_master_id` bigint unsigned DEFAULT NULL AFTER `project_id`');
CALL add_column_if_missing('soc_request', 'request_assignee',
    '`request_assignee` varchar(120) DEFAULT NULL AFTER `document_owner`');
CALL add_column_if_missing('soc_request', 'document_owner_user_id',
    '`document_owner_user_id` int unsigned DEFAULT NULL AFTER `request_assignee`');
CALL add_column_if_missing('soc_request', 'evidence_manual_status',
    '`evidence_manual_status` varchar(40) NOT NULL DEFAULT ''PENDING'' AFTER `document_status`');
CALL add_column_if_missing('soc_request', 'request_send_date',
    '`request_send_date` datetime DEFAULT NULL AFTER `last_update_at`');
CALL add_column_if_missing('soc_request', 'ai_review_status',
    '`ai_review_status` varchar(20) NOT NULL DEFAULT ''PENDING'' AFTER `request_send_date`');
CALL add_column_if_missing('soc_request', 'ai_review_comment',
    '`ai_review_comment` text AFTER `ai_review_status`');
CALL add_column_if_missing('soc_request', 'user_comment',
    '`user_comment` text AFTER `ai_review_comment`');

CALL add_index_if_missing('soc_request', 'idx_request_master', '(`request_master_id`)');

CALL add_column_if_missing('soc_request_master', 'current_version_id',
    '`current_version_id` bigint unsigned DEFAULT NULL AFTER `status`');

CREATE TABLE IF NOT EXISTS `soc_request_master_template_file` (
  `template_file_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_master_id` bigint unsigned NOT NULL,
  `file_no` int NOT NULL DEFAULT 1,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `relevant_criteria` varchar(200) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`template_file_id`),
  KEY `idx_rm_template_master` (`request_master_id`),
  CONSTRAINT `fk_rm_template_master` FOREIGN KEY (`request_master_id`) REFERENCES `soc_request_master` (`request_master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Request Master 模板文件';

CREATE TABLE IF NOT EXISTS `soc_request_master_version` (
  `version_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `request_master_id` bigint unsigned NOT NULL,
  `version_label` varchar(40) NOT NULL,
  `snapshot_json` longtext NOT NULL,
  `is_latest` tinyint NOT NULL DEFAULT 1,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`version_id`),
  KEY `idx_rm_version_master` (`request_master_id`),
  CONSTRAINT `fk_rm_version_master` FOREIGN KEY (`request_master_id`) REFERENCES `soc_request_master` (`request_master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Request Master 版本快照';

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

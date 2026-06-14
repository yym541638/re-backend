SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `soc_request_master` (
  `request_master_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `request_master_code` varchar(50) NOT NULL,
  `request_master_name` varchar(200) NOT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'INACTIVE',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `updated_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`request_master_id`),
  UNIQUE KEY `uk_request_master_code` (`request_master_code`),
  KEY `idx_request_master_project` (`project_id`),
  KEY `idx_request_master_status` (`status`),
  CONSTRAINT `fk_request_master_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Request Master 表';

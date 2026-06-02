-- 项目创建页上传的附件（Template / 支撑文件）
CREATE TABLE IF NOT EXISTS `soc_project_attachment` (
  `attachment_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `project_id` bigint unsigned NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint NOT NULL DEFAULT 0,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_by` int unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`attachment_id`),
  KEY `idx_project_attachment_project` (`project_id`),
  CONSTRAINT `fk_project_attachment_project` FOREIGN KEY (`project_id`) REFERENCES `soc_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目附件表';

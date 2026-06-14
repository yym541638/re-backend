SET NAMES utf8mb4;

/*
  项目列表 UI 变更：
  1. 新增 project_info 文本字段
  2. start_date / end_date 改为 datetime（含时分秒）
*/

DROP PROCEDURE IF EXISTS add_column_if_missing;

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

DELIMITER ;

CALL add_column_if_missing(
    'soc_project',
    'project_info',
    '`project_info` text DEFAULT NULL COMMENT ''项目描述'' AFTER `project_name`'
);

ALTER TABLE `soc_project`
    MODIFY COLUMN `start_date` datetime DEFAULT NULL COMMENT '项目开始时间',
    MODIFY COLUMN `end_date` datetime DEFAULT NULL COMMENT '项目结束时间';

DROP PROCEDURE IF EXISTS add_column_if_missing;

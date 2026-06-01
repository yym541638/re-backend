SET NAMES utf8mb4;

/*
  为订单与用户已购产品增加套餐能力快照字段。
  适用于已执行 001 / 002 的存量库。
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

CALL add_column_if_missing('sys_order', 'included_features',
    '`included_features` text NULL COMMENT ''套餐能力快照JSON'' AFTER `audit_type`');
CALL add_column_if_missing('sys_user_product', 'included_features',
    '`included_features` text NULL COMMENT ''已购套餐能力JSON'' AFTER `audit_type`');

DROP PROCEDURE IF EXISTS add_column_if_missing;

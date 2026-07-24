# -*- coding: utf-8 -*-
"""Generate 009_request_criteria_catalog.sql from parsed Excel JSON."""
import json
from pathlib import Path

rows = json.loads(Path("tmp_catalog_rows.json").read_text(encoding="utf-8"))


def esc(s: str | None) -> str:
    if not s:
        return ""
    return s.replace("\\", "\\\\").replace("'", "''")


out = Path("src/main/resources/sql/009_request_criteria_catalog.sql")
parts: list[str] = []
parts.append("SET NAMES utf8mb4;")
parts.append("")
parts.append("DROP PROCEDURE IF EXISTS add_column_if_missing;")
parts.append("DROP PROCEDURE IF EXISTS add_index_if_missing;")
parts.append("")
parts.append("DELIMITER $$")
parts.append("")
parts.append("CREATE PROCEDURE add_column_if_missing(")
parts.append("    IN p_table_name VARCHAR(64),")
parts.append("    IN p_column_name VARCHAR(64),")
parts.append("    IN p_column_definition TEXT")
parts.append(")")
parts.append("BEGIN")
parts.append("    IF NOT EXISTS (")
parts.append("        SELECT 1 FROM information_schema.COLUMNS")
parts.append("        WHERE TABLE_SCHEMA = DATABASE()")
parts.append("          AND TABLE_NAME = p_table_name")
parts.append("          AND COLUMN_NAME = p_column_name")
parts.append("    ) THEN")
parts.append("        SET @ddl_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD COLUMN ', p_column_definition);")
parts.append("        PREPARE stmt FROM @ddl_sql;")
parts.append("        EXECUTE stmt;")
parts.append("        DEALLOCATE PREPARE stmt;")
parts.append("    END IF;")
parts.append("END $$")
parts.append("")
parts.append("CREATE PROCEDURE add_index_if_missing(")
parts.append("    IN p_table_name VARCHAR(64),")
parts.append("    IN p_index_name VARCHAR(64),")
parts.append("    IN p_index_definition TEXT")
parts.append(")")
parts.append("BEGIN")
parts.append("    IF NOT EXISTS (")
parts.append("        SELECT 1 FROM information_schema.STATISTICS")
parts.append("        WHERE TABLE_SCHEMA = DATABASE()")
parts.append("          AND TABLE_NAME = p_table_name")
parts.append("          AND INDEX_NAME = p_index_name")
parts.append("    ) THEN")
parts.append("        SET @ddl_sql = CONCAT('ALTER TABLE `', p_table_name, '` ADD INDEX `', p_index_name, '` ', p_index_definition);")
parts.append("        PREPARE stmt FROM @ddl_sql;")
parts.append("        EXECUTE stmt;")
parts.append("        DEALLOCATE PREPARE stmt;")
parts.append("    END IF;")
parts.append("END $$")
parts.append("")
parts.append("DELIMITER ;")
parts.append("")
parts.append("CREATE TABLE IF NOT EXISTS `soc_request_criteria_catalog` (")
parts.append("  `catalog_id` bigint unsigned NOT NULL AUTO_INCREMENT,")
parts.append("  `criteria_code` varchar(40) NOT NULL,")
parts.append("  `module_name` varchar(64) NOT NULL,")
parts.append("  `requirement` text,")
parts.append("  `points_of_focus` text,")
parts.append("  `document_description` text,")
parts.append("  `sort_order` int NOT NULL DEFAULT 0,")
parts.append("  `deleted` tinyint NOT NULL DEFAULT 0,")
parts.append("  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,")
parts.append("  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,")
parts.append("  PRIMARY KEY (`catalog_id`),")
parts.append("  KEY `idx_catalog_module_sort` (`module_name`, `sort_order`)")
parts.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")
parts.append("")
parts.append("CALL add_column_if_missing('soc_request', 'catalog_id',")
parts.append("    '`catalog_id` bigint unsigned DEFAULT NULL AFTER `request_master_id`');")
parts.append(
    "CALL add_index_if_missing('soc_request', 'idx_request_master_catalog', "
    "'(`request_master_id`, `catalog_id`)');"
)
parts.append("")
parts.append("TRUNCATE TABLE `soc_request_criteria_catalog`;")
parts.append("")

values = []
for i, row in enumerate(rows, start=1):
    values.append(
        "('{code}', '{module}', '{req}', '{pof}', '{doc}', {sort}, 0)".format(
            code=esc(row["code"]),
            module=esc(row["module"]),
            req=esc(row["requirement"]),
            pof=esc(row["pof"]),
            doc=esc(row["doc"]),
            sort=i,
        )
    )

cols = (
    "INSERT INTO `soc_request_criteria_catalog` "
    "(`criteria_code`, `module_name`, `requirement`, `points_of_focus`, "
    "`document_description`, `sort_order`, `deleted`) VALUES\n"
)
chunk_size = 40
for start in range(0, len(values), chunk_size):
    chunk = values[start : start + chunk_size]
    parts.append(cols + ",\n".join(chunk) + ";")
    parts.append("")

parts.append("DROP PROCEDURE IF EXISTS add_column_if_missing;")
parts.append("DROP PROCEDURE IF EXISTS add_index_if_missing;")
parts.append("")

out.write_text("\n".join(parts), encoding="utf-8")
print(f"wrote {out} rows={len(rows)} size={out.stat().st_size}")

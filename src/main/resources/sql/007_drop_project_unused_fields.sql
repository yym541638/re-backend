SET NAMES utf8mb4;

/*
  项目 UI 精简：移除列表/表单未展示的字段。
*/

ALTER TABLE `soc_project`
    DROP INDEX `uk_project_code`,
    DROP INDEX `idx_project_status`,
    DROP COLUMN `project_code`,
    DROP COLUMN `compliance_type`,
    DROP COLUMN `audit_type`,
    DROP COLUMN `current_version`,
    DROP COLUMN `gap_count`,
    DROP COLUMN `status`;

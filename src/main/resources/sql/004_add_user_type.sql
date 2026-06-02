-- 用户表增加 user_type（Clients / Consultant / Auditor），与 role_code（权限）分离
CALL add_column_if_missing('sys_user', 'user_type',
  '`user_type` varchar(40) NOT NULL DEFAULT ''CLIENT'' AFTER `job_title`');

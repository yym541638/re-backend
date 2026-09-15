-- Reset business data for clean E2E testing. Keep product catalog + criteria catalog.
-- Usage: mysql -uroot -p... ai_shenji_v2 < scripts/reset_db_for_e2e.sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE soc_control_test_version;
TRUNCATE TABLE soc_control_test;
TRUNCATE TABLE soc_gap_analysis;
TRUNCATE TABLE soc_operation_log;
TRUNCATE TABLE soc_rcm_version;
TRUNCATE TABLE soc_rcm;
TRUNCATE TABLE soc_report_task;
TRUNCATE TABLE soc_request_attachment;
TRUNCATE TABLE soc_request_version;
TRUNCATE TABLE soc_request;
TRUNCATE TABLE soc_request_master_template_file;
TRUNCATE TABLE soc_request_master_version;
TRUNCATE TABLE soc_request_master;
TRUNCATE TABLE soc_risk;
TRUNCATE TABLE soc_score_snapshot;
TRUNCATE TABLE soc_project_member;
TRUNCATE TABLE soc_project;
TRUNCATE TABLE sys_invitation_code;
TRUNCATE TABLE sys_order;
TRUNCATE TABLE sys_user_product;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_company;

SET FOREIGN_KEY_CHECKS = 1;

-- Ensure commerce included_features exists (idempotent)
SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1 FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'sys_order'
        AND COLUMN_NAME = 'included_features'
    ),
    'SELECT 1',
    'ALTER TABLE sys_order ADD COLUMN included_features text NULL COMMENT ''套餐能力快照JSON'' AFTER audit_type'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Companies
INSERT INTO sys_company (company_id, company_name, company_code, industry, website, contact_name, contact_phone, address)
VALUES
(1, 'Demo Fresh Co', 'FRESH01', 'Manufacturing', 'https://fresh.demo.local', 'Fresh Admin', '13800000001', 'Shanghai'),
(2, 'Demo Ready Co', 'READY01', 'Software', 'https://ready.demo.local', 'Ready Admin', '13800000002', 'Shanghai');

-- Password for all accounts: Test@123456
-- hash: $2a$10$WFCncDwxf2qk.icDCyouXO8XgsWkUB1GLTcsd4JJ0GioAX9eQcqdW
INSERT INTO sys_user
(user_id, company_id, display_name, email, phone, avatar_url, job_title, user_type, password_hash, role_code, status, deleted)
VALUES
(1, 1, 'Fresh Admin', 'fresh.admin@demo.com', '13800000001', '', 'Administrator', 'CLIENT',
 '$2a$10$WFCncDwxf2qk.icDCyouXO8XgsWkUB1GLTcsd4JJ0GioAX9eQcqdW', 'SYS_ADMIN', 1, 0),
(2, 2, 'Ready Admin', 'ready.admin@demo.com', '13800000002', '', 'Administrator', 'CLIENT',
 '$2a$10$WFCncDwxf2qk.icDCyouXO8XgsWkUB1GLTcsd4JJ0GioAX9eQcqdW', 'SYS_ADMIN', 1, 0),
(3, 2, 'Ready User', 'ready.user@demo.com', '13800000003', '', 'Staff', 'CLIENT',
 '$2a$10$WFCncDwxf2qk.icDCyouXO8XgsWkUB1GLTcsd4JJ0GioAX9eQcqdW', 'SYS_USER', 1, 0),
(4, 2, 'Ready Doc Owner', 'ready.doc@demo.com', '13800000004', '', 'Document Owner', 'CLIENT',
 '$2a$10$WFCncDwxf2qk.icDCyouXO8XgsWkUB1GLTcsd4JJ0GioAX9eQcqdW', 'SYS_USER', 1, 0);

-- Ready Admin already purchased Product Suite / Type2 / all 5 features
INSERT INTO sys_user_product
(user_id, product_id, product_name, package_id, audit_type, included_features, source_order_no, status, start_time)
VALUES
(2, 1, 'SOC 2', 3, 'Type2',
 'Security,Availability,Privacy,Processing Integrity,Confidentiality',
 'SEED-READY-001', 'ACTIVE', NOW());

INSERT INTO sys_order
(order_no, user_id, product_id, package_id, product_name, package_name, audit_type, included_features,
 amount, payment_method, status, transaction_id, pay_time)
VALUES
('SEED-READY-001', 2, 1, 3, 'SOC 2', 'Product Suite', 'Type2',
 'Security,Availability,Privacy,Processing Integrity,Confidentiality',
 9999, 'Paypal', 'PAID', 'MOCK-SEED-READY', NOW());

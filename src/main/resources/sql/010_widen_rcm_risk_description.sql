-- Request Individual 生成时会把 points_of_focus 同步到 RCM.risk_description，
-- 条款目录最长约 600 字符，原 varchar(255) 会导致 Data truncation → 500。
ALTER TABLE `soc_rcm`
  MODIFY COLUMN `risk_description` text;

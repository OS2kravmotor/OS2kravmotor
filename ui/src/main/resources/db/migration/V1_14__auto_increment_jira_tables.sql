SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM jira_task;
DELETE FROM jira_sprint;
ALTER TABLE jira_sprint MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE jira_task MODIFY id BIGINT NOT NULL AUTO_INCREMENT;
SET FOREIGN_KEY_CHECKS = 1;
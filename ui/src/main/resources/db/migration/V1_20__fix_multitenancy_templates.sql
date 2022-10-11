ALTER TABLE email_templates ADD COLUMN cvr VARCHAR(8) NULL;
UPDATE email_templates SET cvr = '29189978';
ALTER TABLE email_templates MODIFY COLUMN cvr VARCHAR(8) NOT NULL;
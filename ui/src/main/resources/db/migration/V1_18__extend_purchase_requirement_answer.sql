ALTER TABLE purchase_requirement_answer 
ADD COLUMN customer_answer VARCHAR(64) DEFAULT 'NONE',
ADD COLUMN customer_comment TEXT NULL;
ALTER TABLE purchase_requirement ADD subcategory_id BIGINT;
ALTER TABLE purchase_requirement ADD CONSTRAINT fk_pr_subcategory_id FOREIGN KEY (subcategory_id) REFERENCES subcategory(id);

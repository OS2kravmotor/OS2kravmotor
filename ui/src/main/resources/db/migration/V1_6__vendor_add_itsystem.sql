ALTER TABLE purchase_vendor
ADD COLUMN it_system_id BIGINT,
ADD FOREIGN KEY (it_system_id) REFERENCES it_system(id) ON DELETE CASCADE;
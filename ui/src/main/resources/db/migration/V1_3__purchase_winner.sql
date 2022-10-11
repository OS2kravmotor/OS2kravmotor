ALTER TABLE purchase
ADD COLUMN winner_id BIGINT NULL,
ADD FOREIGN KEY (winner_id) REFERENCES purchase_vendor(id);
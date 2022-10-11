ALTER TABLE purchase_requirement
ADD COLUMN rationale TEXT,
ADD COLUMN info_requirement BOOL NOT NULL DEFAULT 0;
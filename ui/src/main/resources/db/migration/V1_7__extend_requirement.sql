ALTER TABLE requirement
ADD COLUMN notes TEXT,
ADD COLUMN rationale TEXT,
ADD COLUMN info_requirement BOOL NOT NULL DEFAULT 0,
ADD COLUMN last_changed TIMESTAMP NULL;

UPDATE requirement SET last_changed = CURRENT_TIMESTAMP WHERE id > 0;
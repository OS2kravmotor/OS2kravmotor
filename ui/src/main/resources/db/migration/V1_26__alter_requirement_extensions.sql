ALTER TABLE requirement_extension
ADD COLUMN disable_requirement TINYINT NOT NULL DEFAULT 0,
ADD COLUMN disable_requirement_reason TEXT NULL;

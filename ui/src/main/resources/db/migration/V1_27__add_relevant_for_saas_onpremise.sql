ALTER TABLE requirement ADD COLUMN relevant_for_onpremise BOOLEAN NOT NULL DEFAULT 1;
ALTER TABLE requirement ADD COLUMN relevant_for_saas BOOLEAN NOT NULL DEFAULT 1;

ALTER TABLE purchase_answer ADD COLUMN solution_type varchar(64) NOT NULL DEFAULT 'ON_PREMISE';
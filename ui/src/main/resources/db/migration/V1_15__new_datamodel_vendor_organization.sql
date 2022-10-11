CREATE TABLE vendor_organization (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  domain                          VARCHAR(255) NOT NULL,

  UNIQUE(domain)
);

CREATE TABLE vendor_user (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  vendor_organization_id          BIGINT NOT NULL,
  email                           VARCHAR(255) NOT NULL,
  password                        VARCHAR(255) NOT NULL,
  admin                           BIT NOT NULL,

  UNIQUE(email),
  FOREIGN KEY (vendor_organization_id) REFERENCES vendor_organization(id) ON DELETE CASCADE
);

CREATE TABLE purchase_answer (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  it_system_id                    BIGINT,
  purchase_id                     BIGINT,
  vendor_organization_id          BIGINT,
  created                         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (it_system_id) REFERENCES it_system(id),
  FOREIGN KEY (purchase_id) REFERENCES purchase(id) ON DELETE CASCADE,
  FOREIGN KEY (vendor_organization_id) REFERENCES vendor_organization(id)
);

CREATE TABLE purchase_requirement_answer (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  choice                          VARCHAR(32),
  detail                          TEXT,
  price                           TEXT,
  purchase_requirement_id         BIGINT(20) NOT NULL,
  purchase_answer_id              BIGINT(20) NOT NULL,
  answered                        BIT NOT NULL,
  dirty_copy                      BIT NOT NULL,

  FOREIGN KEY (purchase_requirement_id) REFERENCES purchase_requirement(id) ON DELETE CASCADE,
  FOREIGN KEY (purchase_answer_id) REFERENCES purchase_answer(id) ON DELETE CASCADE
);

CREATE TABLE vendor_user_purchase_answer (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  vendor_user_id                  BIGINT NOT NULL,
  purchase_answer_id              BIGINT NOT NULL,

  FOREIGN KEY (vendor_user_id) REFERENCES vendor_user(id) ON DELETE CASCADE,
  FOREIGN KEY (purchase_answer_id) REFERENCES purchase_answer(id) ON DELETE CASCADE
);

ALTER TABLE purchase_requirement
ADD COLUMN last_changed TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE purchase
ADD COLUMN winner_purchase_answer_id BIGINT NULL,
ADD FOREIGN KEY (winner_purchase_answer_id) REFERENCES purchase_answer(id) ON DELETE SET NULL;

SET @Expression = (SELECT CONCAT('ALTER TABLE purchase DROP FOREIGN KEY ',CONSTRAINT_NAME,';') as x FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE COLUMN_NAME='winner_id' AND TABLE_NAME='purchase' AND TABLE_SCHEMA = DATABASE() );
PREPARE command FROM @Expression;
EXECUTE command;
ALTER TABLE purchase ADD FOREIGN KEY (winner_id) REFERENCES purchase_vendor(id)  ON DELETE SET NULL;
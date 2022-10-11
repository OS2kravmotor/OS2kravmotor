-- enum of 'domains', a 'domain' can be Healthcare, Kontanthjaelp, Dagpenge, etc
CREATE TABLE domain (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(64) NOT NULL,

  UNIQUE(name)
);

CREATE TABLE purchase (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  cvr                             VARCHAR(8) NOT NULL,
  title                           VARCHAR(255) NOT NULL,
  status                          VARCHAR(64) NOT NULL,
  email                           VARCHAR(255) NOT NULL,
  start_time                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  end_time                        TIMESTAMP NULL DEFAULT NULL,
  description                     TEXT NOT NULL,
  questionnaire_filled_out        BOOL NOT NULL DEFAULT 0
);

CREATE TABLE purchase_domain (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  purchase_id                     BIGINT NOT NULL,
  domain_id                       BIGINT NOT NULL,

  FOREIGN KEY (purchase_id) REFERENCES purchase(id) ON DELETE CASCADE,
  FOREIGN KEY (domain_id)   REFERENCES domain(id)
);

CREATE TABLE category (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(64) NOT NULL,

  UNIQUE(name)
);

CREATE TABLE requirement (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,
  cvr                             VARCHAR(8) NOT NULL,
  request_share                   BOOL NOT NULL DEFAULT 0,
  available_for_all_domains       BOOL NOT NULL DEFAULT 0,
  available_for_all_tags          BOOL NOT NULL DEFAULT 0,
  importance                      VARCHAR(64) NOT NULL,
  category_id                     BIGINT NOT NULL,
  description                     TEXT NOT NULL,

  FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE tag (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(64) NOT NULL,
  question                        TEXT NOT NULL
);

CREATE TABLE requirement_domain (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  requirement_id                  BIGINT NOT NULL,
  domain_id                       BIGINT NOT NULL,

  FOREIGN KEY (requirement_id) REFERENCES requirement(id) ON DELETE CASCADE,
  FOREIGN KEY (domain_id) REFERENCES domain(id)
);

CREATE TABLE requirement_tag (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  requirement_id                  BIGINT NOT NULL,
  tag_id                          BIGINT NOT NULL,

  FOREIGN KEY (requirement_id) REFERENCES requirement(id) ON DELETE CASCADE,
  FOREIGN KEY (tag_id)      REFERENCES tag(id)
);

CREATE TABLE purchase_requirement (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  requirement_id                  BIGINT NOT NULL,
  purchase_id                     BIGINT NOT NULL,
  name                            VARCHAR(255) NOT NULL,
  importance                      VARCHAR(64) NOT NULL,
  description                     TEXT NOT NULL,
  category_id                     BIGINT NOT NULL,

  FOREIGN KEY (purchase_id) REFERENCES purchase(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE purchase_vendor (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,
  email                           VARCHAR(255) NOT NULL,
  username                        VARCHAR(64) NOT NULL,
  password                        VARCHAR(64) NOT NULL,
  timestamp                       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  purchase_id                     BIGINT NOT NULL,

  UNIQUE(username),
  FOREIGN KEY (purchase_id) REFERENCES purchase(id) ON DELETE CASCADE
);

CREATE TABLE purchase_vendor_answer (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  choice                          VARCHAR(32) NOT NULL,
  detail                          TEXT,
  price                           TEXT,
  purchase_requirement_id         BIGINT NOT NULL,
  purchase_vendor_id              BIGINT NOT NULL,

  FOREIGN KEY (purchase_requirement_id) REFERENCES purchase_requirement(id),
  FOREIGN KEY (purchase_vendor_id) REFERENCES purchase_vendor(id) ON DELETE CASCADE
);

CREATE TABLE global_editor (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  user_id                         VARCHAR(128) NOT NULL,
  cvr                             VARCHAR(8) NOT NULL
);

CREATE TABLE setting (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  cvr                             VARCHAR(8) NOT NULL,
  setting_key                     VARCHAR(64) NOT NULL,
  setting_value                   TEXT NOT NULL
);

CREATE TABLE identity_provider (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(64) NOT NULL,
  entity_id                       VARCHAR(255) NOT NULL,
  cvr                             VARCHAR(8) NOT NULL,
  metadata                        TEXT
);

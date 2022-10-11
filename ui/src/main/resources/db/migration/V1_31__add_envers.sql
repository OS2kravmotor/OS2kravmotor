CREATE TABLE revinfo (
  rev                          BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  revtstmp                     BIGINT NOT NULL
);

INSERT INTO revinfo (rev, revtstmp) values (1, UNIX_TIMESTAMP());

CREATE TABLE requirement_aud (
  id                           BIGINT NOT NULL,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  name                         VARCHAR(255) NULL,
  cvr                          VARCHAR(8) NULL,
  request_share                TINYINT(1) NULL,
  available_for_all_domains    TINYINT(1) NULL,
  available_for_all_tags       TINYINT(1) NULL,
  importance                   VARCHAR(64) NULL,
  category_id                  BIGINT(20) NULL,
  description                  TEXT NULL,
  notes                        TEXT NULL,
  rationale                    TEXT NULL,
  info_requirement             TINYINT(1) NULL,
  last_changed                 TIMESTAMP NULL,
  help_text                    TEXT NULL,
  interested_party             VARCHAR(255) NULL,
  favorite                     TINYINT(1) NULL,
  relevant_for_onpremise       TINYINT(1) NULL,
  relevant_for_saas            TINYINT(1) NULL,
  request_share_email          VARCHAR(255) NULL,
  deleted                      TINYINT(1) NULL,

  FOREIGN KEY fk_requirement_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

CREATE TABLE category_aud (
  id                           BIGINT NOT NULL,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  name                         VARCHAR(64) NULL,

  FOREIGN KEY fk_category_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

INSERT INTO category_aud (id, rev, revtype, name) SELECT id, 1, 0, name FROM category;

CREATE TABLE domain_aud (
  id                           BIGINT NOT NULL,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  name                         VARCHAR(64) NULL,

  FOREIGN KEY fk_domain_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

INSERT INTO domain_aud (id, rev, revtype, name) SELECT id, 1, 0, name FROM domain;

CREATE TABLE requirement_domain_aud (
  id                           BIGINT NOT NULL AUTO_INCREMENT,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  requirement_id               BIGINT(20) NULL,
  domain_id                    BIGINT(20) NULL,

  FOREIGN KEY fk_req_domain_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

CREATE TABLE tag_aud (
  id                           BIGINT NOT NULL,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  name                         VARCHAR(64) NULL,
  question                     TEXT NULL,

  FOREIGN KEY fk_tag_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

INSERT INTO tag_aud (id, rev, revtype, name, question) SELECT id, 1, 0, name, question FROM tag;

CREATE TABLE requirement_tag_aud (
  id                           BIGINT NOT NULL AUTO_INCREMENT,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  requirement_id               BIGINT(20) NULL,
  tag_id                       BIGINT(20) NULL,

  FOREIGN KEY fk_requirement_tag_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

CREATE TABLE architecture_principle_aud (
  id                           BIGINT NOT NULL,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  name                         VARCHAR(255) NULL,
  reference                    TEXT NULL,

  FOREIGN KEY fk_architecture_principle_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

INSERT INTO architecture_principle_aud (id, rev, revtype, name, reference) SELECT id, 1, 0, name, reference FROM architecture_principle;

CREATE TABLE requirement_principle_aud (
  id                           BIGINT NOT NULL AUTO_INCREMENT,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  requirement_id               BIGINT(20) NULL,
  architecture_principle_id    BIGINT(20) NULL,

  FOREIGN KEY fk_requirement_principle_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

CREATE TABLE attachment_aud (
  id                           BIGINT NOT NULL,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  name                         VARCHAR(64) NULL,
  url                          VARCHAR(255) NULL,
  requirement_id               BIGINT(20) NULL,

  FOREIGN KEY fk_attachment_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

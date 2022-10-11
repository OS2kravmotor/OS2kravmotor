CREATE TABLE subcategory (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(64) NOT NULL,
  category_id					  BIGINT NOT NULL,
  
  FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE
);

ALTER TABLE requirement ADD subcategory_id BIGINT;
ALTER TABLE requirement_aud ADD subcategory_id BIGINT NULL;
ALTER TABLE requirement ADD CONSTRAINT fk_subcategory_id FOREIGN KEY (subcategory_id) REFERENCES subcategory(id);

CREATE TABLE subcategory_aud (
  id                           BIGINT NOT NULL,
  rev                          BIGINT NOT NULL,
  revtype                      TINYINT DEFAULT NULL,

  name                         VARCHAR(64) NULL,
  category_id                  BIGINT NULL,

  FOREIGN KEY fk_subcategory_aud_rev (rev) REFERENCES revinfo(rev),
  PRIMARY KEY (id, rev)
);

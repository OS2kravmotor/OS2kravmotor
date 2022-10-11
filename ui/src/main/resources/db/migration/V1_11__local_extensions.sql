CREATE TABLE requirement_extension (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  requirement_id                  BIGINT NOT NULL,
  cvr                             VARCHAR(8) NOT NULL,
  description                     TEXT,

  FOREIGN KEY (requirement_id) REFERENCES requirement(id) ON DELETE CASCADE,
  UNIQUE(requirement_id, cvr)
);
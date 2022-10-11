CREATE TABLE attachment (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(128) NOT NULL,
  url                             VARCHAR(255) NOT NULL,
  requirement_id                  BIGINT NOT NULL,

  FOREIGN KEY (requirement_id) REFERENCES requirement(id) ON DELETE CASCADE
);
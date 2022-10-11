CREATE TABLE requirement_principle (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  requirement_id                  BIGINT NOT NULL,
  architecture_principle_id       BIGINT NOT NULL,

  FOREIGN KEY (requirement_id) REFERENCES requirement(id) ON DELETE CASCADE,
  FOREIGN KEY (architecture_principle_id) REFERENCES architecture_principle(id)
);
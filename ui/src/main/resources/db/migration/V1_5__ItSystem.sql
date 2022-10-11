CREATE TABLE it_system (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  system_id                       BIGINT NOT NULL,
  name                            VARCHAR(255) NOT NULL,
  vendor                          VARCHAR(128)
);

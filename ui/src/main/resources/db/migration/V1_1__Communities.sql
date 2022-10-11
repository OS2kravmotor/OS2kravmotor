CREATE TABLE community (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name                            VARCHAR(255) NOT NULL,
  community_cvr                   VARCHAR(8) NOT NULL
);

CREATE TABLE community_member (
  id                              BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  municipality_cvr                VARCHAR(8) NOT NULL,
  community_id                    BIGINT NOT NULL,

  FOREIGN KEY (community_id) REFERENCES community(id)
);

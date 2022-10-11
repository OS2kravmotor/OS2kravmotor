CREATE TABLE requirement_extension_attachment (
    id                            BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name                          VARCHAR(128) NOT NULL,
    url                           VARCHAR(255) NOT NULL,
    requirement_extension_id      BIGINT NOT NULL,

    FOREIGN KEY (requirement_extension_id) REFERENCES requirement_extension(id) ON DELETE CASCADE
);
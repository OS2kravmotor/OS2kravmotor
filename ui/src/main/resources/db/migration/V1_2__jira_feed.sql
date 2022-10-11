CREATE TABLE jira_sprint (
  id                              BIGINT NOT NULL PRIMARY KEY,
  state                           VARCHAR(64) NOT NULL,
  name                            VARCHAR(255) NOT NULL,
  start_date                      TIMESTAMP NULL,
  end_date                        TIMESTAMP NULL
);

CREATE TABLE jira_task (
  id                              BIGINT NOT NULL PRIMARY KEY,
  issue_key                       VARCHAR(64) NOT NULL,
  summary                         VARCHAR(255) NOT NULL,
  jira_sprint_id                  BIGINT NOT NULL,

  FOREIGN KEY (jira_sprint_id) REFERENCES jira_sprint(id)
);

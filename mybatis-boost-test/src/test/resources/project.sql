CREATE DATABASE IF NOT EXISTS test;
CREATE TABLE Project
(
  id                 int PRIMARY KEY NOT NULL AUTO_INCREMENT,
  group_id           varchar(100)    NOT NULL,
  artifact_id        varchar(100)    NOT NULL,
  license            varchar(100)    NOT NULL,
  scm                varchar(100)    NOT NULL,
  developer          varchar(100)    NOT NULL,
  __create_time      timestamp       NOT NULL default current_timestamp,
  __last_modify_time timestamp       NOT NULL
  on update current_timestamp
);
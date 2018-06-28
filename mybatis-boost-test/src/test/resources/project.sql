CREATE DATABASE IF NOT EXISTS test;
CREATE TABLE `test`.`project`
(
  id                 int PRIMARY KEY NOT NULL AUTO_INCREMENT,
  group_id           varchar(100),
  artifact_id        varchar(100),
  license            varchar(100),
  scm                varchar(100),
  developer          varchar(100),
  __create_time      timestamp       NOT NULL default current_timestamp,
  __last_modify_time timestamp       NOT NULL
  on update current_timestamp
);
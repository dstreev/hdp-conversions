CREATE DATABASE IF NOT EXISTS ${TARGET_DB};

USE ${TARGET_DB};

CREATE external TABLE sentry_roles(id String, name String, grp String) ROW FORMAT DELIMITED FIELDS
TERMINATED BY ',' STORED AS TEXTFILE LOCATION '${BASE_DIR}/sentry/roles' TBLPROPERTIES
("skip.header.line.count"="1") ;

CREATE external TABLE sentry_access(id String, name String, db_privilege_id String, privilege_scope
String, server_name String, db_name String, table_name String, column_name String, uri String,
action String, with_grant String) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE
LOCATION '${BASE_DIR}/sentry/access' TBLPROPERTIES("skip.header.line.count"="1") ;


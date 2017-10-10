CREATE DATABASE IF NOT EXISTS ${TARGET_DB};

USE ${TARGET_DB};

CREATE external TABLE sentry_roles(id String, name String, grp String) ROW FORMAT DELIMITED FIELDS
TERMINATED BY ',' STORED AS TEXTFILE LOCATION '${BASE_DIR}/sentry/roles' TBLPROPERTIES
("skip.header.line.count"="1") ;

CREATE external TABLE sentry_access(id String, name String, db_privilege_id String, privilege_scope
String, server_name String, db_name String, table_name String, column_name String, uri String,
action String, with_grant String) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE
LOCATION '${BASE_DIR}/sentry/access' TBLPROPERTIES("skip.header.line.count"="1") ;

-- ALL
SELECT
    r.id             ,
    r.name           ,
    r.grp            ,
    a.db_privilege_id,
    a.privilege_scope,
    a.server_name    ,
    a.db_name        ,
    a.table_name     ,
    a.column_name    ,
    a.uri            ,
    a.action         ,
    a.with_grant
FROM
    sentry_roles r
INNER JOIN
    sentry_access a
 ON
    r.id = a.id
    AND r.name = a.name;
-- HDFS
SELECT
    r.grp            ,
    a.privilege_scope,
    CASE SUBSTR(SUBSTR(a.uri,1,LENGTH("hdfs://") +LENGTH("${NAMESERVICE}")) ,1,4)
      WHEN "hdfs"
      THEN SUBSTR(a.uri,LENGTH("hdfs://") +LENGTH("${NAMESERVICE}") +1)
      ELSE a.uri
    END,
    a.action
FROM
    sentry_roles r
INNER JOIN
    sentry_access a
 ON
    r.id = a.id
    AND r.name = a.name
WHERE
    a.privilege_scope = "URI"
    AND SUBSTR(a.uri,1,4) != "file";
-- HIVE
SELECT
    r.grp                     ,
    a.privilege_scope AS scope,
    a.db_name         AS db   ,
    CASE a.table_name
      WHEN '__NULL__'
      THEN '*'
      ELSE a.table_name
    END AS table_name,
    CASE a.column_name
      WHEN '__NULL__'
      THEN '*'
      ELSE a.column_name
    END AS column_name,
    a.action          ,
    a.with_grant
FROM
    sentry_roles r
INNER JOIN
    sentry_access a
 ON
    r.id = a.id
    AND r.name = a.name
WHERE
    a.privilege_scope IN("DATABASE",
                         "TABLE")
    AND SUBSTR(a.uri,1,4) != "file"
ORDER BY
    db        ,
    scope     ,
    table_name,
    grp;
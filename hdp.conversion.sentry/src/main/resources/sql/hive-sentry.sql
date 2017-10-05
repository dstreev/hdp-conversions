SELECT
  r.grp,
  a.privilege_scope AS scope,
  a.db_name         AS db,
  CASE a.table_name
  WHEN '__NULL__'
    THEN '*'
  ELSE a.table_name
  END               AS table_name,
  CASE a.column_name
  WHEN '__NULL__'
    THEN '*'
  ELSE a.column_name
  END               AS column_name,
  a.action,
  a.with_grant
FROM
  sentry_roles r
  INNER JOIN
  sentry_access a
    ON
      r.id = a.id
      AND r.name = a.name
WHERE
  a.privilege_scope IN ("DATABASE","TABLE")
  AND SUBSTR(a.uri, 1, 4) != "file"
ORDER BY
db,
scope,
table_name,
grp

SELECT
    collect_set(r.grp) AS grp,
    a.privilege_scope AS scope          ,
    CASE SUBSTR(SUBSTR(a.path,1,LENGTH('hdfs://') +LENGTH('$nameservice')) ,1,4)
      WHEN "hdfs"
      THEN SUBSTR(a.path,LENGTH('hdfs://') +LENGTH('$nameservice') +1)
      ELSE a.path
    END AS path,
    a.action as action
FROM
    sentry_roles r
INNER JOIN
    (
 SELECT
          id             ,
          name           ,
          privilege_scope,
          CASE SUBSTR(uri, LENGTH(uri))
            WHEN "/"
            THEN SUBSTR(uri, 1, LENGTH(uri) -1)
            ELSE uri
          END AS path,
          action
     FROM
          sentry_access
    WHERE
          privilege_scope = 'URI'
          AND SUBSTR(uri,1,4) != 'file') a
 ON
    r.id = a.id
    AND r.name = a.name
GROUP BY
    a.privilege_scope,
    CASE SUBSTR(SUBSTR(a.path,1,LENGTH('hdfs://') +LENGTH('$nameservice')) ,1,4)
      WHEN "hdfs"
      THEN SUBSTR(a.path,LENGTH('hdfs://') +LENGTH('$nameservice') +1)
      ELSE a.path
    END,
    a.action
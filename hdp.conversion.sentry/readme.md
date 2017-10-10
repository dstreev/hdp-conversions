# Summary
This project is designed to process an extract from the Cloudera Sentry Database and Import those policies into Ranger via the REST API.

## Collect Sentry Information

Using Sql, extract the Roles and Access tables from Sentry into two text files.  The files need to be comma delimited.

Use [this](./src/main/resources/sentry_extracts.sql) sql to extract the Sentry elements.  We'll use these extract to drive Hive External Tables in HDP.

## Build External Tables

Run the [ddl](./src/main/resources/hive_sentry.ddl) in Hive to build the external tables.  Note: Create a new databases and specify it in the hive script for the environment variable "TARGET_DB".

IE: hive -hiveconf TARGET_DB=SENTRY_IMPORT -f sentry.ddl

Test that the database is populated.

## Importing the Policies

This program will import the policies for HDFS and Hive.

You will need the following information to run:
```
usage: LoadSentryPolicies
 -dfs,--hdfs                           Process hdfs policies.
 -dfsns,--hdfs_nameservice <arg>       HDFS Nameservice IE:
                                       hdfs://<nameservice>/user/hdfs.
 -dfss,--hdfs.service <HDFS_Service>   HDFS Repo ranger.rest.v2.Service
                                       name to apply changes
 -h,--hive                             Process hive policies.
 -hs,--hive.service <Hive_Service>     Hive Repo ranger.rest.v2.Service
                                       name to apply changes
 -jp <jdbc_password>                   Hive User
 -ju <jdbc_username>                   Hive User
 -jurl <jdbc_url>                      Hive URL for Sentry
                                       ranger.rest.v2.Policy Extracts
 -p,--password <password>              User password
 -u,--user <user>                      User account
 -url <URL>                            Ranger Base URL
```

### Running

Complied and packaged binary is available [here](https://github.com/dstreev/hdp-conversions/releases)

Example 1:
Load 'HDFS' Policies.
```
java -jar hdp.conversion.sentry-1.0-SNAPSHOT-all.jar -dfs -url http://os2:6080 -u admin -p admin -jurl jdbc:hive2://os3:10000/dstreev_priv -ju dstreev -jp * -dfsns nameservice1
```

Example 2:
Load 'HIVE' Policies
```
java -jar hdp.conversion.sentry-1.0-SNAPSHOT-all.jar -h -url http://os2:6080 -u admin -p admin -jurl jdbc:hive2://os3:10000/dstreev_priv -ju dstreev -jp *
```

Example 3:
Load 'HDFS' and 'Hive' Policies
```
java -jar hdp.conversion.sentry-1.0-SNAPSHOT-all.jar -url http://os2:6080 -u admin -p admin -jurl jdbc:hive2://os3:10000/dstreev_priv -ju dstreev -jp * -dfsns nameservice1
```

*NOTE*: The groups that are referred to in the "Sentry Roles" must exist in Ranger as imported Groups.  If they are missing, Ranger will reject the policies.

Policies can only be imported once.  Followup imports will find the match and not attempt to load again.

## Ranger REST API Docs

Using v2 API

https://cwiki.apache.org/confluence/display/RANGER/Apache+Ranger+0.6+-+REST+APIs+for+Service+Definition%2C+Service+and+Policy+Management


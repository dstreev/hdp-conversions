https://cwiki.apache.org/confluence/display/RANGER/Apache+Ranger+0.6+-+REST+APIs+for+Service+Definition%2C+Service+and+Policy+Management

# Retrieve List of Services

http://os02.streever.local:6080/service/public/v2/api/service

```
LogoYARC! v1.1.1
Yet Another REST Client
Main
Favorites 0
History
About
Help
Request Settings
URL:

http://os02.streever.local:6080/service/public/v2/api/service
GET
Payload:

application/json
Custom Headers
Authentication
Basic Authentication is supported. Enter a username and password and the Authorization Header will be generated and sent with the request.

Authorization:
Basic YWRtaW46YWRtaW4=
Add Credentials Send Request
Response
200
Request URL: http://os02.streever.local:6080/service/public/v2/api/service
Request Method: GET
Response Time: 0.294 seconds
Response Status: 200 - OK
Response Body
Response Body (RAW)
Response Headers
Request Details
 [
  {
    "id": 1,
    "guid": "a2978cfc-964c-4429-9074-c49ef143efb0",
    "isEnabled": true,
    "createdBy": "amb_ranger_admin",
    "createTime": 1502039792000,
    "updateTime": 1502200483000,
    "version": 2,
    "type": "hdfs",
    "name": "STR01_hadoop",
    "description": "hdfs repo",
    "configs": {
      "tag.download.auth.users": "hdfs",
      "password": "*****",
      "policy.download.auth.users": "hdfs",
      "hadoop.rpc.protection": "authentication",
      "hadoop.security.authentication": "simple",
      "fs.default.name": "hdfs://STR01",
      "hadoop.security.authorization": "false",
      "hadoop.security.auth_to_local": "DEFAULT",
      "ambari.service.check.user": "ambari-qa",
      "username": "hdfs"
    },
    "policyVersion": 10,
    "policyUpdateTime": 1505156587000,
    "tagVersion": 1,
    "tagUpdateTime": 1502039792000
  },
  {
    "id": 2,
    "guid": "469db041-2ce4-4c9f-98a3-e3c2e712b49f",
    "isEnabled": true,
    "createdBy": "amb_ranger_admin",
    "createTime": 1502040340000,
    "updateTime": 1502200483000,
    "version": 2,
    "type": "hive",
    "name": "STR01_hive",
    "description": "hive repo",
    "configs": {
      "tag.download.auth.users": "hive",
      "password": "*****",
      "policy.download.auth.users": "hive",
      "policy.grantrevoke.auth.users": "hive",
      "jdbc.driverClassName": "org.apache.hive.jdbc.HiveDriver",
      "ambari.service.check.user": "ambari-qa",
      "jdbc.url": "jdbc:hive2://os02.streever.local:2181,os03.streever.local:2181,os01.streever.local:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2",
      "username": "hive"
    },
    "policyVersion": 12,
    "policyUpdateTime": 1504812330000,
    "tagVersion": 1,
    "tagUpdateTime": 1502040340000
  }
]
  Copy Request    Copy Response
Developed by Paul HitzHome | Feedback | Issues | Donate | License | Back To Top
```


``` http://os02.streever.local:6080/service/public/v2/api/service/STR01_hadoop/policy```



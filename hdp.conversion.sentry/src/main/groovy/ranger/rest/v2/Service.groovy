package ranger.rest.v2
/*
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
  }
 */

class Service {
    int id
    String guid
    boolean isEnabled
    String createdBy
    long createTime
    long updateTime
    int version
    String type
    String name
    String description
    Map configs
    int policyVersion
    long policyUpdateTime
    int tagVersion
    long tagUpdateTime
}

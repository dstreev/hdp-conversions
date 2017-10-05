#!/usr/bin/env groovy
import groovy.json.JsonSlurper
import groovy.sql.Sql
import ranger.rest.v2.HDFSPolicy
import ranger.rest.v2.Service

/*
Required Parameters

- Ranger
    - URL
    - User
    - Password
- Source File
- services (comma seperated value 'hdfs,hive')
If more than a single service exists for a "type", then we need to specify which service to integrate with.
-<service_type> (hive,hdfs)=<service_name>

Process:
Pull ranger.rest.v2.Service Information and validate against request:
    - If there's more than a single service per type, then they must include the -service-type=<service_name> input
    - Type exists
    - ranger.rest.v2.Service Name matches request (if more than one)
    - Get the ranger.rest.v2.Service ID for future requests.


Pull

For each input request:
    - Search for existing policy in service



 */

for (a in this.args)
    println a

def test1 = '{\n' +
        '  "id": 1,\n' +
        '  "isEnabled": true,\n' +
        '  "version": 2,\n' +
        '  "service": "STR01_hadoop",\n' +
        '  "name": "all - path",\n' +
        '  "policyType": 0,\n' +
        '  "description": "ranger.rest.v2.Policy for all - path",\n' +
        '  "isAuditEnabled": true,\n' +
        '  "resources": {\n' +
        '    "path": {\n' +
        '      "values": [\n' +
        '        "/*"\n' +
        '      ],\n' +
        '      "isExcludes": false,\n' +
        '      "isRecursive": true\n' +
        '    }\n' +
        '  },\n' +
        '  "policyItems": [\n' +
        '    {\n' +
        '      "accesses": [\n' +
        '        {\n' +
        '          "type": "read",\n' +
        '          "isAllowed": true\n' +
        '        },\n' +
        '        {\n' +
        '          "type": "write",\n' +
        '          "isAllowed": true\n' +
        '        },\n' +
        '        {\n' +
        '          "type": "execute",\n' +
        '          "isAllowed": true\n' +
        '        }\n' +
        '      ],\n' +
        '      "users": [\n' +
        '        "hdfs",\n' +
        '        "ambari-qa"\n' +
        '      ],\n' +
        '      "groups": [],\n' +
        '      "conditions": [],\n' +
        '      "delegateAdmin": true\n' +
        '    }\n' +
        '  ],\n' +
        '  "denyPolicyItems": [],\n' +
        '  "allowExceptions": [],\n' +
        '  "denyExceptions": [],\n' +
        '  "dataMaskPolicyItems": [],\n' +
        '  "rowFilterPolicyItems": []\n' +
        '}'

def parsed = new JsonSlurper().parseText(test1)

def hdfsPolicy = new HDFSPolicy(parsed)

println hdfsPolicy.name


def test2 = "  {\n" +
        "    \"id\": 1,\n" +
        "    \"guid\": \"a2978cfc-964c-4429-9074-c49ef143efb0\",\n" +
        "    \"isEnabled\": true,\n" +
        "    \"createdBy\": \"amb_ranger_admin\",\n" +
        "    \"createTime\": 1502039792000,\n" +
        "    \"updateTime\": 1502200483000,\n" +
        "    \"version\": 2,\n" +
        "    \"type\": \"hdfs\",\n" +
        "    \"name\": \"STR01_hadoop\",\n" +
        "    \"description\": \"hdfs repo\",\n" +
        "    \"configs\": {\n" +
        "      \"tag.download.auth.users\": \"hdfs\",\n" +
        "      \"password\": \"*****\",\n" +
        "      \"policy.download.auth.users\": \"hdfs\",\n" +
        "      \"hadoop.rpc.protection\": \"authentication\",\n" +
        "      \"hadoop.security.authentication\": \"simple\",\n" +
        "      \"fs.default.name\": \"hdfs://STR01\",\n" +
        "      \"hadoop.security.authorization\": \"false\",\n" +
        "      \"hadoop.security.auth_to_local\": \"DEFAULT\",\n" +
        "      \"ambari.service.check.user\": \"ambari-qa\",\n" +
        "      \"username\": \"hdfs\"\n" +
        "    },\n" +
        "    \"policyVersion\": 10,\n" +
        "    \"policyUpdateTime\": 1505156587000,\n" +
        "    \"tagVersion\": 1,\n" +
        "    \"tagUpdateTime\": 1502039792000\n" +
        "  }"

def parsed1 = new JsonSlurper().parseText(test2)

def service = new Service(parsed1)

println service.name

def sql = Sql.newInstance("jdbc:hive2://localhost:10000/dstreev_priv", "dstreev",
        "*", "org.apache.hive.jdbc.HiveDriver")

def hive_sql_resource = '/sql/hive-sentry.sql'

def hive_sql = new File(getClass().getResource(hive_sql_resource).toURI()).getText('UTF-8')

println "Group\t\tScope\tDatabase\tTable\tColumn\tAction\tWith_Grant"

sql.eachRow(hive_sql){ row ->
    println row.grp + "\t\t" + row.scope + "\t" + row.db + "\t" + row.table_name + "\t" + row.column_name + "\t" + row.action + "\t" + row.with_grant
}

package ranger.rest.v2

import groovy.util.logging.Log4j

/*
  {
    "id": 4,
    "isEnabled": true,
    "version": 3,
    "service": "STR01_hive",
    "name": "all - url",
    "policyType": 0,
    "description": "ranger.rest.v2.Policy for all - url",
    "isAuditEnabled": true,
    "resources": {
      "url": {
        "values": [
          "*"
        ],
        "isExcludes": false,
        "isRecursive": false
      }
    },
    "policyItems": [
      {
        "accesses": [
          {
            "type": "select",
            "isAllowed": true
          },
          {
            "type": "update",
            "isAllowed": true
          },
          {
            "type": "create",
            "isAllowed": true
          },
          {
            "type": "drop",
            "isAllowed": true
          },
          {
            "type": "alter",
            "isAllowed": true
          },
          {
            "type": "index",
            "isAllowed": true
          },
          {
            "type": "lock",
            "isAllowed": true
          },
          {
            "type": "all",
            "isAllowed": true
          },
          {
            "type": "read",
            "isAllowed": true
          },
          {
            "type": "write",
            "isAllowed": true
          }
        ],
        "users": [
          "hive",
          "ambari-qa",
          "dstreev"
        ],
        "groups": [],
        "conditions": [],
        "delegateAdmin": true
      }
    ],
    "denyPolicyItems": [],
    "allowExceptions": [],
    "denyExceptions": [],
    "dataMaskPolicyItems": [],
    "rowFilterPolicyItems": []
  }
 */

@Log4j
class HivePolicy extends Policy {
    HiveResource resources


//    ranger.rest.v2.ranger.rest.HivePolicy (groovy.sql.GroovyRowResult row) {
//        println row.grp + "\t\t" + row.scope + "\t" + row.db + "\t" + row.table_name + "\t" + row.column_name + "\t" + row.action + "\t" + row.with_grant
//    }

    /*
    CREATE ranger.rest.v2.Policy
    {
    "allowExceptions": [],
    "denyExceptions": [],
    "dataMaskPolicyItems": [],
    "rowFilterPolicyItems": [],
    "denyPolicyItems": [],
    "description": "ranger.rest.v2.Policy for ranger.rest.v2.Service: cl1_test",
    "isAuditEnabled": true,
    "isEnabled": true,
    "name": "cl1_test-1",
    "policyItems": [
        {
            "accesses": [
                {
                    "isAllowed": true,
                    "type": "select"
                },
                {
                    "isAllowed": true,
                    "type": "update"
                },
                {
                    "isAllowed": true,
                    "type": "create"
                },
                {
                    "isAllowed": true,
                    "type": "drop"
                }
            ],
            "conditions": [],
            "delegateAdmin": true,
            "groups": ["public"],
            "users": [
            ]
        }
    ],
    "resources": {
        "root": {
            "isExcludes": false,
            "isRecursive": false,
            "values": [
                "abc"
            ]
        },
        "sub": {
            "isExcludes": false,
            "isRecursive": false,
            "values": [
                "*"
            ]
        }
    },
    "service": "cl1_test",
    "version": 1
}
     */

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        HivePolicy that = (HivePolicy) o

        if (resources != that.resources) return false

        return true
    }

    int hashCode() {
        return (resources != null ? resources.hashCode() : 0)
    }

    static HivePolicy buildFromRow(service, row) {
        log.debug row.grp + "\t\t" + row.scope + "\t" + row.db + "\t" + row.table_name + "\t" + row.column_name + "\t" + row.action + "\t" + row.with_grant
        HivePolicy rtn = new HivePolicy()
        rtn.service = service
        rtn.isAuditEnabled = true
        rtn.name = "Sentry.import - " + row.grp + "." + row.db + "." + row.table_name + "." + row.column_name
        rtn.description = "Sentry.import - " + row.grp + "." + row.db + "." + row.table_name + "." + row.column_name
        rtn.isEnabled = true
        rtn.version = 1

        rtn.resources = new HiveResource()
        rtn.resources.database = new PolicyResource()
        rtn.resources.database.values.add(row.db)
        rtn.resources.database.isRecursive = false

        rtn.resources.table = new PolicyResource()
        rtn.resources.table.values.add(row.table_name)
        rtn.resources.table.isRecursive = false

        rtn.resources.column = new PolicyResource()
        rtn.resources.column.values.add(row.column_name)
        rtn.resources.column.isRecursive = false

        def policyItem = new ResourcePolicyItem()
        rtn.policyItems = []
        rtn.policyItems.add(policyItem)
        ["select","update","create","drop","alter","index","lock","all","write",].each{ type ->
            def access = new PolicyAccess(type: type, isAllowed: true)
            policyItem.accesses.add(access)
        }
        // Only deal with Groups.  Sentry doesn't seem to support users.
        policyItem.groups.add(row.grp)
        if (row.with_grant != 'N') {
            policyItem.delegateAdmin = true
        }
        return rtn
    }
}

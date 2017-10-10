package ranger.rest.v2

import groovy.util.logging.Log4j

/*
{
  "id": 1,
  "isEnabled": true,
  "version": 2,
  "service": "STR01_hadoop",
  "name": "all - path",
  "policyType": 0,
  "description": "ranger.rest.v2.Policy for all - path",
  "isAuditEnabled": true,
  "resources": {
    "path": {
      "values": [
        "/*"
      ],
      "isExcludes": false,
      "isRecursive": true
    }
  },
  "policyItems": [
    {
      "accesses": [
        {
          "type": "read",
          "isAllowed": true
        },
        {
          "type": "write",
          "isAllowed": true
        },
        {
          "type": "execute",
          "isAllowed": true
        }
      ],
      "users": [
        "hdfs",
        "ambari-qa"
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
class HDFSPolicy extends Policy {
    HDFSResource resources

    static HDFSPolicy buildFromRow(service, row) {
        log.debug row.grp + "\t\t" + row.scope + "\t" + row.path
        HDFSPolicy rtn = new HDFSPolicy()
        rtn.service = service
        rtn.isAuditEnabled = true
        rtn.name = "Sentry.import - " + "." + row.path
        rtn.description = "Sentry.import - " + "." + row.path
        rtn.isEnabled = true
        rtn.version = 1

        rtn.resources = new HDFSResource()
        rtn.resources.path = new PolicyResource()
        rtn.resources.path.values.add(row.path)
        rtn.resources.path.isRecursive = true

        def policyItem = new ResourcePolicyItem()
        rtn.policyItems = []
        rtn.policyItems.add(policyItem)
        ["read","write","execute"].each{ type ->
            def access = new PolicyAccess(type: type, isAllowed: true)
            policyItem.accesses.add(access)
        }
        // Only deal with Groups.  Sentry doesn't seem to support users.
        def grpWrking = row.grp
        grpWrking.substring(1,grpWrking.length()-1).split(",").each { rec ->
            def group = rec.substring(1,rec.length()-1)
            log.debug "Group: " + group
            policyItem.groups.add(group)
        }

        return rtn
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        HDFSPolicy that = (HDFSPolicy) o

        if (resources != that.resources) return false

        return true
    }

    int hashCode() {
        return (resources != null ? resources.hashCode() : 0)
    }
}

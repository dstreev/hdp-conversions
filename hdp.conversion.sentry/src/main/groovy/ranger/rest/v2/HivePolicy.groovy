package ranger.rest.v2

import groovy.util.logging.Log4j

@Log4j
class HivePolicy extends Policy {
    HiveResource resources

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

        switch(row.action) {
            case "select":
                ["select"].each{ type ->
                    def access = new PolicyAccess(type: type, isAllowed: true)
                    policyItem.accesses.add(access)
                }
                break
            case "insert":
                ["select","update","write"].each{ type ->
                    def access = new PolicyAccess(type: type, isAllowed: true)
                    policyItem.accesses.add(access)
                }
                break
            case "*":
                ["select","update","create","drop","alter","index","lock","all","write",].each{ type ->
                    def access = new PolicyAccess(type: type, isAllowed: true)
                    policyItem.accesses.add(access)
                }
                break
            default:
                ["select","update","create","drop","alter","index","lock","all","write",].each{ type ->
                    def access = new PolicyAccess(type: type, isAllowed: true)
                    policyItem.accesses.add(access)
                }
                break
        }
        // Only deal with Groups.  Sentry doesn't seem to support users.
        policyItem.groups.add(row.grp)
        if (row.with_grant != 'N') {
            policyItem.delegateAdmin = true
        }
        return rtn
    }
}

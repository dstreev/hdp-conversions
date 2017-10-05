package ranger.rest.v2
/*
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

 */

class ResourcePolicyItem {
    def accesses = []
    def users = []
    def groups = []
    def conditions = []
    boolean delegateAdmin = false
}

package ranger.rest.v2
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

class HDFSPolicy extends Policy {
    HDFSResource resources
}

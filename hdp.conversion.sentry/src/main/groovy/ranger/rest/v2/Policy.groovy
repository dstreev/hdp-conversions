package ranger.rest.v2

import groovy.util.logging.Log4j
import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator

@Log4j
class Policy {
    String name
    Integer id
    Integer version
    String service
    boolean isEnabled
    Integer policyType
    String description
    boolean isAuditEnabled
    def policyItems
    def denyPolicyItems
    def allowExceptions
    def denyExceptions
    def dataMaskPolicyItems
    def rowFilterPolicyItems

}

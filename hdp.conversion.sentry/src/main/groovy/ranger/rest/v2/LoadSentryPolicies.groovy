package ranger.rest.v2

import groovy.json.JsonGenerator
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Log4j
import groovyjarjarcommonscli.HelpFormatter
import util.RESTUtil

@Log4j
class LoadSentryPolicies {

    def options
    def db_username
    def db_password
    def jdbc_driver
    def both
    def RESTEndpoint
    def rangerServices = []
    def hiveServiceName
    def hdfsServiceName
    int hdfscount = 0
    int hivecount = 0
    def String hdfsNameService
    def rangerBaseUrl
    def deletePolicy = false
    def delMin = -1
    def delMax = -1
    def dryrun = false

    static void main(String... args) {
        def loadSentryPolicies = new LoadSentryPolicies()
        loadSentryPolicies.execute(args)
    }

    def init(String[] args) {
        def cli = new CliBuilder(usage: 'LoadSentryPolicies')
        cli.url(args: 1, argName: 'URL', required: true, 'Ranger Base URL')
        cli.jurl(args: 1, argName: 'jdbc_url', required: false, 'HS2 JDBC URL for Sentry ranger.rest.v2.Policy Extracts')
        cli.ju(args: 1, argName: 'jdbc_username', required: false, 'Hive User')
        cli.jp(args: 1, argName: 'jdbc_password', required: false, 'Hive Password')
        cli.u(longOpt: 'user', args: 1, argName: 'user', required: true, 'User account')
        cli.p(longOpt: 'password', args: 1, argName: 'password', required: true, 'User password')
        cli.dr(longOpt: 'dryrun', args: 0, required: false, 'Dry Run')
        cli.h(longOpt: 'hive', args: 0, required: false, 'Process hive policies.')
        cli.dfs(longOpt: 'hdfs', args: 0, required: false, 'Process hdfs policies.')
        cli.dfsns(longOpt: 'hdfs_nameservice', args: 1, required: false, 'HDFS Nameservice IE: hdfs://<nameservice>/user/hdfs.')
        //cli.debug(longOpt: 'debug', args: 0, required: false, 'Debug run to a file.')
        cli.hs(longOpt: 'hive.service', args: 1, required: false, argName: "Hive_Service", "Hive Repo ranger.rest.v2.Service name to apply changes")
        cli.dfss(longOpt: 'hdfs.service', args: 1, required: false, argName: "HDFS_Service", "HDFS Repo ranger.rest.v2.Service name to apply changes")
        cli.delete(longOpt: 'delete.policy', args: 2, valueSeparator: " ", required: false, argName: "Delete_Policy_Range", "Delete Policy Range")

        options = cli.parse(args)

        if (!options)
            System.exit(-1)

        if (options.delete) {
            deletePolicy = true
            delMin = options.deletes[0].toInteger()
            delMax = options.deletes[1].toInteger()
            log.warn("Delete Process Called.  Range: " + delMin + ":" + delMax)
        } else if (!options.jurl) {
            log.error("Missing -jurl")
            System.exit(-1)
        }

        if (options.dr) {
            dryrun = true
            log.info("==================")
            log.info(" ** DRY RUN **    ")
            log.info("==================")
        }

        db_username = options.ju ?: 'anonymous'
        db_password = options.jp ?: '*'
        jdbc_driver = options.jd ?: 'org.apache.hive.jdbc.HiveDriver'

        both = (!options.h & !options.dfs)

        hdfsNameService = options.dfsns

        if (options.h == null && options.dfs == null) {
            println "Please specify at least one policy type to import."
            return -1
        }

        rangerBaseUrl = options.url

        log.trace("Establishing REST Endpoint at: " + options.url)
        RESTEndpoint = new RESTUtil(baseUrl: rangerBaseUrl, username: options.u, password: options.p)

    }

    def getServices() {
        def servicePath = "/service/public/v2/api/service"
        log.trace("Retrieving Service List from endpoint at path: " + servicePath)
        def response = RESTEndpoint.getResponse(servicePath)

        if (response == null) {
            log.error("Unable to connect to Ranger REST Services at: " + rangerBaseUrl + servicePath);
            log.error("CHECK URL, Service or Credentials")
            System.exit(-1)
        }

        def parsed = new JsonSlurper().parseText(response)

        log.trace("Service List Size: " + parsed.size)

        parsed.each { serviceMap ->
            def service = new Service(serviceMap)
            rangerServices.add(service)
        }

        // Check if there is a service for each of the processing requests.
        rangerServices.each { service ->
            if (options.h | both) {
                if (service.type == 'hive') {
                    hivecount++
                    hiveServiceName = service.name
                }
            }
            if (options.dfs | both) {
                if (service.type == 'hdfs') {
                    hdfscount++
                    hdfsServiceName = service.name
                }
            }
        }

        log.info("HDFS Service Name: " + hdfsServiceName + " - " + hdfscount)
        log.info("Hive Service Name: " + hiveServiceName + " - " + hivecount)

        // Check to see if multiple services are defined for each type.
        if (options.h | both) {
            switch (hivecount) {
                case 0:
                    log.error "No 'hive' services defined in Ranger"
                    return -2
                case 1:
                    break
                default:
                    if (options.hs) {
                        hiveServiceName = options.hs.value
                    } else {
                        // Need to exit and request user specify the hive service to use for process.
                        log.error "There are multiple Hive Services defined. Please specify which one to use for processing (-hs)"
                        return -3
                    }
            }
        }

        // Check to see if multiple services are defined for each type.
        if (options.dfs | both) {
            switch (hdfscount) {
                case 0:
                    log.error "No 'hdfs' services defined in Ranger"
                    return -2
                case 1:
                    break
                default:
                    if (options.dfss) {
                        hdfsServiceName = options.dfs.value
                    } else {
                        // Need to exit and request user specify the hive service to use for process.
                        log.error "There are multiple HDFS Services defined. Please specify which one to use for processing (-dfss)"
                        return -3
                    }
            }
        }

    }

    def processHdfs() {
        log.info "Processing HDFS"
        // Counters
        int curr = 0
        int new_ = 0
        int newErr = 0
        int matched = 0
        int imp = 0

        def currentHdfsPolicies = []

        // Get the Policies for HDFS
        def hdfsPolicyRESTPath = '/service/public/v2/api/service/' + hdfsServiceName + '/policy'

        log.debug "Getting HDFS Policy List from: " + hdfsServiceName
        def response = RESTEndpoint.getResponse(hdfsPolicyRESTPath)
        log.trace "HDFS Policy List: " + response

        def parsed = new JsonSlurper().parseText(response)

        parsed.each { policyMap ->
            log.trace("Policy Map: " + policyMap)
            def hdfsPolicy = new HDFSPolicy(policyMap)
            currentHdfsPolicies.add(hdfsPolicy)
            curr++
        }
        log.info "HDFS policies loaded: " + curr

        def sql = Sql.newInstance(options.jurl, db_username,
                db_password, jdbc_driver)

        def hdfs_sql_resource = '/sql/hdfs-sentry.sql'

        def hdfs_sql_template = getClass().getResourceAsStream(hdfs_sql_resource).text

        Map model = [
                nameservice: hdfsNameService
        ]

        String hdfs_sql = new SimpleTemplateEngine().createTemplate(hdfs_sql_template)
                .make(model).toString()

        log.debug("HDFS SQL: " + hdfs_sql)

        log.debug "Group\tScope\tPath"

        def inboundHdfsPolicies = []

        def policyRESTPath = '/service/public/v2/api/policy'

        def generator = new JsonGenerator.Options().excludeNulls().excludeFieldsByName('log').build()

        sql.eachRow(hdfs_sql) { row ->
            // Increment Import Hive Counter
            imp++
            // Build ranger.rest.v2.Policy Record
            def hdfsCreatePolicy = HDFSPolicy.buildFromRow(hdfsServiceName, row)

            // Add it to list of inbound policies.
            inboundHdfsPolicies.add(hdfsCreatePolicy)

            def found = doesPolicyExistAlready(hdfsCreatePolicy, currentHdfsPolicies)

            if (!found) {
                // Post ranger.rest.v2.Policy
                def postContent = generator.toJson(hdfsCreatePolicy)
                log.info("==========================")
                log.info("Posting: " + postContent)
                log.info("==========================")
                log.info("Posting Policy: " + hdfsCreatePolicy.name)
                if (!dryrun) {
                    def success = RESTEndpoint.post(policyRESTPath, postContent)

                    if (success) {
                        new_++
                    } else {
                        newErr++
                        log.warn("Issue posting policy: " + hdfsCreatePolicy.name)
                    }
                } else {
                    log.info("Dry Run.  Nothing written.")
                }

                log.info("==========================")
                log.info("Policy: " + hdfsCreatePolicy.name)
                log.info("Post Response: " + response)
            } else {
                log.debug("Found a resource match for policy: " + hdfsCreatePolicy.name)
                matched++
            }
        }

        log.info "Current : " + curr
        log.info "Imported: " + imp
        log.info "Matched : " + matched
        log.info "New     : " + new_
        log.info "Failed  : " + newErr

    }

    def processHive() {
        log.info("Processing Hive")

        int curr = 0
        int new_ = 0
        int newErr = 0
        int matched = 0
        int imp = 0

        def currentHivePolicies = []

        // Get the Policies for Hive
        def hivePolicyRESTPath = '/service/public/v2/api/service/' + hiveServiceName + '/policy'

        log.debug "Getting Hive Policy List from: " + hiveServiceName
        def response = RESTEndpoint.getResponse(hivePolicyRESTPath)
        log.trace "Hive Policy List: " + response

        def parsed = new JsonSlurper().parseText(response)

        parsed.each { policyMap ->
            log.trace("Policy Map: " + policyMap)
            def hivePolicy = new HivePolicy(policyMap)
            currentHivePolicies.add(hivePolicy)
            curr++
        }
        log.info "Hive policies loaded: " + curr

        def sql = Sql.newInstance(options.jurl, db_username,
                db_password, jdbc_driver)

        def hive_sql_resource = '/sql/hive-sentry.sql'

        def hive_sql = getClass().getResourceAsStream(hive_sql_resource).text

        log.debug "Group\t\tScope\tDatabase\tTable\tColumn\tAction\tWith_Grant"

        def inboundHivePolicies = []

        def policyRESTPath = '/service/public/v2/api/policy'

        def generator = new JsonGenerator.Options().excludeNulls().excludeFieldsByName('log').build()

        sql.eachRow(hive_sql) { row ->
            // Increment Import Hive Counter
            imp++
            // Build ranger.rest.v2.Policy Record
            def hiveCreatePolicy = HivePolicy.buildFromRow(hiveServiceName, row)
            // Add it to list of inbound policies.
            inboundHivePolicies.add(hiveCreatePolicy)

            def found = doesPolicyExistAlready(hiveCreatePolicy, currentHivePolicies)

            if (!found) {
                // Post ranger.rest.v2.Policy
                def postContent = generator.toJson(hiveCreatePolicy)
                log.info("==========================")
                log.info("Posting: " + postContent)
                log.info("==========================")
                log.info("Posting Policy: " + hiveCreatePolicy.name)
                if (!dryrun) {
                    def success = RESTEndpoint.post(policyRESTPath, postContent)

                    if (success) {
                        new_++
                    } else {
                        newErr++
                        log.warn("Issue posting policy: " + hiveCreatePolicy.name)
                    }
                } else {
                    log.info ("Dry Run. Nothing written")
                }

                log.info("==========================")
                log.info("Policy: " + hiveCreatePolicy.name)
                log.info("Post Response: " + response)
            } else {
                log.debug("Found a resource match for policy: " + hiveCreatePolicy.name)
                matched++
            }
        }

        log.info "Current : " + curr
        log.info "Imported: " + imp
        log.info "Matched : " + matched
        log.info "New     : " + new_
        log.info "Failed  : " + newErr

    }

    def removePolicyRange() {
        // Get the Policies for Hive

        def range = delMin..delMax
        range.each { i ->
            def policyDeletePath = '/service/public/v2/api/policy/' + i

            log.info("Deleting Policy with id: " + i)
            if (!dryrun) {
                def response = RESTEndpoint.delete(policyDeletePath)
                log.trace "Delete Response: " + response
            } else {
                log.info("Dry Run. Nothing Deleted.")
            }
        }
    }

    def execute(String[] args) {
        init(args)

        if (deletePolicy) {
            removePolicyRange()
            return
        }

        getServices()

        if (options.dfs | both) {
            if (options.dfsns) {
                processHdfs()
            } else {
                log.error("Please specify an HDFS (-dfsns)'nameservice'.")
                System.exit(-1)
            }
        }

        if (options.h | both) {
            processHive()
        }
    }

    boolean doesPolicyExistAlready(importPolicy, currentPolicies) {
        def rtn = false
        currentPolicies.each { currentPolicy ->
            if (currentPolicy.equals(importPolicy)) {
                rtn = true
            }
        }
        return rtn
    }

}

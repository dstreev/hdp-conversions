package ranger.rest.v2

import groovy.json.JsonGenerator
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Log4j
import util.RESTUtil

@Log4j
class LoadSentryPolicies {

    static void main(String... args) {
        def loadSentryPolicies = new LoadSentryPolicies()
        loadSentryPolicies.execute(args)
    }

    def execute(String[] args) {

        def cli = new CliBuilder(usage:'LoadSentryPolicies')
        cli.url(args: 1, argName: 'URL', required: true, 'Ranger Base URL')
        cli.jurl(args: 1, argName: 'jdbc_url', required: true, 'Hive URL for Sentry ranger.rest.v2.Policy Extracts')
        cli.ju(args:1, argName: 'jdbc_username', required: false, 'Hive User')
        cli.jp(args:1, argName: 'jdbc_password', required: false, 'Hive User')
        cli.u(longOpt:'user', args: 1, argName: 'user', required: true, 'User account')
        cli.p(longOpt:'password', args: 1, argName: 'password',required: true, 'User password')
        cli.h(longOpt:'hive', args: 0, required:false, 'Process hive policies.')
        cli.dfs(longOpt:'hdfs', args: 0, required:false, 'Process hdfs policies.')
        cli.debug(longOpt:'debug', args: 0, required:false, 'Debug run to a file.')
        cli.hs(longOpt:'hive.service', args:1, required:false, argName: "Hive_Service", "Hive Repo ranger.rest.v2.Service name to apply changes")
        cli.dfss(longOpt:'hdfs.service', args:1, required:false, argName: "HDFS_Service", "HDFS Repo ranger.rest.v2.Service name to apply changes")
        def options = cli.parse(args)

        if (!options)
            System.exit(-1)

        def db_username = options.ju ?: 'anonymous'
        def db_password = options.jp ?: '*'
        def jdbc_driver = options.jd ?: 'org.apache.hive.jdbc.HiveDriver'

        def both = (!options.h & !options.dfs)

        if (options.h == null && options.dfs == null) {
            println "Please specify at least one policy type to import."
            return -1
        }

// Setup Sign in
        String userPassword = options.u + ":" + options.p;
        String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());

        def rangerBaseUrl = options.url

//def fullUrl = options.url + "/service/public/v2/api/service"
        log.trace("Establishing REST Endpoint at: " + options.url)
        def RESTEndpoint = new RESTUtil(baseUrl: options.url, username: options.u, password: options.p)


        def servicePath = "/service/public/v2/api/service"
        log.trace("Retrieving Service List from endpoint at path: " + servicePath)
        def response = RESTEndpoint.getResponse(servicePath)

        def parsed = new JsonSlurper().parseText(response)

        log.trace("Service List Size: " + parsed.size)

        def services = []

        parsed.each { serviceMap ->
            def service = new Service(serviceMap)
            services.add(service)
        }

        int hivecount = 0
        int hdfscount = 0

        def hiveServiceName
        def hdfsServiceName

// Check if there is a service for each of the processing requests.
        services.each { service ->
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

        log.info ("HDFS Service Name: " + hdfsServiceName)
        log.info ("Hive Service Name: " + hiveServiceName)

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
                        log.error  "There are multiple Hive Services defined. Please specify which one to use for processing (-hs)"
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

// Counters
        int currHive = 0
        int currHdfs = 0
        int newHive = 0
        int newHdfs = 0
        int newErrHive = 0
        int newErrHdfs = 0
//int updHive = 0
//int updHdfs = 0
        int matchedHive = 0
        int matchedHdfs = 0
        int impHive = 0
        int impHdfs = 0
//int processedNewHive = 0
//int processedNewHdfs = 0
//int processUpdHive = 0
//int processUpdHdfs = 0


        def currentHivePolicies = []
        def currentHdfsPolicies = []
        def newHivePolicies = []
        def newHdfsPolicies = []
        def updateHivePolicies = []
        def updateHdfsPolicies = []

//def httpRequest = new util.RESTUtil(baseUrl: hivePolicyUrl, username: options.u, password: options.p)

// Get the Policies for Hive
        if (options.h | both) {
            def hivePolicyRESTPath = '/service/public/v2/api/service/' + hiveServiceName + '/policy'

            log.debug "Getting Hive Policy List from: " + hiveServiceName
            response = RESTEndpoint.getResponse(hivePolicyRESTPath)
            log.trace "Hive Policy List: " + response

            parsed = new JsonSlurper().parseText(response)

            parsed.each { policyMap ->
                log.trace ("Policy Map: " + policyMap)
                def hivePolicy = new HivePolicy(policyMap)
                currentHivePolicies.add(hivePolicy)
                currHive++
            }
            log.info "Hive policies loaded: " + currHive
        }

// Get the Policies for HDFS
        if (options.dfs | both) {
            def hdfsPolicyRESTPath = '/service/public/v2/api/service/' + hdfsServiceName + '/policy'

            log.debug "Getting HDFS Policy List from: " + hdfsServiceName
            response = RESTEndpoint.getResponse(hdfsPolicyRESTPath)
            log.trace "HDFS Policy List: " + response

            parsed = new JsonSlurper().parseText(response)

            parsed.each { policyMap ->
                log.trace ("Policy Map: " + policyMap)
                def hdfsPolicy = new HDFSPolicy(policyMap)
                currentHdfsPolicies.add(hdfsPolicy)
                currHdfs++
            }
            log.info "HDFS policies loaded: " + currHdfs
        }

        def sql = Sql.newInstance(options.jurl, db_username,
                db_password, jdbc_driver)

        def hive_sql_resource = '/sql/hive-sentry.sql'

        def hive_sql = getClass().getResourceAsStream(hive_sql_resource).text

        log.debug "Group\t\tScope\tDatabase\tTable\tColumn\tAction\tWith_Grant"

        def inboundHivePolicies = []

        def policyRESTPath = '/service/public/v2/api/policy'

        def generator = new JsonGenerator.Options().excludeNulls().excludeFieldsByName('log').build()

        sql.eachRow(hive_sql){ row ->
            // Increment Import Hive Counter
            impHive++
            // Build ranger.rest.v2.Policy Record
            def hiveCreatePolicy = HivePolicy.buildFromRow(hiveServiceName, row)
            // Add it to list of inbound policies.
            inboundHivePolicies.add(hiveCreatePolicy)

            def found = doesHivePolicyExistAlready(hiveCreatePolicy, currentHivePolicies)

            if (!found) {
                // Post ranger.rest.v2.Policy
                def postContent = generator.toJson( hiveCreatePolicy )
                log.debug("==========================")
                log.debug("Posting: " + postContent)
                log.debug("==========================")
                log.info("Posting Policy: " + hiveCreatePolicy.name)
                def success = RESTEndpoint.post(policyRESTPath, postContent)

                if (success) {
                    newHive++
                } else {
                    newErrHive++
                    log.warn ("Issue posting policy: " + hiveCreatePolicy.name)
                }


                log.debug("==========================")
                log.debug("Policy: " + hiveCreatePolicy.name)
                log.debug("Post Response: " + response)
            } else {
                log.debug("Found a resource match for policy: " + hiveCreatePolicy.name)
                matchedHive++
            }
        }


/*
int currHive = 0
int currHdfs = 0
int newHive = 0
int newHdfs = 0
int updHive = 0
int updHdfs = 0
int matchingHive = 0
int matchingHdfs = 0
int impHive = 0
int impHdfs = 0
int processedNewHive = 0
int processedNewHdfs = 0
int processUpdHive = 0
int processUpdHdfs = 0

 */

        log.info ("Current Policy Information")
        log.info ("-------------------------")
        log.info ("HDFS: " + currHdfs)
        log.info ("Hive: " + currHive)
        log.info ("")
        log.info ("Import Policy Information")
        log.info ("-------------------------")
        log.info ("HDFS: " + impHdfs)
        log.info ("Hive: " + impHive)
        log.info ("")
        log.info ("Matched Policy Information")
        log.info ("-------------------------")
        log.info ("HDFS: " + matchedHdfs)
        log.info ("Hive: " + matchedHive)
        log.info ("")
        log.info ("Newly Created Policy Information")
        log.info ("-------------------------")
        log.info ("HDFS: " + newHdfs)
        log.info ("Hive: " + newHive)
        log.info ("")
        log.info ("New Policy ERROR Information")
        log.info ("-------------------------")
        log.info ("HDFS: " + newErrHdfs)
        log.info ("Hive: " + newErrHive)


// Load the policy file into the appropriate policy sets.
// for each record
//      parse line
//      determine policy type
//      increment impHive,impHdfs counters.
//      build policy record
//      Look through current policy list to see if there is a matching policy
//      if matching policy
//          if changes in policy
//              add to upd list
//              increment updHive,updHdfs
//          else
//              add to no list
//              log information about matching record for debug purposes
//              increment matchingHive,matchingHdfs
//      else
//          add to new policy list
//          increment newHive,newHdfs

// print out current stats before attempting to implement through api.

// for each type...  process new records
//      build new record and baseUrl
//      PUT/POST new record
//      if return code passes
//          increment processingNewHive,processingNewHdfs counters.
//      else
//          log error and identify which policy had the issues.

// for each type...  process update records
//      build new record and baseUrl
//      PUT/POST new record
//      if return code passes
//          increment processingUpdHive,processingUpdHdfs counters.
//      else
//          log error and identify which policy had the issues.
    }

    boolean doesHivePolicyExistAlready(importPolicy, currentPolicies) {
        def rtn = false
        currentPolicies.each {currentPolicy ->
            if (currentPolicy.equals(importPolicy)) {
                rtn = true
            }
        }
        return rtn
    }

}

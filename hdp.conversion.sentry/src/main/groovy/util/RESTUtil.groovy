package util

import groovy.util.logging.Log4j
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator

@Log4j
class RESTUtil {
    String baseUrl
    String username
    String password

    CloseableHttpClient getHttpClientConnection(String url) {
        return HttpClients.createDefault()
    }

    HttpURLConnection getConnection(String url) {
        String userPassword = username + ":" + password
        String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());

        def connection = new URL( url )
                .openConnection() as HttpURLConnection

        // set some headers
        connection.setRequestProperty( 'User-Agent', 'groovy-2.4.4' )
        connection.setRequestProperty( 'Accept', 'application/json' )
        connection.setRequestProperty( 'Authorization', 'Basic ' + encoding)

        return connection
    }

    String getResponse(String restPath) {
        def connection = getConnection(baseUrl + restPath)

        connection.setRequestMethod("GET")

        // Get a List of the Services
        def response = connection.inputStream.text
        connection.disconnect()

        return response
    }

    Boolean post(String restPath, String postContent) {

        def results = false
        def connection = getConnection(baseUrl + restPath)

        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.setDoInput(true)
        connection.setRequestProperty("Content-Type", "application/json; charset=utf8")
        connection.setRequestProperty("X-XSRF-HEADER", "valid")
        connection.setRequestProperty("User-Agent","groovy-rest")

        //Send request
        DataOutputStream wr = new DataOutputStream (
                connection.getOutputStream ())
        wr.writeBytes (postContent)
        wr.flush ()
        wr.close ()

        // Get Response Code
        int responseCode = connection.getResponseCode()

        // Get Response
        def BufferedReader responseReader
        if (responseCode == 200) {
            responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
            results = true
        } else if (responseCode == 404) {
            log.error "URL Not Found: " + baseUrl + restPath
            System.exit(-1)
        } else {
            responseReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))
        }

        StringBuilder sb = new StringBuilder()
        def inputLine
        while ((inputLine = responseReader.readLine() != null)) {
            sb.append(inputLine)
        }
        responseReader.close()

        connection.disconnect()
        if (!results) {
            log.warn("Post Error: " + responseCode + " - " + sb.toString())
        } else {
            log.debug("Post Results: " + responseCode + " - " + sb.toString())
        }

        return results

    }

    Boolean delete(String restPath) {

        def results = false
        def connection = getConnection(baseUrl + restPath)

        connection.setRequestMethod("DELETE")
        connection.setDoOutput(true)
        connection.setDoInput(true)
        connection.setRequestProperty("Content-Type", "application/json; charset=utf8")
        connection.setRequestProperty("X-XSRF-HEADER", "valid")
        connection.setRequestProperty("User-Agent","groovy-rest")

        // Get Response Code
        int responseCode = connection.getResponseCode()

        // Get Response
        def BufferedReader responseReader
        if (responseCode == 200 || responseCode == 204) {
            responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))
            results = true
        } else if (responseCode == 404) {
            log.error "URL Not Found: " + baseUrl + restPath
            System.exit(-1)
        } else {
            responseReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))
        }

        StringBuilder sb = new StringBuilder()
        def inputLine
        while ((inputLine = responseReader.readLine() != null)) {
            sb.append(inputLine)
        }
        responseReader.close()

        connection.disconnect()
        if (!results) {
            log.warn("Delete Error: " + responseCode + " - " + sb.toString())
        } else {
            log.debug("Delete Results: " + responseCode + " - " + sb.toString())
        }

        return results

    }

}

package org.motechproject.nms.testing.it.api.utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.motechproject.testing.utils.TestContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Request builder helper class for web IT tests
 */
public class RequestBuilder {

    public static final String ADMIN_USERNAME = "motech";
    public static final String ADMIN_PASSWORD = "motech";

    public static String ObjectToJson(Object object) {
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();

        try {
            return writer.writeValueAsString(object);
        } catch (JsonGenerationException | JsonMappingException je) {
            return null;
        } catch (IOException io) {
            return null;
        }
    }

    public static HttpPost createPostRequest(String endpoint, Object requestBody) throws IOException{
        HttpPost postRequest = new HttpPost(endpoint);
        postRequest.setHeader("Content-type", "application/json");
        postRequest.setEntity(new StringEntity(ObjectToJson(requestBody)));
        return postRequest;
    }

    public static HttpGet createGetRequest(String endpoint) {
        HttpGet getRequest = new HttpGet(endpoint);
        return getRequest;
    }

    public static HttpDelete createDeleteRequest(String endpoint) {
        HttpDelete deleteRequest = new HttpDelete(endpoint);
        return deleteRequest;
    }

    public static String createUriWithQueryParamters(String host, int port, String path, Map<String, String> params) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(host).setPort(port).setPath(path);
                for(String key : params.keySet()) {
                    builder.setParameter(key, params.get(key));
                }

        return builder.build().toString();

    }
}

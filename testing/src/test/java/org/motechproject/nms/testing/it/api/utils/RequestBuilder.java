package org.motechproject.nms.testing.it.api.utils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;

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
}

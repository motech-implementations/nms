package org.motechproject.nms.testing.it.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.motechproject.testing.utils.TestContext;

import com.google.common.base.Joiner;

//Class contains method to create request URL for API.
public class ApiRequestHelper {

    public static HttpPost createCallDetailsPost(String serviceName,
            boolean includeCallingNumber, Long callingNumber,
            boolean includeCallId, Long callId, boolean includeOperator,
            String operator, boolean includeCircle, String circle,
            boolean includeCallStartTime, Long callStartTime,
            boolean includeCallEndTime, Long callEndTime,
            boolean includeCallDurationInPulses, Integer callDurationInPulses,
            boolean includeEndOfUsagePromptCounter,
            Integer endOfUsagePromptCounter,
            boolean includeWelcomeMessagePromptFlag,
            Boolean welcomeMessagePromptFlag, boolean includeCallStatus,
            Integer callStatus, boolean includeCallDisconnectReason,
            Integer callDisconnectReason, boolean includeContet, String content) {
        HttpPost httpPost = new HttpPost(String.format(
                "http://localhost:%d/api/%s/callDetails",
                TestContext.getJettyPort(), serviceName));
        String callDetailsJson = createCallDetailsJson(includeCallingNumber,
                callingNumber, includeCallId, callId, includeOperator,
                operator, includeCircle, circle, includeCallStartTime,
                callStartTime, includeCallEndTime, callEndTime,
                includeCallDurationInPulses, callDurationInPulses,
                includeEndOfUsagePromptCounter, endOfUsagePromptCounter,
                includeWelcomeMessagePromptFlag, welcomeMessagePromptFlag,
                includeCallStatus, callStatus, includeCallDisconnectReason,
                callDisconnectReason, includeContet, content);
        StringEntity params;
        try {
            params = new StringEntity(callDetailsJson);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(
                    "We're no expecting this kind of exception in ITs!", e);
        }
        httpPost.setEntity(params);
        httpPost.addHeader("content-type", "application/json");
        return httpPost;
    }

    private static String createCallDetailsJson(boolean includeCallingNumber,
            Long callingNumber, boolean includeCallId, Long callId,
            boolean includeOperator, String operator, boolean includeCircle,
            String circle, boolean includeCallStartTime, Long callStartTime,
            boolean includeCallEndTime, Long callEndTime,
            boolean includeCallDurationInPulses, Integer callDurationInPulses,
            boolean includeEndOfUsagePromptCounter,
            Integer endOfUsagePromptCounter,
            boolean includeWelcomeMessagePromptFlag,
            Boolean welcomeMessagePromptFlag, boolean includeCallStatus,
            Integer callStatus, boolean includeCallDisconnectReason,
            Integer callDisconnectReason, boolean includeContet, String content) {
        StringBuffer contentTemplate = new StringBuffer("{");
        ArrayList<String> array = new ArrayList<>();

        if (includeCallingNumber) {
            array.add(String.format("\"callingNumber\": %s", callingNumber));
        }

        if (includeCallId) {
            array.add(String.format("\"callId\": %s", callId));
        }

        if (includeOperator) {
            array.add(String.format("\"operator\": \"%s\"", operator));
        }

        if (includeCircle) {
            array.add(String.format("\"circle\": \"%s\"", circle));
        }

        if (includeCallStartTime) {
            array.add(String.format("\"callStartTime\": %s", callStartTime));
        }

        if (includeCallEndTime) {
            array.add(String.format("\"callEndTime\": %s", callEndTime));
        }

        if (includeCallDurationInPulses) {
            array.add(String.format("\"callDurationInPulses\": %s",
                    callDurationInPulses));
        }

        if (includeEndOfUsagePromptCounter) {
            array.add(String.format("\"endOfUsagePromptCounter\": %s",
                    endOfUsagePromptCounter));
        }

        if (includeWelcomeMessagePromptFlag) {
            array.add(String.format("\"welcomeMessagePromptFlag\": %s",
                    welcomeMessagePromptFlag));
        }

        if (includeCallStatus) {
            array.add(String.format("\"callStatus\": %s", callStatus));
        }

        if (includeCallDisconnectReason) {
            array.add(String.format("\"callDisconnectReason\": %s",
                    callDisconnectReason));
        }

        if (includeContet) {
            array.add(String.format("\"content\": [%s]", content));
        }

        contentTemplate.append(Joiner.on(",").join(array));
        contentTemplate.append("}");

        return contentTemplate.toString();
    }

}

package org.motechproject.nms.props.domain;

import java.util.regex.Pattern;

/**
 * Simple helper class to serialize & deserialize requestIds.
 * A requestId is a composite key made of a timestamp and a subscription id (which is a uuid) separated by ":",
 * for example:
 *
 *    20150513184533:58747ffc-6b7c-4abb-91d3-f099aa1bf5a3
 *
 */
public class RequestId {
    private static final int TIMESTAMP_LENGTH = 14; //YYYYMMDDHHMMSS
    public static final Pattern TIMESTAMP_PATTERN = Pattern.compile("[0-9]{14}");
    public static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private String subscriptionId;
    private String timestamp;

    //No reason to create an empty RequestId
    private RequestId() { }

    public RequestId(String subscriptionId, String timestamp) {
        this.subscriptionId = subscriptionId;
        this.timestamp = timestamp;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public static final RequestId fromString(String s) {

        String timestamp = s.substring(0, TIMESTAMP_LENGTH);
        if (!TIMESTAMP_PATTERN.matcher(timestamp).matches()) {
            throw new IllegalArgumentException(String.format("Invalid timestamp: %s", timestamp));
        }

        String subscriptionId = s.substring(TIMESTAMP_LENGTH + 1);
        if (!UUID_PATTERN.matcher(subscriptionId).matches()) {
            throw new IllegalArgumentException(String.format("Invalid subscriptionId: %s", subscriptionId));
        }

        return new RequestId(subscriptionId, timestamp);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", timestamp, subscriptionId);
    }
}

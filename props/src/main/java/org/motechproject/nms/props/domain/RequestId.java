package org.motechproject.nms.props.domain;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Simple helper class to serialize & deserialize requestIds.
 * A requestId is a composite key made of a timestamp and a subscription id (which is a uuid) separated by ":",
 * for example:
 *
 *    20150513184533:58747ffc-6b7c-4abb-91d3-f099aa1bf5a3
 *
 */
public class RequestId implements Serializable {
    private static final long serialVersionUID = 8600346000225276856L;

    private static final int TIMESTAMP_LENGTH = 14; //YYYYMMDDHHMMSS
    private static final int UUID_LENGTH = 36;
    private static final int REQUEST_ID_LENGTH = TIMESTAMP_LENGTH + UUID_LENGTH +  1;
    public static final Pattern TIMESTAMP_PATTERN = Pattern.compile("[0-9]{14}");
    public static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    private String subscriptionId;
    private String timestamp;

    //No reason to create an empty RequestId
    private RequestId() { }


    private static void validateSubscriptionId(String s) {
        if (!UUID_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException(String.format("Invalid subscriptionId: %s", s));
        }
    }


    private static void validateTimestamp(String s) {
        if (!TIMESTAMP_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException(String.format("Invalid timestamp: %s", s));
        }
    }


    public RequestId(String subscriptionId, String timestamp) {
        validateSubscriptionId(subscriptionId);
        validateTimestamp(timestamp);

        this.subscriptionId = subscriptionId;
        this.timestamp = timestamp;
    }


    public String getSubscriptionId() {
        return subscriptionId;
    }


    public String getTimestamp() {
        return timestamp;
    }


    public static RequestId fromString(String s) {

        if (s.length() != REQUEST_ID_LENGTH) {
            throw new IllegalArgumentException(String.format("Invalid string length: %s", s));

        }

        String timestamp = s.substring(0, TIMESTAMP_LENGTH);
        validateTimestamp(timestamp);

        String subscriptionId = s.substring(TIMESTAMP_LENGTH + 1);
        validateSubscriptionId(subscriptionId);

        return new RequestId(subscriptionId, timestamp);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestId requestId = (RequestId) o;

        if (!subscriptionId.equals(requestId.subscriptionId)) {
            return false;
        }
        return timestamp.equals(requestId.timestamp);

    }

    @Override
    public int hashCode() {
        int result = subscriptionId.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", timestamp, subscriptionId);
    }
}

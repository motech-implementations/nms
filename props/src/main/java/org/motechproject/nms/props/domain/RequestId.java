package org.motechproject.nms.props.domain;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("[0-9]{14}");
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile(
            "[0-9]{14}:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");


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


    public static String timestampFromDateTime(DateTime dt) {
        return TIME_FORMATTER.print(dt);
    }

    public static DateTime dtFromTimestamp(String timestamp) {
        return DateTime.parse(timestamp, TIME_FORMATTER);
    }


    public String getSubscriptionId() {
        return subscriptionId;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public DateTime getTimestampAsDateTime() {
        return dtFromTimestamp(timestamp);
    }


    public static boolean isValid(String s) {
        return REQUEST_ID_PATTERN.matcher(s).matches();
    }


    public static RequestId fromString(String s) {
        if (!REQUEST_ID_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException(String.format("Invalid requestId: %s", s));
        }

        return new RequestId(s.substring(TIMESTAMP_LENGTH + 1), s.substring(0, TIMESTAMP_LENGTH));
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

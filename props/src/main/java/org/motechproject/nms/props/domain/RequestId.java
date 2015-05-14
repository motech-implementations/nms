package org.motechproject.nms.props.domain;

/**
 * Simple helper class to serialize & deserialize requestIds.
 * A requestId is a composite key made of a file name and subscription id (which is a uuid) separated by ":",
 * for example:
 *
 *    58747ffc-6b7c-4abb-91d3-f099aa1bf5a3:OBD_20150513184533
 *
 */
public class RequestId {
    private static final int UUID_LENGTH = 36;

    private String subscriptionId;
    private String fileName;

    //No reason to create an empty RequestId
    private RequestId() { }

    public RequestId(String subscriptionId, String fileName) {
        this.subscriptionId = subscriptionId;
        this.fileName = fileName;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getFileName() {
        return fileName;
    }

    public static final RequestId fromString(String s) {
        String subscriptionId = s.substring(0, UUID_LENGTH);
        String fileIdentifier = s.substring(UUID_LENGTH + 1);
        return new RequestId(subscriptionId, fileIdentifier);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", subscriptionId, fileName);
    }
}

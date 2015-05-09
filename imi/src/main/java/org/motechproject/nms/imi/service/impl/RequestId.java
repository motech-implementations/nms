package org.motechproject.nms.imi.service.impl;

/**
 * Simple helper class to serialize & deserialize requestIds.
 * A requestId is a composite key made of two UUIDs a file identifier and subscription id separated by ":",
 * for example:
 *
 *    637a4d0e-3924-4fab-83af-bc2112ee394d:58747ffc-6b7c-4abb-91d3-f099aa1bf5a3
 *
 */
public class RequestId {
    private static final int UUID_LENGTH = 36;

    private String fileIdentifier;
    private String subscriptionId;

    //No reason to create an empty RequestId
    private RequestId() { }

    public RequestId(String fileIdentifier, String subscriptionId) {
        this.fileIdentifier = fileIdentifier;
        this.subscriptionId = subscriptionId;
    }

    public String getFileIdentifier() {
        return fileIdentifier;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public static final RequestId fromString(String s) {
        String fileIdentifier = s.substring(0, UUID_LENGTH);
        String subscriptionId = s.substring(UUID_LENGTH, UUID_LENGTH);
        return new RequestId(fileIdentifier, subscriptionId);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", fileIdentifier, subscriptionId);
    }
}

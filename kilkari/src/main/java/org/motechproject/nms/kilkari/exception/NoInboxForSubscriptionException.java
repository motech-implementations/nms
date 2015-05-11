package org.motechproject.nms.kilkari.exception;

/**
 *
 */
public class NoInboxForSubscriptionException extends Exception {

    public NoInboxForSubscriptionException(String message) {
        super(message);
    }

    public NoInboxForSubscriptionException(Exception ex, String message) {
        super(message, ex);
    }

}

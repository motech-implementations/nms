package org.motechproject.nms.kilkari.domain;

/*
 * The mode by which the subscription was created.
 */
public enum SubscriptionOrigin {
    IVR,
    MCTS_IMPORT;

    public final String getCode() {
        return this == IVR ? "I" : "M";
    }
}

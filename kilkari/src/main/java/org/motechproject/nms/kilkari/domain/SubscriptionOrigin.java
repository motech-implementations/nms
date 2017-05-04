package org.motechproject.nms.kilkari.domain;

/*
 * The mode by which the subscription was created.
 */
public enum SubscriptionOrigin {
    IVR,
    RCH_IMPORT,
    MCTS_IMPORT;

    public final String getCode() {
        if (this == IVR) {
            return "I";
        } else if (this == MCTS_IMPORT) {
            return "M";
        } else {
            return "R";
        }
    }
}

package org.motechproject.nms.flw.domain;

public enum SubscriptionOriginFlw {
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

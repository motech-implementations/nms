package org.motechproject.nms.kilkari.domain;

public enum CallStage {
    RETRY_1,
    RETRY_2,
    RETRY_LAST;

    public CallStage nextStage() {
        if (this == RETRY_1) {
            return RETRY_2;
        }
        if (this == RETRY_2) {
            return RETRY_LAST;
        }
        throw new IllegalStateException("nextStage() called on RETRY_LAST");
    }
}

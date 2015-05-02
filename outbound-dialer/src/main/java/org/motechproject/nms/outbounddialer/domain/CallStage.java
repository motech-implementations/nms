package org.motechproject.nms.outbounddialer.domain;

public enum CallStage {
    FRESH,
    RETRY_1,
    RETRY_2,
    RETRY_3,
    ABANDON
}

package org.motechproject.nms.outbounddialer.domain;

public enum CallStage {
    Fresh,
    Retry1,
    Retry2,
    Retry3,
    Abandon
}

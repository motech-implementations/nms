package org.motechproject.nms.kilkari.domain;

/**
 * The reason that a subscription was deactivated
 */
public enum DeactivationReason {
    DEACTIVATED_BY_USER,
    LIVE_BIRTH,
    MISCARRIAGE_OR_ABORTION,
    STILL_BIRTH,
    CHILD_DEATH,
    MATERNAL_DEATH,
    INVALID_NUMBER, // See https://github.com/motech-implementations/mim/issues/169
    DO_NOT_DISTURB,
    MCTS_UPDATE,
    MOHFW_REQUEST
}

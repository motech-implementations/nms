package org.motechproject.nms.imi.service.impl;

/**
 * A method for IMI to treat OBD request in different ways
 * see https://applab.atlassian.net/browse/NIP-56
 */
public enum ImiServiceId {
    CHECK_DND,
    DO_NOT_CHECK_DND;

    public static final String CHECK_DND_STRINGVAL = "S1";
    public static final String NO_NOT_CHECK_DND_STRINGVAL = "S2";

    public static String imiValue(ImiServiceId serviceId) {
        if (serviceId == CHECK_DND) {
            return CHECK_DND_STRINGVAL;
        }

        if (serviceId == DO_NOT_CHECK_DND) {
            return NO_NOT_CHECK_DND_STRINGVAL;
        }

        throw new IllegalStateException("Unexpected ImiServiceId value");
    }
}

package org.motechproject.nms.api.web.contract;

import org.apache.commons.lang3.StringUtils;

public final class LogHelper {
    private static final int MASK_LENGTH = 3;
    private LogHelper() { }

    public static String nullOrString(Object o) {
        if (o == null) {
            return "null";
        }

        return o.toString();
    }

    public static String obscure(Long callingNumber) {
        if (callingNumber == null) {
            return "null";
        }
        String s = callingNumber.toString();
        int l = s.length();
        if (s.length() > MASK_LENGTH) {
            s = StringUtils.repeat('*', l - MASK_LENGTH) + s.substring(l - MASK_LENGTH);
        }
        return s;
    }
}

package org.motechproject.nms.api.web.contract;

import org.apache.commons.lang3.StringUtils;

public final class LogHelper {
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
        if (s.length() > 3) {
            s = StringUtils.repeat('*', l - 3) + s.substring(l - 3);
        }
        return s;
    }
}

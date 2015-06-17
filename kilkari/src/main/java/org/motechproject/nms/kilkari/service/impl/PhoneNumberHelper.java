package org.motechproject.nms.kilkari.service.impl;

public final class PhoneNumberHelper {

    private static final long TEN_DIGITS_MAX = 10000000000L;

    private PhoneNumberHelper() { }

    /**
     * only use the last 10 digits of long phone numbers
     * @return
     */
    public static long truncateLongNumber(final Long msisdn) {
        if (msisdn < 0L) {
            throw new IllegalArgumentException("Negative phone number?!?");
        }
        return msisdn % TEN_DIGITS_MAX;
    }

}

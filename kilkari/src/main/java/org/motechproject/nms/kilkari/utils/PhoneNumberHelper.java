package org.motechproject.nms.kilkari.utils;

public final class PhoneNumberHelper {

    private PhoneNumberHelper() { }

    /**
     * only use the last 10 digits of long phone numbers
     * @return
     */
    public static long truncateLongNumber(final Long msisdn) {
        if (msisdn < 0L) {
            throw new IllegalArgumentException("Negative phone number?!?");
        }
        return msisdn % KilkariConstants.TEN_DIGITS_MAX;
    }

}

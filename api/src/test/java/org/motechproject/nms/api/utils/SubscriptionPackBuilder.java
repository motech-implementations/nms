package org.motechproject.nms.api.utils;


import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPackBuilder {

    public static final int PREGNANCY_PACK_WEEKS = 72;
    public static final int CHILD_PACK_WEEKS = 48;
    private static final int TWO_MINUTES = 120;
    private static final int TEN_SECS = 10;

    public static SubscriptionPack createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                                          int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            messages.add(new SubscriptionPackMessage(week, String.format("w%s_1", week),
                    String.format("w%s_1.wav", week),
                    TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(week, String.format("w%s_2", week),
                        String.format("w%s_2.wav", week),
                        TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));
            }
        }

        return new SubscriptionPack(name, type, weeks, messagesPerWeek, messages);
    }

}

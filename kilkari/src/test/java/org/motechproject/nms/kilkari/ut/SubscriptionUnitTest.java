package org.motechproject.nms.kilkari.ut;

import org.joda.time.DateTime;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.props.domain.DayOfTheWeek;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SubscriptionUnitTest {
    @Test
    public void verifyHasCompleted() {
        Subscription subscription = new Subscription(
                new Subscriber(1111111111L),
                new SubscriptionPack("pack", SubscriptionPackType.CHILD, 10, 1, null),
                SubscriptionOrigin.IVR);
        subscription.setStartDate(DateTime.now().minusDays(5));
        assertFalse(subscription.hasCompleted(DateTime.now()));
        subscription.setStartDate(DateTime.now().minusDays(71));
        assertTrue(subscription.hasCompleted(DateTime.now()));
    }

    @Test
    public void verifyDayOfTheWeek() {
        Subscription subscription = new Subscription(
                new Subscriber(1111111111L),
                new SubscriptionPack("pack", SubscriptionPackType.CHILD, 10, 1, null),
                SubscriptionOrigin.IVR);

        DateTime startDate = DateTime.now().minusDays((int) (Math.random()) * 10);
        DayOfTheWeek startDayOfTheWeek = DayOfTheWeek.fromInt(startDate.getDayOfWeek());
        subscription.setStartDate(startDate);

        assertEquals(startDayOfTheWeek, subscription.getStartDayOfTheWeek());
    }


    private SubscriptionPack createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                        int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            messages.add(new SubscriptionPackMessage(week, String.format("w%s_1", week),
                    String.format("w%s_1.wav", week)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(week, String.format("w%s_2", week),
                        String.format("w%s_2.wav", week)));
            }
        }

        return new SubscriptionPack(name, type, weeks, messagesPerWeek, messages);
    }


    @Test
    public void verifyNextScheduledMessage() {
        Subscription s = new Subscription(
                new Subscriber(1111111111L),
                createSubscriptionPack("pack", SubscriptionPackType.CHILD, 10, 1),
                SubscriptionOrigin.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setStartDate(DateTime.now());

        //day of
        assertEquals("w1_1.wav", s.nextScheduledMessage(DateTime.now()).getMessageFileName());

        //+3 days
        assertEquals("w1_1.wav", s.nextScheduledMessage(DateTime.now().plusDays(3)).getMessageFileName());

        //+9 days
        assertEquals("w2_1.wav", s.nextScheduledMessage(DateTime.now().plusDays(9)).getMessageFileName());
    }


    @Test (expected = IllegalStateException.class)
    public void verifyNextScheduledMessageFailureTooEarly() {
        Subscription subscription = new Subscription(
                new Subscriber(1111111111L),
                createSubscriptionPack("pack", SubscriptionPackType.CHILD, 10, 1),
                SubscriptionOrigin.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscription.setStartDate(DateTime.now());

        SubscriptionPackMessage msg = subscription.nextScheduledMessage(DateTime.now().minusDays(2));
        msg.getMessageFileName();
    }


    @Test (expected = IllegalStateException.class)
    public void verifyNextScheduledMessageFailureTooLate() {
        Subscription subscription = new Subscription(
                new Subscriber(1111111111L),
                createSubscriptionPack("pack", SubscriptionPackType.CHILD, 10, 1),
                SubscriptionOrigin.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscription.setStartDate(DateTime.now());

        SubscriptionPackMessage msg = subscription.nextScheduledMessage(DateTime.now().plusDays(4));
        msg.getMessageFileName();
    }
}

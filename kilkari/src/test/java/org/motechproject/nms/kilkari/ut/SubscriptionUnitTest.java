package org.motechproject.nms.kilkari.ut;

import org.joda.time.DateTime;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.*;
import org.motechproject.nms.kilkari.service.impl.SubscriptionServiceImpl;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class SubscriptionUnitTest {

    @Autowired
    private SubscriptionServiceImpl subscriptionServiceImpl = new SubscriptionServiceImpl() ;

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

        assertEquals(startDayOfTheWeek, subscription.getFirstMessageDayOfWeek());
    }


    private SubscriptionPack createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                        int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            messages.add(new SubscriptionPackMessage(String.format("w%s_1", week),
                    String.format("w%s_1.wav", week), 120));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(String.format("w%s_2", week),
                        String.format("w%s_2.wav", week), 120));
            }
        }

        return new SubscriptionPack(name, type, weeks, messagesPerWeek, messages);
    }

    @Test
    public void verifySetStatusUpdatesEndDate() {
        Subscription s = new Subscription(
                new Subscriber(1111111111L),
                createSubscriptionPack("pack", SubscriptionPackType.CHILD, 10, 1),
                SubscriptionOrigin.IVR);
        s.setStatus(SubscriptionStatus.ACTIVE);

        assertNull(s.getEndDate());

        s.setStatus(SubscriptionStatus.COMPLETED);
        assertNotNull(s.getEndDate());

        s.setStatus(SubscriptionStatus.ACTIVE);
        assertNull(s.getEndDate());

        s.setStatus(SubscriptionStatus.DEACTIVATED);
        assertNotNull(s.getEndDate());
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
    }

    @Test (expected = IllegalStateException.class)
    public void verifyNextScheduledMessageException() {
        Subscription s = new Subscription(
                new Subscriber(1111111111L),
                createSubscriptionPack("pack", SubscriptionPackType.CHILD, 10, 1),
                SubscriptionOrigin.IVR);
        s.setStatus(SubscriptionStatus.DEACTIVATED);
        s.setStartDate(DateTime.now());

        //day of
        assertEquals("w1_1.wav", s.nextScheduledMessage(DateTime.now()).getMessageFileName());

        //+3 days
        assertEquals("w1_1.wav", s.nextScheduledMessage(DateTime.now().plusDays(3)).getMessageFileName());
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


    @Test
    public void verifyNextScheduledSecondMessage() {
        Subscription subscription = new Subscription(
                new Subscriber(1111111111L),
                createSubscriptionPack("pack", SubscriptionPackType.PREGNANCY, 10, 2),
                SubscriptionOrigin.IVR);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscription.setStartDate(DateTime.now());

        SubscriptionPackMessage msg = subscription.nextScheduledMessage(DateTime.now().plusDays(4));
        assertEquals("w1_2.wav", msg.getMessageFileName());
    }

    @Test
    public void verifyRejectingMultipleActiveSubscriptionsForSameMobileNumber(){
        Long msisdn = 1111111111L;
        String motherRchID = "motherRchId";
        String childRchId = "childRchId";
        Subscription subscription1 = new Subscription(
                new Subscriber(msisdn),
                createSubscriptionPack("pack", SubscriptionPackType.PREGNANCY, 36, 2),
                SubscriptionOrigin.RCH_IMPORT);
        subscription1.setStatus(SubscriptionStatus.ACTIVE);
        HashSet<Subscription> subscriptionHashSet = new HashSet<>();
        subscriptionHashSet.add(subscription1);
        subscription1.getSubscriber().setSubscriptions(subscriptionHashSet);
        List<Subscriber> subscribers = new ArrayList<>();
        subscribers.add(subscription1.getSubscriber());
        subscription1.getSubscriber().setMother(new MctsMother(motherRchID));

        boolean flag1 = subscriptionServiceImpl.activeSubscriptionByMsisdnRch(subscribers , msisdn , SubscriptionPackType.PREGNANCY , motherRchID , null);
        assertEquals(flag1 , false);

        boolean flag2 = subscriptionServiceImpl.activeSubscriptionByMsisdnRch(subscribers , msisdn , SubscriptionPackType.PREGNANCY , null , childRchId);
        assertEquals(flag2 , true);
    }
}

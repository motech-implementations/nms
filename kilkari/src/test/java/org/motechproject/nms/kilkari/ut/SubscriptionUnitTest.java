package org.motechproject.nms.kilkari.ut;

import org.joda.time.DateTime;
import org.junit.Test;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;

import static junit.framework.TestCase.assertTrue;
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
}

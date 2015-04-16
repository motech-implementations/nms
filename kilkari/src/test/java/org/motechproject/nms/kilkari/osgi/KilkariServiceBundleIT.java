package org.motechproject.nms.kilkari.osgi;

import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Verify that KilkariService is present & functional.
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class KilkariServiceBundleIT extends BasePaxIT {

    @Inject
    private KilkariService kilkariService;
    @Inject
    private SubscriberDataService subscriberDataService;
    @Inject
    private SubscriptionPackDataService subscriptionPackDataService;
    @Inject
    private SubscriptionDataService subscriptionDataService;

    @Test
    public void testServicePresent() throws Exception {
        assertNotNull(kilkariService);
    }

    @Test
    public void testServiceFunctional() throws Exception {
        subscriptionDataService.deleteAll();
        subscriptionPackDataService.deleteAll();
        subscriberDataService.deleteAll();

        SubscriptionPack pack1 = subscriptionPackDataService.create(new SubscriptionPack("pack1"));
        SubscriptionPack pack2 = subscriptionPackDataService.create(new SubscriptionPack("pack2"));

        Subscriber subscriber = subscriberDataService.create(new Subscriber("0000000001"));

        Subscription subscription1 = subscriptionDataService.create(new Subscription("001", subscriber, pack1));
        Subscription subscription2 = subscriptionDataService.create(new Subscription("002", subscriber, pack2));

        Set<Subscription> subscriptions = kilkariService.getSubscriber("0000000001").getSubscriptions();
        Set<SubscriptionPack> packs = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            packs.add(subscription.getSubscriptionPack());
        }

        assertEquals(new HashSet<>(Arrays.asList(pack1, pack2)), packs);
    }
}

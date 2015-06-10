package org.motechproject.nms.testing.it.utils;


import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.Circle;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscriptionHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionHelper.class);
    private static final int PREGNANCY_PACK_WEEKS = 72;
    private static final int CHILD_PACK_WEEKS = 48;
    private static final int TWO_MINUTES = 120;
    private static final int TEN_SECS = 10;

    private SubscriptionService subscriptionService;
    private SubscriberDataService subscriberDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private RegionHelper regionHelper;

    public SubscriptionHelper(SubscriptionService subscriptionService,
                              SubscriberDataService subscriberDataService,
                              SubscriptionPackDataService subscriptionPackDataService,
                              LanguageDataService languageDataService,
                              CircleDataService circleDataService,
                              StateDataService stateDataService,
                              DistrictDataService districtDataService) {

        this.subscriptionService = subscriptionService;
        this.subscriberDataService = subscriberDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;

        this.regionHelper = new RegionHelper(languageDataService, circleDataService, stateDataService,
                districtDataService);
    }

    public SubscriptionPack childPack() {
        if (subscriptionPackDataService.byName("childPack") == null) {
            createSubscriptionPack("childPack", SubscriptionPackType.CHILD, CHILD_PACK_WEEKS, 1);
        }
        return subscriptionService.getSubscriptionPack("childPack");
    }

    public SubscriptionPack pregnancyPack() {
        if (subscriptionPackDataService.byName("pregnancyPack") == null) {
            createSubscriptionPack("pregnancyPack", SubscriptionPackType.PREGNANCY, PREGNANCY_PACK_WEEKS, 2);
        }
        return subscriptionService.getSubscriptionPack("pregnancyPack");
    }

    private void createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
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

        subscriptionPackDataService.create(new SubscriptionPack(name, type, weeks, messagesPerWeek, messages));
    }


    public Long makeNumber() {
        return (long) (Math.random() * 9000000000L) + 1000000000L;
    }


    public int getRandomMessageIndex(Subscription sub) {
        return (int) (Math.random() * sub.getSubscriptionPack().getMessages().size());
    }


    public int getLastMessageIndex(Subscription sub) {
        return sub.getSubscriptionPack().getMessages().size() - 1;
    }


    public String getWeekId(Subscription sub, int index) {
        return sub.getSubscriptionPack().getMessages().get(index).getWeekId();
    }


    public String getLanguageCode(Subscription sub) {
        return ((Language) subscriberDataService.getDetachedField(
                sub.getSubscriber(),"language")).getCode();
    }


    public String getCircle(Subscription sub) {
        return ((Circle) subscriberDataService.getDetachedField(
                sub.getSubscriber(),"circle")).getName();
    }


    public String getContentMessageFile(Subscription sub, int index) {
        return sub.getSubscriptionPack().getMessages().get(index).getMessageFileName();
    }


    public Subscription mksub(SubscriptionOrigin origin, DateTime startDate, SubscriptionPackType packType) {
        return mksub(origin, startDate, packType, makeNumber());
    }

    public Subscription mksub(SubscriptionOrigin origin, DateTime startDate, SubscriptionPackType packType, Long number) {

        Subscription subscription;
        Subscriber subscriber = subscriberDataService.findByCallingNumber(number);

        if (null == subscriber) {
            subscriber = subscriberDataService.create(new Subscriber(
                    number,
                    regionHelper.hindiLanguage(),
                    regionHelper.delhiCircle()
            ));
        }

        if (SubscriptionPackType.PREGNANCY == packType) {
            subscription = new Subscription(subscriber, pregnancyPack() , origin);
        } else {
            subscription = new Subscription(subscriber, childPack(), origin);
        }

        subscription.setStartDate(startDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription = subscriptionService.create(subscription);
        LOGGER.debug("Created subscription {}", subscription.toString());
        return subscription;
    }


    public Subscription mksub(SubscriptionOrigin origin, DateTime startDate) {
        return mksub(origin, startDate, SubscriptionPackType.CHILD);
    }
    
    public SubscriptionPack childPackFor2MessagePerWeek(SubscriptionPackMessageDataService subscriptionPackMessageDataService) {
    	SubscriptionPack pack = subscriptionPackDataService.byName("childPack");
    	if (pack == null) {
            createSubscriptionPack("childPack", SubscriptionPackType.CHILD, CHILD_PACK_WEEKS, 2);
        } else {
        	if(pack.getMessagesPerWeek() == 1) {
        		updateSubscriptionPack(subscriptionPackMessageDataService, pack, CHILD_PACK_WEEKS, 2);
        	}
        }
        return subscriptionService.getSubscriptionPack("childPack");
    }
    
    public SubscriptionPack pregnancyPackFor1MessagePerWeek(SubscriptionPackMessageDataService subscriptionPackMessageDataService) {
    	SubscriptionPack pack = subscriptionPackDataService.byName("pregnancyPack");
    	if (pack == null) {
            createSubscriptionPack("pregnancyPack", SubscriptionPackType.PREGNANCY, PREGNANCY_PACK_WEEKS, 1);
        } else {
        	if(pack.getMessagesPerWeek() == 2) {
        		updateSubscriptionPack(subscriptionPackMessageDataService, pack, PREGNANCY_PACK_WEEKS, 1);
        	}
        }
        return subscriptionService.getSubscriptionPack("pregnancyPack");
    }

    private void updateSubscriptionPack(SubscriptionPackMessageDataService subscriptionPackMessageDataService, 
    		SubscriptionPack pack, int packWeeks, int messagesPerWeek) {
    	for(SubscriptionPackMessage message : pack.getMessages()) {
    		subscriptionPackMessageDataService.delete(message);
    	}
    	pack.setMessagesPerWeek(messagesPerWeek);
    	pack.setMessages(genratePackMessageList(packWeeks, messagesPerWeek));
    	subscriptionPackDataService.update(pack);
	}

	private List<SubscriptionPackMessage> genratePackMessageList(int packWeeks, int messagesPerWeek) {
		List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= packWeeks; week++) {
            messages.add(new SubscriptionPackMessage(week, String.format("w%s_1", week),
                    String.format("w%s_1.wav", week),
                    TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(week, String.format("w%s_2", week),
                        String.format("w%s_2.wav", week),
                        TWO_MINUTES - TEN_SECS + (int) (Math.random() * 2 * TEN_SECS)));
            }
        }
        return messages;
	}
}

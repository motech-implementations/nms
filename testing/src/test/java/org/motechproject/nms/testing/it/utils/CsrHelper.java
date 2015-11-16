package org.motechproject.nms.testing.it.utils;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.region.service.DistrictService;
import org.motechproject.nms.region.service.LanguageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsrHelper {
    private final String TIMESTAMP;

    private SubscriptionHelper sh;

    private List<CallSummaryRecordDto> records;


    public CsrHelper(
            String timestamp,
            SubscriptionService subscriptionService,
            SubscriptionPackDataService subscriptionPackDataService,
            SubscriberDataService subscriberDataService,
            LanguageDataService languageDataService,
            LanguageService languageService,
            CircleDataService circleDataService,
            StateDataService stateDataService,
            DistrictDataService districtDataService,
            DistrictService districtService
    ) {

        TIMESTAMP = timestamp;

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService, subscriptionPackDataService,
                languageDataService, languageService, circleDataService, stateDataService, districtDataService,
                districtService);
    }


    public List<CallSummaryRecordDto> getRecords() {
        return records;
    }


    private Map<Integer, Integer> makeStatsMap(StatusCode statusCode, int count) {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(statusCode.getValue(), count);
        return map;
    }


    public void makeRecords(int numSuccess, int numCompleted, int numFailed, int numInvalid) {
        records = new ArrayList<>();

        for (int i=0 ; i<numSuccess ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30),
                    SubscriptionPackType.CHILD);
            int index = sh.getRandomMessageIndex(sub);
            if (index == sh.getLastMessageIndex(sub)) {
                // We don't want this subscription to be completed
                index--;
            }
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    sub.getSubscriptionId(),
                    1001,
                    1,
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageCode(sub),
                    sh.getCircle(sub)
            );
            records.add(r);
        }

        for (int i=0 ; i<numCompleted ; i++) {
            int days = sh.childPack().getWeeks() * 7;
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(days),
                    SubscriptionPackType.CHILD);
            int index = sh.getLastMessageIndex(sub);
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    sub.getSubscriptionId(),
                    1001,
                    1,
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageCode(sub),
                    sh.getCircle(sub)
            );
            records.add(r);
        }

        for (int i=0 ; i<numFailed ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30),
                    SubscriptionPackType.CHILD);
            int index = sh.getRandomMessageIndex(sub);
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    sub.getSubscriptionId(),
                    2001,
                    2,
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageCode(sub),
                    sh.getCircle(sub)
            );
            records.add(r);
        }

        for (int i=0 ; i<numInvalid ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30),
                    SubscriptionPackType.CHILD);
            int index = sh.getRandomMessageIndex(sub);
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    "00000000-0000-0000-0000-000000000000",
                    2001,
                    2,
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageCode(sub),
                    sh.getCircle(sub)
            );
            records.add(r);
        }
    }
}

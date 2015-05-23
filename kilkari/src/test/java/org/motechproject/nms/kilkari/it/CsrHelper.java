package org.motechproject.nms.kilkari.it;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.dto.CallSummaryRecordDto;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.FinalCallStatus;
import org.motechproject.nms.props.domain.RequestId;
import org.motechproject.nms.props.domain.StatusCode;
import org.motechproject.nms.region.repository.CircleDataService;
import org.motechproject.nms.region.repository.DistrictDataService;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.LanguageLocationDataService;
import org.motechproject.nms.region.repository.StateDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsrHelper {
    private final String TIMESTAMP;

    private SubscriptionHelper sh;

    private List<CallSummaryRecordDto> records;


    public CsrHelper(String timestamp, SubscriptionService subscriptionService,
                     SubscriberDataService subscriberDataService, LanguageDataService languageDataService,
                     LanguageLocationDataService languageLocationDataService,
                     CircleDataService circleDataService, StateDataService stateDataService,
                     DistrictDataService districtDataService) {

        TIMESTAMP = timestamp;

        sh = new SubscriptionHelper(subscriptionService, subscriberDataService,
                languageDataService, languageLocationDataService, circleDataService, stateDataService,
                districtDataService);
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
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            int index = sh.getRandomMessageIndex(sub);
            if (index == sh.getLastMessageIndex(sub)) {
                // We don't want this subscription to be completed
                index--;
            }
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    new RequestId(sub.getSubscriptionId(), TIMESTAMP),
                    sub.getSubscriber().getCallingNumber(),
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageLocationCode(sub),
                    sh.getCircle(sub),
                    FinalCallStatus.SUCCESS,
                    makeStatsMap(StatusCode.OBD_SUCCESS_CALL_CONNECTED, 1),
                    120,
                    1
            );
            records.add(r);
        }

        for (int i=0 ; i<numCompleted ; i++) {
            int days = sh.getChildPack().getWeeks() * 7;
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(days));
            int index = sh.getLastMessageIndex(sub);
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    new RequestId(sub.getSubscriptionId(), TIMESTAMP),
                    sub.getSubscriber().getCallingNumber(),
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageLocationCode(sub),
                    sh.getCircle(sub),
                    FinalCallStatus.SUCCESS,
                    makeStatsMap(StatusCode.OBD_SUCCESS_CALL_CONNECTED, 1),
                    120,
                    1
            );
            records.add(r);
        }

        for (int i=0 ; i<numFailed ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            int index = sh.getRandomMessageIndex(sub);
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    new RequestId(sub.getSubscriptionId(), TIMESTAMP),
                    sub.getSubscriber().getCallingNumber(),
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageLocationCode(sub),
                    sh.getCircle(sub),
                    FinalCallStatus.FAILED,
                    makeStatsMap(StatusCode.OBD_FAILED_BUSY, 3),
                    0,
                    3
            );
            records.add(r);
        }

        for (int i=0 ; i<numInvalid ; i++) {
            Subscription sub = sh.mksub(SubscriptionOrigin.MCTS_IMPORT, DateTime.now().minusDays(30));
            int index = sh.getRandomMessageIndex(sub);
            CallSummaryRecordDto r = new CallSummaryRecordDto(
                    new RequestId("00000000-0000-0000-0000-000000000000", TIMESTAMP),
                    sub.getSubscriber().getCallingNumber(),
                    sh.getContentMessageFile(sub, index),
                    sh.getWeekId(sub, index),
                    sh.getLanguageLocationCode(sub),
                    sh.getCircle(sub),
                    FinalCallStatus.SUCCESS,
                    makeStatsMap(StatusCode.OBD_SUCCESS_CALL_CONNECTED, 1),
                    120,
                    1
            );
            records.add(r);
        }
    }
}

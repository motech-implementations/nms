package org.motechproject.nms.imi.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.domain.CallRetry;
import org.motechproject.nms.imi.domain.CallStage;
import org.motechproject.nms.imi.repository.CallRetryDataService;
import org.motechproject.nms.imi.service.RequestId;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RescheduleController {

    private static final String RESCHEDULE_CALL = "nms.imi.reschedule_call";

    private static final Logger LOGGER = LoggerFactory.getLogger(RescheduleController.class);

    private SubscriptionService subscriptionService;
    private CallRetryDataService callRetryDataService;


    @Autowired
    public RescheduleController(SubscriptionService subscriptionService, CallRetryDataService callRetryDataService) {
        this.subscriptionService = subscriptionService;
        this.callRetryDataService = callRetryDataService;
    }


    @MotechListener(subjects = { RESCHEDULE_CALL })
    public void rescheduleCall(MotechEvent event) {
        LOGGER.info("rescheduleCall() is handling {}", event.toString());

        CallDetailRecord cdr = (CallDetailRecord) event.getParameters().get("CDR");
        RequestId requestId = RequestId.fromString(cdr.getRequestId());
        Subscription subscription = subscriptionService.getSubscription(requestId.getSubscriptionId());
        CallRetry callRetry = callRetryDataService.findBySubscriptionId(requestId.getSubscriptionId());
        if (callRetry == null) {
            //first retry day
            callRetryDataService.create(new CallRetry(
                    requestId.getSubscriptionId(),
                    Long.parseLong(cdr.getMsisdn()),
                    DayOfTheWeek.fromInt(subscription.getStartDate().dayOfWeek().get()).nextDay(),
                    CallStage.RETRY_1,
                    subscription.getSubscriber().getLanguage().getCode(),
                    subscription.getSubscriber().getCircle(),
                    subscription.getOrigin().getCode()
            ));
        } else {
            //we've already rescheduled this call, let's see if it needs to be re-rescheduled

            if (subscription.getSubscriptionPack().retryCount() == 1) {
                //This message should only be retried once, so let's delete it from the CallRetry table
                callRetryDataService.delete(callRetry);
                return;
            }

            if (callRetry.getCallStage() == CallStage.RETRY_LAST) {
                //This message has been re-scheduled for the last (3rd) time, let's delete it from the CallRetry
                //table
                callRetryDataService.delete(callRetry);
                return;
            }

            //re-reschedule the call

            //update the callStage
            callRetry.setCallStage((callRetry.getCallStage() == CallStage.RETRY_1) ? CallStage.RETRY_2 :
                    CallStage.RETRY_LAST);
            //increment the day of the week
            callRetry.setDayOfTheWeek(callRetry.getDayOfTheWeek().nextDay());
            //update the CallRetry record
            callRetryDataService.update(callRetry);
        }
    }
}

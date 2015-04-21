package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.api.web.contract.InboxSubscriptionDetail;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.HashSet;

/**
 * KilkariController
 */
@RequestMapping("/kilkari")
@Controller
public class KilkariController extends BaseController {

    @Autowired
    private KilkariService kilkariService;

    /**
     *
     * 4.2.2
     * Get Inbox Details API
     *
     * IVR shall invoke this API to get the Inbox details of the beneficiary, identified by ‘callingNumber’.
     *
     */
    @RequestMapping("/inbox")
    @ResponseBody
    public InboxResponse inbox(@RequestParam String callingNumber, @RequestParam String callId)
            throws NotFoundException {

        StringBuilder failureReasons = validate(callingNumber, callId);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscriber subscriber = kilkariService.getSubscriber(String.valueOf(callingNumber));
        if (subscriber == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }

        Set<Subscription> subscriptions = subscriber.getSubscriptions();
        Set<InboxSubscriptionDetail> subscriptionDetails = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            subscriptionDetails.add(new InboxSubscriptionDetail(subscription.getSubscriptionId(),
                    subscription.getSubscriptionPack().getName(),
                    "10_1",
                    "xyz.wav"));
        }

        return new InboxResponse(subscriptionDetails);
    }


    /**
     * 4.2.5
     * Save Inbox Call Details
     * 
     * IVR shall invoke this API to send the call detail information corresponding to the Inbox access
     * inbound call for which inbox message(s) is played.
     *
     */
    @RequestMapping(value = "/inboxCallDetails",
                    method = RequestMethod.POST,
                    headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void saveInboxCallDetails(@RequestBody InboxCallDetailsRequest request) {
        StringBuilder failureReasons = validate(request.getCallingNumber(), request.getOperator(),
                request.getCircle(), request.getCallId());
        //todo: more validations
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        //todo: the work...
    }

}

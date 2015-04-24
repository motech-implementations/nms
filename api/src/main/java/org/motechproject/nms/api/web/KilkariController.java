package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.api.web.contract.kilkari.InboxSubscriptionDetailResponse;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * KilkariController
 */
@RequestMapping("/kilkari")
@Controller
public class KilkariController extends BaseController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * 4.2.2
     * Get Inbox Details API
     * <p/>
     * IVR shall invoke this API to get the Inbox details of the beneficiary, identified by ‘callingNumber’.
     */
    @RequestMapping("/inbox")
    @ResponseBody
    public InboxResponse getInboxDetails(@RequestParam Long callingNumber, @RequestParam Long callId) {

        StringBuilder failureReasons = validate(callingNumber, callId);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscriber subscriber = subscriptionService.getSubscriber(callingNumber);
        if (subscriber == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }

        Set<Subscription> subscriptions = subscriber.getSubscriptions();
        Set<InboxSubscriptionDetailResponse> subscriptionDetails = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            subscriptionDetails.add(new InboxSubscriptionDetailResponse(subscription.getSubscriptionId(),
                    subscription.getSubscriptionPack().getName(),
                    "10_1",
                    "xyz.wav"));
        }

        return new InboxResponse(subscriptionDetails);
    }


    private StringBuilder validateSaveInboxCallDetails(InboxCallDetailsRequest request) {
        StringBuilder failureReasons = validate(request.getCallingNumber(), request.getCallId(),
                request.getOperator(), request.getCircle());

        validateFieldPresent(failureReasons, "callStartTime", request.getCallStartTime());
        validateFieldPresent(failureReasons, "callEndTime", request.getCallEndTime());
        validateFieldPresent(failureReasons, "callDurationInPulses", request.getCallDurationInPulses());
        validateFieldCallStatus(failureReasons, "callStatus", request.getCallStatus());
        validateFieldCallStatus(failureReasons, "callDisconnectReason", request.getCallDisconnectReason());

        return failureReasons;
    }


    /**
     * 4.2.5
     * Save Inbox Call Details
     * <p/>
     * IVR shall invoke this API to send the call detail information corresponding to the Inbox access
     * inbound call for which inbox message(s) is played.
     */
    @RequestMapping(value = "/inboxCallDetails",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void saveInboxCallDetails(@RequestBody InboxCallDetailsRequest request) {
        StringBuilder failureReasons = validateSaveInboxCallDetails(request);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        //todo: the work...
    }

    /**
     * 4.2.3
     * Create Subscription
     * <p/>
     * IVR shall invoke this API to create a new Kilkari subscription
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.POST,
            headers = {"Content-type=application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void createSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {
        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getCallId(), subscriptionRequest.getOperator(),
                subscriptionRequest.getCircle());
        validateFieldPresent(failureReasons, "subscriptionPack", subscriptionRequest.getSubscriptionPack());
        validateFieldPresent(failureReasons, "languageLocationCode",
                subscriptionRequest.getLanguageLocationCode().toString());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }
        if (!validateSubscriptionPack(subscriptionRequest.getSubscriptionPack())) {
            throw new NotFoundException(String.format(NOT_FOUND, "subscriptionPack"));
        }

        subscriptionService.createSubscription(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getLanguageLocationCode(), subscriptionRequest.getSubscriptionPack());
    }

    /**
     * 4.2.4
     * Deactivate Subscription
     * <p/>
     * IVR shall invoke this API to deactivate an existing Kilkari subscription
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.DELETE,
            headers = {"Content-type=application/json"})
    @ResponseStatus(HttpStatus.OK)
    public void deactivateSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {
        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getCallId(), subscriptionRequest.getOperator(),
                subscriptionRequest.getCircle());

        validateFieldPresent(failureReasons, "subscriptionId", subscriptionRequest.getSubscriptionId());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscription subscription = subscriptionService.getSubscription(subscriptionRequest.getSubscriptionId());

        if (subscription == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "subscriptionId"));
        }

        subscriptionService.deactivateSubscription(subscription);
    }

    private boolean validateSubscriptionPack(String name) {
        return subscriptionService.getCountSubscriptionPack(name) == 1;
    }

}

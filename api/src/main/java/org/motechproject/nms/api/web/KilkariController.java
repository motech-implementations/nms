package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequest;
import org.motechproject.nms.api.web.contract.kilkari.InboxCallDetailsRequestCallData;
import org.motechproject.nms.api.web.contract.kilkari.InboxResponse;
import org.motechproject.nms.api.web.contract.kilkari.InboxSubscriptionDetailResponse;
import org.motechproject.nms.api.web.contract.kilkari.SubscriptionRequest;
import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.kilkari.domain.InboxCallData;
import org.motechproject.nms.kilkari.domain.InboxCallDetails;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * KilkariController
 */
@RequestMapping("/kilkari")
@Controller
public class KilkariController extends BaseController {

    /*
     4.2.5.1.5 Body Elements
     */
    public static final int SUBSCRIPTION_ID_LENGTH = 36;
    public static final Set<String> SUBSCRIPTION_PACK_SET = new HashSet<>(Arrays.asList("48WeeksPack", "76WeeksPack"));

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
    public InboxResponse getInboxDetails(@RequestParam(required = false) Long callingNumber,
                                         @RequestParam(required = false) Long callId) {

        StringBuilder failureReasons = validate(callingNumber, callId);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscriber subscriber = kilkariService.getSubscriber(callingNumber);
        if (subscriber == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "callingNumber"));
        }

        Set<Subscription> subscriptions = subscriber.getSubscriptions();
        Set<InboxSubscriptionDetailResponse> subscriptionDetails = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            //todo: something tells me this is not complete/real code
            subscriptionDetails.add(new InboxSubscriptionDetailResponse(subscription.getSubscriptionId(),
                    subscription.getSubscriptionPack().getName(),
                    "10_1",
                    "xyz.wav"));
        }

        return new InboxResponse(subscriptionDetails);
    }

    private void validateSaveInboxCallDetailsContent(StringBuilder failureReasons,
                                                     Set<InboxCallDetailsRequestCallData> content) {
        if (content == null || content.size() == 0) {
            // Empty content is acceptable (when the IVR vendor plays promotional content)
            return;
        }
        if (content.size() != 2) {
            // Valid content must contain two elements
            failureReasons.append(String.format(INVALID, "content"));
            return;
        }
        int failureReasonsLength = failureReasons.length();
        Set<String> subscriptionPacks = new HashSet<>();
        for (InboxCallDetailsRequestCallData data : content) {
            validateFieldExactLength(failureReasons, "subscriptionId", data.getSubscriptionId(), SUBSCRIPTION_ID_LENGTH);
            subscriptionPacks.add(data.getSubscriptionPack());
            validateFieldString(failureReasons, "inboxWeekId", data.getInboxWeekId());
            validateFieldString(failureReasons, "contentFileName", data.getContentFileName());
            validateFieldPositiveLong(failureReasons, "startTime", data.getStartTime());
            validateFieldPositiveLong(failureReasons, "endTime", data.getEndTime());
        }

        //check we have all required subscription packs
        if (!SUBSCRIPTION_PACK_SET.equals(subscriptionPacks)) {
            failureReasons.append(String.format(INVALID, "subscriptionPack"));
        }

        //some field error occurred, add an error on the "content" parent field
        if (failureReasonsLength != failureReasons.length()) {
            failureReasons.append(String.format(INVALID, "content"));
        }

    }

    private StringBuilder validateSaveInboxCallDetails(InboxCallDetailsRequest request) {
        StringBuilder failureReasons = validate(request.getCallingNumber(), request.getCallId(),
                request.getOperator(), request.getCircle());

        validateFieldPresent(failureReasons, "callStartTime", request.getCallStartTime());
        validateFieldPresent(failureReasons, "callEndTime", request.getCallEndTime());
        validateFieldPresent(failureReasons, "callDurationInPulses", request.getCallDurationInPulses());
        validateFieldCallStatus(failureReasons, "callStatus", request.getCallStatus());
        validateFieldCallDisconnectReason(failureReasons, "callDisconnectReason", request.getCallDisconnectReason());
        validateSaveInboxCallDetailsContent(failureReasons, request.getContent());

        return failureReasons;
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
        StringBuilder failureReasons = validateSaveInboxCallDetails(request);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Set<InboxCallData> content = new HashSet<>();
        if (request.getContent() != null && request.getContent().size() > 0) {
            for (InboxCallDetailsRequestCallData inboxCallDetailsRequestCallData : request.getContent()) {
                content.add(new InboxCallData(
                        inboxCallDetailsRequestCallData.getSubscriptionId(),
                        inboxCallDetailsRequestCallData.getSubscriptionPack(),
                        inboxCallDetailsRequestCallData.getInboxWeekId(),
                        inboxCallDetailsRequestCallData.getContentFileName(),
                        epochToDateTime(inboxCallDetailsRequestCallData.getStartTime()),
                        epochToDateTime(inboxCallDetailsRequestCallData.getEndTime())
                ));
            }
        }

        InboxCallDetails inboxCallDetails = new InboxCallDetails(
                request.getCallingNumber(),
                request.getOperator(),
                request.getCircle(),
                request.getCallId(),
                epochToDateTime(request.getCallStartTime()),
                epochToDateTime(request.getCallEndTime()),
                request.getCallDurationInPulses(),
                request.getCallStatus(),
                request.getCallDisconnectReason(),
                content);

        kilkariService.addInboxCallDetails(inboxCallDetails);
    }

    /**
     * 4.2.3
     * Create Subscription
     *
     * IVR shall invoke this API to create a new Kilkari subscription
     *
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
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

        kilkariService.createSubscription(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getLanguageLocationCode(), subscriptionRequest.getSubscriptionPack());
    }

    /**
     * 4.2.4
     * Deactivate Subscription
     *
     * IVR shall invoke this API to deactivate an existing Kilkari subscription
     *
     */
    @RequestMapping(value = "/subscription",
            method = RequestMethod.DELETE,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void deactivateSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {

        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getCallId(), subscriptionRequest.getOperator(),
                subscriptionRequest.getCircle());

        validateFieldPresent(failureReasons, "subscriptionId", subscriptionRequest.getSubscriptionId());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Subscription subscription = kilkariService.getSubscription(subscriptionRequest.getSubscriptionId());

        if (subscription == null) {
            throw new NotFoundException(String.format(NOT_FOUND, "subscriptionId"));
        }

        kilkariService.deactivateSubscription(subscription);
    }

    private boolean validateSubscriptionPack(String name) {
        return kilkariService.getCountSubscriptionPack(name) == 1;
    }

}

package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.exception.NotFoundException;
import org.motechproject.nms.api.web.contract.InboxSubscriptionDetail;
import org.motechproject.nms.api.web.contract.KilkariResponseInbox;
import org.motechproject.nms.api.web.contract.SubscriptionRequest;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    @RequestMapping("/inbox")
    @ResponseBody
    public KilkariResponseInbox inbox(@RequestParam String callingNumber, @RequestParam String callId) throws NotFoundException {

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
            subscriptionDetails.add(new InboxSubscriptionDetail(subscription.getSubscriptionId().toString(),
                subscription.getSubscriptionPack().getName(),
                "10_1",
                "xyz.wav"));
        }

        return new KilkariResponseInbox(subscriptionDetails);
    }

    @RequestMapping(value = "/subscription", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void createSubscription(@RequestBody SubscriptionRequest subscriptionRequest) {

        StringBuilder failureReasons = validate(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getOperator(), subscriptionRequest.getCircle(), subscriptionRequest.getCallId());

        // TODO: validate the language code and pack

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        kilkariService.createSubscription(subscriptionRequest.getCallingNumber(),
                subscriptionRequest.getLanguageLocationCode(), subscriptionRequest.getSubscriptionPack());
    }

}

package org.motechproject.nms.api.web;

import org.motechproject.nms.api.web.contract.InboxSubscriptionDetail;
import org.motechproject.nms.api.web.contract.KilkariResponseInbox;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.service.KilkariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public KilkariResponseInbox inbox(@RequestParam String callingNumber, @RequestParam String callId) {

        StringBuilder failureReasons = validate(callingNumber, callId);

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Set<Subscription> subscriptions = kilkariService.getSubscriptions(callingNumber);
        Set<InboxSubscriptionDetail> subscriptionDetails = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            subscriptionDetails.add(new InboxSubscriptionDetail(subscription.getSubscriptionId(),
                subscription.getSubscriptionPack().getName(),
                "10_1",
                "xyz.wav"));
        }

        return new KilkariResponseInbox(subscriptionDetails);
    }

}

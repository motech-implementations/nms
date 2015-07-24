package org.motechproject.nms.testing.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.api.web.contract.mobileAcademy.sms.DeliveryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestingController {

    @Autowired
    private EventRelay eventRelay;

    @RequestMapping(value = "/sendEvent")
    @ResponseBody
    @Transactional
    public String sendEvent(@RequestParam(required = true) String subject) {

        Map<String, Object> eventParams = new HashMap<>();
        MotechEvent motechEvent = new MotechEvent(subject, eventParams);
        eventRelay.sendEventMessage(motechEvent);
        return String.format("Sent MOTECH event: %s with empty params: %s", subject, eventParams.toString());
    }

    @RequestMapping(value = "/sendSMS200", method = RequestMethod.POST, headers = { "Content-type = application/json" })
    @ResponseBody
    public String sendSMS200(HttpServletRequest request) {

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("address", "tel: 1234567890");
        eventParams.put("deliveryStatus",
                DeliveryStatus.DeliveredToTerminal.toString());
        MotechEvent motechEvent = new MotechEvent("nms.ma.sms.deliveryStatus",
                eventParams);
        eventRelay.sendEventMessage(motechEvent);

        return "OK";
    }

    /*
     * @RequestMapping(value = "/sendSMS200")
     * 
     * @ResponseBody public String sendSMS200Get() {
     * 
     * Map<String, Object> eventParams = new HashMap<>();
     * eventParams.put("address", "tel: 1234567890");
     * eventParams.put("deliveryStatus",
     * DeliveryStatus.DeliveredToTerminal.toString()); MotechEvent motechEvent =
     * new MotechEvent("nms.ma.sms.deliveryStatus", eventParams);
     * eventRelay.sendEventMessage(motechEvent);
     * 
     * return "OK"; }
     */
}

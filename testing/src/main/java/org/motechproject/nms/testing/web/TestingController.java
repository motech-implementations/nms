package org.motechproject.nms.testing.web;

import java.util.HashMap;
import java.util.Map;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    /**
     * API used by NMS to send SMS for testing purpose only.
     */
    @RequestMapping(value = "/sendSMS/outbound", method = RequestMethod.POST, headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ResponseBody
    public String sendSMSOutbound(@RequestBody String template) {
        return "OK";
    }
}

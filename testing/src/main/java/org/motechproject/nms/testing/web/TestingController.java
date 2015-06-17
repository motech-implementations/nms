package org.motechproject.nms.testing.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

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
        return String.format("Sent MOTECH event: %s with params: %s", subject, eventParams.toString());
    }

    @RequestMapping(value = "/sendEventWithParams")
    @ResponseBody
    @Transactional
    public String sendEvent(@RequestParam(required = true) String subject,
                            @RequestParam(required = true) Map<String, Object> eventParams) {

        MotechEvent motechEvent = new MotechEvent(subject, eventParams);
        eventRelay.sendEventMessage(motechEvent);
        return String.format("Sent MOTECH event: %s with params: %s", subject, eventParams.toString());
    }
}

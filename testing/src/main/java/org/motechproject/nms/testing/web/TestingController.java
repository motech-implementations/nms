package org.motechproject.nms.testing.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.testing.service.TestingService;
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

    @Autowired
    private TestingService testingService;


    @RequestMapping(value = "/sendEvent")
    @ResponseBody
    @Transactional
    public String sendEvent(@RequestParam(required = true) String subject) {

        Map<String, Object> eventParams = new HashMap<>();
        MotechEvent motechEvent = new MotechEvent(subject, eventParams);
        eventRelay.sendEventMessage(motechEvent);
        return String.format("Sent MOTECH event: %s with empty params: %s", subject, eventParams.toString());
    }

    @RequestMapping(value = "/clearDatabase")
    @ResponseBody
    public String clearDatabase() {
        testingService.clearDatabase();
        return "OK";
    }

}

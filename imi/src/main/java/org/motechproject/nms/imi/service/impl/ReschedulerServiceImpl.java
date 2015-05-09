package org.motechproject.nms.imi.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.imi.domain.CallDetailRecord;
import org.motechproject.nms.imi.service.ReschedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("reschedulerService")
public class ReschedulerServiceImpl implements ReschedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReschedulerServiceImpl.class);

    private EventRelay eventRelay;


    @Autowired
    public ReschedulerServiceImpl(EventRelay eventRelay) {
        this.eventRelay = eventRelay;
    }


    public void sendRescheduleMessage(CallDetailRecord cdr) {
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("CDR", cdr);
        MotechEvent motechEvent = new MotechEvent(RESCHEDULE_CALL, eventParams);
        eventRelay.sendEventMessage(motechEvent);
    }


    @MotechListener(subjects = { RESCHEDULE_CALL })
    public void rescheduleCall(MotechEvent event) {
        LOGGER.info("rescheduleCall() is handling {}", event.toString());

        //todo:...
    }
}

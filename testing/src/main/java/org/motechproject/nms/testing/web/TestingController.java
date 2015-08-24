package org.motechproject.nms.testing.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.testing.service.TestingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

@Controller
public class TestingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestingController.class);
    private static final String[] UNITS = new String[] { "B", "kB", "MB", "GB", "TB" };

    private static final DecimalFormat FMT_DEC = new DecimalFormat("#,##0.000");

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


    @RequestMapping(value = "/createSubscriptionPacks")
    @ResponseBody
    public String createSubscriptionPacks() {
        testingService.createSubscriptionPacks();
        return "OK";
    }


    @RequestMapping(value = "/createMctsMoms")
    @ResponseBody
    public String createMctsMoms(@RequestParam(required = true) int count) throws IOException {
        return testingService.createMctsMoms(count);
    }


    //from http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
    private static String readableFileSize(long size) {
        if (size <= 0) { return "0"; }
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return FMT_DEC.format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
    }


    //from http://stackoverflow.com/questions/12807797/java-get-available-memory
    @RequestMapping(value = "/mem")
    @ResponseBody
    public String sysInfo() {
        StringBuilder sb = new StringBuilder("\n");
        
        /* Total amount of free memory available to the JVM */
        sb.append("Free memory: " + readableFileSize(Runtime.getRuntime().freeMemory()));
        sb.append("\n");

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        sb.append("Maximum memory: " + (maxMemory == Long.MAX_VALUE ? "no limit" : readableFileSize(maxMemory)));
        sb.append("\n");

        /* Total memory currently in use by the JVM */
        sb.append("Total memory: " + readableFileSize(Runtime.getRuntime().totalMemory()));

        LOGGER.debug(sb.toString());
        
        return sb.toString();
    }
}

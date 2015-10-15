package org.motechproject.nms.api.web;

import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);
    private SubscriptionDataService subscriptionDataService;

    @Autowired
    public CacheController(SubscriptionDataService subscriptionDataService) {
        this.subscriptionDataService = subscriptionDataService;
    }

    /**
     * Provided for OPS as a crutch to be able to empty all MDS cache directly after modifying the database by hand
     */
    @RequestMapping("/evictAllCache")
    public void evictAllCache() {
        LOGGER.debug("/evictAllCache()");
        subscriptionDataService.evictAllCache();
    }
}

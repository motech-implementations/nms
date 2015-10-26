package org.motechproject.nms.api.web;

import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller to expose methods for OPS personnel
 */
@RequestMapping("/ops")
@Controller
public class OpsController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpsController.class);
    private SubscriptionDataService subscriptionDataService;

    @Autowired
    public OpsController(SubscriptionDataService subscriptionDataService) {
        this.subscriptionDataService = subscriptionDataService;
    }

    /**
     * Provided for OPS as a crutch to be able to empty all MDS cache directly after modifying the database by hand
     */
    @RequestMapping("/evictAllCache")
    @ResponseStatus(HttpStatus.OK)
    public void evictAllCache() {
        LOGGER.info("/evictAllCache()");
        subscriptionDataService.evictAllCache();
    }

    @RequestMapping("/cleanSubscriptions")
    @ResponseStatus(HttpStatus.OK)
    public void cleanSubscriptions() {
        LOGGER.info("/cleanSubscriptions()");
    }

    @RequestMapping("/cleanCdr")
    @ResponseStatus(HttpStatus.OK)
    public void clearCdr() {
        LOGGER.info("/cleanCdr()");
    }

    @RequestMapping("/cleanCsr")
    @ResponseStatus(HttpStatus.OK)
    public void clearCsr() {
        LOGGER.info("/cleanCsr()");
    }

}

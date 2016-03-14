package org.motechproject.nms.api.web;

import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.mcts.service.MctsWsImportService;
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
    private SubscriptionService subscriptionService;
    private CdrFileService cdrFileService;
    private MctsWsImportService mctsWsImportService;


    @Autowired
    public OpsController(SubscriptionDataService subscriptionDataService, SubscriptionService subscriptionService,
                         CdrFileService cdrFileService, MctsWsImportService mctsWsImportService) {
        this.subscriptionDataService = subscriptionDataService;
        this.subscriptionService = subscriptionService;
        this.cdrFileService = cdrFileService;
        this.mctsWsImportService = mctsWsImportService;
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
        subscriptionService.completePastDueSubscriptions();
    }

    @RequestMapping("/cleanCallRecords")
    @ResponseStatus(HttpStatus.OK)
    public void clearCallRecords() {

        LOGGER.info("/cleanCdr()");
        cdrFileService.cleanOldCallRecords();
    }

    @RequestMapping("/startMctsSync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startMctsSync() {

        LOGGER.info("/startMctsSync");
        mctsWsImportService.startMctsImport();
    }
}

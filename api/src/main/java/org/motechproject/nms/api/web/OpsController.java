package org.motechproject.nms.api.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.api.web.contract.AddRchFlwRequest;
import org.motechproject.nms.api.web.service.FlwCsvService;
import org.motechproject.nms.csv.exception.CsvImportException;
import org.motechproject.nms.kilkari.utils.FlwConstants;
import org.motechproject.nms.api.web.contract.mobileAcademy.GetBookmarkResponse;
import org.motechproject.nms.api.web.converter.MobileAcademyConverter;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.MctsChildFixService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.nms.mobileacademy.dto.MaBookmark;
import org.motechproject.nms.mobileacademy.service.MobileAcademyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Controller to expose methods for OPS personnel
 */
@RequestMapping("/ops")
@Controller
public class OpsController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpsController.class);

    @Autowired
    private SubscriptionDataService subscriptionDataService;

    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private CdrFileService cdrFileService;

    @Autowired
    private MctsWsImportService mctsWsImportService;

    @Autowired
    private EventRelay eventRelay;

    @Autowired
    private MobileAcademyService mobileAcademyService;

    @Autowired
    private MctsChildFixService mctsChildFixService;

    @Autowired
    private FlwCsvService flwCsvService;

    private final String contactNumber = "contactNumber";

    //only for debugging purposes and will not be returned anywhere
    public static final String NON_ASHA_TYPE = "<MctsId: %s,Contact Number: %s, Invalid Type: %s>";

    protected static boolean validatetypeASHA(StringBuilder errors, String fieldName, String mctsFlwId, Long contactNumber, String type) {
        if (!validateFieldPresent(errors, fieldName, type)) {
            return false;
        }
        String designation = type.trim();
        if (FlwConstants.ASHA_TYPE.equalsIgnoreCase(designation)) {
            return true;
        }
        errors.append(String.format(NON_ASHA_TYPE, mctsFlwId, contactNumber, type));
        return false;
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

    @RequestMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    public String ping() {
        LOGGER.info("/ping()");
        return "PING";
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

    @RequestMapping("/upkeep")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void startUpkeep() {
        LOGGER.info("/upkeep");
        eventRelay.sendEventMessage(new MotechEvent(KilkariConstants.SUBSCRIPTION_UPKEEP_SUBJECT));
    }


    @RequestMapping(value = "/createUpdateFlw",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void createUpdateFlw(@RequestBody AddFlwRequest addFlwRequest) {
        // TODO: add a field updatedDateNic for Add Flw Request.
        // Will Fix this with NMS-349

        LOGGER.debug("healthfacilityName {}", addFlwRequest.getPhcName());
        StringBuilder failureReasons = flwCsvService.csvUploadMcts(addFlwRequest);
        if (failureReasons != null) {
            throw new IllegalArgumentException(failureReasons.toString());
        } else {
            flwCsvService.persistFlwMcts(addFlwRequest);
        }
    }

    @RequestMapping(value = "/createUpdateRchFlw",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    public void createUpdateRchFlw(@RequestBody AddRchFlwRequest addRchFlwRequest) {
        StringBuilder failureReasons = flwCsvService.csvUploadRch(addRchFlwRequest);
        if (failureReasons != null) {
            throw new IllegalArgumentException(failureReasons.toString());
        } else {
            flwCsvService.persistFlwRch(addRchFlwRequest);
        }
    }

    @RequestMapping("/getbookmark")
    @ResponseBody
    public GetBookmarkResponse getBookmarkWithScore(@RequestParam(required = false) Long callingNumber) {
        LOGGER.info("/getbookmark");
        MaBookmark bookmark = mobileAcademyService.getBookmarkOps(callingNumber);
        GetBookmarkResponse ret = MobileAcademyConverter.convertBookmarkDto(bookmark);
        log("RESPONSE: /ops/getbookmark", String.format("bookmark=%s", ret.toString()));
        return ret;
    }

    @RequestMapping(value = "/deactivationRequest",
            method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void deactivationRequest(@RequestParam(value = "msisdn") Long msisdn, @RequestParam(value = "deactivationReason") String deactivationReason) {
        log("REQUEST: /ops/deactivationRequest", String.format(
                "callingNumber=%s",
                LogHelper.obscure(msisdn)));
        StringBuilder failureReasons = new StringBuilder();
        validateField10Digits(failureReasons, contactNumber, msisdn);
        validateFieldPositiveLong(failureReasons, contactNumber, msisdn);
        validateDeactivationReason(failureReasons, "deactivationReason", deactivationReason);
        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }
        DeactivationReason reason = DeactivationReason.valueOf(deactivationReason);
        subscriberService.deactivateAllSubscriptionsForSubscriber(msisdn, reason);
    }

    @RequestMapping("/getScores")
    @ResponseBody
    public String getScoresForNumber(@RequestParam(required = true) Long callingNumber) {
        LOGGER.info("/getScores Getting scores for user");
        String scores = mobileAcademyService.getScoresForUser(callingNumber);
        LOGGER.info("Scores: " + scores);
        return scores;
    }

    @RequestMapping(value = "/updateMotherInChild", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void importChildUpdate(@RequestParam MultipartFile csvFile) {

        LOGGER.debug("updateMotherInChild() BEGIN");
        try {
            try (InputStream in = csvFile.getInputStream()) {
                mctsChildFixService.updateMotherChild(new InputStreamReader(in));
            }
        } catch (CsvImportException e) {
            LOGGER.error(csvFile.getOriginalFilename(), "/updateMotherInChild", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(csvFile.getOriginalFilename(), "/updateMotherInChild", e);
            throw new CsvImportException("An error occurred during CSV import", e);
        }
        LOGGER.debug("updateMotherInChild() END");
    }

}


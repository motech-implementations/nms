package org.motechproject.nms.api.web;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.api.web.contract.kilkari.DeactivateSubscriptionContract;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.utils.FlwConstants;
import org.motechproject.nms.api.web.contract.mobileAcademy.GetBookmarkResponse;
import org.motechproject.nms.api.web.converter.MobileAcademyConverter;
import org.motechproject.nms.imi.service.CdrFileService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
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

import java.util.HashMap;
import java.util.Map;

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
    private FrontLineWorkerService frontLineWorkerService;

    private String contactNumber = "contactNumber";

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
    @Transactional
    public void createUpdateFlw(@RequestBody AddFlwRequest addFlwRequest) {
        log("REQUEST: /ops/createUpdateFlw", String.format(
                "callingNumber=%s, mctsId=%s, name=%s, state=%d, district=%d",
                LogHelper.obscure(addFlwRequest.getContactNumber()),
                addFlwRequest.getMctsFlwId(),
                addFlwRequest.getName(),
                addFlwRequest.getStateId(),
                addFlwRequest.getDistrictId()));

        StringBuilder failureReasons = new StringBuilder();
        validateField10Digits(failureReasons, contactNumber, addFlwRequest.getContactNumber());
        validateFieldPositiveLong(failureReasons, contactNumber, addFlwRequest.getContactNumber());
        validateFieldPresent(failureReasons, "mctsFlwId", addFlwRequest.getMctsFlwId());
        validateFieldPresent(failureReasons, "stateId", addFlwRequest.getStateId());
        validateFieldPresent(failureReasons, "districtId", addFlwRequest.getDistrictId());
        validateFieldString(failureReasons, "name", addFlwRequest.getName());
        validateFieldString(failureReasons, "type", addFlwRequest.getType());

        if (failureReasons.length() > 0) {
            throw new IllegalArgumentException(failureReasons.toString());
        }

        Map<String, Object> flwProperties = new HashMap<>();
        flwProperties.put(FlwConstants.NAME, addFlwRequest.getName());
        flwProperties.put(FlwConstants.ID, addFlwRequest.getMctsFlwId());
        flwProperties.put(FlwConstants.CONTACT_NO, addFlwRequest.getContactNumber());
        flwProperties.put(FlwConstants.STATE_ID, addFlwRequest.getStateId());
        flwProperties.put(FlwConstants.DISTRICT_ID, addFlwRequest.getDistrictId());

        if (addFlwRequest.getType() != null) {
            flwProperties.put(FlwConstants.TYPE, addFlwRequest.getType());
        }

        if (addFlwRequest.getTalukaId() != null) {
            flwProperties.put(FlwConstants.TALUKA_ID, addFlwRequest.getTalukaId());
        }

        if (addFlwRequest.getPhcId() != null) {
            flwProperties.put(FlwConstants.PHC_ID, addFlwRequest.getPhcId());
        }

        if (addFlwRequest.getHealthblockId() != null) {
            flwProperties.put(FlwConstants.HEALTH_BLOCK_ID, addFlwRequest.getHealthblockId());
        }

        if (addFlwRequest.getSubcentreId() != null) {
            flwProperties.put(FlwConstants.SUB_CENTRE_ID, addFlwRequest.getSubcentreId());
        }

        if (addFlwRequest.getVillageId() != null) {
            flwProperties.put(FlwConstants.CENSUS_VILLAGE_ID, addFlwRequest.getVillageId());
        }

        frontLineWorkerService.createUpdate(flwProperties);
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

    @RequestMapping(value = "/releaseNumber",
            method = RequestMethod.POST,
            headers = { "Content-type=application/json" })
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void releaseNumber(@RequestBody DeactivateSubscriptionContract deactivateSubscriptionContract) {
        log("REQUEST: /ops/releaseNumber", String.format(
                "callingNumber=%s",
                LogHelper.obscure(deactivateSubscriptionContract.getContactNumber())));
        StringBuilder failureReasons = new StringBuilder();
        validateField10Digits(failureReasons, contactNumber, deactivateSubscriptionContract.getContactNumber());
        validateFieldPositiveLong(failureReasons, contactNumber, deactivateSubscriptionContract.getContactNumber());
        subscriptionService.deactivateSubscriptionForSpecificMsisdn(deactivateSubscriptionContract.getContactNumber());
    }
}


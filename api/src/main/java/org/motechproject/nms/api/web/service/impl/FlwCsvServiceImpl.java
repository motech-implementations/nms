package org.motechproject.nms.api.web.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.motechproject.nms.api.web.contract.AddFlwRequest;
import org.motechproject.nms.api.web.contract.AddRchFlwRequest;
import org.motechproject.nms.api.web.contract.UpdateFlwLocationRequest;
import org.motechproject.nms.api.web.service.FlwCsvService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
//import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.flw.domain.SubscriptionOriginFlw;
import org.motechproject.nms.kilkari.utils.FlwConstants;
import org.motechproject.nms.props.service.LogHelper;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.nms.rejectionhandler.domain.FlwImportRejection;
import org.motechproject.nms.rejectionhandler.service.FlwRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vishnu on 25/9/17.
 */
@Service("flwRejectionCsvService")
public class FlwCsvServiceImpl implements FlwCsvService {

    private static final String NOT_PRESENT = "<%s: Not Present>";
    private static final String INVALID = "<%s: Invalid>";
    private static final String IVR_INTERACTION_LOG = "IVR INTERACTION: %s";

    private static final long SMALLEST_10_DIGIT_NUMBER = 1000000000L;
    private static final long LARGEST_10_DIGIT_NUMBER  = 9999999999L;
    private final String contactNumber = "contactNumber";
    private final String gfStatus = "gfStatus";
    private static final String NON_ASHA_TYPE = "<MctsId: %s,Contact Number: %s, Invalid Type: %s>";


    private static final Logger LOGGER = LoggerFactory.getLogger(FlwCsvServiceImpl.class);

    @Autowired
    private FlwRejectionService flwRejectionService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

    @Autowired
    private StateDataService stateDataService;

    @Autowired
    private FrontLineWorkerImportService frontLineWorkerImportService;

    @Override
    @Transactional
    public StringBuilder csvUploadMcts(AddFlwRequest addFlwRequest) {
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
        validateNameFields(failureReasons, "talukaName", addFlwRequest.getTalukaName(), addFlwRequest.getTalukaId());
        validateNameFields(failureReasons, "phcName", addFlwRequest.getPhcName(), addFlwRequest.getPhcId());
        validateNameFields(failureReasons, "subcentreName", addFlwRequest.getSubcentreName(), addFlwRequest.getSubcentreId());
        validateNameFields(failureReasons, "villageName", addFlwRequest.getVillageName(), addFlwRequest.getVillageId());
        validateNameFields(failureReasons, "healthBlockName", addFlwRequest.getHealthBlockName(), addFlwRequest.getHealthblockId());
        validateFieldString(failureReasons, "name", addFlwRequest.getName());
        validateFieldGfStatus(failureReasons, gfStatus, addFlwRequest.getGfStatus());
        validatetypeASHA(failureReasons, "type", addFlwRequest.getMctsFlwId(), addFlwRequest.getContactNumber(), addFlwRequest.getType());

        if (failureReasons.length() > 0) {
            String fieldName = failureReasons.toString().split("[\\W]")[1];
            csvRejectionsMcts(fieldName, addFlwRequest);
            return failureReasons;
        }
        return null;
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void persistFlwMcts(AddFlwRequest addFlwRequest) {
        Map<String, Object> flwProperties = new HashMap<>();
        flwProperties.put(FlwConstants.NAME, addFlwRequest.getName());
        flwProperties.put(FlwConstants.ID, addFlwRequest.getMctsFlwId());
        flwProperties.put(FlwConstants.CONTACT_NO, addFlwRequest.getContactNumber());
        flwProperties.put(FlwConstants.STATE_ID, addFlwRequest.getStateId());
        flwProperties.put(FlwConstants.DISTRICT_ID, addFlwRequest.getDistrictId());
        flwProperties.put(FlwConstants.GF_STATUS, addFlwRequest.getGfStatus());

        if (addFlwRequest.getType() != null) {
            flwProperties.put(FlwConstants.TYPE, addFlwRequest.getType());
        }

        if (addFlwRequest.getTalukaId() != null) {
            flwProperties.put(FlwConstants.TALUKA_ID, addFlwRequest.getTalukaId());
        }

        if (addFlwRequest.getTalukaName() != null) {
            flwProperties.put(FlwConstants.TALUKA_NAME, addFlwRequest.getTalukaName());
        }

        if (addFlwRequest.getPhcName() != null) {
            flwProperties.put(FlwConstants.PHC_NAME, addFlwRequest.getPhcName());
        }

        if (addFlwRequest.getPhcId() != null) {
            flwProperties.put(FlwConstants.PHC_ID, addFlwRequest.getPhcId());
        }

        if (addFlwRequest.getHealthblockId() != null) {
            flwProperties.put(FlwConstants.HEALTH_BLOCK_ID, addFlwRequest.getHealthblockId());
        }

        if (addFlwRequest.getHealthBlockName() != null) {
            flwProperties.put(FlwConstants.HEALTH_BLOCK_NAME, addFlwRequest.getHealthBlockName());
        }

        if (addFlwRequest.getSubcentreId() != null) {
            flwProperties.put(FlwConstants.SUB_CENTRE_ID, addFlwRequest.getSubcentreId());
        }

        if (addFlwRequest.getSubcentreName() != null) {
            flwProperties.put(FlwConstants.SUB_CENTRE_NAME, addFlwRequest.getSubcentreName());
        }

        if (addFlwRequest.getVillageId() != null) {
            flwProperties.put(FlwConstants.CENSUS_VILLAGE_ID, addFlwRequest.getVillageId());
        }

        if (addFlwRequest.getVillageName() != null) {
            flwProperties.put(FlwConstants.VILLAGE_NAME, addFlwRequest.getVillageName());
        }

        frontLineWorkerImportService.createUpdate(flwProperties, SubscriptionOriginFlw.MCTS_IMPORT);
    }

    @Override
    @Transactional
    public StringBuilder csvUploadRch(AddRchFlwRequest addRchFlwRequest) {
        log("REQUEST: /ops/createUpdateRchFlw", String.format(
                "callingNumber=%s, rchId=%s, name=%s, state=%d, district=%d",
                LogHelper.obscure(addRchFlwRequest.getMsisdn()),
                addRchFlwRequest.getFlwId(),
                addRchFlwRequest.getName(),
                addRchFlwRequest.getStateId(),
                addRchFlwRequest.getDistrictId()));

        StringBuilder failureReasons = new StringBuilder();
        validateField10Digits(failureReasons, contactNumber, addRchFlwRequest.getMsisdn());
        validateFieldPositiveLong(failureReasons, contactNumber, addRchFlwRequest.getMsisdn());
        validateFieldPresent(failureReasons, "rchFlwId", addRchFlwRequest.getFlwId());
        validateFieldPresent(failureReasons, "stateId", addRchFlwRequest.getStateId());
        validateFieldPresent(failureReasons, "districtId", addRchFlwRequest.getDistrictId());
        validateNameFields(failureReasons, "talukaName", addRchFlwRequest.getTalukaName(), addRchFlwRequest.getTalukaId());
        validateNameFields(failureReasons, "phcName", addRchFlwRequest.getPhcName(), addRchFlwRequest.getPhcId());
        validateNameFields(failureReasons, "subcentreName", addRchFlwRequest.getSubcentreName(), addRchFlwRequest.getSubcentreId());
        validateNameFields(failureReasons, "villageName", addRchFlwRequest.getVillageName(), addRchFlwRequest.getVillageId());
        validateNameFields(failureReasons, "healthBlockName", addRchFlwRequest.getHealthBlockName(), addRchFlwRequest.getHealthblockId());
        validateFieldString(failureReasons, "name", addRchFlwRequest.getName());
        validateFieldGfStatus(failureReasons, gfStatus, addRchFlwRequest.getGfStatus());
        validatetypeASHA(failureReasons, "gfType", addRchFlwRequest.getFlwId(), addRchFlwRequest.getMsisdn(), addRchFlwRequest.getGfType());

        if (failureReasons.length() > 0) {
            String fieldName = failureReasons.toString().split("[\\W]")[1];
            csvRejectionsRch(fieldName, addRchFlwRequest);
            return failureReasons;
        }
        return null;
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void persistFlwRch(AddRchFlwRequest addRchFlwRequest) {
        Map<String, Object> flwProperties = new HashMap<>();
        flwProperties.put(FlwConstants.NAME, addRchFlwRequest.getName());
        flwProperties.put(FlwConstants.GF_ID, addRchFlwRequest.getFlwId());
        flwProperties.put(FlwConstants.MOBILE_NO, addRchFlwRequest.getMsisdn());
        flwProperties.put(FlwConstants.STATE_ID, addRchFlwRequest.getStateId());
        flwProperties.put(FlwConstants.DISTRICT_ID, addRchFlwRequest.getDistrictId());
        flwProperties.put(FlwConstants.GF_STATUS, addRchFlwRequest.getGfStatus());

        if (addRchFlwRequest.getGfType() != null) {
            flwProperties.put(FlwConstants.GF_TYPE, addRchFlwRequest.getGfType());
        }

        if (addRchFlwRequest.getTalukaId() != null) {
            flwProperties.put(FlwConstants.TALUKA_ID, addRchFlwRequest.getTalukaId());
        }

        if (addRchFlwRequest.getTalukaName() != null) {
            flwProperties.put(FlwConstants.TALUKA_NAME, addRchFlwRequest.getTalukaName());
        }

        if (addRchFlwRequest.getPhcId() != null) {
            flwProperties.put(FlwConstants.PHC_ID, addRchFlwRequest.getPhcId());
        }

        if (addRchFlwRequest.getPhcName() != null) {
            flwProperties.put(FlwConstants.PHC_NAME, addRchFlwRequest.getPhcName());
        }

        if (addRchFlwRequest.getHealthblockId() != null) {
            flwProperties.put(FlwConstants.HEALTH_BLOCK_ID, addRchFlwRequest.getHealthblockId());
        }

        if (addRchFlwRequest.getHealthBlockName() != null) {
            flwProperties.put(FlwConstants.HEALTH_BLOCK_NAME, addRchFlwRequest.getHealthBlockName());
        }

        if (addRchFlwRequest.getSubcentreId() != null) {
            flwProperties.put(FlwConstants.SUB_CENTRE_ID, addRchFlwRequest.getSubcentreId());
        }

        if (addRchFlwRequest.getSubcentreName() != null) {
            flwProperties.put(FlwConstants.SUB_CENTRE_NAME, addRchFlwRequest.getSubcentreName());
        }

        if (addRchFlwRequest.getVillageId() != null) {
            flwProperties.put(FlwConstants.CENSUS_VILLAGE_ID, addRchFlwRequest.getVillageId());
        }

        if (addRchFlwRequest.getVillageName() != null) {
            flwProperties.put(FlwConstants.VILLAGE_NAME, addRchFlwRequest.getVillageName());
        }

        frontLineWorkerImportService.createUpdate(flwProperties, SubscriptionOriginFlw.RCH_IMPORT);
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public void persistFlwLoc(UpdateFlwLocationRequest updateFlwLocationRequest) {
        Map<String, Object> flwLocProperties = new HashMap<>();
        flwLocProperties.put(FlwConstants.ID, updateFlwLocationRequest.getMctsFlwId());
        flwLocProperties.put(FlwConstants.STATE_ID, updateFlwLocationRequest.getStateId());
        flwLocProperties.put(FlwConstants.DISTRICT_ID, updateFlwLocationRequest.getDistrictId());

        if (updateFlwLocationRequest.getTalukaId() != null) {
            flwLocProperties.put(FlwConstants.TALUKA_ID, updateFlwLocationRequest.getTalukaId());
        }
        if (updateFlwLocationRequest.getTalukaName() != null) {
            flwLocProperties.put(FlwConstants.TALUKA_NAME, updateFlwLocationRequest.getTalukaName());
        }

        if (updateFlwLocationRequest.getPhcId() != null) {
            flwLocProperties.put(FlwConstants.PHC_ID, updateFlwLocationRequest.getPhcId());
        }

        if (updateFlwLocationRequest.getPhcName() != null) {
            flwLocProperties.put(FlwConstants.PHC_NAME, updateFlwLocationRequest.getPhcName());
        }

        if (updateFlwLocationRequest.getHealthblockId() != null) {
            flwLocProperties.put(FlwConstants.HEALTH_BLOCK_ID, updateFlwLocationRequest.getHealthblockId());
        }

        if (updateFlwLocationRequest.getHealthblockName() != null) {
            flwLocProperties.put(FlwConstants.HEALTH_BLOCK_NAME, updateFlwLocationRequest.getHealthblockName());
        }

        if (updateFlwLocationRequest.getSubcentreId() != null) {
            flwLocProperties.put(FlwConstants.SUB_CENTRE_ID, updateFlwLocationRequest.getSubcentreId());
        }

        if (updateFlwLocationRequest.getSubcentreName() != null) {
            flwLocProperties.put(FlwConstants.SUB_CENTRE_NAME, updateFlwLocationRequest.getSubcentreName());
        }

        if (updateFlwLocationRequest.getVillageId() != null) {
            flwLocProperties.put(FlwConstants.CENSUS_VILLAGE_ID, updateFlwLocationRequest.getVillageId());
        }

        if (updateFlwLocationRequest.getVillageName() != null) {
            flwLocProperties.put(FlwConstants.VILLAGE_NAME, updateFlwLocationRequest.getVillageName());
        }

        frontLineWorkerImportService.updateLoc(flwLocProperties);
    }

    @Override
    public void csvRejectionsMcts(String fieldName, AddFlwRequest addFlwRequest) {
        String action = this.flwActionFinder(addFlwRequest);
        if ("contactNumber".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if (gfStatus.equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.GF_STATUS_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if ("type".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.FLW_TYPE_NOT_ASHA.toString(), action));
        } else {
            flwRejectionService.createUpdate(flwRejectionMcts(addFlwRequest, false, RejectionReasons.FIELD_NOT_PRESENT.toString(), action));
        }
    }

    @Override
    @Transactional
    public void csvRejectionsRch(String fieldName, AddRchFlwRequest addRchFlwRequest) {
        String action = this.rchFlwActionFinder(addRchFlwRequest);
        if ("contactNumber".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if (gfStatus.equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.GF_STATUS_EMPTY_OR_WRONG_FORMAT.toString(), action));
        } else if ("type".equals(fieldName)) {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.FLW_TYPE_NOT_ASHA.toString(), action));
        } else {
            flwRejectionService.createUpdate(flwRejectionRch(addRchFlwRequest, false, RejectionReasons.FIELD_NOT_PRESENT.toString(), action));
        }
    }

    private String flwActionFinder(AddFlwRequest record) {
        if (frontLineWorkerService.getByMctsFlwIdAndState(record.getMctsFlwId(), stateDataService.findByCode(record.getStateId())) == null) {
            return "CREATE";
        } else {
            return "UPDATE";
        }
    }

    private String rchFlwActionFinder(AddRchFlwRequest record) {
        if (frontLineWorkerService.getByMctsFlwIdAndState(record.getFlwId(), stateDataService.findByCode(record.getStateId())) == null) {
            return "CREATE";
        } else {
            return "UPDATE";
        }
    }

    public static FlwImportRejection flwRejectionMcts(AddFlwRequest record, Boolean accepted, String rejectionReason, String action) {
        FlwImportRejection flwImportRejection = new FlwImportRejection();
        flwImportRejection.setGfName(record.getName());
        flwImportRejection.setFlwId(Long.parseLong(record.getMctsFlwId()));
        flwImportRejection.setMsisdn(record.getContactNumber().toString());
        flwImportRejection.setStateId(record.getStateId());
        flwImportRejection.setDistrictId(record.getDistrictId());
        flwImportRejection.setTalukaId(record.getTalukaId());
        flwImportRejection.setPhcId(record.getPhcId());
        flwImportRejection.setSubcentreId(record.getSubcentreId());
        flwImportRejection.setVillageId(record.getVillageId());
        flwImportRejection.setHealthBlockId(record.getHealthblockId());
        flwImportRejection.setType(record.getType());
        flwImportRejection.setGfStatus(record.getGfStatus());
        flwImportRejection.setSource("MCTS-Import");
        flwImportRejection.setAccepted(accepted);
        flwImportRejection.setRejectionReason(rejectionReason);
        flwImportRejection.setAction(action);

        return flwImportRejection;
    }

    public static FlwImportRejection flwRejectionRch(AddRchFlwRequest record, Boolean accepted, String rejectionReason, String action) {
        FlwImportRejection flwImportRejection = new FlwImportRejection();
        flwImportRejection.setGfName(record.getName());
        flwImportRejection.setFlwId(Long.parseLong(record.getFlwId()));
        flwImportRejection.setMsisdn(record.getMsisdn().toString());
        flwImportRejection.setStateId(record.getStateId());
        flwImportRejection.setDistrictId(record.getDistrictId());
        flwImportRejection.setTalukaId(record.getTalukaId());
        flwImportRejection.setPhcId(record.getPhcId());
        flwImportRejection.setSubcentreId(record.getSubcentreId());
        flwImportRejection.setVillageId(record.getVillageId());
        flwImportRejection.setHealthBlockId(record.getHealthblockId());
        flwImportRejection.setType(record.getGfType());
        flwImportRejection.setGfStatus(record.getGfStatus());
        flwImportRejection.setSource("RCH-Import");
        flwImportRejection.setAccepted(accepted);
        flwImportRejection.setRejectionReason(rejectionReason);
        flwImportRejection.setAction(action);

        return flwImportRejection;
    }

    private static boolean validateField10Digits(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= SMALLEST_10_DIGIT_NUMBER && value <= LARGEST_10_DIGIT_NUMBER) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    private static boolean validateFieldPositiveLong(StringBuilder errors, String fieldName, Long value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value >= 0) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    private static boolean validateFieldPresent(StringBuilder errors, String fieldName, Object value) {
        if (value != null) {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }

    private static boolean validateNameFields(StringBuilder errors, String fieldName, Object value1, Object value2) {
        if (value2 != null && !"NULL".equalsIgnoreCase(value2.toString()) && !value2.toString().isEmpty() && !"0".equals(value2.toString())) {
            if (value1 != null && !"NULL".equalsIgnoreCase(value1.toString()) && !value1.toString().isEmpty()) {
                return true;
            }
        } else {
            return true;
        }
        errors.append(String.format(NOT_PRESENT, fieldName));
        return false;
    }

    private static boolean validateFieldString(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (value.length() > 0) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }


    private static boolean validateFieldGfStatus(StringBuilder errors, String fieldName, String value) {
        if (!validateFieldPresent(errors, fieldName, value)) {
            return false;
        }
        if (("Active").equals(value) || ("Inactive").equals(value)) {
            return true;
        }
        errors.append(String.format(INVALID, fieldName));
        return false;
    }

    private static boolean validatetypeASHA(StringBuilder errors, String fieldName, String mctsFlwId, Long contactNumber, String type) {
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

    protected static void log(final String endpoint, final String s) {
        LOGGER.info(IVR_INTERACTION_LOG.format(endpoint) + (StringUtils.isBlank(s) ? "" : " : " + s));
    }
}

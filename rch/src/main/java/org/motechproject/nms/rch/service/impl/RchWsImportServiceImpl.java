package org.motechproject.nms.rch.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.Order;
import org.motechproject.nms.rch.domain.RchImportAudit;
import org.motechproject.nms.rch.domain.RchImportFacilitator;
import org.motechproject.nms.rch.domain.RchImportFailRecord;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.exception.RchWebServiceException;
import org.motechproject.nms.rch.repository.RchImportAuditDataService;
import org.motechproject.nms.rch.repository.RchImportFacilitatorDataService;
import org.motechproject.nms.rch.repository.RchImportFailRecordDataService;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.motechproject.nms.rch.service.RchWsImportService;
import org.motechproject.nms.rch.utils.Constants;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.jdo.annotations.Transactional;
import java.io.File;
import java.net.URL;
import java.util.*;

@Service("rchWsImportService")
public class RchWsImportServiceImpl implements RchWsImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RchWsImportServiceImpl.class);
    private static final String RCH_WEB_SERVICE = "RCH Web Service";
    private static final String FILE_RECORD_SUCCESS = "RCH Responses for state id {} recorded to file successfully.";
    private static final String LOG_STATEID = "stateId = {}";

    @Autowired
    private StateDataService stateDataService;

    @Autowired
    private RchWebServiceFacade rchWebServiceFacade;

    @Autowired
    private AlertService alertService;

    @Autowired
    private RchImportAuditDataService rchImportAuditDataService;

    @Autowired
    private RchImportFailRecordDataService rchImportFailRecordDataService;

    @Autowired
    private RchImportFacilitatorDataService rchImportFacilitatorDataService;

    @Autowired
    @Qualifier("rchSettings")
    private SettingsFacade settingsFacade;

    /**
     * Event relay service to handle async notifications
     */
    @Autowired
    private EventRelay eventRelay;

    @Override
    public void startRchImport() {
        eventRelay.sendEventMessage(new MotechEvent(Constants.RCH_IMPORT_EVENT));
    }

    @Override
    public void importFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting import from RCH web service");
        LOGGER.info("Pulling data for {}, for states {}", referenceDate, stateIds);

        if (endpoint == null) {
            LOGGER.debug("Using default service endpoint from WSDL");
        } else {
            LOGGER.debug("Using custom endpoint {}", endpoint);
        }

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.MOTHER, referenceDate, endpoint, Constants.RCH_MOTHER_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.CHILD, referenceDate, endpoint, Constants.RCH_CHILD_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.ASHA, referenceDate, endpoint, Constants.RCH_ASHA_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.DISTRICT, referenceDate, endpoint, Constants.RCH_DISTRICT_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.TALUKA, referenceDate, endpoint, Constants.RCH_TALUKA_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.VILLAGE, referenceDate, endpoint, Constants.RCH_VILLAGE_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.HEALTHBLOCK, referenceDate, endpoint, Constants.RCH_HEALTHBLOCK_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.TALUKAHEALTHBLOCK, referenceDate, endpoint, Constants.RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.HEALTHFACILITY, referenceDate, endpoint, Constants.RCH_HEALTHFACILITY_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.HEALTHSUBFACILITY, referenceDate, endpoint, Constants.RCH_HEALTHSUBFACILITY_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, RchUserType.VILLAGEHEALTHSUBFACILITY, referenceDate, endpoint, Constants.RCH_VILLAGEHEALTHSUBFACILITY_IMPORT_SUBJECT);
        }

        LOGGER.info("Initiated import workflow from RCH for mothers and children");
    }

    @Override
    public void importMothersFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint){
        LOGGER.info("Starting Mother import from RCH web service");
        LOGGER.info("Pulling mother data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.MOTHER, referenceDate, endpoint, Constants.RCH_MOTHER_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for mothers");

    }
    @Override
    public void importChildFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting Child import from RCH web service");
        LOGGER.info("Pulling child data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.CHILD, referenceDate, endpoint, Constants.RCH_CHILD_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for children");
    }
    @Override
    public void importAshaFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting Asha import from RCH web service");
        LOGGER.info("Pulling asha data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.ASHA, referenceDate, endpoint, Constants.RCH_ASHA_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for asha");
    }

    @Override
    public void importTalukaFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting Taluka import from RCH web service");
        LOGGER.info("Pulling taluka data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.TALUKA, referenceDate, endpoint, Constants.RCH_TALUKA_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for taluka");
    }
    @Override
    public void importVillageFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint){
        LOGGER.info("Starting Village import from RCH web service");
        LOGGER.info("Pulling village data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.VILLAGE, referenceDate, endpoint, Constants.RCH_VILLAGE_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for villages");
    }
    @Override
    public void importHealthBlockFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting Healthblock import from RCH web service");
        LOGGER.info("Pulling Healthblock data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.HEALTHBLOCK, referenceDate, endpoint, Constants.RCH_HEALTHBLOCK_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for health blocks");
    }
    @Override
    public void importHealthFacilityFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint){
        LOGGER.info("Starting Healthfacility import from RCH web service");
        LOGGER.info("Pulling Healthfacility data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.HEALTHFACILITY, referenceDate, endpoint, Constants.RCH_HEALTHFACILITY_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for Healthfacility");
    }
    @Override
    public void importHealthSubFacilityFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint){
        LOGGER.info("Starting Healthsubfacility import from RCH web service");
        LOGGER.info("Pulling Healthsubfacility data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.HEALTHSUBFACILITY, referenceDate, endpoint, Constants.RCH_HEALTHSUBFACILITY_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for Healthsubfacility");
    }
    @Override
    public void importDistrictFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting District import from RCH web service");
        LOGGER.info("Pulling District data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.DISTRICT, referenceDate, endpoint, Constants.RCH_DISTRICT_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for District");
    }
    @Override
    public void importTalukaHealthBlockFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting TalukaHealthblock import from RCH web service");
        LOGGER.info("Pulling TalukaHealthblock data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.TALUKAHEALTHBLOCK, referenceDate, endpoint, Constants.RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for TalukaHealthblock");
    }
    @Override
    public void importVillageHealthSubFacilityFromRch(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        LOGGER.info("Starting villagesubfacility import from RCH web service");
        LOGGER.info("Pulling villagesubfacility data for {}, for states {}", referenceDate, stateIds);

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, RchUserType.VILLAGEHEALTHSUBFACILITY, referenceDate, endpoint, Constants.RCH_VILLAGEHEALTHSUBFACILITY_IMPORT_SUBJECT);
        }
        LOGGER.info("Initiated import workflow from RCH for villagesubfacility");
    }

    @MotechListener(subjects = { Constants.RCH_MOTHER_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchMothersData(MotechEvent motechEvent) {
        LOGGER.info("Starting import of RCH mother data");


        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s does not exist in database. Skipping RCH Mother import for this state", stateId);
            LOGGER.error(error);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateId, null, 0, 0, error));
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        boolean success = false;
        int retryCount = 0;

        while (!success && retryCount < 2) {
            try {
                String keelDeelData = rchWebServiceFacade.callEncryptApi(stateId.toString(), settingsFacade.getProperty(Constants.RCH_MOTHER_USER), startReferenceDate, endReferenceDate);

                if (keelDeelData == null) {
                    String error = String.format("Received null response from encrypt API for mother data for state %s (ID: %d)", stateName, stateId);
                    LOGGER.error(error);

                    if (retryCount == 0) {
                        rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
                        rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.MOTHER, stateId));
                    }
                    retryCount++;
                    continue;
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode encryptJson = objectMapper.readTree(keelDeelData);
                String keel = encryptJson.get("keel").asText();
                String deel = encryptJson.get("deel").asText();

                String token = rchWebServiceFacade.generateAuthToken(); // Ensure token is generated before use
                String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApi(token, keel, deel);

                if (keelDeelApiResponse != null) {
                    String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "mother", stateId.toString());

                    Map<String, Object> params = new HashMap<>();
                    params.put("state", stateId.toString());
                    params.put("tempFilePath", tempFilePath);
                    params.put("fromDate", startReferenceDate);
                    params.put("endDate", endReferenceDate);
                    params.put("stateName", stateName);
                    params.put("stateCode", stateCode.toString());

                    MotechEvent thirdEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "mother", params);
                    eventRelay.sendEventMessage(thirdEvent);

                    success = true; // Mark as successful
                } else {
                    String error = String.format("Received null response from keel-deel API for mother data for state %s (ID: %d)", stateName, stateId);
                    LOGGER.error(error);

                    if (retryCount == 0) {
                        rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
                        rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.MOTHER, stateId));
                    }
                    retryCount++;
                }

            } catch (Exception e) {
                String error = String.format("Failed to process RCH mother data for state %s (ID: %d) after retries: %s", stateName, stateId, e.getMessage());
                LOGGER.error(error);

                if (retryCount == 0) {
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.MOTHER, stateCode, stateName, 0, 0, error));
                    rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.MOTHER, stateId));
                }
                retryCount++;
            }
        }
    }

    @MotechListener(subjects = { Constants.SECOND_EVENT_PREFIX + "mother", Constants.SECOND_EVENT_PREFIX + "child", Constants.SECOND_EVENT_PREFIX + "asha" })
    public void handleThirdApiEvent(MotechEvent event) {
        String stateId = (String) event.getParameters().get("state");
        LocalDate from = (LocalDate) event.getParameters().get("fromDate");
        LocalDate to = (LocalDate) event.getParameters().get("endDate");
        String tempFilePath = (String) event.getParameters().get("tempFilePath");
        String entityType = event.getSubject().split("\\.")[2]; // Determines if it's mother, child, or asha
        String stateName = (String) event.getParameters().get("stateName");
        String stateCode = (String) event.getParameters().get("stateCode");
        boolean status = false;

        RchUserType userType = RchUserType.valueOf(entityType.toUpperCase());

        try {
            // 1. Read the payload from the temp file
            String payload = rchWebServiceFacade.readPayloadFromTempFile(tempFilePath);


            // 2. Call the third API using the payload
            String thirdApiResponse = rchWebServiceFacade.callThirdApi(payload);


            // 3. Save the response from the third API using generateJsonResponseFile
            File responseFile = rchWebServiceFacade.generateJsonResponseFile(thirdApiResponse, userType, Long.valueOf(stateId));
            if (responseFile != null) {
                LOGGER.info("RCH {} response successfully written to file. Copying to remote directory.", userType);
                status = rchWebServiceFacade.retryScpAndAudit(responseFile.getName(), from, to, Long.valueOf(stateId), userType, 0);
            } else {
                LOGGER.error("Error writing {} response to file for state {}", userType, stateId);
            }

            // 4. Post-file-processing based on status
            if (status) {
                LOGGER.info(FILE_RECORD_SUCCESS, stateId);
            } else {
                handleError(userType, Long.valueOf(stateId), stateName, Long.valueOf(stateCode), from, to, "Failed to process third API response.");
            }

        } catch (Exception e) {
            LOGGER.error("Error handling third API event for state: {}", stateId, e);
            handleError(userType, Long.valueOf(stateId), stateName, Long.valueOf(stateCode), from, to, e.getMessage());
        }
    }

    /**
     * Method to handle errors and create alerts, audit records, and failure records.
     */
    private void handleError(RchUserType userType, Long stateId, String stateName, Long stateCode, LocalDate from, LocalDate to, String errorMessage) {
        try {
            String error = String.format("Cannot process RCH %s data for state %s with state ID: %d. Error: %s",
                    userType, stateName, stateId, errorMessage);
            LOGGER.error(error);
            alertService.create(RCH_WEB_SERVICE, "RCH Web Service " + userType + " Import", errorMessage, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            rchImportAuditDataService.create(new RchImportAudit(from, to, userType, stateCode, stateName, 0, 0, error));
            rchImportFailRecordDataService.create(new RchImportFailRecord(to, userType, stateId));
        } catch (Exception e) {
            LOGGER.error("Error creating failure records: {}", e.getMessage(), e);
        }
    }



    @MotechListener(subjects = { Constants.RCH_DISTRICT_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchDistrictData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in the database. Skipping district import for this state.", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();

        try {
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_DISTRICT));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "district", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "district", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH district data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.DISTRICT, stateId, stateName, stateCode, startDate, endDate, error);
        }
    }


    private RchUserType getUserTypeFromEventSubject(String subject) {
        if (subject.endsWith("villagehealthsubfacility")) return RchUserType.VILLAGEHEALTHSUBFACILITY;
        if (subject.endsWith("talukahealthblock")) return RchUserType.TALUKAHEALTHBLOCK;
        if (subject.endsWith("district")) return RchUserType.DISTRICT;
        if (subject.endsWith("taluka")) return RchUserType.TALUKA;
        if (subject.endsWith("village")) return RchUserType.VILLAGE;
        if (subject.endsWith("healthblock")) return RchUserType.HEALTHBLOCK;
        if (subject.endsWith("healthfacility")) return RchUserType.HEALTHFACILITY;
        if (subject.endsWith("healthsubfacility")) return RchUserType.HEALTHSUBFACILITY;
        return null;
    }



    @MotechListener(subjects = {
            Constants.SECOND_EVENT_PREFIX + "district",
            Constants.SECOND_EVENT_PREFIX + "taluka",
            Constants.SECOND_EVENT_PREFIX + "village",
            Constants.SECOND_EVENT_PREFIX + "healthblock",
            Constants.SECOND_EVENT_PREFIX + "talukahealthblock",
            Constants.SECOND_EVENT_PREFIX + "healthfacility",
            Constants.SECOND_EVENT_PREFIX + "healthsubfacility",
            Constants.SECOND_EVENT_PREFIX + "villagehealthsubfacility"
    })
    public void handleApiEvent(MotechEvent event) {
        String subject = event.getSubject();
        RchUserType userType = getUserTypeFromEventSubject(subject);
        if (userType == null) {
            LOGGER.error("Unrecognized event subject: {}", subject);
            return;
        }

        String stateId = (String) event.getParameters().get("state");
        LocalDate from = (LocalDate) event.getParameters().get("fromDate");
        LocalDate to = (LocalDate) event.getParameters().get("endDate");
        String tempFilePath = (String) event.getParameters().get("tempFilePath");
        String stateName = (String) event.getParameters().get("stateName");
        String stateCode = (String) event.getParameters().get("stateCode");
        boolean status = false;

        try {
            String payload = rchWebServiceFacade.readPayloadFromTempFile(tempFilePath);

            String thirdApiResponse = rchWebServiceFacade.callThirdApi(payload);

            File responseFile = rchWebServiceFacade.generateJsonResponseFile(thirdApiResponse, userType, Long.valueOf(stateId));
            if (responseFile != null) {
                LOGGER.info("RCH {} response successfully written to file. Copying to remote directory.", userType);
                status = rchWebServiceFacade.retryScpAndAudit(responseFile.getName(), from, to, Long.valueOf(stateId), userType, 0);
            } else {
                LOGGER.error("Error writing {} response to file for state {}", userType, stateId);
            }

            if (status) {
                LOGGER.info("{} data processed successfully for state ID: {}", userType, stateId);
            } else {
                handleError(userType, Long.valueOf(stateId), stateName, Long.valueOf(stateCode), from, to, "Failed to process third API response.");
            }

        } catch (Exception e) {
            LOGGER.error("Error handling third API event for {} data for state: {}", userType, stateId, e);
            handleError(userType, Long.valueOf(stateId), stateName, Long.valueOf(stateCode), from, to, e.getMessage());
        }
    }


    @MotechListener(subjects = { Constants.RCH_TALUKA_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchTalukaData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping Taluka import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_SUB_DISTRICT));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "taluka", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "taluka", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH taluka data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.TALUKA, stateId, stateName, stateCode, startDate, endDate, error);
        }
    }

    @MotechListener(subjects = { Constants.RCH_VILLAGE_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchVillageData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping Village import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            LOGGER.debug("Calling encrypt API for village data for state ID: {}", stateId);
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_VILLAGE));
            LOGGER.debug("Keel and Deel data received for village import: {}", keelDeelData);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "village", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "village", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH village data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.VILLAGE, stateId, stateName, stateCode, startDate, endDate, error);
        }
    }

    @MotechListener(subjects = { Constants.RCH_HEALTHBLOCK_IMPORT_SUBJECT})
    @Transactional
    @Override
    public void importRchHealthBlockData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LOGGER.debug(LOG_STATEID, stateId);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping HealthBlock import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            LOGGER.debug("Calling encrypt API for healthblock data for state ID: {}", stateId);
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_HEALTH_BLOCK));
            LOGGER.debug("Keel and Deel data received for healthblock import: {}", keelDeelData);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "healthblock", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "healthblock", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH healthblock data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.HEALTHBLOCK, stateId, stateName, stateCode, startDate, endDate, error);
        }
    }

    @MotechListener(subjects = { Constants.RCH_TALUKA_HEALTHBLOCK_IMPORT_SUBJECT})
    @Transactional
    @Override
    public void importRchTalukaHealthBlockData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping taluka-healthblock import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_TALUKA_HEALTH_BLOCK));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "talukahealthblock", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "talukahealthblock", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH talukahealthblock data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.TALUKAHEALTHBLOCK, stateId, stateName, stateCode, startDate, endDate, error);
        }
    }

    @MotechListener(subjects = { Constants.RCH_HEALTHFACILITY_IMPORT_SUBJECT})
    @Transactional
    @Override
    public void importRchHealthFacilityData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LOGGER.debug(LOG_STATEID, stateId);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping HealthFacility import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_HEALTH_FACILITY));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "healthfacility", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "healthfacility", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH healthfacility data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.HEALTHFACILITY, stateId, stateName, stateCode, startDate, endDate, error);
        }

    }

    @MotechListener(subjects = { Constants.RCH_HEALTHSUBFACILITY_IMPORT_SUBJECT})
    @Transactional
    @Override
    public void importRchHealthSubFacilityData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LOGGER.debug(LOG_STATEID, stateId);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping HealthSubFacility import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_HEALTH_SUB_FACILITY));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "healthsubfacility", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "healthsubfacility", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH healthfacility data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.HEALTHSUBFACILITY, stateId, stateName, stateCode, startDate, endDate, error);
        }
    }

    @MotechListener(subjects = { Constants.RCH_VILLAGEHEALTHSUBFACILITY_IMPORT_SUBJECT})
    @Transactional
    @Override
    public void importRchVillageHealthSubFacilityData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LOGGER.debug(LOG_STATEID, stateId);
        LocalDate startDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping VillageHealthSubFacility import for this state", stateId);
            LOGGER.error(error);
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            String keelDeelData = rchWebServiceFacade.callEncryptApiLocations(stateId.toString(), settingsFacade.getProperty(Constants.TYPE_ID_HEALTH_VILLAGE));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApiLocations(token, keel, deel);

            String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "villagehealthsubfacility", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate", startDate);
            params.put("endDate", endDate);
            params.put("stateName", stateName);
            params.put("stateCode", stateCode.toString());
            MotechEvent nextEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "villagehealthsubfacility", params);
            eventRelay.sendEventMessage(nextEvent);

        } catch (Exception e) {
            String error = String.format("Error processing RCH villagehealthsubfacility data for state %s (ID: %d): %s", stateName, stateId, e.getMessage());
            LOGGER.error(error, e);
            handleError(RchUserType.VILLAGEHEALTHSUBFACILITY, stateId, stateName, stateCode, startDate, endDate, error);
        }
    }

    @MotechListener(subjects = { Constants.RCH_CHILD_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchChildrenData(MotechEvent motechEvent) {
        LOGGER.info("Starting import of RCH child data");

        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s does not exist in database. Skipping RCH Children import for this state", stateId);
            LOGGER.error(error);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateId, null, 0, 0, error));
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        boolean success = false;
        int retryCount = 0;

        while (!success && retryCount < 2) {
            try {
                String keelDeelData = rchWebServiceFacade.callEncryptApi(stateId.toString(), settingsFacade.getProperty(Constants.RCH_CHILD_USER), startReferenceDate, endReferenceDate);

                if (keelDeelData == null) {
                    String error = String.format("Received null response from encrypt API for child data for state %s (ID: %d)", stateName, stateId);

                    if (retryCount == 0) {
                        rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
                        rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
                    }
                    retryCount++;
                    continue;
                }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

            String token = rchWebServiceFacade.generateAuthToken();
            String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApi(token, keel, deel);

                if (keelDeelApiResponse != null) {
                    String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "child", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate",startReferenceDate);
            params.put("endDate",endReferenceDate);
            params.put("stateName",stateName);
            params.put("stateCode",stateCode.toString());
            MotechEvent thirdEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "child", params);
            eventRelay.sendEventMessage(thirdEvent);

                    success = true;
                } else {
                    String error = String.format("Received null response from keel-deel API for child data for state %s (ID: %d)", stateName, stateId);

                    if (retryCount == 0) {
                        rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
                        rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
                    }
                    retryCount++;
                }
            } catch (Exception e) {
                String error = String.format("Failed to process RCH child data for state %s (ID: %d) after retries: %s", stateName, stateId, e.getMessage());

                LOGGER.error(error);
                if (retryCount == 0) {
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.CHILD, stateCode, stateName, 0, 0, error));
                    rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.CHILD, stateId));
                }
                retryCount++;
            }
        }
    }

    @MotechListener(subjects = { Constants.RCH_ASHA_IMPORT_SUBJECT })
    @Transactional
    @Override
    public void importRchAshaData(MotechEvent motechEvent) {
        LOGGER.info("Starting import of RCH ASHA data");

        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s does not exist in database. Skipping RCH ASHA import for this state", stateId);
            LOGGER.error(error);
            rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateId, null, 0, 0, error));
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        boolean success = false;
        int retryCount = 0;

        while (!success && retryCount < 2) {
            try {
                String keelDeelData = rchWebServiceFacade.callEncryptApi(stateId.toString(), settingsFacade.getProperty(Constants.RCH_ASHA_USER), startReferenceDate, endReferenceDate);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode encryptJson = objectMapper.readTree(keelDeelData);
            String keel = encryptJson.get("keel").asText();
            String deel = encryptJson.get("deel").asText();

                String token = rchWebServiceFacade.generateAuthToken();
                String keelDeelApiResponse = rchWebServiceFacade.callKeelDeelApi(token, keel, deel);

                if (keelDeelApiResponse != null) {
                    String tempFilePath = rchWebServiceFacade.saveToFile(keelDeelApiResponse, "asha", stateId.toString());

            Map<String, Object> params = new HashMap<>();
            params.put("state", stateId.toString());
            params.put("tempFilePath", tempFilePath);
            params.put("fromDate",startReferenceDate);
            params.put("endDate",endReferenceDate);
            params.put("stateName",stateName);
            params.put("stateCode",stateCode.toString());
            MotechEvent thirdEvent = new MotechEvent(Constants.SECOND_EVENT_PREFIX + "asha", params);
            eventRelay.sendEventMessage(thirdEvent);

                    success = true;
                } else {
                    String error = String.format("Received null response from keel-deel API for ASHA data for state %s (ID: %d)", stateName, stateId);
                    if (retryCount == 0) {
                        rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, error));
                        rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
                    }
                    retryCount++;
                }

            } catch (Exception e) {
                String error = String.format("Failed to process RCH ASHA data for state %s (ID: %d) after retries: %s", stateName, stateId, e.getMessage());
                LOGGER.error(error);
                if (retryCount == 0) {
                    rchImportAuditDataService.create(new RchImportAudit(startReferenceDate, endReferenceDate, RchUserType.ASHA, stateCode, stateName, 0, 0, error));
                    rchImportFailRecordDataService.create(new RchImportFailRecord(endReferenceDate, RchUserType.ASHA, stateId));
                }
                retryCount++;
            }
        }
    }

    private void sendImportEventForAUserType(Long stateId, RchUserType userType, LocalDate referenceDate, URL endpoint, String importSubject) {

        LOGGER.debug("Fetching all the failed imports in the last 7 days for stateId {} and UserType {}", stateId, userType);
        QueryParams queryParams = new QueryParams(new Order("importDate", Order.Direction.ASC));
        // QueryParams queryParamsfinal = new QueryParams(new Order("importDate", Order.Direction.DESC));
        //List<RchImportFailRecord> failedImports = rchImportFailRecordDataService.getByStateAndImportdateAndUsertype(stateId, referenceDate.minusDays(6), userType, queryParams);
//        LOGGER.info("failedImports {}", failedImports);
        //LocalDate startDate = failedImports.isEmpty() ? referenceDate : failedImports.get(0).getImportDate();
        //LOGGER.info("fromDate {}", startDate);
        List<RchImportFacilitator> failedRecords = rchImportFacilitatorDataService.getByStateIdAndImportDateAndUserType(stateId, LocalDate.now().minusDays(6), userType, queryParams);
        LOGGER.debug("total file size = ", failedRecords.size());
        Collections.reverse(failedRecords);
        LocalDate startDateFinal = referenceDate;
        int i = 0;
        if (failedRecords.isEmpty()) {
            startDateFinal = referenceDate.minusDays(6);
        } else if (failedRecords.get(i).getFinalStatus()) {
            startDateFinal = failedRecords.get(i).getImportDate();
        } else {
            startDateFinal = referenceDate.minusDays(1);
            while (!failedRecords.get(i).getFinalStatus() && i < failedRecords.size() - 1) {
                startDateFinal = failedRecords.get(i).getImportDate().minusDays(1);
                i++;
            }
        }
        LOGGER.info("fromDate {}", startDateFinal);
        if (!startDateFinal.equals(LocalDate.now())) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(Constants.START_DATE_PARAM, startDateFinal);
            eventParams.put(Constants.END_DATE_PARAM, referenceDate);
            eventParams.put(Constants.STATE_ID_PARAM, stateId);
            eventParams.put(Constants.ENDPOINT_PARAM, endpoint);
            LOGGER.debug("Sending import message for stateId {} and UserType {}", stateId, userType);
            eventRelay.sendEventMessage(new MotechEvent(importSubject, eventParams));
        }
        /*LOGGER.debug("Fetching all the failed imports in the last 7 days for stateId {} and UserType {}", stateId, userType);
        QueryParams queryParams = new QueryParams(new Order("importDate", Order.Direction.ASC));
        List<RchImportFailRecord> failedImports = rchImportFailRecordDataService.getByStateAndImportdateAndUsertype(stateId, referenceDate.minusDays(6), userType, queryParams);
        LOGGER.info("failedImports {}", failedImports);
        LocalDate startDate = failedImports.isEmpty() ? referenceDate : failedImports.get(0).getImportDate();
        LOGGER.info("fromDate {}", startDate);
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(Constants.START_DATE_PARAM, startDate);
        eventParams.put(Constants.END_DATE_PARAM, referenceDate);
        eventParams.put(Constants.STATE_ID_PARAM, stateId);
        eventParams.put(Constants.ENDPOINT_PARAM, endpoint);
        LOGGER.debug("Sending import message for stateId {} and UserType {}", stateId, userType);
        eventRelay.sendEventMessage(new MotechEvent(importSubject, eventParams));*/
    }
}

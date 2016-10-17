package org.motechproject.nms.mcts.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.util.Order;
import org.motechproject.nms.flw.exception.FlwExistingRecordException;
import org.motechproject.nms.flw.exception.FlwImportException;
import org.motechproject.nms.flw.utils.FlwConstants;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.AnmAshaRecord;
import org.motechproject.nms.mcts.contract.ChildRecord;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MotherRecord;
import org.motechproject.nms.mcts.contract.MothersDataSet;
import org.motechproject.nms.mcts.domain.MctsImportAudit;
import org.motechproject.nms.mcts.domain.MctsImportFailRecord;
import org.motechproject.nms.mcts.domain.MctsUserType;
import org.motechproject.nms.mcts.exception.MctsInvalidResponseStructureException;
import org.motechproject.nms.mcts.exception.MctsWebServiceException;
import org.motechproject.nms.mcts.repository.MctsImportAuditDataService;
import org.motechproject.nms.mcts.repository.MctsImportFailRecordDataService;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("mctsWsImportService")
public class MctsWsImportServiceImpl implements MctsWsImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsWsImportServiceImpl.class);
    private static final String MCTS_WEB_SERVICE = "MCTS Web Service";
    private static final double THOUSAND = 1000d;

    @Autowired
    private FrontLineWorkerImportService frontLineWorkerImportService;

    @Autowired
    private StateDataService stateDataService;

    @Autowired
    private MctsWebServiceFacade mctsWebServiceFacade;

    @Autowired
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;

    @Autowired
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private MctsImportAuditDataService mctsImportAuditDataService;

    @Autowired
    private MctsImportFailRecordDataService mctsImportFailRecordDataService;

    @Autowired
    @Qualifier("mctsSettings")
    private SettingsFacade settingsFacade;

    /**
     * Event relay service to handle async notifications
     */
    @Autowired
    private EventRelay eventRelay;

    /**
     * Ops override to kick off mcts import for all states and all types
     */
    @Override
    public void startMctsImport() {
        eventRelay.sendEventMessage(new MotechEvent(Constants.MCTS_IMPORT_EVENT));
    }

    @Override
    public void importFromMcts(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {

        LOGGER.info("Starting import from MCTS web service");
        LOGGER.info("Pulling data for {}, for states {}", referenceDate, stateIds);

        if (endpoint == null) {
            LOGGER.debug("Using default service endpoint from WSDL");
        } else {
            LOGGER.debug("Using custom endpoint {}", endpoint);
        }

        for (Long stateId : stateIds) {
            sendImportEventForAUserType(stateId, MctsUserType.MOTHER, referenceDate, endpoint, Constants.MCTS_MOTHER_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, MctsUserType.CHILD, referenceDate, endpoint, Constants.MCTS_CHILD_IMPORT_SUBJECT);
            sendImportEventForAUserType(stateId, MctsUserType.ASHA, referenceDate, endpoint, Constants.MCTS_ASHA_IMPORT_SUBJECT);
        }

        LOGGER.info("Initiated import workflow from MCTS for mothers, children and ashas");
    }

    private void sendImportEventForAUserType(Long stateId, MctsUserType userType, LocalDate referenceDate, URL endpoint, String importSubject) {

        LOGGER.debug("Fetching all the failed imports in the last 7days for stateId {} and UserType {}", stateId, userType);
        QueryParams queryParams = new QueryParams(new Order("importDate", Order.Direction.ASC));
        List<MctsImportFailRecord> failedImports = mctsImportFailRecordDataService.getByStateAndImportdateAndUsertype(stateId, referenceDate.minusDays(6), userType, queryParams);
        LocalDate startDate = failedImports.isEmpty() ? referenceDate : failedImports.get(0).getImportDate();

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(Constants.START_DATE_PARAM, startDate);
        eventParams.put(Constants.END_DATE_PARAM, referenceDate);
        eventParams.put(Constants.STATE_ID_PARAM, stateId);
        eventParams.put(Constants.ENDPOINT_PARAM, endpoint);
        LOGGER.debug("Sending import message for stateId {} and UserType {}", stateId, userType);
        eventRelay.sendEventMessage(new MotechEvent(importSubject, eventParams));
    }

    @MotechListener(subjects = { Constants.MCTS_MOTHER_IMPORT_SUBJECT })
    @Transactional
    public void importMothersData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        LOGGER.info("Starting mother import");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping Mother importing for this state", stateId);
            LOGGER.error(error);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.MOTHER, stateId, null, 0, 0, error));
            return;
        }

        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {
            MothersDataSet mothersDataSet = mctsWebServiceFacade.getMothersData(startReferenceDate, endReferenceDate, endpoint, stateId);
            if (mothersDataSet == null || mothersDataSet.getRecords() == null) {
                String warning = String.format("No mother data set received from MCTS for %s state", stateName);
                LOGGER.warn(warning);
                mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.MOTHER, stateCode, stateName, 0, 0, warning));
                return;
            }
            LOGGER.info("Received {} mother records from MCTS for {} state", sizeNullSafe(mothersDataSet.getRecords()), stateName);

            MctsImportAudit audit = saveImportedMothersData(mothersDataSet, stateName, stateCode, startReferenceDate, endReferenceDate);
            mctsImportAuditDataService.create(audit);
            stopWatch.stop();
            double seconds = stopWatch.getTime() / THOUSAND;
            LOGGER.info("Finished mother import dispatch in {} seconds. Accepted {} mothers, Rejected {} mothers",
                    seconds, audit.getAccepted(), audit.getRejected());

            // Delete MctsImportFailRecords once import is successful
            deleteMctsImportFailRecords(startReferenceDate, endReferenceDate, MctsUserType.MOTHER, stateId);

        } catch (MctsWebServiceException e) {
            String error = String.format("Cannot read mothers data from %s state with stateId: %d", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Mother Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.MOTHER, stateCode, stateName, 0, 0, error));
            mctsImportFailRecordDataService.create(new MctsImportFailRecord(endReferenceDate, MctsUserType.MOTHER, stateId));
        } catch (MctsInvalidResponseStructureException e) {
            String error = String.format("Cannot read mothers data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Mother Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.MOTHER, stateCode, stateName, 0, 0, error));
            mctsImportFailRecordDataService.create(new MctsImportFailRecord(endReferenceDate, MctsUserType.MOTHER, stateId));
        }
    }

    private MctsImportAudit saveImportedMothersData(MothersDataSet mothersDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        LOGGER.info("Starting mother import for state {}", stateName);

        int saved = 0;
        int rejected = 0;
        Map<Long, Set<Long>> hpdMap = getHpdFilters();
        for (MotherRecord record : mothersDataSet.getRecords()) {
            try {
                // get user property map
                Map<String, Object> recordMap = toMap(record);

                // validate if user needs to be hpd filtered (true if user can be added)
                boolean hpdValidation = validateHpdUser(hpdMap,
                        (long) recordMap.get(KilkariConstants.STATE_ID),
                        (long) recordMap.get(KilkariConstants.DISTRICT_ID));
                if (hpdValidation && mctsBeneficiaryImportService.importMotherRecord(recordMap)) {
                    saved++;
                } else {
                    rejected++;
                }
            } catch (RuntimeException e) {
                LOGGER.error("Mother import Error. Cannot import Mother with ID: {} for state ID: {}",
                        record.getIdNo(), stateCode, e);
                rejected++;
            }
            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("{} state, Progress: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("{} state, Total: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
        return new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.MOTHER, stateCode, stateName, saved, rejected, null);
    }

    @MotechListener(subjects = { Constants.MCTS_CHILD_IMPORT_SUBJECT })
    @Transactional
    public void importChildrenData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        LOGGER.info("Starting children import for stateId: {}", stateId);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            String error = String.format("State with code %s doesn't exist in database. Skipping Children import for this state", stateId);
            LOGGER.error(error);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.CHILD, stateId, null, 0, 0, error));
            return;
        }
        String stateName = state.getName();
        Long stateCode = state.getCode();
        try {

            ChildrenDataSet childrenDataSet = mctsWebServiceFacade.getChildrenData(startReferenceDate, endReferenceDate, endpoint, stateId);
            if (childrenDataSet == null || childrenDataSet.getRecords() == null) {
                String warning = String.format("No child data set received from MCTS for %s state", stateName);
                LOGGER.warn(warning);
                mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.CHILD, stateCode, stateName, 0, 0, warning));
                return;
            }
            LOGGER.info("Received {} children records from MCTS for {} state", sizeNullSafe(childrenDataSet.getRecords()), stateName);

            MctsImportAudit audit = saveImportedChildrenData(childrenDataSet, stateName, stateCode, startReferenceDate, endReferenceDate);
            mctsImportAuditDataService.create(audit);
            stopWatch.stop();
            double seconds = stopWatch.getTime() / THOUSAND;
            LOGGER.info("Finished children import dispatch in {} seconds. Accepted {} children, Rejected {} children",
                    seconds, audit.getAccepted(), audit.getRejected());

            // Delete MctsImportFailRecords once import is successful
            deleteMctsImportFailRecords(startReferenceDate, endReferenceDate, MctsUserType.CHILD, stateId);

        } catch (MctsWebServiceException e) {
            String error = String.format("Cannot read children data from %s State with stateId:%d", stateName, stateCode);
            LOGGER.error(error, e);
            alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Child Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.CHILD, stateCode, stateName, 0, 0, error));
            mctsImportFailRecordDataService.create(new MctsImportFailRecord(endReferenceDate, MctsUserType.CHILD, stateId));
        } catch (MctsInvalidResponseStructureException e) {
            String error = String.format("Cannot read children data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
            LOGGER.error(error, e);
            alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Child Import", e.getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.CHILD, stateCode, stateName, 0, 0, error));
            mctsImportFailRecordDataService.create(new MctsImportFailRecord(endReferenceDate, MctsUserType.CHILD, stateId));
        }
    }

    private MctsImportAudit saveImportedChildrenData(ChildrenDataSet childrenDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        LOGGER.info("Starting children import for state {}", stateName);

        int saved = 0;
        int rejected = 0;
        Map<Long, Set<Long>> hpdMap = getHpdFilters();

        for (ChildRecord record : childrenDataSet.getRecords()) {
            try {
                // get user property map
                Map<String, Object> recordMap = toMap(record);

                // validate if user needs to be hpd filtered (true if user can be added)
                boolean hpdValidation = validateHpdUser(hpdMap,
                        (long) recordMap.get(KilkariConstants.STATE_ID),
                        (long) recordMap.get(KilkariConstants.DISTRICT_ID));

                if (hpdValidation && mctsBeneficiaryImportService.importChildRecord(toMap(record))) {
                    saved++;
                } else {
                    rejected++;
                }

            } catch (RuntimeException e) {
                LOGGER.error("Child import Error. Cannot import Child with ID: {} for state:{} with state ID: {}",
                        record.getIdNo(), stateName, stateCode, e);
                rejected++;
            }

            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("{} state, Progress: {} children imported, {} children rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("{} state, Total: {} children imported, {} children rejected", stateName, saved, rejected);
        return new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.CHILD, stateCode, stateName, saved, rejected, null);
    }

    @MotechListener(subjects = { Constants.MCTS_ASHA_IMPORT_SUBJECT })
    @Transactional
    public void importAnmAshaData(MotechEvent motechEvent) {
        Long stateId = (Long) motechEvent.getParameters().get(Constants.STATE_ID_PARAM);
        LocalDate startReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.START_DATE_PARAM);
        LocalDate endReferenceDate = (LocalDate) motechEvent.getParameters().get(Constants.END_DATE_PARAM);
        URL endpoint = (URL) motechEvent.getParameters().get(Constants.ENDPOINT_PARAM);

        LOGGER.info("Starting Anm Asha import");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        State state = stateDataService.findByCode(stateId);
        if (state == null) {
            LOGGER.warn("State with code {} doesn't exist in database. Skipping FLW import for this state", stateId);
            return;
        }
        String stateName = state.getName();
        Long stateCode = state.getCode();

        try {
            AnmAshaDataSet anmAshaDataSet = mctsWebServiceFacade.getAnmAshaData(startReferenceDate, endReferenceDate, endpoint, stateId);
            if (anmAshaDataSet == null || anmAshaDataSet.getRecords() == null) {
                String warning = String.format("No ANM Asha data set received from MCTS for %s state", stateName);
                LOGGER.warn(warning, stateName);
                mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.ASHA, stateCode, stateName, 0, 0, warning));
                return;
            }
            LOGGER.info("Received {} ASHA records from MCTS for {} state", sizeNullSafe(anmAshaDataSet.getRecords()), stateName);

            MctsImportAudit audit = saveImportedAnmAshaData(anmAshaDataSet, state, startReferenceDate, endReferenceDate);
            mctsImportAuditDataService.create(audit);
            stopWatch.stop();
            double seconds = stopWatch.getTime() / THOUSAND;
            LOGGER.info("Finished Anm Asha import dispatch in {} seconds. Accepted {} ASHA, Rejected {} ASHA",
                    seconds, audit.getAccepted(), audit.getRejected());

            // Delete MctsImportFailRecords once import is successful
            deleteMctsImportFailRecords(startReferenceDate, endReferenceDate, MctsUserType.ASHA, stateId);

        } catch (MctsWebServiceException e) {
            String error = String.format("Cannot read anm asha data from %s state with stateId:%d", stateName, stateId);
            LOGGER.error(error, e);
            alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service FLW Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.ASHA, stateCode, stateName, 0, 0, error));
            mctsImportFailRecordDataService.create(new MctsImportFailRecord(endReferenceDate, MctsUserType.ASHA, stateId));
        } catch (MctsInvalidResponseStructureException e) {
            String error = String.format("Cannot read anm asha data from %s state with stateId: %d. Response Deserialization Error", stateName, stateCode);
            LOGGER.error(error, e);
            alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service FLW Import", e
                    .getMessage() + " " + error, AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            mctsImportAuditDataService.create(new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.ASHA, stateCode, stateName, 0, 0, error));
            mctsImportFailRecordDataService.create(new MctsImportFailRecord(endReferenceDate, MctsUserType.ASHA, stateId));
        }
    }

    private MctsImportAudit saveImportedAnmAshaData(AnmAshaDataSet anmAshaDataSet, State state, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        String stateName = state.getName();
        Long stateCode = state.getCode();
        LOGGER.info("Starting ASHA import for state {}", stateName);

        int saved = 0;
        int rejected = 0;

        for (AnmAshaRecord record : anmAshaDataSet.getRecords()) {
            String designation = record.getType();
            designation = (designation != null) ? designation.trim() : designation;
            if (!(FlwConstants.VALID_TYPE.equalsIgnoreCase(designation))) {
                rejected++;
            } else {
                try {
                    // get user property map
                    Map<String, Object> recordMap = record.toFlwRecordMap();    // temp var used for debugging
                    frontLineWorkerImportService.importFrontLineWorker(recordMap, state);
                    saved++;
                } catch (InvalidLocationException e) {
                    LOGGER.warn("Invalid location for FLW: ", e);
                    rejected++;
                } catch (FlwImportException e) {
                    LOGGER.error("Existing FLW with same MSISDN but different MCTS ID", e);
                    rejected++;
                } catch (FlwExistingRecordException e) {
                    LOGGER.error("Cannot import FLW with ID: {}, and MSISDN (Contact_No): {}", record.getId(), record.getContactNo(), e);
                    rejected++;
                } catch (Exception e) {
                    LOGGER.error("Flw import Error. Cannot import FLW with ID: {}, and MSISDN (Contact_No): {}",
                            record.getId(), record.getContactNo(), e);
                    rejected++;
                }
            }
            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("{} state, Progress: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
            }
        }
        LOGGER.info("{} state, Total: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
        return new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.ASHA, stateCode, stateName, saved, rejected, null);
    }

    private void deleteMctsImportFailRecords(final LocalDate startReferenceDate, final LocalDate endReferenceDate, final MctsUserType mctsUserType, final Long stateId) {

        LOGGER.debug("Deleting nms_mcts_failures records which are successfully imported");
        if (startReferenceDate.equals(endReferenceDate)) {
            LOGGER.debug("No failed imports in the past 7days ");
        } else {
            QueryParams queryParams = new QueryParams(new Order("importDate", Order.Direction.ASC));
            List<MctsImportFailRecord> failedImports = mctsImportFailRecordDataService.getByStateAndImportdateAndUsertype(stateId, startReferenceDate, mctsUserType, queryParams);
            int counter = 0;
            for (MctsImportFailRecord eachFailedImport: failedImports) {
                mctsImportFailRecordDataService.delete(eachFailedImport);
                counter++;
            }
            LOGGER.debug("Deleted {} rows from nms_mcts_failures", counter);
        }
    }

    private Map<String, Object> toMap(ChildRecord childRecord) {
        Map<String, Object> map = new HashMap<>();

        map.put(KilkariConstants.STATE_ID, childRecord.getStateID());
        map.put(KilkariConstants.DISTRICT_ID, childRecord.getDistrictId());
        map.put(KilkariConstants.DISTRICT_NAME, childRecord.getDistrictName());
        map.put(KilkariConstants.TALUKA_ID, childRecord.getTalukaId());
        map.put(KilkariConstants.TALUKA_NAME, childRecord.getTalukaName());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, childRecord.getHealthBlockId());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, childRecord.getHealthBlockName());
        map.put(KilkariConstants.PHC_ID, childRecord.getPhcId());
        map.put(KilkariConstants.PHC_NAME, childRecord.getPhcName());
        map.put(KilkariConstants.SUB_CENTRE_ID, childRecord.getSubCentreId());
        map.put(KilkariConstants.SUB_CENTRE_NAME, childRecord.getSubCentreName());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, childRecord.getVillageId());
        map.put(KilkariConstants.VILLAGE_NAME, childRecord.getVillageName());
        map.put(KilkariConstants.LAST_UPDATE_DATE, "".equals(childRecord.getLastUpdateDate()) ? null : LocalDate.parse(childRecord.getLastUpdateDate(), DateTimeFormat.forPattern("dd-MM-yyyy")));

        map.put(KilkariConstants.BENEFICIARY_NAME, childRecord.getName());

        map.put(KilkariConstants.MSISDN, mctsBeneficiaryValueProcessor.getMsisdnByString(childRecord.getWhomPhoneNo()));
        map.put(KilkariConstants.DOB, mctsBeneficiaryValueProcessor.getDateByString(childRecord.getBirthdate()));

        map.put(KilkariConstants.BENEFICIARY_ID,
                mctsBeneficiaryValueProcessor.getChildInstanceByString(childRecord.getIdNo()));
        map.put(KilkariConstants.MOTHER_ID,
                mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(childRecord.getMotherId()));
        map.put(KilkariConstants.DEATH,
                mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(childRecord.getEntryType())));

        return map;
    }

    private Map<String, Object> toMap(MotherRecord motherRecord) {
        Map<String, Object> map = new HashMap<>();
        map.put(KilkariConstants.STATE_ID, motherRecord.getStateId());
        map.put(KilkariConstants.DISTRICT_ID, motherRecord.getDistrictId());
        map.put(KilkariConstants.DISTRICT_NAME, motherRecord.getDistrictName());
        map.put(KilkariConstants.TALUKA_ID, motherRecord.getTalukaId());
        map.put(KilkariConstants.TALUKA_NAME, motherRecord.getTalukaName());
        map.put(KilkariConstants.HEALTH_BLOCK_ID, motherRecord.getHealthBlockId());
        map.put(KilkariConstants.HEALTH_BLOCK_NAME, motherRecord.getHealthBlockName());
        map.put(KilkariConstants.PHC_ID, motherRecord.getPhcid());
        map.put(KilkariConstants.PHC_NAME, motherRecord.getPhcName());
        map.put(KilkariConstants.SUB_CENTRE_ID, motherRecord.getSubCentreid());
        map.put(KilkariConstants.SUB_CENTRE_NAME, motherRecord.getSubCentreName());
        map.put(KilkariConstants.CENSUS_VILLAGE_ID, motherRecord.getVillageId());
        map.put(KilkariConstants.VILLAGE_NAME, motherRecord.getVillageName());
        map.put(KilkariConstants.LAST_UPDATE_DATE, "".equals(motherRecord.getLastUpdateDate()) ? null : LocalDate.parse(motherRecord.getLastUpdateDate(), DateTimeFormat.forPattern("dd-MM-yyyy")));

        map.put(KilkariConstants.BENEFICIARY_ID, mctsBeneficiaryValueProcessor.getOrCreateMotherInstance(motherRecord.getIdNo()));
        map.put(KilkariConstants.BENEFICIARY_NAME, motherRecord.getName());
        map.put(KilkariConstants.MSISDN, mctsBeneficiaryValueProcessor.getMsisdnByString(motherRecord.getWhomPhoneNo()));
        map.put(KilkariConstants.LMP, mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getLmpDate()));
        map.put(KilkariConstants.MOTHER_DOB, mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getBirthdate()));
        map.put(KilkariConstants.ABORTION, mctsBeneficiaryValueProcessor.getAbortionDataFromString(motherRecord.getAbortion()));
        map.put(KilkariConstants.STILLBIRTH, mctsBeneficiaryValueProcessor.getStillBirthFromString(String.valueOf(motherRecord.getOutcomeNos())));
        map.put(KilkariConstants.DEATH, mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(motherRecord.getEntryType())));

        return map;
    }

    private int sizeNullSafe(Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Helper to check if the user exists in HPD filter state
     * @param hpdFilters set of districts group by state
     * @param stateId stateId of user
     * @param districtId districtId of user
     * @return true, if the user exists in the hpd district filter or if state is not HPD filtered
     */
    private boolean validateHpdUser(Map<Long, Set<Long>> hpdFilters, long stateId, long districtId) {

        // if we have the state for hpd filter
        if (hpdFilters.containsKey(stateId)) {
            // if district exists in the hpd filter set
            Set<Long> districtSet = hpdFilters.get(stateId);
            if (districtSet != null) {
                return districtSet.contains(districtId);
            }
        }

        return true;
    }

    private Map<Long, Set<Long>> getHpdFilters() {
        Map<Long, Set<Long>> hpdMap = new HashMap<>();
        String locationProp = settingsFacade.getProperty(Constants.HPD_STATES);
        if (StringUtils.isBlank(locationProp)) {
            return hpdMap;
        }

        String[] locationParts = StringUtils.split(locationProp, ',');
        for (String locationPart : locationParts) {
            Long stateId = Long.valueOf(locationPart);
            hpdMap.put(stateId, getHpdForState(stateId));
        }

        return hpdMap;
    }

    private Set<Long> getHpdForState(Long stateId) {

        Set<Long> districtSet = new HashSet<>();
        String hpdProp = settingsFacade.getProperty(Constants.BASE_HPD_CONFIG + stateId);
        if (StringUtils.isBlank(hpdProp)) {
            return districtSet;
        }

        String[] districtParts = StringUtils.split(hpdProp, ',');
        for (String districtPart : districtParts) {
            districtSet.add(Long.valueOf(districtPart));
        }

        return districtSet;
    }
}

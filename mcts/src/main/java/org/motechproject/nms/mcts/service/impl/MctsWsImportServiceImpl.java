package org.motechproject.nms.mcts.service.impl;

import org.apache.commons.lang.time.StopWatch;
import org.joda.time.LocalDate;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.nms.flw.exception.FlwImportException;
import org.motechproject.nms.flw.service.FrontLineWorkerImportService;
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
import org.motechproject.nms.mcts.domain.MctsUserType;
import org.motechproject.nms.mcts.exception.MctsInvalidResponseStructureException;
import org.motechproject.nms.mcts.exception.MctsWebServiceException;
import org.motechproject.nms.mcts.repository.MctsImportAuditDataService;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.mcts.service.MctsWsImportService;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Event relay service to handle async notifications
     */
    @Autowired
    private EventRelay eventRelay;

    @Override
    public void importFromMcts(List<Long> stateIds, LocalDate referenceDate, URL endpoint) {
        StopWatch stopWatch = new StopWatch();

        LOGGER.info("Starting import from MCTS web service");
        stopWatch.start();

        LOGGER.info("Pulling data for {}, for states {}", referenceDate, stateIds);
        if (endpoint == null) {
            LOGGER.debug("Using default service endpoint from WSDL");
        } else {
            LOGGER.debug("Using custom endpoint {}", endpoint);
        }

        int savedMothers = importMothersData(endpoint, stateIds, referenceDate);
        int savedChildren = importChildrenData(endpoint, stateIds, referenceDate);
        int savedAnmAsha = importAnmAshaData(endpoint, stateIds, referenceDate);

        stopWatch.stop();

        double seconds = stopWatch.getTime() / THOUSAND;
        LOGGER.info("Initiated import from MCTS in {} seconds. Received {} mothers, {} children and {} front line workers.",
                seconds, savedMothers, savedChildren, savedAnmAsha);
    }

    private int importChildrenData(URL endpoint, List<Long> locations, LocalDate referenceDate) {
        LOGGER.info("Starting children import");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int totalChildren = 0;
        for (Long stateId : locations) {
            State state = stateDataService.findByCode(stateId);
            if (state == null) {
                String error = String.format("State with code %s doesn't exist in database. Skipping Children import for this state", stateId);
                LOGGER.error(error);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.CHILD, stateId, null, 0, 0, error));
                continue;
            }
            String stateName = state.getName();
            Long stateCode = state.getCode();
            try {

                ChildrenDataSet childrenDataSet = mctsWebServiceFacade.getChildrenData(referenceDate, referenceDate, endpoint, stateId);
                if (childrenDataSet == null || childrenDataSet.getRecords() == null) {
                    String error = "No child data set received from MCTS";
                    LOGGER.debug(error);
                    mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.CHILD, stateCode, stateName, 0, 0, error));
                    continue;
                }

                Map<String, Object> eventParams = new HashMap<>();
                eventParams.put("childFeed", childrenDataSet);
                eventParams.put(Constants.DATE_PARAM, referenceDate);
                eventParams.put(Constants.STATE_NAME_PARAM, stateName);
                eventParams.put(Constants.STATE_CODE_PARAM, stateCode);
                eventRelay.sendEventMessage(new MotechEvent(Constants.MCTS_CHILD_IMPORT_SUBJECT, eventParams));

                totalChildren += sizeNullSafe(childrenDataSet.getRecords());
                LOGGER.debug("Dispatched children data set with {} records for {} state",
                        sizeNullSafe(childrenDataSet.getRecords()), state.getName());

            } catch (MctsWebServiceException e) {
                String error = String.format("Cannot read children data from %s State with stateId:%d", stateName, stateCode);
                LOGGER.error(error, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Child Import", e.getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.CHILD, stateCode, stateName, 0, 0, error));
            } catch (MctsInvalidResponseStructureException e) {
                String error = String.format("Cannot read children data from %s state with stateId:%d. Response Deserialization Error", stateName, stateCode);
                LOGGER.error(error, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Child Import", e.getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.CHILD, stateCode, stateName, 0, 0, error));
            }
        }

        stopWatch.stop();

        double seconds = stopWatch.getTime() / THOUSAND;
        LOGGER.info("Finished children import dispatch in {} seconds. Received {} childred", seconds, totalChildren);
        return totalChildren;
    }

    @MotechListener(subjects = { Constants.MCTS_CHILD_IMPORT_SUBJECT })
    public void saveImportedChildrenData(MotechEvent motechEvent) {
        String stateName = (String) motechEvent.getParameters().get(Constants.STATE_NAME_PARAM);
        Long stateCode = (Long) motechEvent.getParameters().get(Constants.STATE_CODE_PARAM);
        LOGGER.info("Starting children import for state {}", stateName);

        ChildrenDataSet childrenDataSet = (ChildrenDataSet) motechEvent.getParameters().get("childFeed");
        LocalDate referenceDate = (LocalDate) motechEvent.getParameters().get(Constants.DATE_PARAM);
        int saved = 0;
        int rejected = 0;

        for (ChildRecord record : childrenDataSet.getRecords()) {
            try {
                if (mctsBeneficiaryImportService.importChildRecord(toMap(record))) {
                    saved++;
                } else {
                    rejected++;
                }

                if ((saved + rejected) % THOUSAND == 0) {
                    LOGGER.debug("{} state, Progress: {} children imported, {} children rejected", stateName, saved, rejected);
                }
            } catch (RuntimeException e) {
                LOGGER.error("Child import Error. Cannot import Child with ID: {} for state:{} with state ID: {}",
                        record.getIdNo(), stateName, stateCode, e);
            }
        }
        LOGGER.debug("{} state, Total: {} children imported, {} children rejected", stateName, saved, rejected);
        mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.CHILD, stateCode, stateName, saved, rejected, null));
    }

    private int importMothersData(URL endpoint, List<Long> locations, LocalDate referenceDate) {
        LOGGER.info("Starting mother import");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int totalMothers = 0;
        for (Long stateId : locations) {
            State state = stateDataService.findByCode(stateId);
            if (state == null) {
                String error = String.format("State with code %s doesn't exist in database. Skipping Mother importing for this state", stateId);
                LOGGER.error(error);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.MOTHER, stateId, null, 0, 0, error));
                continue;
            }
            String stateName = state.getName();
            Long stateCode = state.getCode();
            try {
                MothersDataSet mothersDataSet = mctsWebServiceFacade.getMothersData(referenceDate, referenceDate, endpoint, stateId);
                if (mothersDataSet == null || mothersDataSet.getRecords() == null) {
                    String error = String.format("No mother data set received from MCTS for %s state", stateName);
                    LOGGER.debug(error);
                    mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.MOTHER, stateCode, stateName, 0, 0, error));
                    continue;
                }

                Map<String, Object> eventParams = new HashMap<>();
                eventParams.put("motherFeed", mothersDataSet);
                eventParams.put(Constants.DATE_PARAM, referenceDate);
                eventParams.put(Constants.STATE_NAME_PARAM, stateName);
                eventParams.put(Constants.STATE_CODE_PARAM, stateCode);
                eventRelay.sendEventMessage(new MotechEvent(Constants.MCTS_MOTHER_IMPORT_SUBJECT, eventParams));

                totalMothers += sizeNullSafe(mothersDataSet.getRecords());
                LOGGER.debug("Dispatched Mothers data set with {} records for {} state",
                        sizeNullSafe(mothersDataSet.getRecords()), state.getName());

            } catch (MctsWebServiceException e) {
                String error = String.format("Cannot read mothers data from %s state with stateId: %d", stateName, stateId);
                LOGGER.error(error, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Mother Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.MOTHER, stateCode, stateName, 0, 0, error));
            } catch (MctsInvalidResponseStructureException e) {
                String error = String.format("Cannot read mothers data from %s state with stateId: %d. Response Deserialization Error", stateName, stateId);
                LOGGER.error(error, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Mother Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.MOTHER, stateCode, stateName, 0, 0, error));
            }
        }

        stopWatch.stop();
        double seconds = stopWatch.getTime() / THOUSAND;
        LOGGER.info("Finished mother import dispatch in {} seconds. Received {} mothers.", seconds, totalMothers);
        return totalMothers;
    }

    @MotechListener(subjects = { Constants.MCTS_MOTHER_IMPORT_SUBJECT })
    public void saveImportedMothersData(MotechEvent motechEvent) {
        String stateName = (String) motechEvent.getParameters().get(Constants.STATE_NAME_PARAM);
        Long stateCode = (Long) motechEvent.getParameters().get(Constants.STATE_CODE_PARAM);
        LOGGER.info("Starting mother import for state {}", stateName);

        MothersDataSet mothersDataSet = (MothersDataSet) motechEvent.getParameters().get("motherFeed");
        LocalDate referenceDate = (LocalDate) motechEvent.getParameters().get(Constants.DATE_PARAM);
        int saved = 0;
        int rejected = 0;

        for (MotherRecord record : mothersDataSet.getRecords()) {
            try {
                if (mctsBeneficiaryImportService.importMotherRecord(toMap(record))) {
                    saved++;
                } else {
                    rejected++;
                }
                if ((saved + rejected) % THOUSAND == 0) {
                    LOGGER.debug("{} state, Progress: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
                }
            } catch (RuntimeException e) {
                LOGGER.error("Mother import Error. Cannot import Mother with ID: {} for state ID: {}",
                        record.getIdNo(), stateCode, e);
            }
        }
        LOGGER.debug("{} state, Total: {} mothers imported, {} mothers rejected", stateName, saved, rejected);
        mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.MOTHER, stateCode, stateName, saved, rejected, null));
    }

    private int importAnmAshaData(URL endpoint, List<Long> locations, LocalDate referenceDate) {
        LOGGER.info("Starting Anm Asha import");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int totalAsha = 0;
        for (Long stateId : locations) {
            State state = stateDataService.findByCode(stateId);
            if (state == null) {
                LOGGER.warn("State with code {} doesn't exist in database. Skipping FLW import for this state", stateId);
                continue;
            }
            String stateName = state.getName();
            Long stateCode = state.getCode();

            try {
                AnmAshaDataSet anmAshaDataSet = mctsWebServiceFacade.getAnmAshaData(referenceDate, referenceDate, endpoint, stateId);
                if (anmAshaDataSet == null || anmAshaDataSet.getRecords() == null) {
                    String error = String.format("No ANM Asha data set received from MCTS for %s state", stateName);
                    LOGGER.debug(error, stateName);
                    mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.ASHA, stateCode, stateName, 0, 0, error));
                    continue;
                }

                Map<String, Object> eventParams = new HashMap<>();
                eventParams.put("ashaFeed", anmAshaDataSet);
                eventParams.put(Constants.DATE_PARAM, referenceDate);
                eventParams.put(Constants.STATE_PARAM, state);
                eventParams.put(Constants.STATE_NAME_PARAM, stateName);
                eventParams.put(Constants.STATE_CODE_PARAM, stateCode);
                eventRelay.sendEventMessage(new MotechEvent(Constants.MCTS_ASHA_IMPORT_SUBJECT, eventParams));

                totalAsha += sizeNullSafe(anmAshaDataSet.getRecords());
                LOGGER.debug("Dispatched Anm Asha data set with {} records for {} state",
                        sizeNullSafe(anmAshaDataSet.getRecords()), state.getName());

            } catch (MctsWebServiceException e) {
                String error = String.format("Cannot read anm asha data from %s state with stateId:%d", stateName, stateId);
                LOGGER.error(error, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service FLW Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.ASHA, stateCode, stateName, 0, 0, error));
            } catch (MctsInvalidResponseStructureException e) {
                String error = String.format("Cannot read anm asha data from %s state with stateId: %d. Response Deserialization Error", stateName, stateCode);
                LOGGER.error(error, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service FLW Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
                mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.ASHA, stateCode, stateName, 0, 0, error));
            }
        }

        stopWatch.stop();
        double seconds = stopWatch.getTime() / THOUSAND;
        LOGGER.info("Finished Anm Asha import dispatch in {} seconds. Received {} front line workers.", seconds, totalAsha);
        return totalAsha;
    }

    @MotechListener(subjects = { Constants.MCTS_ASHA_IMPORT_SUBJECT })
    public void saveImportedAnmAshaData(MotechEvent motechEvent) {
        State state = (State) motechEvent.getParameters().get(Constants.STATE_PARAM);
        String stateName = (String) motechEvent.getParameters().get(Constants.STATE_NAME_PARAM);
        Long stateCode = (Long) motechEvent.getParameters().get(Constants.STATE_CODE_PARAM);
        LOGGER.info("Starting ASHA import for state {}", stateName);

        AnmAshaDataSet anmAshaDataSet = (AnmAshaDataSet) motechEvent.getParameters().get("ashaFeed");
        LocalDate referenceDate = (LocalDate) motechEvent.getParameters().get(Constants.DATE_PARAM);
        int saved = 0;
        int rejected = 0;

        for (AnmAshaRecord record : anmAshaDataSet.getRecords()) {
            try {
                frontLineWorkerImportService.importFrontLineWorker(record.toFlwRecordMap(), state);
                saved++;
            } catch (InvalidLocationException e) {
                LOGGER.warn("Invalid location for FLW: ", e);
                rejected++;
            } catch (FlwImportException e) {
                LOGGER.error("Existing FLW with same MSISDN but different MCTS ID", e);
                rejected++;
            } catch (Exception e) {
                LOGGER.error("Flw import Error. Cannot import FLW with ID: {}, and MSISDN (Contact_No): {}",
                        record.getId(), record.getContactNo(), e);
                rejected++;
            }
            if ((saved + rejected) % THOUSAND == 0) {
                LOGGER.debug("{} state, Progress: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
            }
        }
        LOGGER.debug("{} state, Total: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
        mctsImportAuditDataService.create(new MctsImportAudit(referenceDate, MctsUserType.ASHA, stateCode, stateName, saved, rejected, null));
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
}

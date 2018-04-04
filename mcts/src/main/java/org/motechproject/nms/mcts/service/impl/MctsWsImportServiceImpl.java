package org.motechproject.nms.mcts.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.DateTime;
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
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.exception.FlwExistingRecordException;
import org.motechproject.nms.flw.exception.FlwImportException;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.utils.FlwConstants;
import org.motechproject.nms.flwUpdate.service.FrontLineWorkerImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.kilkari.contract.AnmAshaRecord;
import org.motechproject.nms.kilkari.contract.ChildRecord;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.kilkari.contract.MotherRecord;
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
import org.motechproject.nms.kilkari.service.ActionFinderService;
import org.motechproject.nms.rejectionhandler.service.ChildRejectionService;
import org.motechproject.nms.rejectionhandler.service.FlwRejectionService;
import org.motechproject.nms.rejectionhandler.service.MotherRejectionService;
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
import java.util.ArrayList;

import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanMotherRecords;
import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanChildRecords;
import static org.motechproject.nms.kilkari.utils.ObjectListCleaner.cleanFlwRecords;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.childRejectionMcts;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToChild;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToMother;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.motherRejectionMcts;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.flwRejectionMcts;



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

    @Autowired
    private FlwRejectionService flwRejectionService;

    @Autowired
    private MotherRejectionService motherRejectionService;

    @Autowired
    private ChildRejectionService childRejectionService;

    @Autowired
    private ActionFinderService actionFinderService;

    @Autowired
    private FrontLineWorkerService frontLineWorkerService;

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

    @MotechListener(subjects = {Constants.MCTS_MOTHER_IMPORT_SUBJECT })
    @Transactional (noRollbackFor = Exception.class)
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

    public MctsImportAudit saveImportedMothersData(MothersDataSet mothersDataSet, String stateName, Long stateCode, LocalDate startReferenceDate, LocalDate endReferenceDate) {
        LOGGER.info("Starting mother import for state {}", stateName);
        List<MotherRecord> motherRecords = mothersDataSet.getRecords();
        List<MotherRecord> validMotherRecords = new ArrayList<>();
        validMotherRecords = getLMPValidRecords(motherRecords);
        List<List<MotherRecord>> motherRecordsSet = cleanMotherRecords(validMotherRecords);
        List<MotherRecord> rejectedMotherRecords = motherRecordsSet.get(0);
        String action = "";
        int saved = 0;
        int rejected = motherRecords.size() - validMotherRecords.size();
        for (MotherRecord record : rejectedMotherRecords) {
            action = actionFinderService.motherActionFinder(record);
            LOGGER.error("Existing Mother Record with same MSISDN in the data set");
            motherRejectionService.createOrUpdateMother(motherRejectionMcts(record, false, RejectionReasons.DUPLICATE_MOBILE_NUMBER_IN_DATASET.toString(), action));
            rejected++;

        }


        List<MotherRecord> acceptedMotherRecords = motherRecordsSet.get(1);

        Map<Long, Set<Long>> hpdMap = getHpdFilters();
        for (MotherRecord record : acceptedMotherRecords) {
            action = actionFinderService.motherActionFinder(record);
            try {
                // get user property map
                Map<String, Object> recordMap = toMap(record);

                // validate if user needs to be hpd filtered (true if user can be added)
                boolean hpdValidation = validateHpdUser(hpdMap,
                        (long) recordMap.get(KilkariConstants.STATE_ID),
                        (long) recordMap.get(KilkariConstants.DISTRICT_ID));
                if (hpdValidation && mctsBeneficiaryImportService.importMotherRecord(recordMap, SubscriptionOrigin.MCTS_IMPORT)) {
                    saved++;
                    LOGGER.info("saved mother {}", record.getIdNo());
                } else {
                    rejected++;
                    LOGGER.info("rejected mother {}", record.getIdNo());
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
        LOGGER.info("rejected mothers count {}", rejected);
        return new MctsImportAudit(startReferenceDate, endReferenceDate, MctsUserType.MOTHER, stateCode, stateName, saved, rejected, null);
    }

    private  List<MotherRecord> getLMPValidRecords(List<MotherRecord> motherRecords) {
        List<MotherRecord> validMotherRecords = new ArrayList<>();
        for (MotherRecord record : motherRecords) {
            Map<String, Object> recordMap = toMap(record);
            MctsMother mother;
            Long msisdn;
            String beneficiaryId;
            String action = "";
            action = actionFinderService.motherActionFinder(convertMapToMother(recordMap));
            beneficiaryId = (String) recordMap.get(KilkariConstants.BENEFICIARY_ID);
            mother = mctsBeneficiaryValueProcessor.getOrCreateMotherInstance(beneficiaryId);
            msisdn = (Long) recordMap.get(KilkariConstants.MSISDN);
            DateTime lmp = (DateTime) recordMap.get(KilkariConstants.LMP);
            if(mother == null) {
                motherRejectionService.createOrUpdateMother(motherRejectionMcts(convertMapToMother(recordMap), false, RejectionReasons.DATA_INTEGRITY_ERROR.toString(), action));
            } else {
                boolean isValidLMP =  (mother.getId() == null || (mother.getId() != null && mother.getLastMenstrualPeriod() == null)) && !mctsBeneficiaryImportService.validateReferenceDate(lmp, SubscriptionPackType.PREGNANCY, msisdn, beneficiaryId, SubscriptionOrigin.MCTS_IMPORT);
                        if (isValidLMP) {
                        motherRejectionService.createOrUpdateMother(motherRejectionMcts(convertMapToMother(recordMap), false, RejectionReasons.INVALID_LMP_DATE.toString(), action));
                        } else {
                             validMotherRecords.add(record);
                        }
                    }
        }
        return validMotherRecords;
    }
    @MotechListener(subjects = {Constants.MCTS_CHILD_IMPORT_SUBJECT })
    @Transactional (noRollbackFor = Exception.class)
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
        List<ChildRecord> childRecords = childrenDataSet.getRecords();
        List<ChildRecord> validChildRecords = new ArrayList<>();
        validChildRecords = getDOBValidChildRecords(childRecords);
        List<List<ChildRecord>> childRecordsSet = cleanChildRecords(validChildRecords);
        List<ChildRecord> rejectedChildRecords = childRecordsSet.get(0);
        String action = "";
        int saved = 0;
        int rejected = childRecords.size() - validChildRecords.size();
        for (ChildRecord record : rejectedChildRecords) {
            action = actionFinderService.childActionFinder(record);
            LOGGER.error("Existing Child Record with same MSISDN in the data set");
            childRejectionService.createOrUpdateChild(childRejectionMcts(record, false, RejectionReasons.DUPLICATE_MOBILE_NUMBER_IN_DATASET.toString(), action));
            rejected++;
        }
        List<ChildRecord> acceptedChildRecords = childRecordsSet.get(1);

        Map<Long, Set<Long>> hpdMap = getHpdFilters();

        for (ChildRecord record : acceptedChildRecords) {
            action = actionFinderService.childActionFinder(record);
            try {
                // get user property map
                Map<String, Object> recordMap = toMap(record);

                // validate if user needs to be hpd filtered (true if user can be added)
                boolean hpdValidation = validateHpdUser(hpdMap,
                        (long) recordMap.get(KilkariConstants.STATE_ID),
                        (long) recordMap.get(KilkariConstants.DISTRICT_ID));

                if (hpdValidation) { // && mctsBeneficiaryImportService.importChildRecord(toMap(record), SubscriptionOrigin.MCTS_IMPORT)) {
                    saved++;
                    LOGGER.info("saved child {}", record.getIdNo());
                } else {
                    rejected++;
                    LOGGER.info("rejected child {}", record.getIdNo());
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

    private  List<ChildRecord> getDOBValidChildRecords(List<ChildRecord> childRecords) {
        List<ChildRecord> validChildRecords = new ArrayList<>();
        for (ChildRecord record : childRecords) {
            Map<String, Object> recordMap = toMap(record);
            MctsChild child;
            Long msisdn;
            String childId;
            String action = "";
            action = actionFinderService.childActionFinder(convertMapToChild(recordMap));
            childId = (String) recordMap.get(KilkariConstants.BENEFICIARY_ID);
            child = mctsBeneficiaryValueProcessor.getOrCreateChildInstance(childId);
            msisdn = (Long) recordMap.get(KilkariConstants.MSISDN);
            DateTime dob = (DateTime) recordMap.get(KilkariConstants.DOB);
            LOGGER.info("Starting children import for size {}", !mctsBeneficiaryImportService.validateReferenceDate(dob, SubscriptionPackType.CHILD, msisdn, childId, SubscriptionOrigin.MCTS_IMPORT));
            if (child == null) {
                childRejectionService.createOrUpdateChild(childRejectionMcts(convertMapToChild(recordMap), false, RejectionReasons.DATA_INTEGRITY_ERROR.toString(), action));
            } else {
                boolean isValidDOB = child.getId() == null && !mctsBeneficiaryImportService.validateReferenceDate(dob, SubscriptionPackType.CHILD, msisdn, childId, SubscriptionOrigin.MCTS_IMPORT);
                if (isValidDOB) {
                    childRejectionService.createOrUpdateChild(childRejectionMcts(convertMapToChild(recordMap), false, RejectionReasons.INVALID_DOB.toString(), action));
                } else {
                    validChildRecords.add(record);
                }
            }
        }
        return validChildRecords;
    }

    @MotechListener(subjects = {Constants.MCTS_ASHA_IMPORT_SUBJECT })
    @Transactional (noRollbackFor = Exception.class)
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

    private MctsImportAudit saveImportedAnmAshaData(AnmAshaDataSet anmAshaDataSet, State state, LocalDate startReferenceDate, LocalDate endReferenceDate) { //NOPMD NcssMethodCount // NO CHECKSTYLE Cyclomatic Complexity
        String stateName = state.getName();
        Long stateCode = state.getCode();
        LOGGER.info("Starting ASHA import for state {}", stateName);
        List<List<AnmAshaRecord>> ashaRecordsSet = cleanFlwRecords(anmAshaDataSet.getRecords());
        List<AnmAshaRecord> rejectedAshaRecords = ashaRecordsSet.get(0);
        String action = "";
        int saved = 0;
        int rejected = 0;
        for (AnmAshaRecord record : rejectedAshaRecords) {
            record.setStateId(stateCode);
            action = this.flwActionFinder(record);
            LOGGER.error("Existing Asha Record with same MSISDN in the data set");
            flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.DUPLICATE_MOBILE_NUMBER_IN_DATASET.toString(), action));
            rejected++;
        }
        List<AnmAshaRecord> acceptedAshaRecords = ashaRecordsSet.get(1);


        for (AnmAshaRecord record : acceptedAshaRecords) {
            try {
                record.setStateId(stateCode);
                action = this.flwActionFinder(record);
                String designation = record.getType();
                designation = (designation != null) ? designation.trim() : designation;
                Long msisdn = Long.parseLong(record.getContactNo());
                String mctsFlwId = record.getId().toString();
                FrontLineWorker flw = frontLineWorkerService.getByContactNumber(msisdn);
                if (flw != null && ((!mctsFlwId.equals(flw.getMctsFlwId()) || state.getId() != flw.getState().getId())) && flw.getStatus() != FrontLineWorkerStatus.ANONYMOUS) {
                    LOGGER.error("Existing FLW with same MSISDN but different MCTS ID");
                    flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.MOBILE_NUMBER_ALREADY_IN_USE.toString(), action));
                    rejected++;
                } else {
                    if (!(FlwConstants.ASHA_TYPE.equalsIgnoreCase(designation))) {
                        flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.FLW_TYPE_NOT_ASHA.toString(), action));
                        rejected++;
                    } else {
                        try {
                            // get user property map
                            Map<String, Object> recordMap = record.toFlwRecordMap();    // temp var used for debugging
                            frontLineWorkerImportService.importMctsFrontLineWorker(recordMap, state);
                            flwRejectionService.createUpdate(flwRejectionMcts(record, true, null, action));
                            saved++;
                        } catch (InvalidLocationException e) {
                            LOGGER.warn("Invalid location for FLW: ", e);
                            flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.INVALID_LOCATION.toString(), action));
                            rejected++;
                        } catch (FlwImportException e) {
                            LOGGER.error("Flw import Error", e);
                            flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.FLW_IMPORT_ERROR.toString(), action));
                            rejected++;
                        } catch (FlwExistingRecordException e) {
                            LOGGER.error("Cannot import FLW with ID: {}, and MSISDN (Contact_No): {}", record.getId(), record.getContactNo(), e);
                            flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.UPDATED_RECORD_ALREADY_EXISTS.toString(), action));
                            rejected++;
                        } catch (Exception e) {
                            LOGGER.error("Flw import Error. Cannot import FLW with ID: {}, and MSISDN (Contact_No): {}",
                                    record.getId(), record.getContactNo(), e);
                            flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.FLW_IMPORT_ERROR.toString(), action));
                            rejected++;
                        }
                    }
                    if ((saved + rejected) % THOUSAND == 0) {
                        LOGGER.debug("{} state, Progress: {} Ashas imported, {} Ashas rejected", stateName, saved, rejected);
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.error("Mobile number either not present or is not in number format");
                flwRejectionService.createUpdate(flwRejectionMcts(record, false, RejectionReasons.MOBILE_NUMBER_EMPTY_OR_WRONG_FORMAT.toString(), action));
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
            for (MctsImportFailRecord eachFailedImport : failedImports) {
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
                childRecord.getIdNo());
        map.put(KilkariConstants.MOTHER_ID,
                mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(childRecord.getMotherId()) == null ? null : mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(childRecord.getMotherId()).getBeneficiaryId());
        map.put(KilkariConstants.DEATH,
                mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(childRecord.getEntryType())));

        map.put(KilkariConstants.GP_VILLAGE, childRecord.getGpVillage());
        map.put(KilkariConstants.ADDRESS, childRecord.getAddress());
        map.put("Yr", childRecord.getYr());
        map.put("City_Maholla", childRecord.getCityMaholla());
        map.put("Mother_Name", childRecord.getMotherName());
        map.put(KilkariConstants.PH_OF_WHOM, childRecord.getPhoneNoOfWhom());
        map.put("Place_of_Delivery", childRecord.getPlaceOfDelivery());
        map.put(KilkariConstants.ANM_PHONE, childRecord.getAnmPhone());
        map.put("Blood_Group", childRecord.getBloodGroup());
        map.put(KilkariConstants.ASHA_NAME, childRecord.getAshaName());
        map.put(KilkariConstants.ASHA_PHONE, childRecord.getAshaPhone());

        map = addToMap(map, childRecord);

        return map;
    }

    private Map<String, Object> addToMap(Map<String, Object> map, ChildRecord childRecord) {
        map.put(KilkariConstants.SUB_CENTRE_NAME1, childRecord.getSubCentreName1());
        map.put(KilkariConstants.ANM_NAME, childRecord.getAnmName());
        map.put(KilkariConstants.CASTE, childRecord.getCaste());
        map.put("BCG_Dt", childRecord.getBcgDt());
        map.put("OPV0_Dt", childRecord.getOpv0Dt());
        map.put("HepatitisB1_Dt", childRecord.getHepatitisB1Dt());
        map.put("DPT1_Dt", childRecord.getDpt1Dt());
        map.put("OPV1_Dt", childRecord.getOpv1Dt());
        map.put("HepatitisB2_Dt", childRecord.getHepatitisB2Dt());
        map.put("DPT2_Dt", childRecord.getdPT2Dt());
        map.put("OPV2_Dt", childRecord.getOpv2Dt());
        map.put("HepatitisB3_Dt", childRecord.getHepatitisB3Dt());
        map.put("DPT3_Dt", childRecord.getDpt3Dt());
        map.put("OPV3_Dt", childRecord.getOpv3Dt());
        map.put("HepatitisB4_Dt", childRecord.getHepatitisB4Dt());
        map.put("Measles_Dt", childRecord.getMeaslesDt());
        map.put("VitA_Dose1_Dt", childRecord.getVitADose1Dt());
        map.put("MR_Dt", childRecord.getMrDt());
        map.put("DPTBooster_Dt", childRecord.getDptBoosterDt());
        map.put("OPVBooster_Dt", childRecord.getOpvBoosterDt());
        map.put("VitA_Dose2_Dt", childRecord.getVitADose2Dt());
        map.put("VitA_Dose3_Dt", childRecord.getVitADose3Dt());
        map.put("JE_Dt", childRecord.getJeDt());
        map.put("VitA_Dose9_Dt", childRecord.getVitADose9Dt());
        map.put("DT5_Dt", childRecord.getDt5Dt());
        map.put("TT10_Dt", childRecord.getTt10Dt());
        map.put("TT16_Dt", childRecord.getTt16Dt());
        map.put("CLD_REG_DATE", childRecord.getCldRegDate());
        map.put("Sex", childRecord.getSex());
        map.put("VitA_Dose5_Dt", childRecord.getVitADose5Dt());
        map.put(KilkariConstants.REMARKS, childRecord.getRemarks());
        map.put("VitA_Dose6_Dt", childRecord.getVitADose6Dt());
        map.put(KilkariConstants.ANM_ID, childRecord.getAnmID());
        map.put(KilkariConstants.ASHA_ID, childRecord.getAshaID());
        map.put("VitA_Dose7_Dt", childRecord.getVitADose7Dt());
        map.put("VitA_Dose8_Dt", childRecord.getVitADose8Dt());
        map.put(KilkariConstants.CREATED_BY, childRecord.getCreatedBy());
        map.put(KilkariConstants.UPDATED_BY, childRecord.getUpdatedBy());
        map.put("Measles2_Dt", childRecord.getMeasles2Dt());
        map.put("Weight_of_Child", childRecord.getWeightofChild());
        map.put("Child_Aadhaar_No", childRecord.getChildAadhaarNo());
        map.put("Child_EID", childRecord.getChildEID());
        map.put("Child_EIDTime", childRecord.getChildEIDTime());
        map.put("Father_Name", childRecord.getFatherName());
        map.put("Birth_Certificate_Number", childRecord.getBirthCertificateNumber());

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

        map.put(KilkariConstants.BENEFICIARY_ID, motherRecord.getIdNo());
        map.put(KilkariConstants.BENEFICIARY_NAME, motherRecord.getName());
        map.put(KilkariConstants.MSISDN, mctsBeneficiaryValueProcessor.getMsisdnByString(motherRecord.getWhomPhoneNo()));
        map.put(KilkariConstants.LMP, mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getLmpDate()));
        map.put(KilkariConstants.MOTHER_DOB, "".equals(motherRecord.getBirthdate()) ? null : mctsBeneficiaryValueProcessor.getDateByString(motherRecord.getBirthdate()));
        map.put(KilkariConstants.ABORTION, mctsBeneficiaryValueProcessor.getAbortionDataFromString(motherRecord.getAbortion()));
        map.put(KilkariConstants.STILLBIRTH, mctsBeneficiaryValueProcessor.getStillBirthFromString(String.valueOf(motherRecord.getOutcomeNos())));
        map.put(KilkariConstants.DEATH, mctsBeneficiaryValueProcessor.getDeathFromString(String.valueOf(motherRecord.getEntryType())));

        map.put("Yr", motherRecord.getYr());
        map.put(KilkariConstants.GP_VILLAGE, motherRecord.getGpVillage());
        map.put(KilkariConstants.ADDRESS, motherRecord.getAddress());
        map.put("Husband_Name", motherRecord.getHusbandName());
        map.put(KilkariConstants.PH_OF_WHOM, motherRecord.getPhoneNoOfWhom());
        map.put("JSY_Beneficiary", motherRecord.getJsyBeneficiary());
        map.put(KilkariConstants.CASTE, motherRecord.getCaste());
        map.put(KilkariConstants.SUB_CENTRE_NAME1, motherRecord.getSubCentreName1());
        map.put(KilkariConstants.ANM_NAME, motherRecord.getAnmName());
        map.put(KilkariConstants.ANM_PHONE, motherRecord.getAnmPhone());
        map.put(KilkariConstants.ASHA_NAME, motherRecord.getAshaName());
        map.put(KilkariConstants.ASHA_PHONE, motherRecord.getAshaPhone());
        map.put("Delivery_Lnk_Facility", motherRecord.getDeliveryLnkFacility());
        map.put("Facility_Name", motherRecord.getFacilityName());
        map.put("ANC1_Date", motherRecord.getAnc1Date());
        map.put("ANC2_Date", motherRecord.getAnc2Date());
        map.put("ANC3_Date", motherRecord.getAnc3Date());
        map.put("ANC4_Date", motherRecord.getAnc4Date());
        map.put("TT1_Date", motherRecord.getTt1Date());
        map.put("TT2_Date", motherRecord.getTt2Date());
        map.put("TTBooster_Date", motherRecord.getTtBoosterDate());

        map = addToMap(map, motherRecord);

        return map;
    }

    private Map<String, Object> addToMap(Map<String, Object> map, MotherRecord motherRecord) {
        map.put("IFA100_Given_Date", motherRecord.getIfA100GivenDate());
        map.put("Anemia", motherRecord.getAnemia());
        map.put("ANC_Complication", motherRecord.getAncComplication());
        map.put("RTI_STI", motherRecord.getRtiSTI());
        map.put("Dly_Date", motherRecord.getDlyDate());
        map.put("Dly_Place_Home_Type", motherRecord.getDlyPlaceHomeType());
        map.put("Dly_Place_Public", motherRecord.getDlyPlacePublic());
        map.put("Dly_Place_Private", motherRecord.getDlyPlacePrivate());
        map.put("Dly_Type", motherRecord.getDlyType());
        map.put("Dly_Complication", motherRecord.getDlyComplication());
        map.put("Discharge_Date", motherRecord.getDischargeDate());
        map.put("JSY_Paid_Date", motherRecord.getJsyPaidDate());
        map.put("PNC_Home_Visit", motherRecord.getPncHomeVisit());
        map.put("PNC_Complication", motherRecord.getPncComplication());
        map.put("PPC_Method", motherRecord.getPpcMethod());
        map.put("PNC_Checkup", motherRecord.getPncCheckup());
        map.put("Child1_Name", motherRecord.getChild1Name());
        map.put("Child1_Sex", motherRecord.getChild1Sex());
        map.put("Child1_Wt", motherRecord.getChild1Wt());
        map.put("Child1_Brestfeeding", motherRecord.getChild1Brestfeeding());
        map.put("Child2_Name", motherRecord.getChild2Name());
        map.put("Child2_Sex", motherRecord.getChild2Sex());
        map.put("Child2_Wt", motherRecord.getChild2Wt());
        map.put("Child2_Brestfeeding", motherRecord.getChild2Brestfeeding());
        map.put("Child3_Name", motherRecord.getChild3Sex());
        map.put("Child3_Sex", motherRecord.getChild3Sex());
        map.put("Child3_Wt", motherRecord.getChild3Wt());
        map.put("Child3_Brestfeeding", motherRecord.getChild3Brestfeeding());
        map.put("Child4_Name", motherRecord.getChild4Name());
        map.put("Child4_Sex", motherRecord.getChild4Sex());
        map.put("Child4_Wt", motherRecord.getChild4Sex());
        map.put("Child4_Brestfeeding", motherRecord.getChild4Brestfeeding());
        map.put("Age", motherRecord.getAge());
        map.put("MTHR_REG_DATE", motherRecord.getMthrRegDate());
        map.put(KilkariConstants.REMARKS, motherRecord.getRemarks());
        map.put(KilkariConstants.ANM_ID, motherRecord.getAnmID());
        map.put(KilkariConstants.ASHA_ID, motherRecord.getAshaID());
        map.put("Call_Ans", motherRecord.getCallAns());
        map.put("NoCall_Reason", motherRecord.getNoCallReason());
        map.put("NoPhone_Reason", motherRecord.getNoPhoneReason());
        map.put(KilkariConstants.CREATED_BY, motherRecord.getCreatedBy());
        map.put(KilkariConstants.UPDATED_BY, motherRecord.getUpdatedBy());
        map.put("Aadhar_No", motherRecord.getAadharNo());
        map.put("BPL_APL", motherRecord.getBplAPL());
        map.put("EID", motherRecord.geteId());
        map.put("EIDTime", motherRecord.geteIdTime());

        return map;
    }

    private int sizeNullSafe(Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * Helper to check if the user exists in HPD filter state
     *
     * @param hpdFilters set of districts group by state
     * @param stateId    stateId of user
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

    private String flwActionFinder(AnmAshaRecord record) {
        if (frontLineWorkerService.getByMctsFlwIdAndState(record.getId().toString(), stateDataService.findByCode(record.getStateId())) == null) {
            return "CREATE";
        } else {
            return "UPDATE";
        }
    }
}

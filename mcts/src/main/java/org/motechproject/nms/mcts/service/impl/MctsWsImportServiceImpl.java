package org.motechproject.nms.mcts.service.impl;

import org.apache.commons.lang.time.StopWatch;
import org.joda.time.LocalDate;
import org.motechproject.alerts.contract.AlertService;
import org.motechproject.alerts.domain.AlertStatus;
import org.motechproject.alerts.domain.AlertType;
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
import org.motechproject.nms.mcts.exception.MctsInvalidResponseStructureException;
import org.motechproject.nms.mcts.exception.MctsWebServiceException;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.mcts.service.MctsWsImportService;
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
    public static final String MCTS_WEB_SERVICE = "MCTS Web Service";

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

        double seconds = stopWatch.getTime() / 1000d;
        LOGGER.info("Finished import from MCTS in {} seconds. Imported {} mothers, {} children and {} front line workers.",
                seconds, savedMothers, savedChildren, savedAnmAsha);
    }

    private int importChildrenData(URL endpoint, List<Long> locations, LocalDate referenceDate) {
        LOGGER.info("Starting children import");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int saved = 0;
        for (Long stateId : locations) {
            try {
                State state = stateDataService.findByCode(stateId);
                if (state == null) {
                    LOGGER.warn("State with code {} doesn't exist in database. Skipping Children import for this state",
                            stateId);
                    continue;
                }

                ChildrenDataSet childrenDataSet = mctsWebServiceFacade.getChildrenData(referenceDate, referenceDate, endpoint, stateId);

                LOGGER.debug("Received children data set with {} records", sizeNullSafe(childrenDataSet.getRecords()));

                saved += saveImportedChildrenData(childrenDataSet, state);

            } catch (MctsWebServiceException e) {
                LOGGER.error("Cannot read children data from {} state.", stateId, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Child Import", e.getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            } catch (MctsInvalidResponseStructureException e) {
                LOGGER.error("Cannot read children data from {} state. Response Deserialization Error", stateId, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Child Import", e.getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            }
        }

        stopWatch.stop();

        double seconds = stopWatch.getTime() / 1000d;
        LOGGER.info("Finished children import {} seconds. Imported {} children.", seconds, saved);

        return saved;
    }

    private int saveImportedChildrenData(ChildrenDataSet childrenDataSet, State state) {
        int saved = 0;
        for (ChildRecord record : childrenDataSet.getRecords()) {
            try {
                boolean success = mctsBeneficiaryImportService.importChildRecord(toMap(record));
                if (success) {
                    saved++;
                }
            } catch (RuntimeException e) {
                LOGGER.error("Child import Error. Cannot import Child with ID: {} for state ID: {}",
                        record.getIdNo(), state.getCode(), e);
            }
        }
        return saved;
    }

    private int importMothersData(URL endpoint, List<Long> locations, LocalDate referenceDate) {
        LOGGER.info("Starting mother import");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int saved = 0;
        for (Long stateId : locations) {
            try {
                State state = stateDataService.findByCode(stateId);
                if (state == null) {
                    LOGGER.warn("State with code {} doesn't exist in database. Skipping Mother importing for this state", stateId);
                    continue;
                }

                MothersDataSet mothersDataSet = mctsWebServiceFacade.getMothersData(referenceDate, referenceDate, endpoint, stateId);

                LOGGER.debug("Received Mothers data set with {} records for {} state",
                        sizeNullSafe(mothersDataSet.getRecords()), state.getName());

                saved += saveImportedMothersData(mothersDataSet, state);

            } catch (MctsWebServiceException e) {
                LOGGER.error("Cannot read mothers data from {} state.", stateId, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Mother Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            } catch (MctsInvalidResponseStructureException e) {
                LOGGER.error("Cannot read mothers data from {} state. Response Deserialization Error", stateId, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service Mother Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            }
        }

        stopWatch.stop();

        double seconds = stopWatch.getTime() / 1000d;
        LOGGER.info("Finished mother import {} seconds. Imported {} mothers.", seconds, saved);

        return saved;
    }

    private int importAnmAshaData(URL endpoint, List<Long> locations, LocalDate referenceDate) {
        LOGGER.info("Starting Anm Asha import");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int saved = 0;
        for (Long stateId : locations) {
            try {
                State state = stateDataService.findByCode(stateId);
                if (state == null) {
                    LOGGER.warn("State with code {} doesn't exist in database. Skipping FLW import for this state", stateId);
                    continue;
                }
                AnmAshaDataSet anmAshaDataSet = mctsWebServiceFacade.getAnmAshaData(referenceDate, referenceDate, endpoint, stateId);

                LOGGER.debug("Received Anm Asha data set with {} records for {} state",
                        sizeNullSafe(anmAshaDataSet.getRecords()), state.getName());

                saved += saveImportedAnmAshaData(anmAshaDataSet, state);

            } catch (MctsWebServiceException e) {
                LOGGER.error("Cannot read anm asha data from {} state.", stateId, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service FLW Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            } catch (MctsInvalidResponseStructureException e) {
                LOGGER.error("Cannot read anm asha data from {} state. Response Deserialization Error", stateId, e);
                alertService.create(MCTS_WEB_SERVICE, "MCTS Web Service FLW Import", e
                        .getMessage(), AlertType.CRITICAL, AlertStatus.NEW, 0, null);
            }
        }

        stopWatch.stop();

        double seconds = stopWatch.getTime() / 1000d;
        LOGGER.info("Finished Anm Asha import {} seconds. Imported {} front line workers.", seconds, saved);

        return saved;
    }

    private int saveImportedAnmAshaData(AnmAshaDataSet anmAshaDataSet, State state) {
        int saved = 0;
        for (AnmAshaRecord record : anmAshaDataSet.getRecords()) {
            try {
                frontLineWorkerImportService.importFrontLineWorker(record.toFlwRecordMap(), state);
                saved++;
            } catch (InvalidLocationException e) {
                LOGGER.warn("Invalid location for FLW: ", e);
            } catch (FlwImportException e) {
                LOGGER.error("Existing FLW with same MSISDN but different MCTS ID", e);
            } catch (Exception e) {
                LOGGER.error("Flw import Error. Cannot import FLW with ID: {}, and MSISDN (Contact_No): {}",
                        record.getId(), record.getContactNo(), e);
            }
        }
        return saved;
    }

    private int saveImportedMothersData(MothersDataSet mothersDataSet, State state) {
        int saved = 0;
        for (MotherRecord record : mothersDataSet.getRecords()) {
            try {
                boolean success = mctsBeneficiaryImportService.importMotherRecord(toMap(record));
                if (success) {
                    saved++;
                }
            } catch (RuntimeException e) {
                LOGGER.error("Mother import Error. Cannot import Mother with ID: {} for state ID: {}",
                        record.getIdNo(), state.getCode(), e);
            }
        }
        return saved;
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

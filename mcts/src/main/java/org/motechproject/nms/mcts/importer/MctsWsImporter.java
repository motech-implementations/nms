package org.motechproject.nms.mcts.importer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
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
import org.motechproject.nms.mcts.exception.MctsImportConfigurationException;
import org.motechproject.nms.mcts.exception.MctsInvalidResponseStructureException;
import org.motechproject.nms.mcts.exception.MctsWebServiceExeption;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.mcts.utils.Constants;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
import org.motechproject.scheduler.contract.CronSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MctsWsImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsWsImporter.class);

    @Autowired
    private FrontLineWorkerImportService frontLineWorkerImportService;

    @Autowired
    private StateDataService stateDataService;

    @Autowired
    private MctsWebServiceFacade mctsWebServiceFacade;

    @Autowired
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;

    @Autowired
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    @Autowired
    @Qualifier("mctsSettings")
    private SettingsFacade settingsFacade;

    @PostConstruct
    public void initImportJob() {
        String cronExpression = settingsFacade.getProperty(Constants.MCTS_SYNC_START_TIME);
        if (StringUtils.isBlank(cronExpression) || !CronExpression.isValidExpression(cronExpression)) {
            LOGGER.error("Cron expression from setting is invalid");
            throw new MctsImportConfigurationException("Cron expression from setting is invalid");
        }

        CronSchedulableJob mctsImportJob = new CronSchedulableJob(new MotechEvent(Constants.MCTS_IMPORT_EVENT), cronExpression);
        motechSchedulerService.safeScheduleJob(mctsImportJob);
    }

    @MotechListener(subjects = Constants.MCTS_IMPORT_EVENT)
    public void handleImportEvent(MotechEvent event) {
        StopWatch stopWatch = new StopWatch();

        LOGGER.info("Starting import from MCTS web service");
        stopWatch.start();


        List<Long> stateIds = getStateIds();
        URL endpoint = getEndpointUrl();
        LocalDate referenceDate = DateUtil.today().minusDays(1);

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
        LOGGER.info("Finished import from MCTS in {} seconds. Import {} mothers, {} children and {} front line workers.",
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
                    LOGGER.warn("State with code {} doesn't exist in database. Skipping child import for this state",
                            stateId);
                    continue;
                }

                ChildrenDataSet childrenDataSet = mctsWebServiceFacade.getChildrenData(referenceDate, referenceDate, endpoint, stateId);

                LOGGER.debug("Received children data set with {} records", sizeNullSafe(childrenDataSet.getRecords()));

                saved += saveImportedChildrenData(childrenDataSet, state);

            } catch (MctsWebServiceExeption e) {
                LOGGER.error("Cannot read children data from {} state.", stateId, e);
            } catch (MctsInvalidResponseStructureException e) {
                LOGGER.error("Cannot read children data from {} state. Response Deserialization Error", stateId, e);
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
                mctsBeneficiaryImportService.importChildRecord(toMap(record));
                saved++;
            } catch (RuntimeException e) {
                LOGGER.error("Flw import Error. Cannot import Child with ID: {} for state ID: {}",
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

                LOGGER.debug("Received mother data set with {} records for {} state", sizeNullSafe(mothersDataSet.getRecords()),
                        state.getName());


            } catch (MctsWebServiceExeption e) {
                LOGGER.error("Cannot read mothers data from {} state.", stateId, e);
            } catch (MctsInvalidResponseStructureException e) {
                LOGGER.error("Cannot read mothers data from {} state. Response Deserialization Error", stateId, e);
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

            } catch (MctsWebServiceExeption e) {
                LOGGER.error("Cannot read anm asha data from {} state.", stateId, e);
            } catch (MctsInvalidResponseStructureException e) {
                LOGGER.error("Cannot read anm asha data from {} state. Response Deserialization Error", stateId, e);
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
                mctsBeneficiaryImportService.importMotherRecord(toMap(record));
                saved++;
            } catch (RuntimeException e) {
                LOGGER.error("Flw import Error. Cannot import Mother with ID: {} for state ID: {}",
                        record.getIdNo(), state.getCode(), e);
            }
        }
        return saved;
    }

    private Map<String, Object> toMap(ChildRecord childRecord) {
        Map<String, Object> map = new HashMap<>();

        map.put(KilkariConstants.STATE, childRecord.getStateID());
        map.put(KilkariConstants.DISTRICT, childRecord.getDistrictId());
        map.put(KilkariConstants.TALUKA, childRecord.getTalukaId());
        map.put(KilkariConstants.HEALTH_BLOCK, childRecord.getHealthBlockId());
        map.put(KilkariConstants.PHC, childRecord.getPhcId());
        map.put(KilkariConstants.SUBCENTRE, childRecord.getSubCentreId());
        map.put(KilkariConstants.CENSUS_VILLAGE, childRecord.getVillageId());

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
        map.put(KilkariConstants.STATE, motherRecord.getStateId());
        map.put(KilkariConstants.DISTRICT, motherRecord.getDistrictId());
        map.put(KilkariConstants.TALUKA, motherRecord.getTalukaId());
        map.put(KilkariConstants.HEALTH_BLOCK, motherRecord.getHealthBlockId());
        map.put(KilkariConstants.PHC, motherRecord.getPhcid());
        map.put(KilkariConstants.SUBCENTRE, motherRecord.getSubCentreid());
        map.put(KilkariConstants.CENSUS_VILLAGE, motherRecord.getVillageId());
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

    private List<Long> getStateIds() {
        String locationProp = settingsFacade.getProperty(Constants.MCTS_LOCATIONS);
        if (StringUtils.isBlank(locationProp)) {
            LOGGER.warn("No states configured for import");
            return Collections.emptyList();
        }

        String[] locationParts = StringUtils.split(locationProp, ',');

        List<Long> stateIds = new ArrayList<>();
        for (String locationPart : locationParts) {
            stateIds.add(Long.valueOf(locationPart));
        }

        return stateIds;
    }

    private URL getEndpointUrl() {
        String endpoint = settingsFacade.getProperty(Constants.MCTS_ENDPOINT);
        try {
            return StringUtils.isBlank(endpoint) ? null : new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new MctsImportConfigurationException("Malformed endpoint configured: " + endpoint, e);
        }
    }

    private int sizeNullSafe(Collection collection) {
        return collection == null ? 0 : collection.size();
    }
}

package org.motechproject.nms.flw.service.impl;

import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.mds.query.QueryExecution;
import org.motechproject.mds.util.InstanceSecurityRestriction;
import org.motechproject.nms.flw.domain.FlwError;
import org.motechproject.nms.flw.domain.FlwErrorReason;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.domain.FrontLineWorkerStatus;
import org.motechproject.nms.flw.repository.FlwErrorDataService;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.flw.service.FrontLineWorkerService;
import org.motechproject.nms.flw.utils.FlwConstants;
import org.motechproject.nms.flw.utils.FlwMapper;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.service.LanguageService;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.scheduler.contract.RepeatingSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jdo.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of the {@link org.motechproject.nms.flw.service.FrontLineWorkerService} interface.
 */
@Service("frontLineWorkerService")
public class FrontLineWorkerServiceImpl implements FrontLineWorkerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontLineWorkerServiceImpl.class);

    private static final String FLW_PURGE_TIME = "flw.purge_invalid_flw_start_time";
    private static final String FLW_PURGE_SEC_INTERVAL = "flw.purge_invalid_flw_sec_interval";
    private static final String WEEKS_TO_KEEP_INVALID_FLWS = "flw.weeks_to_keep_invalid_flws";

    private static final String FLW_PURGE_EVENT_SUBJECT = "nms.flw.purge_invalid_flw";

    private FrontLineWorkerDataService frontLineWorkerDataService;

    private SettingsFacade settingsFacade;
    private MotechSchedulerService schedulerService;
    private LanguageService languageService;
    private LocationService locationService;
    private FlwErrorDataService flwErrorDataService;

    @Autowired
    public FrontLineWorkerServiceImpl(@Qualifier("flwSettings") SettingsFacade settingsFacade,
                                      MotechSchedulerService schedulerService,
                                      FrontLineWorkerDataService frontLineWorkerDataService,
                                      LanguageService languageService, LocationService locationService,
                                      FlwErrorDataService flwErrorDataService) {
        this.frontLineWorkerDataService = frontLineWorkerDataService;
        this.schedulerService = schedulerService;
        this.settingsFacade = settingsFacade;
        this.languageService = languageService;
        this.locationService = locationService;
        this.flwErrorDataService = flwErrorDataService;
        schedulePurgeOfOldFrontLineWorkers();
    }

    /**
     * Use the MOTECH scheduler to setup a repeating job
     * The job will start today at the time stored in flw.purge_invalid_flw_start_time in flw.properties
     * It will repeat every flw.purge_invalid_flw_sec_interval seconds (default value is a day)
     */
    private void schedulePurgeOfOldFrontLineWorkers() {
        //Calculate today's fire time
        DateTimeFormatter fmt = DateTimeFormat.forPattern("H:m");
        String timeProp = settingsFacade.getProperty(FLW_PURGE_TIME);
        DateTime time = fmt.parseDateTime(timeProp);
        DateTime today = DateTime.now()                     // This means today's date...
                .withHourOfDay(time.getHourOfDay())         // ...at the hour...
                .withMinuteOfHour(time.getMinuteOfHour())   // ...and minute specified in imi.properties
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        //Second interval between events
        String intervalProp = settingsFacade.getProperty(FLW_PURGE_SEC_INTERVAL);
        Integer secInterval = Integer.parseInt(intervalProp);

        LOGGER.debug(String.format("The %s message will be sent every %ss starting at %s",
                                    FLW_PURGE_EVENT_SUBJECT, secInterval.toString(), today.toString()));

        //Schedule repeating job
        MotechEvent event = new MotechEvent(FLW_PURGE_EVENT_SUBJECT);
        RepeatingSchedulableJob job = new RepeatingSchedulableJob(
                event,          //MOTECH event
                null,           //repeatCount, null means infinity
                secInterval,    //repeatIntervalInSeconds
                today.toDate(), //startTime
                null,           //endTime, null means no end time
                true);          //ignorePastFiresAtStart

        schedulerService.safeScheduleRepeatingJob(job);
    }

    @MotechListener(subjects = { FLW_PURGE_EVENT_SUBJECT })
    @Transactional
    public void purgeOldInvalidFLWs(MotechEvent event) {
        int weeksToKeepInvalidFLWs = Integer.parseInt(settingsFacade.getProperty(WEEKS_TO_KEEP_INVALID_FLWS));
        final FrontLineWorkerStatus status = FrontLineWorkerStatus.INVALID;
        final DateTime cutoff = DateTime.now().minusWeeks(weeksToKeepInvalidFLWs).withTimeAtStartOfDay();

        @SuppressWarnings("unchecked")
        QueryExecution<Long> queryExecution = new QueryExecution<Long>() {
            @Override
            public Long execute(Query query, InstanceSecurityRestriction restriction) {

                query.setFilter("status == invalid && invalidationDate < cutoff");
                query.declareParameters("org.motechproject.nms.flw.domain.FrontLineWorkerStatus invalid, org.joda.time.DateTime cutoff");

                return query.deletePersistentAll(status, cutoff);
            }
        };

        Long purgedRecordCount = frontLineWorkerDataService.executeQuery(queryExecution);
        LOGGER.info(String.format("Purged %s FLWs with status %s and invalidation date before %s",
                purgedRecordCount, status, cutoff.toString()));
    }

    @Override
    public State getState(FrontLineWorker frontLineWorker) {
        if (frontLineWorker == null) {
            return null;
        }

        State state = null;
        District district = frontLineWorker.getDistrict();

        if (district != null) {
            state = district.getState();
        }

        if (state == null) {
            Language language = frontLineWorker.getLanguage();

            if (language != null) {
                Set<State> states = languageService.getAllStatesForLanguage(language);

                if (states.size() == 1) {
                    state = states.iterator().next();
                }
            }
        }

        return state;
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    public boolean createUpdate(Map<String, Object> flw) {

        long stateId = (long) flw.get(FlwConstants.STATE_ID);
        long districtId = (long) flw.get(FlwConstants.DISTRICT_ID);
        String mctsFlwId = flw.get(FlwConstants.ID).toString();
        long contactNumber = (long) flw.get(FlwConstants.CONTACT_NO);

        State state = locationService.getState(stateId);
        if (state == null) {
            flwErrorDataService.create(new FlwError(mctsFlwId, stateId, districtId, FlwErrorReason.INVALID_LOCATION_STATE));
            return false;
        }
        District district = locationService.getDistrict(stateId, districtId);
        if (district == null) {
            flwErrorDataService.create(new FlwError(mctsFlwId, stateId, districtId, FlwErrorReason.INVALID_LOCATION_DISTRICT));
            return false;
        }

        FrontLineWorker existingFlwByNumber = getByContactNumber(contactNumber);
        FrontLineWorker existingFlwByMctsFlwId = getByMctsFlwIdAndState(mctsFlwId, state);
        Map<String, Object> location = new HashMap<>();
        try {
            location = locationService.getLocations(flw);

            if (existingFlwByMctsFlwId != null && existingFlwByNumber != null) {

                if (existingFlwByMctsFlwId.getMctsFlwId().equalsIgnoreCase(existingFlwByNumber.getMctsFlwId()) &&
                        existingFlwByMctsFlwId.getState().equals(existingFlwByNumber.getState())) {
                    // we are trying to update the same existing flw. set fields and update
                    LOGGER.debug("Updating existing user with same phone number");
                    update(FlwMapper.updateFlw(existingFlwByMctsFlwId, flw, location));
                    return true;
                } else {
                    // we are trying to update 2 different users and/or phone number used by someone else
                    LOGGER.debug("Existing flw but phone number(update) already in use");
                    flwErrorDataService.create(new FlwError(mctsFlwId, stateId, districtId, FlwErrorReason.PHONE_NUMBER_IN_USE));
                    return false;
                }
            } else if (existingFlwByMctsFlwId != null && existingFlwByNumber == null) {
                // trying to update the phone number of the person. possible migration scenario
                // making design decision that flw will lose all progress when phone number is changed. Usage and tracking is not
                // worth the effort & we don't really know that its the same flw
                LOGGER.debug("Updating phone number for flw");
                update(FlwMapper.updateFlw(existingFlwByMctsFlwId, flw, location));
                return true;
            } else if (existingFlwByMctsFlwId == null && existingFlwByNumber != null) {

                if (existingFlwByNumber.getMctsFlwId() == null) {
                    // we just got data from mcts for a previous anonymous user that subscribed by phone number
                    // merging those records
                    LOGGER.debug("Merging mcts data with previously anonymous user");
                    update(FlwMapper.updateFlw(existingFlwByNumber, flw, location));
                    return true;
                } else {
                    // phone number used by someone else.
                    LOGGER.debug("New flw but phone number(update) already in use");
                    flwErrorDataService.create(new FlwError(mctsFlwId, stateId, districtId, FlwErrorReason.PHONE_NUMBER_IN_USE));
                    return false;
                }

            } else { // existingFlwByMctsFlwId & existingFlwByNumber are null)
                // new user. set fields and add
                LOGGER.debug("Adding new flw user");
                add(FlwMapper.createFlw(flw, location));
                return true;
            }
        } catch (InvalidLocationException ile) {
                LOGGER.debug(ile.toString());
                return false;
        }
    }

    @Override
    public void add(FrontLineWorker record) {

        // TODO: also check for FLWDesignation, once we add that field
        // TODO: find out which language/location fields are mandatory
        if ((record.getName() != null) && (record.getContactNumber() != null) &&
                (record.getLanguage() != null) && (record.getDistrict() != null)) {

            // the record was added via CSV upload and the FLW hasn't called the service yet
            record.setStatus(FrontLineWorkerStatus.INACTIVE);

        } else if (record.getContactNumber() != null) {

            // the record was added when the FLW called the IVR service for the first time
            record.setStatus(FrontLineWorkerStatus.ANONYMOUS);
        }

        frontLineWorkerDataService.create(record);
    }

    @Override
    public FrontLineWorker getByFlwId(String flwId) {
        return frontLineWorkerDataService.findByFlwId(flwId);
    }

    @Override
    public FrontLineWorker getByMctsFlwIdAndState(final String mctsFlwId, final State state) {
        if (mctsFlwId == null || state == null) {
            LOGGER.error(String.format("Attempt to look up FLW by a null mctsFlwId (%s) or state (%s)",
                    mctsFlwId, state == null ? "null" : state.getName()));
            return null;
        }

        @SuppressWarnings("unchecked")
        QueryExecution<FrontLineWorker> queryExecution = new QueryExecution<FrontLineWorker>() {
            @Override
            public FrontLineWorker execute(Query query, InstanceSecurityRestriction restriction) {
                query.setFilter("mctsFlwId == _mctsFlwId && state == _state");
                query.declareParameters("String _mctsFlwId, org.motechproject.nms.region.domain.State _state");
                query.setClass(FrontLineWorker.class);
                query.setUnique(true);

                return (FrontLineWorker) query.execute(mctsFlwId, state);
            }
        };

        return frontLineWorkerDataService.executeQuery(queryExecution);
    }

    @Override
    public FrontLineWorker getById(Long id) {
        return frontLineWorkerDataService.findById(id);
    }

    @Override
    public FrontLineWorker getByContactNumber(Long contactNumber) {
        return frontLineWorkerDataService.findByContactNumber(contactNumber);
    }

    @Override
    public List<FrontLineWorker> getRecords() {
        return frontLineWorkerDataService.retrieveAll();
    }

    /**
     * Update FrontLineWorker. If specific fields are added to the record (name, contactNumber, languageLocation,
     * district, designation), the FrontLineWorker's status will also be updated.
     * @param record The FrontLineWorker to update
     */
    @Override
    @Transactional
    public void update(FrontLineWorker record) {

        if (record.getStatus() == FrontLineWorkerStatus.INVALID) {
            // if the caller sets the status to INVALID, that takes precedence over any other status change
            frontLineWorkerDataService.update(record);
            return;
        }

        FrontLineWorker retrievedFlw = frontLineWorkerDataService.findByContactNumber(record.getContactNumber());
        FrontLineWorkerStatus oldStatus = retrievedFlw.getStatus();

        if (oldStatus == FrontLineWorkerStatus.ANONYMOUS) {
            // if the FLW was ANONYMOUS and the required fields get added, update her status to ACTIVE

            // TODO: also check for FLWDesignation once we get spec clarity on what that is
            if ((record.getName() != null) && (record.getContactNumber() != null) &&
                    (record.getLanguage() != null) && (record.getDistrict() != null)) {

                record.setStatus(FrontLineWorkerStatus.ACTIVE);
            }
        }

        frontLineWorkerDataService.update(record);
    }

    @Override
    public void delete(FrontLineWorker record) {
        frontLineWorkerDataService.delete(record);
    }

    @Override
    public void deletePreconditionCheck(FrontLineWorker frontLineWorker) {
        int weeksToKeepInvalidFLWs = Integer.parseInt(settingsFacade.getProperty(WEEKS_TO_KEEP_INVALID_FLWS));
        FrontLineWorkerStatus status = FrontLineWorkerStatus.INVALID;
        DateTime now = new DateTime();

        if (frontLineWorker.getStatus() != status) {
            throw new IllegalStateException("Can not delete a valid FLW");
        }

        if (frontLineWorker.getInvalidationDate() == null) {
            throw new IllegalStateException(String.format("FLW in %s state with null invalidation date", status));
        }

        if (Math.abs(Weeks.weeksBetween(now, frontLineWorker.getInvalidationDate()).getWeeks()) < weeksToKeepInvalidFLWs) {
            throw new IllegalStateException(String.format("FLW must be in %s state for %s weeks before deleting",
                                                          status, weeksToKeepInvalidFLWs));
        }
    }
}

package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.BeneficiaryImportOrigin;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.exception.MultipleSubscriberException;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.kilkari.utils.MctsBeneficiaryUtils;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the {@link MctsBeneficiaryImportService} interface.
 */
@Service("mctsBeneficiaryImportService")
public class MctsBeneficiaryImportServiceImpl implements MctsBeneficiaryImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportServiceImpl.class);

    private SubscriptionService subscriptionService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private LocationService locationService;
    private SubscriberService subscriberService;
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;
    private SubscriptionPack pregnancyPack;
    private SubscriptionPack childPack;
    private MctsMotherDataService mctsMotherDataService;

    @Autowired
    public MctsBeneficiaryImportServiceImpl(SubscriptionService subscriptionService,
                                            SubscriptionErrorDataService subscriptionErrorDataService,
                                            LocationService locationService, SubscriberService subscriberService,
                                            MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor,
                                            MctsMotherDataService mctsMotherDataService) {
        this.subscriptionService = subscriptionService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.locationService = locationService;
        this.subscriberService = subscriberService;
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
        this.mctsMotherDataService = mctsMotherDataService;
    }


    /**
     * Expected file format:
     * - any number of empty lines
     * - header lines in the following format:  State Name : ACTUAL STATE_ID NAME
     * - one empty line
     * - CSV data (tab-separated)
     */
    @Override
    @Transactional
    public int importMotherData(Reader reader, BeneficiaryImportOrigin beneficiaryImportOrigin) throws IOException {
        pregnancyPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.PREGNANCY);
        int count = 0;
        /**
         * Count of all the records rejected for unknown exceptions. So, doesn't include the ones saved in nms_subscription_errors.
         * This is used just for debugging purpose.
         */
        int rejectedWithException = 0;

        BufferedReader bufferedReader = new BufferedReader(reader);
        Map<String, CellProcessor> cellProcessorMapper;
        String id;
        String contactNumber;

        if (beneficiaryImportOrigin.equals(BeneficiaryImportOrigin.MCTS)) {
            cellProcessorMapper = this.getMotherProcessorMapping();
            id = KilkariConstants.BENEFICIARY_ID;
            contactNumber = KilkariConstants.MSISDN;
        } else {
            cellProcessorMapper = this.getRchMotherProcessorMapping();
            id = KilkariConstants.RCH_ID;
            contactNumber = KilkariConstants.MOBILE_NO;
        }

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(cellProcessorMapper)
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        try {
            Map<String, Object> record;
            Timer timer = new Timer("mom", "moms");
            while (null != (record = csvImporter.read())) {
                MctsMother mother = (MctsMother) record.get(id);
                LOGGER.debug("Started import for msisdn {} beneficiary_id {}", record.get(contactNumber), mother.getBeneficiaryId());  // TODO: change mother id to valid one
                try {
                    importMotherRecord(record, beneficiaryImportOrigin);  // TODO: valid method
                    count++;
                    if (count % KilkariConstants.PROGRESS_INTERVAL == 0) {
                        LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
                    }
                } catch (RuntimeException e) {
                    LOGGER.error("Error at msisdn {} beneficiary_id {}", record.get(contactNumber), mother.getBeneficiaryId(), e);  // TODO: change mother id to valid one
                    rejectedWithException++;
                }
            }

            LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
            LOGGER.debug(KilkariConstants.REJECTED, timer.frequency(rejectedWithException));

        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("Mother import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }

        return count;
    }

    @Transactional
    public int importChildData(Reader reader, BeneficiaryImportOrigin beneficiaryImportOrigin) throws IOException {
        childPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.CHILD);
        int count = 0;
        /**
         * Count of all the records rejected for unknown exceptions. So, doesn't include the ones saved in nms_subscription_errors.
         * This is used just for debugging purpose.
         */
        int rejectedWithException = 0;

        BufferedReader bufferedReader = new BufferedReader(reader);
        Map<String, CellProcessor> cellProcessorMapper;
        String id;
        String contactNumber;

        if (beneficiaryImportOrigin.equals(BeneficiaryImportOrigin.MCTS)) {
            cellProcessorMapper = this.getChildProcessorMapping();
            id = KilkariConstants.BENEFICIARY_ID;
            contactNumber = KilkariConstants.MSISDN;
        } else {
            cellProcessorMapper = this.getRchChildProcessorMapping();
            id = KilkariConstants.RCH_ID;   // TODO: change it once we set id
            contactNumber = KilkariConstants.MOBILE_NO;
        }

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(cellProcessorMapper)
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        try {
            Map<String, Object> record;
            Timer timer = new Timer("kid", "kids");
            while (null != (record = csvImporter.read())) {
                MctsChild child = (MctsChild) record.get(id);              // TODO: better to add beneficiaryID to mapper in MCTS
                LOGGER.debug("Started import for msisdn {} beneficiary_id {}", record.get(contactNumber), child.getBeneficiaryId());
                try {
                    importChildRecord(record, beneficiaryImportOrigin);  // TODO: change to valid method
                    count++;
                    if (count % KilkariConstants.PROGRESS_INTERVAL == 0) {
                        LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
                    }
                } catch (RuntimeException e) {
                    LOGGER.error("Error at msisdn {} beneficiary_id {}", record.get(contactNumber), child.getBeneficiaryId(), e);
                    rejectedWithException++;
                }
            }

            LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
            LOGGER.debug(KilkariConstants.REJECTED, timer.frequency(rejectedWithException));

        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("Child import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }

        return count;
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public boolean importMotherRecord(Map<String, Object> record, BeneficiaryImportOrigin beneficiaryImportOrigin) { //NOPMD NcssMethodCount
        if (pregnancyPack == null) {
            pregnancyPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.PREGNANCY);
        }

        MctsMother mother;
        Long msisdn;
        Boolean abortion;
        Boolean stillBirth;
        LocalDate lastUpdatedDateNic;
        String beneficiaryId;

        if (beneficiaryImportOrigin.equals(BeneficiaryImportOrigin.MCTS)) {
            mother = (MctsMother) record.get(KilkariConstants.BENEFICIARY_ID);
            msisdn = (Long) record.get(KilkariConstants.MSISDN);
            abortion = (Boolean) record.get(KilkariConstants.ABORTION);
            stillBirth = (Boolean) record.get(KilkariConstants.STILLBIRTH);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.LAST_UPDATE_DATE);
            beneficiaryId = mother.getBeneficiaryId();
        } else {
            beneficiaryId = (String) record.get(KilkariConstants.RCH_ID);  // TODO: Customize variable once u finalize on subscriptionerror
            String mctsId = (String) record.get(KilkariConstants.MCTS_ID);
            mother = mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance(beneficiaryId, mctsId);
            msisdn = (Long) record.get(KilkariConstants.MOBILE_NO);
            abortion = (Boolean) record.get(KilkariConstants.ABORTION_TYPE);
            stillBirth = (Boolean) record.get(KilkariConstants.DELIVERY_OUTCOMES);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.EXECUTION_DATE);
        }

        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        DateTime lmp = (DateTime) record.get(KilkariConstants.LMP);
        DateTime motherDOB = (DateTime) record.get(KilkariConstants.MOTHER_DOB);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        // validate msisdn
        if (!validateMsisdn(msisdn, SubscriptionPackType.PREGNANCY, beneficiaryImportOrigin)) {
            return false;
        }

        // validate lmp date. We do not sanitize for lmp in the future to be in sync with MCTS data
        // NOTE: getId is a way to see if this is a new user. We only accept new users if they
        // have 12 weeks left in the pack. For existing users, their lmp could be updated to an earlier date
        if (mother.getId() == null && !validateReferenceDate(lmp, SubscriptionPackType.PREGNANCY, msisdn, beneficiaryImportOrigin)) {
            return false;
        }

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), mother);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.PREGNANCY, le.getMessage(), beneficiaryImportOrigin));
            return false;
        }

        //validate if it's an updated record compared to one from database
        if (mother.getUpdatedDateNic() != null && (lastUpdatedDateNic == null || mother.getUpdatedDateNic().isAfter(lastUpdatedDateNic))) {
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                    SubscriptionRejectionReason.ALREADY_SUBSCRIBED, SubscriptionPackType.PREGNANCY, "Updated Record exits", beneficiaryImportOrigin));
            return false;
        }

        mother.setName(name);
        mother.setDateOfBirth(motherDOB);
        mother.setUpdatedDateNic(lastUpdatedDateNic);

        if (beneficiaryImportOrigin.equals(BeneficiaryImportOrigin.MCTS)) {
            //validate if an ACTIVE child is already present for the mother. If yes, ignore the update
            if (childAlreadyPresent(mother.getBeneficiaryId())) {
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                        SubscriptionRejectionReason.ACTIVE_CHILD_PRESENT, SubscriptionPackType.PREGNANCY, "Active child is present for this mother.", beneficiaryImportOrigin));
                return false;
            }
        } else {
            Long caseNo = (Long) record.get(KilkariConstants.CASE_NO);
            if ((mother.getId() != null) && !caseNoValidation(mother, caseNo)) {  // TODO: just one or different rejection reasons?
                subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                        SubscriptionRejectionReason.INVALID_CASE_NO, SubscriptionPackType.PREGNANCY, "Not a valid Case no to enable multiple Pregnancy", beneficiaryImportOrigin));
                return false;
            }
            mother.setCaseNo(caseNo);
        }

        Subscription subscription = subscriberService.updateMotherSubscriber(msisdn, mother, lmp, beneficiaryImportOrigin);

        // We rejected the update/create for the subscriber
        if (subscription == null) {
            return false;
        }

        if ((abortion != null) && abortion) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
            return true;
        }
        if ((stillBirth != null) && stillBirth) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
            return true;
        }
        if ((death != null) && death) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MATERNAL_DEATH);
        }

        return true;
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public boolean importChildRecord(Map<String, Object> record, BeneficiaryImportOrigin beneficiaryImportOrigin) {  //NOPMD NcssMethodCount
        if (childPack == null) {
            childPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.CHILD);
        }

        MctsChild child;
        MctsMother mother;
        Long msisdn;
        LocalDate lastUpdatedDateNic;
        String beneficiaryId;

        if (beneficiaryImportOrigin.equals(BeneficiaryImportOrigin.MCTS)) {
            child = (MctsChild) record.get(KilkariConstants.BENEFICIARY_ID);
            msisdn = (Long) record.get(KilkariConstants.MSISDN);
            mother = (MctsMother) record.get(KilkariConstants.MOTHER_ID);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.LAST_UPDATE_DATE);
            beneficiaryId = child.getBeneficiaryId();
        } else {
            beneficiaryId = (String) record.get(KilkariConstants.RCH_ID);
            child = mctsBeneficiaryValueProcessor.getOrCreateRchChildInstance(beneficiaryId, (String) record.get(KilkariConstants.MCTS_ID));
            mother = mctsBeneficiaryValueProcessor.getOrCreateRchMotherInstance((String) record.get(KilkariConstants.RCH_MOTHER_ID), (String) record.get(KilkariConstants.MCTS_MOTHER_ID));
            msisdn = (Long) record.get(KilkariConstants.MOBILE_NO);
            lastUpdatedDateNic = (LocalDate) record.get(KilkariConstants.EXECUTION_DATE);
        }

        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        DateTime dob = (DateTime) record.get(KilkariConstants.DOB);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        // validate msisdn
        if (!validateMsisdn(msisdn, SubscriptionPackType.CHILD, beneficiaryImportOrigin)) {
            return false;
        }

        // validate dob. We do not sanitize for dob in the future to be in sync with MCTS data
        // NOTE: getId is a way to check for new user. We only accept new children if they have 12 weeks left
        // in the pack. Existing children could have their dob udpated to an earlier date
        if (child.getId() == null && !validateReferenceDate(dob, SubscriptionPackType.CHILD, msisdn, beneficiaryImportOrigin)) {
            return false;
        }

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), child);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                    SubscriptionRejectionReason.INVALID_LOCATION, SubscriptionPackType.CHILD, le.getMessage(), beneficiaryImportOrigin));
            return false;
        }

        //validate if it's an updated record compared to one from database
        if (child.getUpdatedDateNic() != null && (lastUpdatedDateNic == null || child.getUpdatedDateNic().isAfter(lastUpdatedDateNic))) {
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, beneficiaryId,
                    SubscriptionRejectionReason.ALREADY_SUBSCRIBED, SubscriptionPackType.CHILD, "Updated Record exits", beneficiaryImportOrigin));
            return false;
        }

        child.setName(name);
        child.setMother(mother);
        child.setUpdatedDateNic(lastUpdatedDateNic);

        Subscription childSubscription = subscriberService.updateChildSubscriber(msisdn, child, dob, beneficiaryImportOrigin);
        // child subscription create/update was rejected
        if (childSubscription == null) {
            return false;
        }

        // a new child subscription was created -- deactivate mother's pregnancy subscription if she has one
        Subscriber subscriber = childSubscription.getSubscriber();
        MctsMother childMother = subscriber.getMother();
        String existingMotherId;
        String newMotherId;

        if (beneficiaryImportOrigin.equals(BeneficiaryImportOrigin.MCTS)) {
            existingMotherId = (childMother != null) ? childMother.getBeneficiaryId() : null;
            newMotherId = (mother != null) ? mother.getBeneficiaryId() : null;
        } else {
            existingMotherId = (childMother != null) ? childMother.getRchId() : null;
            newMotherId = (mother != null) ? mother.getRchId() : null;
        }
        if ((mother != null) && ((existingMotherId != null) && newMotherId.equals(existingMotherId))) {  // TODO: (mother != null) and (mother.equals(subscriber.getMother())) replaced with this
            Subscription pregnancySubscription = subscriptionService.getActiveSubscription(subscriber,
                    SubscriptionPackType.PREGNANCY);
            if (pregnancySubscription != null) {
                subscriptionService.deactivateSubscription(pregnancySubscription, DeactivationReason.LIVE_BIRTH);
            }
        }

        if ((death != null) && death) {
            subscriptionService.deactivateSubscription(childSubscription, DeactivationReason.CHILD_DEATH);
        }

        return true;
    }

    private boolean validateMsisdn(Long msisdn, SubscriptionPackType packType, BeneficiaryImportOrigin importOrigin) {
        if (msisdn == null) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(-1, SubscriptionRejectionReason.MISSING_MSISDN, packType, importOrigin));
            return false;
        }

        return true;
    }

    private boolean validateReferenceDate(DateTime referenceDate, SubscriptionPackType packType, Long msisdn, BeneficiaryImportOrigin importOrigin) {

        if (referenceDate == null) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn,
                            (packType == SubscriptionPackType.PREGNANCY) ?
                                    SubscriptionRejectionReason.MISSING_LMP :
                                    SubscriptionRejectionReason.MISSING_DOB,
                            packType, importOrigin));
            return false;
        }

        if (packType == SubscriptionPackType.PREGNANCY) {
            String referenceDateValidationError = pregnancyPack.isReferenceDateValidForPack(referenceDate);
            if (!referenceDateValidationError.isEmpty()) {
                subscriptionErrorDataService.create(
                        new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_LMP, SubscriptionPackType.PREGNANCY, referenceDateValidationError, importOrigin));
                return false;
            }
        } else { // childPack
            String referenceDateValidationError = childPack.isReferenceDateValidForPack(referenceDate);
            if (!referenceDateValidationError.isEmpty()) {
                subscriptionErrorDataService.create(
                        new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_DOB, SubscriptionPackType.CHILD, referenceDateValidationError, importOrigin));
                return false;
            }
        }

        return true;
    }

    private boolean multiPregnancyCheck(MctsMother mother, Boolean match) {
        Subscriber subscriber = subscriberService.getSubscriberByBeneficiary(mother);
        if (subscriber == null) {
            return (!match);  //Reject
        } else {
            Subscription subscription = subscriptionService.getActiveSubscription(subscriber, SubscriptionPackType.PREGNANCY);
            if (subscription == null) {
                return (!match);
            } else {
                if (!match) {
                    LOGGER.error("Cant update CaseNo when we have active Subscription for the older one for RchId {}", mother.getRchId());
                }
                return match;
            }
        }
    }

    private boolean caseNoValidation(MctsMother mother, Long caseNo) {
        Long existingCaseNo = mother.getCaseNo();
        Boolean matches;

        if (existingCaseNo == null) {
            if (caseNo == null) {
                // Both caseNo matches
                matches = true;
                multiPregnancyCheck(mother, matches);
            } else {
                // Reject if there is any active Subscription else Update mother details.
                matches = false;
                multiPregnancyCheck(mother, matches);
            }
        } else {
            if (caseNo == null) {
                return false;
            } else {
                if (existingCaseNo == caseNo) {
                    // Both caseNo matches
                    matches = true;
                    multiPregnancyCheck(mother, matches);
                } else if (existingCaseNo < caseNo) {
                    // Reject if there is any active Subscription else Update mother details.
                    matches = false;
                    multiPregnancyCheck(mother, matches);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean childAlreadyPresent(final String motherBenificiaryId) {
        //Found mother by beneficiary id. If there is no mother already present,then import will
        //go to the next check. Else we get the subscriber by the mother id
        //and check if the child subscription is ACTIVE. If yes we do not update the mother.
        MctsMother mctsMother = null;

        try {
           mctsMother = mctsMotherDataService.findByBeneficiaryId(motherBenificiaryId);

            if (mctsMother == null) {
                return false;
            } else {
                Long motherId = mctsMother.getId();
                Subscriber subscriber = subscriberService.getSubscriberByMother(motherId);
                for (Subscription subscription : subscriber.getAllSubscriptions()) {
                    if (subscription.getSubscriptionPack().getType().equals(SubscriptionPackType.CHILD)
                            && subscription.getStatus().equals(SubscriptionStatus.ACTIVE)
                            && subscriber.getChild().getMother() != null
                            && subscriber.getChild().getMother().getBeneficiaryId().equals(motherBenificiaryId)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (MultipleSubscriberException m) {
            LOGGER.error(m.toString());
            return true;
        }
    }

    private Map<String, CellProcessor> getMotherProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        getMctsRchMotherMapping(mapping);

        mapping.put(KilkariConstants.BENEFICIARY_ID, new GetInstanceByString<MctsMother>() {
            @Override
            public MctsMother retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getOrCreateMotherInstance(value);
            }
        });
        mapping.put(KilkariConstants.MSISDN, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
        mapping.put(KilkariConstants.ABORTION, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getAbortionDataFromString(value);
            }
        }));
        mapping.put(KilkariConstants.STILLBIRTH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getStillBirthFromString(value);
            }
        }));
        mapping.put(KilkariConstants.LAST_UPDATE_DATE, new Optional(new GetInstanceByString<LocalDate>() {
            @Override
            public LocalDate retrieve(String value) {
                return (LocalDate) mctsBeneficiaryValueProcessor.getDateByString(value).toLocalDate();
            }
        }));
        return mapping;
    }

    private Map<String, CellProcessor> getRchMotherProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        getMctsRchMotherMapping(mapping);

        mapping.put(KilkariConstants.RCH_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MCTS_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MOBILE_NO, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
        mapping.put(KilkariConstants.ABORTION_TYPE, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getAbortionDataFromString(value);
            }
        }));
        mapping.put(KilkariConstants.DELIVERY_OUTCOMES, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getStillBirthFromString(value);
            }
        }));
        mapping.put(KilkariConstants.EXECUTION_DATE, new Optional(new GetInstanceByString<LocalDate>() {
            @Override
            public LocalDate retrieve(String value) {
                return (LocalDate) mctsBeneficiaryValueProcessor.getDateByString(value).toLocalDate();
            }
        }));
        mapping.put(KilkariConstants.CASE_NO, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getCaseNoByString(value);
            }
        }));
        return mapping;
    }

    private void getMctsRchMotherMapping(Map<String, CellProcessor> mapping) {

        mapping.put(KilkariConstants.BENEFICIARY_NAME, new GetString());
        mapping.put(KilkariConstants.LMP, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
            }
        }));
        mapping.put(KilkariConstants.MOTHER_DOB, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
            }
        }));
        mapping.put(KilkariConstants.DEATH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDeathFromString(value);
            }
        }));
    }

    private Map<String, CellProcessor> getChildProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        getMctsRchChildMapping(mapping);

        mapping.put(KilkariConstants.BENEFICIARY_ID, new GetInstanceByString<MctsChild>() {
            @Override
            public MctsChild retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getChildInstanceByString(value);
            }
        });
        mapping.put(KilkariConstants.MOTHER_ID, new Optional(new GetInstanceByString<MctsMother>() {
            @Override
            public MctsMother retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMotherInstanceByBeneficiaryId(value);
            }
        }));
        mapping.put(KilkariConstants.MSISDN, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
        mapping.put(KilkariConstants.LAST_UPDATE_DATE, new Optional(new GetInstanceByString<LocalDate>() {
            @Override
            public LocalDate retrieve(String value) {
                return (LocalDate) mctsBeneficiaryValueProcessor.getDateByString(value).toLocalDate();
            }
        }));
        return mapping;
    }

    private Map<String, CellProcessor> getRchChildProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        getMctsRchChildMapping(mapping);

        mapping.put(KilkariConstants.RCH_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.RCH_MOTHER_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MCTS_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MCTS_MOTHER_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MOBILE_NO, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
        mapping.put(KilkariConstants.EXECUTION_DATE, new Optional(new GetInstanceByString<LocalDate>() {
            @Override
            public LocalDate retrieve(String value) {
                return (LocalDate) mctsBeneficiaryValueProcessor.getDateByString(value).toLocalDate();
            }
        }));
        return mapping;
    }

    private void getMctsRchChildMapping(Map<String, CellProcessor> mapping) {

        mapping.put(KilkariConstants.BENEFICIARY_NAME, new GetString());
        mapping.put(KilkariConstants.DOB, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
            }
        }));
        mapping.put(KilkariConstants.DEATH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDeathFromString(value);
            }
        }));
    }
}

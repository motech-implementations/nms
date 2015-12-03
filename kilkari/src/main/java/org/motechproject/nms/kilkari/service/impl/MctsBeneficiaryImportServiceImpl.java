package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
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

    public static final String IMPORTED = "Imported {}";
    private SubscriptionService subscriptionService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private LocationService locationService;
    private SubscriberService subscriberService;
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportServiceImpl.class);

    private static final int PROGRESS_INTERVAL = 100;

    private SubscriptionPack pregnancyPack;
    private SubscriptionPack childPack;


    @Autowired
    public MctsBeneficiaryImportServiceImpl(SubscriptionService subscriptionService,
                                            SubscriptionErrorDataService subscriptionErrorDataService,
                                            LocationService locationService, SubscriberService subscriberService,
                                            MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor) {
        this.subscriptionService = subscriptionService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.locationService = locationService;
        this.subscriberService = subscriberService;
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
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
    public int importMotherData(Reader reader) throws IOException {
        pregnancyPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.PREGNANCY);
        int count = 0;

        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getMotherProcessorMapping())
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        try {
            Map<String, Object> record;
            Timer timer = new Timer("mom", "moms");
            while (null != (record = csvImporter.read())) {
                importMotherRecord(record);
                count++;
                if (count % PROGRESS_INTERVAL == 0) {
                    LOGGER.debug(IMPORTED, timer.frequency(count));
                }
            }
            if (count % PROGRESS_INTERVAL != 0) {
                LOGGER.debug(IMPORTED, timer.frequency(count));
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("MCTS mother import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }

        return count;
    }

    @Transactional
    public int importChildData(Reader reader) throws IOException {
        childPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.CHILD);
        int count = 0;

        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getChildProcessorMapping())
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        try {
            Map<String, Object> record;
            Timer timer = new Timer("kid", "kids");
            while (null != (record = csvImporter.read())) {
                importChildRecord(record);
                count++;
                if (count % PROGRESS_INTERVAL == 0) {
                    LOGGER.debug(IMPORTED, timer.frequency(count));
                }
            }
            if (count % PROGRESS_INTERVAL != 0) {
                LOGGER.debug(IMPORTED, timer.frequency(count));
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("MCTS child import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }

        return count;
    }

    @Override // NO CHECKSTYLE Cyclomatic Complexity
    @Transactional
    public boolean importMotherRecord(Map<String, Object> record) {
        if (pregnancyPack == null) {
            pregnancyPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.PREGNANCY);
        }

        MctsMother mother = (MctsMother) record.get(KilkariConstants.BENEFICIARY_ID);
        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(KilkariConstants.MSISDN);
        DateTime lmp = (DateTime) record.get(KilkariConstants.LMP);
        DateTime motherDOB = (DateTime) record.get(KilkariConstants.MOTHER_DOB);
        Boolean abortion = (Boolean) record.get(KilkariConstants.ABORTION);
        Boolean stillBirth = (Boolean) record.get(KilkariConstants.STILLBIRTH);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), mother);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.PREGNANCY, le.getMessage()));
            return false;
        }

        if (!validateLMP(lmp, msisdn)) {
            return false;
        }

        if (name != null && name.isEmpty()) {
            mother.setName(name);
        }
        mother.setDateOfBirth(motherDOB);

        Subscription subscription = subscriberService.updateMotherSubscriber(msisdn, mother, lmp);
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

    @Override
    @Transactional
    public boolean importChildRecord(Map<String, Object> record) {
        if (childPack == null) {
            childPack = subscriptionService.getSubscriptionPack(SubscriptionPackType.CHILD);
        }

        MctsChild child = (MctsChild) record.get(KilkariConstants.BENEFICIARY_ID);
        String name = (String) record.get(KilkariConstants.BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(KilkariConstants.MSISDN);
        MctsMother mother = (MctsMother) record.get(KilkariConstants.MOTHER_ID);
        DateTime dob = (DateTime) record.get(KilkariConstants.DOB);
        Boolean death = (Boolean) record.get(KilkariConstants.DEATH);

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), child);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.CHILD, le.getMessage()));
            return false;
        }

        if (!validateDOB(dob, msisdn)) {
            return false;
        }

        child.setName(name);
        child.setMother(mother);

        Subscription childSubscription = subscriberService.updateChildSubscriber(msisdn, child, dob);
        // child subscription create/update was rejected
        if (childSubscription == null) {
            return false;
        }

        // a new child subscription was created -- deactivate mother's pregnancy subscription if she has one
        Subscriber subscriber = childSubscription.getSubscriber();

        if ((mother != null) && (mother.equals(subscriber.getMother()))) {

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

    private boolean validateLMP(DateTime lmp, Long msisdn) {
        if (lmp == null) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, SubscriptionRejectionReason.MISSING_LMP, SubscriptionPackType.PREGNANCY));
            return false;
        }
        String referenceDateValidationError = pregnancyPack.isReferenceDateValidForPack(lmp);
        if (!referenceDateValidationError.isEmpty()) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_LMP, SubscriptionPackType.PREGNANCY, referenceDateValidationError));
            return false;
        }

        return true;
    }

    private boolean validateDOB(DateTime dob, Long msisdn) {
        if (dob == null) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, SubscriptionRejectionReason.MISSING_DOB, SubscriptionPackType.CHILD));
            return false;
        }
        String referenceDateValidationError = childPack.isReferenceDateValidForPack(dob);
        if (!referenceDateValidationError.isEmpty()) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_DOB, SubscriptionPackType.CHILD, referenceDateValidationError));
            return false;
        }

        return true;
    }

    private Map<String, CellProcessor> getMotherProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);

        mapping.put(KilkariConstants.BENEFICIARY_ID, new GetInstanceByString<MctsMother>() {
            @Override
            public MctsMother retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getOrCreateMotherInstance(value);
            }
        });
        mapping.put(KilkariConstants.BENEFICIARY_NAME, new GetString());
        mapping.put(KilkariConstants.MSISDN, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
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
        mapping.put(KilkariConstants.DEATH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDeathFromString(value);
            }
        }));

        return mapping;
    }

    private Map<String, CellProcessor> getChildProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);

        mapping.put(KilkariConstants.BENEFICIARY_ID, new GetInstanceByString<MctsChild>() {
            @Override
            public MctsChild retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getChildInstanceByString(value);
            }
        });
        mapping.put(KilkariConstants.BENEFICIARY_NAME, new Optional(new GetString()));
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

        return mapping;
    }
}

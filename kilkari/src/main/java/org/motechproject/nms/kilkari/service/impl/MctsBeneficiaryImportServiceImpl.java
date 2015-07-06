package org.motechproject.nms.kilkari.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.DeactivationReason;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.HealthSubFacility;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
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

    private static final String STATE = "StateID";
    private static final String DISTRICT = "District_ID";
    private static final String TALUKA = "Taluka_ID";
    private static final String HEALTH_BLOCK = "HealthBlock_ID";
    private static final String PHC = "PHC_ID";
    private static final String SUBCENTRE = "SubCentre_ID";
    private static final String CENSUS_VILLAGE = "Village_ID";
    private static final String NON_CENSUS_VILAGE = "SVID";
    private static final String BENEFICIARY_ID = "ID_No";
    private static final String BENEFICIARY_NAME = "Name";
    private static final String MSISDN = "Whom_PhoneNo";
    private static final String LMP = "LMP_Date";
    private static final String DOB = "Birthdate";
    private static final String MOTHER_ID = "Mother_ID";
    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportServiceImpl.class);

    private LocationService locationService;
    private MctsMotherDataService mctsMotherDataService;
    private MctsChildDataService mctsChildDataService;
    private SubscriptionService subscriptionService;
    private SubscriberService subscriberService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private SubscriptionPackDataService subscriptionPackDataService;

    private SubscriptionPack pregnancyPack;
    private SubscriptionPack childPack;

    @Autowired
      MctsBeneficiaryImportServiceImpl(LocationService locationService,
                                       MctsMotherDataService mctsMotherDataService,
                                       MctsChildDataService mctsChildDataService,
                                       SubscriptionService subscriptionService,
                                       SubscriberService subscriberService,
                                       SubscriptionPackDataService subscriptionPackDataService,
                                       SubscriptionErrorDataService subscriptionErrorDataService) {
        this.locationService = locationService;
        this.mctsMotherDataService = mctsMotherDataService;
        this.mctsChildDataService = mctsChildDataService;
        this.subscriptionService = subscriptionService;
        this.subscriberService = subscriberService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
    }

    /**
     * Expected file format:
     * - any number of empty lines
     * - header lines in the following format:  State Name : ACTUAL STATE NAME
     * - one empty line
     * - CSV data (tab-separated)
     */
    @Override
    @Transactional
    public void importMotherData(Reader reader) throws IOException {
        pregnancyPack = subscriptionPackDataService.byType(SubscriptionPackType.PREGNANCY);

        BufferedReader bufferedReader = new BufferedReader(reader);
        readHeader(bufferedReader); // ignoring header as all interesting data in tab separated rows

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getMotherProcessorMapping())
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        try {
            Map<String, Object> record;
            while (null != (record = csvImporter.read())) {
                importMotherRecord(record);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("MCTS mother import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }
    }

    @Override
    @Transactional
    public void importChildData(Reader reader) throws IOException {
        childPack = subscriptionPackDataService.byType(SubscriptionPackType.CHILD);

        BufferedReader bufferedReader = new BufferedReader(reader);
        readHeader(bufferedReader); // ignoring header as all interesting data in tab separated rows

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getChildProcessorMapping())
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        try {
            Map<String, Object> record;
            while (null != (record = csvImporter.read())) {
                importChildRecord(record);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("MCTS child import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }
    }

    private void importMotherRecord(Map<String, Object> record) {
        MctsMother mother = (MctsMother) record.get(BENEFICIARY_ID);
        String name = (String) record.get(BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(MSISDN);
        DateTime lmp = (DateTime) record.get(LMP);

        // validate and set location
        try {
            setLocationFields(locationService.getLocations(record), mother);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            rejectBeneficiaryWithMessage(msisdn, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.PREGNANCY, le.getMessage());
            return;
        }

        if (!validateLMP(lmp, msisdn)) {
            return;
        }

        // TODO: more data validation specified in #111
        mother.setName(name);

        processSubscriptionForBeneficiary(mother, msisdn, lmp, pregnancyPack);
    }

    private void importChildRecord(Map<String, Object> record) {
        MctsChild child = (MctsChild) record.get(BENEFICIARY_ID);
        String name = (String) record.get(BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(MSISDN);
        MctsMother mother = (MctsMother) record.get(MOTHER_ID);
        DateTime dob = (DateTime) record.get(DOB);

        // validate and set location
        try {
            setLocationFields(locationService.getLocations(record), child);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            rejectBeneficiaryWithMessage(msisdn, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.CHILD, le.getMessage());
            return;
        }

        if (!validateDOB(dob, msisdn)) {
            return;
        }
        // TODO: more data validation specified in #111

        child.setName(name);
        child.setMother(mother);

        Subscription childSubscription = processSubscriptionForBeneficiary(child, msisdn, dob, childPack);

        if (childSubscription != null) {
            // a new child subscription was created -- deactivate mother's pregnancy subscription if she has one

            Subscriber subscriber = childSubscription.getSubscriber();

            if ((mother != null) && (mother.equals(subscriber.getMother()))) {

                Subscription pregnancySubscription = subscriptionService.getActiveSubscription(subscriber,
                        SubscriptionPackType.PREGNANCY);
                if (pregnancySubscription != null) {
                    subscriptionService.deactivateSubscription(pregnancySubscription, DeactivationReason.LIVE_BIRTH);
                }

            }
        }
    }

    private boolean validateLMP(DateTime lmp, Long msisdn) {
        if (lmp == null) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.MISSING_LMP, SubscriptionPackType.PREGNANCY);
            return false;
        }
        if (!pregnancyPack.isReferenceDateValidForPack(lmp)) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.INVALID_LMP, SubscriptionPackType.PREGNANCY);
            return false;
        }

        return true;
    }

    private boolean validateDOB(DateTime dob, Long msisdn) {
        if (dob == null) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.MISSING_DOB, SubscriptionPackType.CHILD);
            return false;
        }
        if (!childPack.isReferenceDateValidForPack(dob)) {
            rejectBeneficiary(msisdn, SubscriptionRejectionReason.INVALID_DOB, SubscriptionPackType.CHILD);
            return false;
        }

        return true;
    }

    private Subscription processSubscriptionForBeneficiary(MctsBeneficiary beneficiary, Long msisdn, DateTime referenceDate,
                                                  SubscriptionPack pack) {
        Language language = beneficiary.getDistrict().getLanguage();
        Subscriber subscriber = subscriberService.getSubscriber(msisdn);

        if (subscriber == null) {
            // there's no subscriber with this MSISDN, create one

            subscriber = new Subscriber(msisdn, language);
            subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, pack.getType());
            subscriberService.create(subscriber);
            return subscriptionService.createSubscription(msisdn, language, pack, SubscriptionOrigin.MCTS_IMPORT);
        }

        Subscription subscription = subscriptionService.getActiveSubscription(subscriber, pack.getType());
        if (subscription != null) {
            // subscriber already has an active subscription to this pack

            MctsBeneficiary existingBeneficiary = (pack.getType() == SubscriptionPackType.PREGNANCY) ? subscriber.getMother() :
                    subscriber.getChild();

            if (existingBeneficiary == null) {
                // there's already an IVR-originated subscription for this MSISDN
                rejectBeneficiary(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType());

                // TODO: Do we just reject the subscription request, or do we update the subscriber record with MCTS data?
                // TODO: Should we change the subscription start date based on the provided LMP/DOB?

            } else if (!existingBeneficiary.getBeneficiaryId().equals(beneficiary.getBeneficiaryId())) {
                // if the MCTS ID doesn't match (i.e. there are two beneficiaries with the same phone number), reject the import
                rejectBeneficiary(msisdn, SubscriptionRejectionReason.ALREADY_SUBSCRIBED, pack.getType());
            } else {
                // it's the same beneficiary, treat this import as an update
                subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, pack.getType());
                subscriberService.update(subscriber);
            }

            return subscription;
        }

        // subscriber exists, but doesn't have a subscription to this pack
        subscriber = updateSubscriber(subscriber, beneficiary, referenceDate, pack.getType());
        subscriberService.update(subscriber);
        return subscriptionService.createSubscription(msisdn, language, pack, SubscriptionOrigin.MCTS_IMPORT);
    }


    private Subscriber updateSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, DateTime referenceDate,
                                        SubscriptionPackType packType) {
        if (packType == SubscriptionPackType.PREGNANCY) {
            subscriber.setLastMenstrualPeriod(referenceDate);
            subscriber.setMother((MctsMother) beneficiary);
        } else {
            subscriber.setDateOfBirth(referenceDate);
            subscriber.setChild((MctsChild) beneficiary);
        }
        return subscriber;
    }

    private void setLocationFields(Map<String, Object> locations, MctsBeneficiary beneficiary) throws InvalidLocationException {

        if (locations.get(STATE) == null && locations.get(DISTRICT) == null) {
            throw new InvalidLocationException("Missing mandatory state and district fields");
        }
        
        if (locations.get(STATE) == null) {
            throw new InvalidLocationException("Missing mandatory state field");
        }

        if (locations.get(DISTRICT) == null) {
            throw new InvalidLocationException("Missing mandatory district field");
        }

        beneficiary.setState((State) locations.get(STATE));
        beneficiary.setDistrict((District) locations.get(DISTRICT));
        beneficiary.setTaluka((Taluka) locations.get(TALUKA));
        beneficiary.setHealthBlock((HealthBlock) locations.get(HEALTH_BLOCK));
        beneficiary.setHealthFacility((HealthFacility) locations.get(PHC));
        beneficiary.setHealthSubFacility((HealthSubFacility) locations.get(SUBCENTRE));
        beneficiary.setVillage((Village) locations.get(CENSUS_VILLAGE + NON_CENSUS_VILAGE));
    }

    private void rejectBeneficiary(Long msisdn, SubscriptionRejectionReason reason, SubscriptionPackType packType) {
        subscriptionErrorDataService.create(new SubscriptionError(msisdn, reason, packType));
    }

    private void rejectBeneficiaryWithMessage(Long msisdn, SubscriptionRejectionReason reason, SubscriptionPackType packType, String rejectionMessage) {
        subscriptionErrorDataService.create(new SubscriptionError(msisdn, reason, packType, rejectionMessage));
    }

    private Map<String, CellProcessor> getBeneficiaryLocationMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(STATE, new GetLong());
        mapping.put(DISTRICT, new GetLong());
        mapping.put(TALUKA, new Optional(new GetString()));
        mapping.put(HEALTH_BLOCK, new Optional(new GetLong()));
        mapping.put(PHC, new Optional(new GetLong()));
        mapping.put(SUBCENTRE, new Optional(new GetLong()));
        mapping.put(CENSUS_VILLAGE, new Optional(new GetLong()));
        mapping.put(NON_CENSUS_VILAGE, new Optional(new GetLong()));

        return mapping;
    }

    private Map<String, CellProcessor> getMotherProcessorMapping() {
        Map<String, CellProcessor> mapping = getBeneficiaryLocationMapping();

        mapping.put(BENEFICIARY_ID, new GetInstanceByString<MctsMother>() {
            @Override
            public MctsMother retrieve(String value) {
                MctsMother mother = mctsMotherDataService.findByBeneficiaryId(value);
                if (mother == null) {
                    mother = new MctsMother(value);
                }
                return mother;
            }
        });
        mapping.put(BENEFICIARY_NAME, new GetString());
        mapping.put(MSISDN, new GetLong());
        mapping.put(LMP, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                if (value == null) {
                    return null;
                }

                DateTime lmp;

                try {
                    DateTimeParser[] parsers = {
                            DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                            DateTimeFormat.forPattern("dd/MM/yyyy").getParser()};
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

                    lmp = formatter.parseDateTime(value);

                } catch (IllegalArgumentException e) {
                    throw new CsvImportDataException(String.format("LMP date %s is invalid", value), e);
                }

                return lmp;
            }
        }));

        // TODO: Any other fields needed for mothers? e.g. Abortion, etc.

        return mapping;
    }

    private Map<String, CellProcessor> getChildProcessorMapping() {
        Map<String, CellProcessor> mapping = getBeneficiaryLocationMapping();

        mapping.put(BENEFICIARY_ID, new GetInstanceByString<MctsChild>() {
            @Override
            public MctsChild retrieve(String value) {
                MctsChild child = mctsChildDataService.findByBeneficiaryId(value);
                if (child == null) {
                    child = new MctsChild(value);
                }
                return child;
            }
        });
        mapping.put(BENEFICIARY_NAME, new GetString());
        mapping.put(MOTHER_ID, new Optional(new GetInstanceByString<MctsMother>() {
            @Override
            public MctsMother retrieve(String value) {
                if (value == null) {
                    return null;
                }
                return mctsMotherDataService.findByBeneficiaryId(value);
            }
        }));
        mapping.put(MSISDN, new GetLong());
        mapping.put(DOB, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                if (value == null) {
                    return null;
                }

                DateTime dob;

                try {
                    DateTimeParser[] parsers = {
                            DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                            DateTimeFormat.forPattern("dd/MM/yyyy").getParser() };
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

                    dob = formatter.parseDateTime(value);

                } catch (IllegalArgumentException e) {
                    throw new CsvImportDataException(String.format("DOB date %s is invalid", value), e);
                }

                return dob;
            }
        }));

        return mapping;

    }

    private void readHeader(BufferedReader bufferedReader) throws IOException {
        readLineWhileBlank(bufferedReader);
        readLineWhileNotBlank(bufferedReader);
    }

    private String readLineWhileBlank(BufferedReader bufferedReader) throws IOException {
        String line;
        do {
            line = bufferedReader.readLine();
        } while (null != line && StringUtils.isBlank(line));
        return line;
    }

    private String readLineWhileNotBlank(BufferedReader bufferedReader) throws IOException {
        String line;
        do {
            line = bufferedReader.readLine();
        } while (null != line && StringUtils.isNotBlank(line));
        return line;
    }

}

package org.motechproject.nms.kilkari.service.impl;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
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
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionPackDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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

    private SubscriptionService subscriptionService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private SubscriptionPackDataService subscriptionPackDataService;
    private LocationService locationService;
    private MctsMotherDataService mctsMotherDataService;
    private MctsChildDataService mctsChildDataService;
    private SubscriberService subscriberService;

    private static final String BENEFICIARY_ID = "ID_No";
    private static final String BENEFICIARY_NAME = "Name";
    private static final String MSISDN = "Whom_PhoneNo";
    private static final String LMP = "LMP_Date";
    private static final String DOB = "Birthdate";
    private static final String MOTHER_ID = "Mother_ID";
    private static final String MOTHER_DOB = "Birthdate";
    private static final String ABORTION = "Abortion";
    private static final String STILLBIRTH = "Outcome_Nos";
    private static final String DEATH = "Entry_Type";
    private static final String STATE = "StateID";
    private static final String DISTRICT = "District_ID";
    private static final String TALUKA = "Taluka_ID";
    private static final String HEALTH_BLOCK = "HealthBlock_ID";
    private static final String PHC = "PHC_ID";
    private static final String SUBCENTRE = "SubCentre_ID";
    private static final String CENSUS_VILLAGE = "Village_ID";
    private static final String NON_CENSUS_VILLAGE = "SVID";

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportServiceImpl.class);

    private SubscriptionPack pregnancyPack;
    private SubscriptionPack childPack;


    @Autowired
    public MctsBeneficiaryImportServiceImpl(SubscriptionService subscriptionService,
                                            SubscriptionErrorDataService subscriptionErrorDataService,
                                            SubscriptionPackDataService subscriptionPackDataService,
                                            LocationService locationService, MctsMotherDataService mctsMotherDataService,
                                            MctsChildDataService mctsChildDataService, SubscriberService subscriberService) {
        this.subscriptionService = subscriptionService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.subscriptionPackDataService = subscriptionPackDataService;
        this.locationService = locationService;
        this.mctsMotherDataService = mctsMotherDataService;
        this.mctsChildDataService = mctsChildDataService;
        this.subscriberService = subscriberService;
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
        readHeader(bufferedReader); // ignoring header as all interesting data is in the tab separated rows

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
        DateTime motherDOB = (DateTime) record.get(MOTHER_DOB);
        Boolean abortion = (Boolean) record.get(ABORTION);
        Boolean stillBirth = (Boolean) record.get(STILLBIRTH);
        Boolean death = (Boolean) record.get(DEATH);

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), mother);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.PREGNANCY, le.getMessage()));
            return;
        }

        if (!validateLMP(lmp, msisdn)) {
            return;
        }

        mother.setName(name);
        mother.setDateOfBirth(motherDOB);

        Subscription subscription = subscriberService.updateOrCreateMctsSubscriber(mother, msisdn, lmp,
                SubscriptionPackType.PREGNANCY);

        if ((abortion != null) && abortion) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MISCARRIAGE_OR_ABORTION);
            return;
        }
        if ((stillBirth != null) && stillBirth) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.STILL_BIRTH);
            return;
        }
        if ((death != null) && death) {
            subscriptionService.deactivateSubscription(subscription, DeactivationReason.MATERNAL_DEATH);
        }

    }

    private void importChildRecord(Map<String, Object> record) {
        MctsChild child = (MctsChild) record.get(BENEFICIARY_ID);
        String name = (String) record.get(BENEFICIARY_NAME);
        Long msisdn = (Long) record.get(MSISDN);
        MctsMother mother = (MctsMother) record.get(MOTHER_ID);
        DateTime dob = (DateTime) record.get(DOB);
        Boolean death = (Boolean) record.get(DEATH);

        // validate and set location
        try {
            MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), child);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());
            subscriptionErrorDataService.create(new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.CHILD, le.getMessage()));
            return;
        }

        if (!validateDOB(dob, msisdn)) {
            return;
        }

        child.setName(name);
        child.setMother(mother);

        Subscription childSubscription = subscriberService.updateOrCreateMctsSubscriber(child, msisdn, dob,
                SubscriptionPackType.CHILD);

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

        if ((death != null) && death) {
            subscriptionService.deactivateSubscription(childSubscription, DeactivationReason.CHILD_DEATH);
        }

    }

    private boolean validateLMP(DateTime lmp, Long msisdn) {
        if (lmp == null) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, SubscriptionRejectionReason.MISSING_LMP, SubscriptionPackType.PREGNANCY));
            return false;
        }
        if (!pregnancyPack.isReferenceDateValidForPack(lmp)) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_LMP, SubscriptionPackType.PREGNANCY));
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
        if (!childPack.isReferenceDateValidForPack(dob)) {
            subscriptionErrorDataService.create(
                    new SubscriptionError(msisdn, SubscriptionRejectionReason.INVALID_DOB, SubscriptionPackType.CHILD));
            return false;
        }

        return true;
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
        mapping.put(NON_CENSUS_VILLAGE, new Optional(new GetLong()));

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
        mapping.put(MSISDN, MctsBeneficiaryUtils.MSISDN_BY_STRING);
        mapping.put(LMP, new Optional(MctsBeneficiaryUtils.DATE_BY_STRING));
        mapping.put(MOTHER_DOB, new Optional(MctsBeneficiaryUtils.DATE_BY_STRING));
        mapping.put(ABORTION, new Optional(new GetInstanceByString<Boolean>() {
             @Override
             public Boolean retrieve(String value) {
                 String trimmedValue = value.trim();
                 return "Spontaneous".equals(trimmedValue) || "MTP<12 Weeks".equals(trimmedValue) ||
                         "MTP>12 Weeks".equals(trimmedValue); // "None" or blank indicates no abortion/miscarriage
             }
        }));
        mapping.put(STILLBIRTH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return "0".equals(value.trim()); // This column indicates the number of live births that resulted from this pregnancy.
                                                 // 0 implies stillbirth, other values (including blank) do not.
            }
        }));
        mapping.put(DEATH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return "9".equals(value.trim()); // 9 indicates beneficiary death; other values do not
            }
        }));

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
        mapping.put(MSISDN, MctsBeneficiaryUtils.MSISDN_BY_STRING);
        mapping.put(DOB, new Optional(MctsBeneficiaryUtils.DATE_BY_STRING));
        mapping.put(DEATH, new Optional(new GetInstanceByString<Boolean>() {
            @Override
            public Boolean retrieve(String value) {
                return "9".equals(value.trim()); // 9 indicates beneficiary death; other values do not
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

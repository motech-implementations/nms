package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
import org.motechproject.nms.region.domain.District;
import org.motechproject.nms.region.domain.HealthBlock;
import org.motechproject.nms.region.domain.HealthFacility;
import org.motechproject.nms.region.domain.Language;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
import org.motechproject.nms.region.repository.LanguageDataService;
import org.motechproject.nms.region.repository.StateDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the {@link MctsBeneficiaryUpdateService} interface.
 */
@Service("mctsBeneficiaryUpdateService")
public class MctsBeneficiaryUpdateServiceImpl implements MctsBeneficiaryUpdateService {

    @Autowired
    private SubscriberDataService subscriberDataService;
    @Autowired
    private MctsMotherDataService mctsMotherDataService;
    @Autowired
    private MctsChildDataService mctsChildDataService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private SubscriptionDataService subscriptionDataService;
    @Autowired
    private SubscriberService subscriberService;
    @Autowired
    private StateDataService stateDataService;
    @Autowired
    private LanguageDataService languageDataService;

    public static final String MCTS_ID = "MCTS ID";
    public static final String STATE_MCTS_ID = "STATE ID";
    public static final String NEW_DOB = "Beneficiary New DOB change";
    public static final String NEW_LMP = "Beneficiary New LMP change";
    public static final String NEW_STATE = "State_ID";
    public static final String NEW_DISTRICT = "District_ID";
    public static final String NEW_TALUKA = "Taluka_ID";
    public static final String NEW_HEALTH_BLOCK = "HealthBlock_ID";
    public static final String NEW_PHC = "PHC_ID";
    public static final String NEW_SUBCENTRE = "SubCentre_ID";
    public static final String NEW_VILLAGE = "Village_ID";
    public static final String NEW_GPVILLAGE = "GP_Village";
    public static final String NEW_ADDRESS = "Address";
    public static final String NEW_MSISDN = "Beneficiary New Mobile no change";
    public static final String NEW_LANGUAGE = "Beneficiary New Language change";

    @Override
    public void updateMsisdn(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setPreferences(CsvPreference.STANDARD_PREFERENCE)
                .createAndOpen(bufferedReader);
        try {
            Map<String, Object> record;
            while (null != (record = csvImporter.read())) {

                processRecord(record);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        } catch (NumberFormatException e) {
            throw new CsvImportDataException(createErrorMessage("Invalid number", csvImporter.getRowNumber()), e);
        }
    }

    private void processRecord(Map<String, Object> record) {

        String mctsId = (String) record.get(MCTS_ID);
        String stateMctsId = (String) record.get(STATE_MCTS_ID);

        MctsBeneficiary beneficiary = beneficiaryFromId(mctsId);

        // TODO: also lookup by state id

        if (beneficiary == null) {
            throw new CsvImportDataException(String.format("Unable to locate MCTS beneficiary: %s(%s)", MCTS_ID, mctsId));
        }
        SubscriptionPackType packType = beneficiary instanceof MctsMother ? SubscriptionPackType.PREGNANCY :
                SubscriptionPackType.CHILD;

        State newState = (State) record.get(NEW_STATE);
        Long newDistrict = (Long) record.get(NEW_DISTRICT);
        String newTaluka = (String) record.get(NEW_TALUKA);
        Long newHealthBlock = (Long) record.get(NEW_HEALTH_BLOCK);
        Long newPhc = (Long) record.get(NEW_PHC);
        Long newVillage = (Long) record.get(NEW_VILLAGE);
        Long newMsisdn = (Long) record.get(NEW_MSISDN);
        Language newLanguage = (Language) record.get(NEW_LANGUAGE);
        DateTime newLmp = (DateTime) record.get(NEW_LMP);
        DateTime newDob = (DateTime) record.get(NEW_DOB);

        Subscriber subscriber = subscriberFromBeneficiary(beneficiary);
        if (subscriber == null) {
            // potentially create one?
        }

        // if subscriber is null (and we have the appropriate info), or we have a new reference date, call
        // processSubscriptionForBeneficiary (move it to base class)

        // update location fields for beneficiary if they are updated (this should also be a common method across updates / imports)

        processDateUpdate(subscriber, packType, newLmp, newDob);
        processLocationUpdate(beneficiary, newState, newDistrict, newTaluka, newHealthBlock, newPhc, newVillage);

        if (newMsisdn != null) {
            subscriberService.updateMsisdnForSubscriber(subscriber, beneficiary, newMsisdn); // roll this logic into the subscriber update method?
        }
        if (newLanguage != null) {
            subscriber.setLanguage(newLanguage);
            subscriberService.update(subscriber);
        }

    }

    private void processDateUpdate(Subscriber subscriber, SubscriptionPackType packType, DateTime newLmp, DateTime newDob) {
        if (newLmp != null) {
            if ((packType == SubscriptionPackType.CHILD) || (newDob != null)) {
                // throw
            }

        }
        if (newDob != null) {
            if ((packType == SubscriptionPackType.PREGNANCY) || (newLmp != null)) {
                // throw
            }

        }

    }

    private void processLocationUpdate(MctsBeneficiary beneficiary, State newState, Long newDistrictCode,
                                       String newTalukaCode, Long newHealthBlockCode, Long newPhcCode,
                                       Long newVillageCode) {
        if ((newState == null) || (newDistrictCode == null)) {
            // throw
        }

        // call new location hierarchy validation API from Koshal

    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(MCTS_ID, new Optional(new GetString()));
        mapping.put(STATE_MCTS_ID, new Optional(new GetString()));
        mapping.put(NEW_DOB, new Optional(getDateByString));
        mapping.put(NEW_LMP, new Optional(getDateByString));

        mapping.put(NEW_STATE, new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                State state = stateDataService.findByCode(value);
                verify(null != state, "State does not exist");
                return state;
            }
        });

        mapping.put(NEW_DISTRICT, new Optional(new GetLong()));
        mapping.put(NEW_TALUKA, new Optional(new GetString()));
        mapping.put(NEW_HEALTH_BLOCK, new Optional(new GetLong()));
        mapping.put(NEW_PHC, new Optional(new GetLong()));
        mapping.put(NEW_SUBCENTRE, new Optional(new GetLong()));
        mapping.put(NEW_VILLAGE, new Optional(new GetLong()));

        mapping.put(NEW_MSISDN, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                if (value.length() < 10) {
                    throw new NumberFormatException(String.format("%s too short, must be at least 10 digits", NEW_MSISDN));
                }
                String msisdn = value.substring(value.length() - 10);

                return Long.parseLong(msisdn);
            }
        }));

        mapping.put(NEW_LANGUAGE, new Optional(new GetInstanceByString<Language>() {
            @Override
            public Language retrieve(String value) {
                if (value == null) {
                    return null;
                }
                return languageDataService.findByCode(value);
            }
        }));

        return mapping;
    }


    private GetInstanceByString<DateTime> getDateByString = new GetInstanceByString<DateTime>() {
        @Override
        public DateTime retrieve(String value) {
            if (value == null) {
                return null;
            }

            DateTime referenceDate;

            try {
                DateTimeParser[] parsers = {
                        DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                        DateTimeFormat.forPattern("dd/MM/yyyy").getParser()};
                DateTimeFormatter formatter = new DateTimeFormatterBuilder().append(null, parsers).toFormatter();

                referenceDate = formatter.parseDateTime(value);

            } catch (IllegalArgumentException e) {
                throw new CsvImportDataException(String.format("Reference date %s is invalid", value), e);
            }

            return referenceDate;
        }
    };


    private MctsBeneficiary beneficiaryFromId(String mctsId) {
        if (mctsId == null) {
            return null;
        }

        MctsBeneficiary beneficiary = mctsMotherDataService.findByBeneficiaryId(mctsId);
        if (beneficiary == null) {
            beneficiary = mctsChildDataService.findByBeneficiaryId(mctsId);
        }

        return beneficiary;
    }

    private Subscriber subscriberFromBeneficiary(MctsBeneficiary beneficiary) {
        return subscriberService.getSubscriberByBeneficiaryId(beneficiary.getBeneficiaryId());
    }

    private String createErrorMessage(String message, int rowNumber) {
        return String.format("CSV instance error [row: %d]: %s", rowNumber, message);
    }

    private String createErrorMessage(Set<ConstraintViolation<?>> violations, int rowNumber) {
        return String.format("CSV instance error [row: %d]: validation failed for instance of type %s, violations: %s",
                rowNumber, MctsBeneficiary.class.getName(), ConstraintViolationUtils.toString(violations));
    }

    private void verify(boolean condition, String message, String... args) {
        if (!condition) {
            throw new CsvImportDataException(String.format(message, args));
        }
    }

}

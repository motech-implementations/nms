package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.ConstraintViolationUtils;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.Subscriber;
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
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.domain.Taluka;
import org.motechproject.nms.region.domain.Village;
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
                .setProcessorMapping(getMsisdnProcessorMapping())
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

        DateTime newLmp = (DateTime) record.get(NEW_LMP);
        DateTime newDob = (DateTime) record.get(NEW_DOB);
        State newState = (State) record.get(NEW_STATE);
        District newDistrict = (District) record.get(NEW_DISTRICT);
        Taluka newTaluka = (Taluka) record.get(NEW_TALUKA);
        HealthBlock newHealthBlock = (HealthBlock) record.get(NEW_HEALTH_BLOCK);
        HealthFacility newPhc = (HealthFacility) record.get(NEW_PHC);
        Village newVillage = (Village) record.get(NEW_VILLAGE);
        Long newMsisdn = (Long) record.get(NEW_MSISDN);
        String newLanguage = (String) record.get(NEW_LANGUAGE);

        Subscriber subscriber = subscriberFromBeneficiary(beneficiary);
        if (subscriber == null) {
            // potentially create one?
        }

        if (newLmp != null) {

        }
        if (newDob != null) {

        }
        if ((newState != null) || (newDistrict != null) || (newTaluka != null) || (newHealthBlock != null) ||
                (newPhc != null) || (newVillage != null)) {

        }
        if (newMsisdn != null) {
            subscriberService.updateMsisdnForSubscriber(subscriber, beneficiary, newMsisdn);
        }
        if (newLanguage != null) {

        }

    }

    @Override
    public void updateReferenceDate(Reader reader) throws IOException {

    }

    @Override
    public void updateAddress(Reader reader) throws IOException {

    }


    private Map<String, CellProcessor> getMsisdnProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(MCTS_ID, new Optional(new GetString()));

        mapping.put(NEW_MSISDN, new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                if (value.length() < 10) {
                    throw new NumberFormatException(String.format("%s too short, must be at least 10 digits", NEW_MSISDN));
                }
                String msisdn = value.substring(value.length() - 10);

                return Long.parseLong(msisdn);
            }
        });

        return mapping;
    }

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


}

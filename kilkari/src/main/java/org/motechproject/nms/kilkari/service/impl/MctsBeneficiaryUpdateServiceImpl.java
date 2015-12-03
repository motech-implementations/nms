package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
 * Implementation of the {@link MctsBeneficiaryUpdateService} interface.
 */
@Service("mctsBeneficiaryUpdateService")
public class MctsBeneficiaryUpdateServiceImpl implements MctsBeneficiaryUpdateService {

    private SubscriberService subscriberService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private LocationService locationService;
    private MctsMotherDataService mctsMotherDataService;
    private MctsChildDataService mctsChildDataService;
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;

    private static final String SR_NO = "Sr No";
    private static final String MCTS_ID = "MCTS ID";
    private static final String STATE_MCTS_ID = "STATE ID";
    private static final String DOB = "Beneficiary New DOB change";
    private static final String LMP = "Beneficiary New LMP change";
    private static final String MSISDN = "Beneficiary New Mobile no change";

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryUpdateServiceImpl.class);

    @Autowired
    public MctsBeneficiaryUpdateServiceImpl(SubscriberService subscriberService,
                                            SubscriptionErrorDataService subscriptionErrorDataService,
                                            LocationService locationService, MctsMotherDataService mctsMotherDataService,
                                            MctsChildDataService mctsChildDataService, MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor) {
        this.subscriberService = subscriberService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.locationService = locationService;
        this.mctsMotherDataService = mctsMotherDataService;
        this.mctsChildDataService = mctsChildDataService;
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
    }


    @Override
    public void updateBeneficiaryData(Reader reader) throws IOException {
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
            throw new CsvImportDataException(MctsBeneficiaryUtils.createErrorMessage(
                    e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        } catch (NumberFormatException e) {
            throw new CsvImportDataException(MctsBeneficiaryUtils.createErrorMessage(
                    "Invalid number", csvImporter.getRowNumber()), e);
        }
    }

    /**
     * This method processes one CSV row (corresponding to an MCTS beneficiary) and potentially makes several types of
     * updates based on the fields provided:
     *
     *  - Location update - if new location fields are provided, validate them and update the beneficiary.
     *
     *  - Reference date update - if a new reference date (DOB/LMP) is provided, the status of the beneficiary's
     *    subscription may change. Check for subscription state based on this new date and make the necessary updates
     *    to the subscriber/subscription. This could result in an existing subscription being deactivated or an inactive
     *    subscription being reactivated.
     *
     *  - MSISDN update - if a new MSISDN is provided, update the subscriber unless the same MSISDN is already in use
     *    for the same subscription pack. If the MSISDN is in use for a *different* subscription pack, the update is
     *    allowed.
     *
     * @param record Data fields corresponding to one row of the CSV file
     */
    private void processRecord(Map<String, Object> record) { //NO CHECKSTYLE CyclomaticComplexity

        // First, find the subscriber corresponding to the beneficiary and determine its type (mother/child)

        String mctsId = (String) record.get(MCTS_ID);
        String stateMctsId = (String) record.get(STATE_MCTS_ID);
        Long newMsisdn = (Long) record.get(MSISDN);

        MctsBeneficiary beneficiary = beneficiaryFromId(mctsId, stateMctsId);
        if (beneficiary == null) {
            throw new CsvImportDataException(String.format("Unable to locate MCTS beneficiary: %s(%s), %s(%s)", MCTS_ID,
                    mctsId, STATE_MCTS_ID, stateMctsId));
        }

        Subscriber subscriber = subscriberService.getSubscriberByBeneficiary(beneficiary);
        SubscriptionPackType packType;
        DateTime newReferenceDate;

        if (beneficiary instanceof MctsMother) {
            packType = SubscriptionPackType.PREGNANCY;
            newReferenceDate = (DateTime) record.get(LMP);
        } else {
            packType = SubscriptionPackType.CHILD;
            newReferenceDate = (DateTime) record.get(DOB);
        }

        // Second, update the beneficiary's location if new location fields are provided

        if (containsLocationUpdate(record)) {
            // validate and set location
            try {
                MctsBeneficiaryUtils.setLocationFields(locationService.getLocations(record), beneficiary);
            } catch (InvalidLocationException le) {
                LOGGER.error(le.toString());
                subscriptionErrorDataService.create(new SubscriptionError(
                        beneficiary.getBeneficiaryId(),
                        SubscriptionRejectionReason.INVALID_LOCATION,
                        packType,
                        le.getMessage()));
               return;
            }
        }

        // Third, update the beneficiary's subscription date/status based on the new reference date if provided

        // if subscriber is null (and we have the necessary info), or we have a new reference date, create/update the
        // subscription for this beneficiary
        if ((subscriber == null) && (newMsisdn != null) && (newReferenceDate != null)) {
            // create a new subscription if the beneficiary's LMP/DOB indicates that a subscription should be created

            if (packType == SubscriptionPackType.PREGNANCY) {
                subscriberService.updateMotherSubscriber(newMsisdn, (MctsMother) beneficiary, newReferenceDate);
            } else {
                subscriberService.updateChildSubscriber(newMsisdn, (MctsChild) beneficiary, newReferenceDate);
            }
        }
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(SR_NO, new Optional(new GetString()));
        mapping.put(MCTS_ID, new Optional(new GetString()));
        mapping.put(STATE_MCTS_ID, new Optional(new GetString()));
        mapping.put(DOB, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
            }
        }));
        mapping.put(LMP, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
            }
        }));
        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        mapping.put(MSISDN, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));

        return mapping;
    }

    private MctsBeneficiary beneficiaryFromId(String mctsId, String stateMctsId) {
        if ((mctsId == null) && (stateMctsId == null)) {
            return null;
        }

        // try the MCTS ID first
        MctsBeneficiary beneficiary = mctsMotherDataService.findByBeneficiaryId(mctsId);
        if (beneficiary == null) {
            beneficiary = mctsChildDataService.findByBeneficiaryId(mctsId);
        }

        // then try the State ID
        if (beneficiary == null) {
            beneficiary = mctsMotherDataService.findByBeneficiaryId(stateMctsId);
        }
        if (beneficiary == null) {
            beneficiary = mctsChildDataService.findByBeneficiaryId(stateMctsId);
        }

        return beneficiary;
    }

    private boolean containsLocationUpdate(Map<String, Object> record) {
        return ((record.get(KilkariConstants.STATE_ID) != null) || (record.get(KilkariConstants.DISTRICT_ID) != null) || (record.get(KilkariConstants.TALUKA_ID) != null) || //NO CHECKSTYLE BooleanExpressionComplexity
                (record.get(KilkariConstants.HEALTH_BLOCK_ID) != null) || (record.get(KilkariConstants.PHC_ID) != null) || (record.get(KilkariConstants.SUB_CENTRE_ID) != null) ||
                (record.get(KilkariConstants.CENSUS_VILLAGE_ID) != null));
    }

}

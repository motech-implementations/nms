package org.motechproject.nms.kilkari.csv.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByLong;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionError;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.domain.SubscriptionRejectionReason;
import org.motechproject.nms.kilkari.csv.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriptionErrorDataService;
import org.motechproject.nms.kilkari.service.SubscriberService;
import org.motechproject.nms.region.domain.State;
import org.motechproject.nms.region.exception.InvalidLocationException;
import org.motechproject.nms.region.repository.StateDataService;
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
public class MctsBeneficiaryUpdateServiceImpl extends BaseMctsBeneficiaryService implements MctsBeneficiaryUpdateService {

    private StateDataService stateDataService;
    private SubscriberService subscriberService;
    private SubscriptionErrorDataService subscriptionErrorDataService;
    private LocationService locationService;
    private MctsMotherDataService mctsMotherDataService;
    private MctsChildDataService mctsChildDataService;

    private static final String MCTS_ID = "MCTS ID";
    private static final String STATE_MCTS_ID = "STATE ID";
    private static final String DOB = "Beneficiary New DOB change";
    private static final String LMP = "Beneficiary New LMP change";
    private static final String MSISDN = "Beneficiary New Mobile no change";
    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryUpdateServiceImpl.class);

    @Autowired
    public MctsBeneficiaryUpdateServiceImpl(StateDataService stateDataService, SubscriberService subscriberService,
                                            SubscriptionErrorDataService subscriptionErrorDataService,
                                            LocationService locationService, MctsMotherDataService mctsMotherDataService,
                                            MctsChildDataService mctsChildDataService) {
        this.subscriberService = subscriberService;
        this.stateDataService = stateDataService;
        this.subscriptionErrorDataService = subscriptionErrorDataService;
        this.locationService = locationService;
        this.mctsMotherDataService = mctsMotherDataService;
        this.mctsChildDataService = mctsChildDataService;
    }


    @Override
    public void updateBeneficiary(Reader reader) throws IOException {
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

        MctsBeneficiary beneficiary = beneficiaryFromId(mctsId, stateMctsId);

        if (beneficiary == null) {
            throw new CsvImportDataException(String.format("Unable to locate MCTS beneficiary: %s(%s), %s(%s)", MCTS_ID,
                    mctsId, STATE_MCTS_ID, stateMctsId));
        }

        SubscriptionPackType packType;
        DateTime newReferenceDate;
        Subscriber subscriber = subscriberFromBeneficiary(beneficiary);

        if (beneficiary instanceof MctsMother) {
            packType = SubscriptionPackType.PREGNANCY;
            newReferenceDate = (DateTime) record.get(LMP);
        } else {
            packType = SubscriptionPackType.CHILD;
            newReferenceDate = (DateTime) record.get(DOB);
        }

        Long newMsisdn = (Long) record.get(MSISDN);

        // validate and set location
        try {
            setLocationFields(locationService.getLocations(record), beneficiary);
        } catch (InvalidLocationException le) {
            LOGGER.error(le.toString());

            subscriptionErrorDataService.create(new SubscriptionError(
                    (subscriber == null) ? newMsisdn : subscriber.getCallingNumber(),
                    SubscriptionRejectionReason.INVALID_LOCATION,
                    SubscriptionPackType.CHILD,
                    le.getMessage()));
            return;
        }

        // if subscriber is null (and we have the necessary info), or we have a new reference date, create/update the
        // subscription for this beneficiary
        if ((subscriber == null) && (newMsisdn != null) && (newReferenceDate != null)) {
            // create a new subscription if the beneficiary's LMP/DOB indicates that a subscription should be created
            subscriberService.updateOrCreateMctsSubscriber(beneficiary, newMsisdn, newReferenceDate, packType);
            return;
        } else if (newReferenceDate != null) {
            subscriberService.updateOrCreateMctsSubscriber(beneficiary, subscriber.getCallingNumber(), newReferenceDate, packType);
        }

        if (newMsisdn != null) {
            subscriberService.updateMsisdnForSubscriber(subscriber, beneficiary, newMsisdn); // roll this logic into the subscriber update method?
        }

    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        mapping.put(MCTS_ID, new Optional(new GetString()));
        mapping.put(STATE_MCTS_ID, new Optional(new GetString()));
        mapping.put(DOB, new Optional(getDateProcessor()));
        mapping.put(LMP, new Optional(getDateProcessor()));

        mapping.put(STATE, new GetInstanceByLong<State>() {
            @Override
            public State retrieve(Long value) {
                State state = stateDataService.findByCode(value);
                verify(null != state, "State does not exist");
                return state;
            }
        });

        mapping.put(DISTRICT, new Optional(new GetLong()));
        mapping.put(TALUKA, new Optional(new GetString()));
        mapping.put(HEALTH_BLOCK, new Optional(new GetLong()));
        mapping.put(PHC, new Optional(new GetLong()));
        mapping.put(SUBCENTRE, new Optional(new GetLong()));
        mapping.put(CENSUS_VILLAGE, new Optional(new GetLong()));

        mapping.put(MSISDN, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                if (value.length() < 10) {
                    throw new NumberFormatException(String.format("%s too short, must be at least 10 digits", MSISDN));
                }
                String msisdn = value.substring(value.length() - 10);

                return Long.parseLong(msisdn);
            }
        }));

        return mapping;
    }


    private MctsBeneficiary beneficiaryFromId(String mctsId, String stateMctsId) {
        if (mctsId == null) {
            return null;
        }

        MctsBeneficiary beneficiary = mctsMotherDataService.findByBeneficiaryId(mctsId);
        if (beneficiary == null) {
            beneficiary = mctsChildDataService.findByBeneficiaryId(mctsId);

            if (beneficiary == null) {
                beneficiary = mctsChildDataService.findByBeneficiaryId(stateMctsId);
            }
        }

        return beneficiary;
    }

    private Subscriber subscriberFromBeneficiary(MctsBeneficiary beneficiary) {
        return subscriberService.getSubscriberByBeneficiaryId(beneficiary.getBeneficiaryId());
    }


}

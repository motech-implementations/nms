package org.motechproject.nms.kilkari.service.impl;

import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.Subscriber;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.repository.SubscriberDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.SubscriptionService;
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

    @Autowired
    private SubscriberDataService subscriberDataService;
    @Autowired
    private MctsMotherDataService mctsMotherDataService;
    @Autowired
    private MctsChildDataService mctsChildDataService;
    @Autowired
    private SubscriptionService subscriptionService;

    public static final String MCTS_ID = "FLW ID";
    public static final String NEW_MSISDN = "NEW MSISDN";


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

                Long msisdn = (Long) record.get(NEW_MSISDN);
                if (msisdn == null) {
                    // throw
                }

                MctsBeneficiary beneficiary = beneficiaryFromRecord(record);
                if (beneficiary == null) {
                    // throw
                }

                Subscriber subscriber = subscriberFromBeneficiary(beneficiary);
                if (subscriber == null) {
                    // throw
                }

                updateMsisdnForSubscriber(subscriber, beneficiary, msisdn);
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(createErrorMessage(e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        } catch (NumberFormatException e) {
            throw new CsvImportDataException(createErrorMessage("Invalid number", csvImporter.getRowNumber()), e);
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

    private void updateMsisdnForSubscriber(Subscriber subscriber, MctsBeneficiary beneficiary, Long newMsisdn) {

        SubscriptionPackType packType;
        packType = (beneficiary instanceof MctsChild) ? SubscriptionPackType.CHILD : SubscriptionPackType.PREGNANCY;

        Subscriber subscriberWithMsisdn = subscriberDataService.findByCallingNumber(newMsisdn);
        if (subscriberWithMsisdn != null) {
            // this number is already in use
            if (subscriptionService.)
        }
    }

    private MctsBeneficiary beneficiaryFromRecord(Map<String, Object> record) {
        String mctsId = (String) record.get(MCTS_ID);
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
        if (beneficiary instanceof MctsChild) {
            return subscriberDataService.findByMctsChild((MctsChild) beneficiary);
        } else if (beneficiary instanceof MctsMother) {
            return subscriberDataService.findByMctsMother((MctsMother) beneficiary);
        }
        return null;
    }

}

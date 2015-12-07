package org.motechproject.nms.kilkari.service.impl;

import org.joda.time.DateTime;
import org.motechproject.nms.csv.exception.CsvImportDataException;
import org.motechproject.nms.csv.utils.CsvImporterBuilder;
import org.motechproject.nms.csv.utils.CsvMapImporter;
import org.motechproject.nms.csv.utils.GetInstanceByString;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.kilkari.domain.MctsBeneficiary;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryUpdateService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
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

    private MctsMotherDataService mctsMotherDataService;
    private MctsChildDataService mctsChildDataService;
    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;

    private static final String SR_NO = "Sr No";
    private static final String MCTS_ID = "MCTS ID";
    private static final String STATE_MCTS_ID = "STATE ID";
    private static final String DOB = "Beneficiary New DOB change";
    private static final String LMP = "Beneficiary New LMP change";
    private static final String MSISDN = "Beneficiary New Mobile no change";

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryUpdateServiceImpl.class);

    @Autowired
    public MctsBeneficiaryUpdateServiceImpl(MctsMotherDataService mctsMotherDataService,
                                            MctsChildDataService mctsChildDataService,
                                            MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor,
                                            MctsBeneficiaryImportService mctsBeneficiaryImportService) {

        this.mctsMotherDataService = mctsMotherDataService;
        this.mctsChildDataService = mctsChildDataService;
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
        this.mctsBeneficiaryImportService = mctsBeneficiaryImportService;
    }


    @Override
    public void updateBeneficiaryData(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(getProcessorMapping())
                .setPreferences(CsvPreference.STANDARD_PREFERENCE)
                .createAndOpen(bufferedReader);
        int importCount = 0;
        try {
            Map<String, Object> record;
            while (null != (record = csvImporter.read())) {
                if (processRecord(record)) {
                    importCount++;
                }
            }
        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(MctsBeneficiaryUtils.createErrorMessage(
                    e.getConstraintViolations(), csvImporter.getRowNumber()), e);
        } catch (NumberFormatException e) {
            throw new CsvImportDataException(MctsBeneficiaryUtils.createErrorMessage(
                    "Invalid number", csvImporter.getRowNumber()), e);
        }
        LOGGER.info("Imported {} beneficiaries", importCount);
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
     * @return true if the record was successfully updated
     */
    private boolean processRecord(Map<String, Object> record) { //NO CHECKSTYLE CyclomaticComplexity

        // Step 1: find the beneficiary
        String mctsId = (String) record.get(MCTS_ID);
        String stateMctsId = (String) record.get(STATE_MCTS_ID);

        MctsBeneficiary beneficiary = beneficiaryFromId(mctsId, stateMctsId);
        if (beneficiary == null) {
            throw new CsvImportDataException(String.format("Unable to locate MCTS beneficiary: %s(%s), %s(%s)", MCTS_ID,
                    mctsId, STATE_MCTS_ID, stateMctsId));
        }

        // Step 1.5: remap the headers to use standard field names from MCTS
        mapUpdateHeaders(record, beneficiary);

        // Step 2: Re-route and call the import service for the update
        return (beneficiary instanceof MctsMother) ?
                mctsBeneficiaryImportService.importMotherRecord(record) :
                mctsBeneficiaryImportService.importChildRecord(record);
    }

    private Map<String, Object> mapUpdateHeaders(Map<String, Object> updates, MctsBeneficiary beneficiary) {
        if (updates.containsKey(MSISDN)) {
            updates.put(KilkariConstants.MSISDN, updates.get(MSISDN));
        }

        if (updates.containsKey(DOB)) {
            updates.put(KilkariConstants.DOB, updates.get(DOB));
        }

        if (updates.containsKey(LMP)) {
            updates.put(KilkariConstants.LMP, updates.get(LMP));
        }

        if (updates.containsKey(MCTS_ID)) {
            updates.put(KilkariConstants.BENEFICIARY_ID, beneficiary);
        }

        return updates;
    }

    private Map<String, CellProcessor> getProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);

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

        // then try the State ID // What on earth is a stateMctsId??
        if (beneficiary == null) {
            beneficiary = mctsMotherDataService.findByBeneficiaryId(stateMctsId);
        }
        if (beneficiary == null) {
            beneficiary = mctsChildDataService.findByBeneficiaryId(stateMctsId);
        }

        return beneficiary;
    }
}

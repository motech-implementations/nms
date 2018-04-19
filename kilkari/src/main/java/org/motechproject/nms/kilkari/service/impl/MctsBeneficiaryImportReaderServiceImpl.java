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
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.RejectionReasons;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.kilkari.utils.MctsBeneficiaryUtils;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.nms.rejectionhandler.domain.ChildImportRejection;
import org.motechproject.nms.rejectionhandler.service.ChildRejectionService;
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

import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.childRejectionRch;
import static org.motechproject.nms.kilkari.utils.RejectedObjectConverter.convertMapToRchChild;

@Service("mctsBeneficiaryImportReaderService")
public class MctsBeneficiaryImportReaderServiceImpl implements MctsBeneficiaryImportReaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportReaderServiceImpl.class);

    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;
    private LocationService locationService;

    @Autowired
    private ChildRejectionService childRejectionService;


    @Autowired
    public MctsBeneficiaryImportReaderServiceImpl(MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor, MctsBeneficiaryImportService mctsBeneficiaryImportService, LocationService locationService) {
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
        this.mctsBeneficiaryImportService = mctsBeneficiaryImportService;
        this.locationService = locationService;
    }

    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public int  importChildData(Reader reader, SubscriptionOrigin importOrigin) throws IOException { //NOPMD NcssMethodCount
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
        String childInstance;
        Boolean mctsImport = importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT);

        if (mctsImport) {
            cellProcessorMapper = this.getChildProcessorMapping();
            id = KilkariConstants.BENEFICIARY_ID;
            contactNumber = KilkariConstants.MSISDN;
            childInstance = KilkariConstants.MCTS_CHILD;
        } else {
            cellProcessorMapper = this.getRchChildProcessorMapping();
            id = KilkariConstants.RCH_ID;
            contactNumber = KilkariConstants.MOBILE_NO;
            childInstance = KilkariConstants.RCH_CHILD;
        }

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(cellProcessorMapper)
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        LocationFinder locationFinder = new LocationFinder();
        locationService.updateLocations(csvImporter, locationFinder);

        bufferedReader = new BufferedReader(reader);

        CsvMapImporter csvImporter1 = new CsvImporterBuilder()
                .setProcessorMapping(cellProcessorMapper)
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        try {
            Map<String, Object> record;
            Map<String, Object> rejectedChilds = new HashMap<>();
            Map<String, Object> rejectionStatus = new HashMap<>();
            ChildImportRejection childImportRejection;

            Timer timer = new Timer("kid", "kids");
            while (null != (record = csvImporter1.read())) {
                count++;
                LOGGER.debug("Started child import for msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id));

                MctsChild child = mctsImport ? mctsBeneficiaryValueProcessor.getOrCreateChildInstance((String) record.get(id)) : mctsBeneficiaryValueProcessor.getOrCreateRchChildInstance((String) record.get(id), (String) record.get(KilkariConstants.MCTS_ID));
                // TODO: Add this to bulk insert
                if (child == null) {
                    childRejectionService.createOrUpdateChild(childRejectionRch(convertMapToRchChild(record), false, RejectionReasons.DATA_INTEGRITY_ERROR.toString(), KilkariConstants.CREATE));
                    LOGGER.error("RchId is empty while importing child at msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id));
                    rejectedWithException++;
                    continue;
                }

                String action = (child.getId() == null) ? KilkariConstants.CREATE : KilkariConstants.UPDATE;
                record.put(KilkariConstants.ACTION, action);
                record.put(childInstance, child);

                try {
                    childImportRejection = mctsBeneficiaryImportService.importChildRecordCSV(record, importOrigin, locationFinder);
                    if (childImportRejection != null) {
                        if (mctsImport) {
                            rejectedChilds.put(childImportRejection.getIdNo(), childImportRejection);
                            rejectionStatus.put(childImportRejection.getIdNo(), childImportRejection.getAccepted());
                        } else {
                            rejectedChilds.put(childImportRejection.getRegistrationNo(), childImportRejection);
                            rejectionStatus.put(childImportRejection.getRegistrationNo(), childImportRejection.getAccepted());
                        }
                    }
                    if (count % KilkariConstants.PROGRESS_INTERVAL == 0) {
                        LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
                    }
                } catch (RuntimeException e) {
                    LOGGER.error("Error while importing child at msisdn {} beneficiary_id {}", record.get(contactNumber), record.get(id), e);
                    rejectedWithException++;
                }
            }

            try {
                if (mctsImport) {
                    mctsBeneficiaryImportService.createOrUpdateMctsRejections(rejectedChilds , rejectionStatus);
                } else {
                    mctsBeneficiaryImportService.createOrUpdateRchRejections(rejectedChilds , rejectionStatus);
                }
            } catch (RuntimeException e) {
                LOGGER.error("Error while bulk updating rejection records", e);

            }

            LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(count));
            LOGGER.debug(KilkariConstants.REJECTED, timer.frequency(rejectedWithException));

        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("Child import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }

        return count;
    }

    private Map<String, CellProcessor> getChildProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();
        getChildMapping(mapping);

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);

        mapping.put(KilkariConstants.BENEFICIARY_ID, new Optional(new GetString()));
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
        getChildMapping(mapping);

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);

        mapping.put(KilkariConstants.MCTS_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MCTS_MOTHER_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.MOBILE_NO, new Optional(new GetInstanceByString<Long>() {
            @Override
            public Long retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getMsisdnByString(value);
            }
        }));
        mapping.put(KilkariConstants.RCH_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.RCH_MOTHER_ID, new Optional(new GetString()));
        mapping.put(KilkariConstants.EXECUTION_DATE, new Optional(new GetInstanceByString<LocalDate>() {
            @Override
            public LocalDate retrieve(String value) {
                return (LocalDate) mctsBeneficiaryValueProcessor.getDateByString(value).toLocalDate();
            }
        }));

        return mapping;
    }

    private void getChildMapping(Map<String, CellProcessor> mapping) {
        mapping.put(KilkariConstants.BENEFICIARY_NAME, new Optional(new GetString()));
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


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
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.domain.ThreadProcessorObject;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.service.ChildCsvThreadProcessor;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryValueProcessor;
import org.motechproject.nms.kilkari.service.MotherCsvThreadProcessor;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.kilkari.utils.MctsBeneficiaryUtils;
import org.motechproject.nms.region.domain.LocationFinder;
import org.motechproject.nms.region.service.LocationService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.prefs.CsvPreference;

import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service("mctsBeneficiaryImportReaderService")
public class MctsBeneficiaryImportReaderServiceImpl implements MctsBeneficiaryImportReaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MctsBeneficiaryImportReaderServiceImpl.class);

    //Number of records to be processed by each thread
    private static final String RECORDS_PART_SIZE = "kilkari.thread.size";

    private MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor;
    private MctsBeneficiaryImportService mctsBeneficiaryImportService;
    private LocationService locationService;
    private SettingsFacade settingsFacade;

    @Autowired
    public MctsBeneficiaryImportReaderServiceImpl(@Qualifier("kilkariSettings") SettingsFacade settingsFacade, MctsBeneficiaryValueProcessor mctsBeneficiaryValueProcessor, MctsBeneficiaryImportService mctsBeneficiaryImportService, LocationService locationService) {
        this.mctsBeneficiaryValueProcessor = mctsBeneficiaryValueProcessor;
        this.mctsBeneficiaryImportService = mctsBeneficiaryImportService;
        this.locationService = locationService;
        this.settingsFacade = settingsFacade;
    }

    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public int  importChildData(Reader reader, SubscriptionOrigin importOrigin) throws IOException { //NOPMD NcssMethodCount
        /**
         * Count of all the records rejected for unknown exceptions. So, doesn't include the ones saved in nms_subscription_errors.
         * This is used just for debugging purpose.
         */
        int rejectedWithException = 0;
        mctsBeneficiaryImportService.setChildRecords(0);

        BufferedReader bufferedReader = new BufferedReader(reader);
        Map<String, CellProcessor> cellProcessorMapper;
        String contactNumber;
        final Boolean mctsImport = importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT);

        if (mctsImport) {
            cellProcessorMapper = this.getChildProcessorMapping();
            contactNumber = KilkariConstants.MSISDN;
        } else {
            cellProcessorMapper = this.getRchChildProcessorMapping();
            contactNumber = KilkariConstants.MOBILE_NO;
        }

        List<Map<String, Object>> recordList = this.readCsv(bufferedReader, cellProcessorMapper);

        LocationFinder locationFinder = locationService.updateLocations(recordList);

        recordList = this.sortByMobileNumber(recordList, mctsImport);

        try {
            Map<String, Object> rejectedChilds = new HashMap<>();
            Map<String, Object> rejectionStatus = new HashMap<>();
            Timer timer = new Timer("kid", "kids");

            List<List<Map<String, Object>>> recordListArray = this.splitRecords(recordList, contactNumber);

            LOGGER.debug("Thread Processing Start");
            Integer recordsProcessed = 0;
            ExecutorService executor = Executors.newCachedThreadPool();
            List<Future<ThreadProcessorObject>> list = new ArrayList<>();

            for (int i = 0; i < recordListArray.size(); i++) {
                Callable<ThreadProcessorObject> callable = new ChildCsvThreadProcessor(recordListArray.get(i), mctsImport, importOrigin, locationFinder,
                        mctsBeneficiaryValueProcessor, mctsBeneficiaryImportService);
                Future<ThreadProcessorObject> future = executor.submit(callable);
                list.add(future);
            }

            for (Future<ThreadProcessorObject> fut : list) {
                try {
                    ThreadProcessorObject threadProcessorObject = fut.get();
                    rejectedChilds.putAll(threadProcessorObject.getRejectedBeneficiaries());
                    rejectionStatus.putAll(threadProcessorObject.getRejectionStatus());
                    recordsProcessed += threadProcessorObject.getRecordsProcessed();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Error while running thread", e);
                }
            }
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Error while Terminating thread", e);
            }
            LOGGER.debug("Thread Processing End");
            try {

                if (!rejectedChilds.isEmpty()) {
                    if (mctsImport) {
                        mctsBeneficiaryImportService.createOrUpdateMctsChildRejections(rejectedChilds, rejectionStatus);
                    } else {
                        mctsBeneficiaryImportService.createOrUpdateRchChildRejections(rejectedChilds, rejectionStatus);
                    }

                

                }
            } catch (RuntimeException e) {
                LOGGER.error("Error while bulk updating rejection records", e);

            }

            LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(recordsProcessed));
            LOGGER.debug(KilkariConstants.REJECTED, timer.frequency(rejectedWithException));

        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("Child import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }

        return recordList.size();
    }

    /**
     * Expected file format:
     * - any number of empty lines
     * - header lines in the following format:  State Name : ACTUAL STATE_ID NAME
     * - one empty line
     * - CSV data (tab-separated)
     */
    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public int importMotherData(Reader reader, SubscriptionOrigin importOrigin) throws IOException { //NOPMD NcssMethodCount
        /**
         * Count of all the records rejected for unknown exceptions. So, doesn't include the ones saved in nms_subscription_errors.
         * This is used just for debugging purpose.
         */
        int rejectedWithException = 0;
        mctsBeneficiaryImportService.setRecords(0);

        BufferedReader bufferedReader = new BufferedReader(reader);
        Map<String, CellProcessor> cellProcessorMapper;
        String contactNumber;
        final Boolean mctsImport = importOrigin.equals(SubscriptionOrigin.MCTS_IMPORT);

        if (mctsImport) {
            cellProcessorMapper = mctsBeneficiaryImportService.getMotherProcessorMapping();
            contactNumber = KilkariConstants.MSISDN;
        } else {
            cellProcessorMapper = mctsBeneficiaryImportService.getRchMotherProcessorMapping();
            contactNumber = KilkariConstants.MOBILE_NO;
        }

        List<Map<String, Object>> recordList = this.readCsv(bufferedReader, cellProcessorMapper);

        LocationFinder locationFinder = locationService.updateLocations(recordList);

        recordList = this.sortByMobileNumber(recordList, mctsImport);

        try {
            Map<String, Object> rejectedMothers = new HashMap<>();
            Map<String, Object> rejectionStatus = new HashMap<>();
            Timer timer = new Timer("mom", "moms");
            List<List<Map<String, Object>>> recordListArray = this.splitRecords(recordList, contactNumber);


            Integer recordsProcessed = 0;
            ExecutorService executor = Executors.newCachedThreadPool();
            List<Future<ThreadProcessorObject>> list = new ArrayList<>();

            for (int i = 0; i < recordListArray.size(); i++) {
                LOGGER.debug("Thread Processing Start" + i);
                Callable<ThreadProcessorObject> callable = new MotherCsvThreadProcessor(recordListArray.get(i), mctsImport, importOrigin, locationFinder,
                        mctsBeneficiaryValueProcessor, mctsBeneficiaryImportService);
                Future<ThreadProcessorObject> future = executor.submit(callable);
                list.add(future);
            }

            for (Future<ThreadProcessorObject> fut : list) {
                try {
                    ThreadProcessorObject threadProcessorObject = fut.get();
                    rejectedMothers.putAll(threadProcessorObject.getRejectedBeneficiaries());
                    rejectionStatus.putAll(threadProcessorObject.getRejectionStatus());
                    recordsProcessed += threadProcessorObject.getRecordsProcessed();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("Error while running thread", e);
                }
            }
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Error while Terminating thread", e);
            }
            LOGGER.debug("Thread Processing End" );
            try {
                if (mctsImport) {
                    mctsBeneficiaryImportService.createOrUpdateMctsMotherRejections(rejectedMothers , rejectionStatus);
                } else {
                    mctsBeneficiaryImportService.createOrUpdateRchMotherRejections(rejectedMothers , rejectionStatus);
                }
            } catch (RuntimeException e) {
                LOGGER.error("Error while bulk updating rejection records", e);

            }

            LOGGER.debug(KilkariConstants.IMPORTED, timer.frequency(recordsProcessed));
            LOGGER.debug(KilkariConstants.REJECTED, timer.frequency(rejectedWithException));

        } catch (ConstraintViolationException e) {
            throw new CsvImportDataException(String.format("Mother import error, constraints violated: %s",
                    ConstraintViolationUtils.toString(e.getConstraintViolations())), e);
        }

        return recordList.size();
    }

    @Override
    public List<List<Map<String, Object>>> splitRecords(List<Map<String, Object>> recordList, String contactNumber) {
        List<List<Map<String, Object>>> recordListArray = new ArrayList<>();
        int count = 0;
        while (count < recordList.size()) {
            List<Map<String, Object>> recordListPart = new ArrayList<>();
            int threadSize = Integer.parseInt(settingsFacade.getProperty(RECORDS_PART_SIZE));
            while (recordListPart.size() < threadSize && count < recordList.size()) {
                recordListPart.add(recordList.get(count));
                count++;
            }
            //Add all records with same contact number to the same part
            while (count < recordList.size() && (recordList.get(count).get(contactNumber) == null ? "0": recordList.get(count).get(contactNumber))
                    .equals(recordListPart.get(recordListPart.size() - 1)
                            .get(contactNumber))) {
                recordListPart.add(recordList.get(count));
                count++;
            }
            LOGGER.debug("Added records to part {}", recordListArray.size() + 1);
            recordListArray.add(recordListPart);
            LOGGER.debug("Added recordListPart to recordListArray");
        }
        LOGGER.debug("Split {} records to {} parts", recordList.size(), recordListArray.size());
        return recordListArray;
    }

    @Override
    public List<Map<String, Object>> readCsv(BufferedReader bufferedReader, Map<String, CellProcessor> cellProcessorMapper) throws IOException {
        int count = 0;

        CsvMapImporter csvImporter = new CsvImporterBuilder()
                .setProcessorMapping(cellProcessorMapper)
                .setPreferences(CsvPreference.TAB_PREFERENCE)
                .createAndOpen(bufferedReader);

        List<Map<String, Object>> recordList = new ArrayList<>();
        Map<String, Object> record;
        while (null != (record = csvImporter.read())) {
            recordList.add(record);
            count++;
        }
        LOGGER.debug("{} records added to object", count);
        return recordList;
    }

    @Override
    public List<Map<String, Object>> sortByMobileNumber(List<Map<String, Object>> recordList, final Boolean mctsImport) {
        Collections.sort(recordList, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                Object phoneM1 = m1.get(mctsImport ? KilkariConstants.MSISDN : KilkariConstants.MOBILE_NO);
                Object phoneM2 = m2.get(mctsImport ? KilkariConstants.MSISDN : KilkariConstants.MOBILE_NO);
                return ((Long) (phoneM1 == null ? 0L : phoneM1))
                        .compareTo((Long) (phoneM2 == null ? 0L : phoneM2)); //ascending order
            }
        });
        return recordList;
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

    @Override
    public Map<String, CellProcessor> getRchChildProcessorMapping() {
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
        mapping.put(KilkariConstants.CHILD_REGISTRATION_DATE, new Optional(new GetInstanceByString<DateTime>() {
            @Override
            public DateTime retrieve(String value) {
                return mctsBeneficiaryValueProcessor.getDateByString(value);
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
                LOGGER.debug("Entry type : {}", value);
                return mctsBeneficiaryValueProcessor.getDeathFromString(value);
            }
        }));
    }
}


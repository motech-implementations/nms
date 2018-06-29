package org.motechproject.nms.api.web.service.impl;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.api.web.service.BeneficiaryUpdateService;
import org.motechproject.nms.csv.utils.GetLong;
import org.motechproject.nms.csv.utils.GetString;
import org.motechproject.nms.flw.domain.FrontLineWorker;
import org.motechproject.nms.flw.repository.FrontLineWorkerDataService;
import org.motechproject.nms.kilkari.domain.MctsChild;
import org.motechproject.nms.kilkari.domain.MctsMother;
import org.motechproject.nms.kilkari.repository.MctsChildDataService;
import org.motechproject.nms.kilkari.repository.MctsMotherDataService;
import org.motechproject.nms.kilkari.service.MctsBeneficiaryImportReaderService;
import org.motechproject.nms.kilkari.utils.FlwConstants;
import org.motechproject.nms.kilkari.utils.KilkariConstants;
import org.motechproject.nms.kilkari.utils.MctsBeneficiaryUtils;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.nms.rch.domain.RchUserType;
import org.motechproject.nms.rch.service.RchWebServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.ift.CellProcessor;

import javax.jdo.Query;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by beehyv on 19/6/18.
 */
@Service("beneficiaryUpdateService")
public class BeneficiaryUpdateServiceImpl implements BeneficiaryUpdateService {

    @Autowired
    private FrontLineWorkerDataService frontLineWorkerDataService;

    @Autowired
    private MctsChildDataService mctsChildDataService;

    @Autowired
    private MctsMotherDataService mctsMotherDataService;

    @Autowired
    private RchWebServiceFacade rchWebServiceFacade;

    @Autowired
    private MctsBeneficiaryImportReaderService mctsBeneficiaryImportReaderService;

    @Autowired
    private MctsWebServiceFacade mctsWebServiceFacade;


    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String CHILD_LOG_STRING = "List of child rejects in {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT_STRING_FOR_CSV_FILE = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String MCTS = "mcts";
    private static final Integer PARTITION_SIZE = 10000;
    private static final Logger LOGGER = LoggerFactory.getLogger(BeneficiaryUpdateServiceImpl.class);

    @Transactional
    public void rchBulkUpdate(Long stateId, RchUserType rchUserType, String origin) throws IOException {

        List<MultipartFile> rchImportFiles = findByStateIdAndRchUserType(stateId, rchUserType, origin);

        Collections.sort(rchImportFiles, new Comparator<MultipartFile>() {
            public int compare(MultipartFile m1, MultipartFile m2) {
                Date file1Date;
                Date file2Date;
                int flag = 1;
                try {
                    file1Date = getDateFromFileName(m1.getOriginalFilename());
                    file2Date = getDateFromFileName(m2.getOriginalFilename());
                    flag = file1Date.compareTo(file2Date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return flag; //ascending order
            }
        });

        for (MultipartFile rchImportFile : rchImportFiles) {
            try (InputStream in = rchImportFile.getInputStream()) {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                Map<String, CellProcessor> cellProcessorMapper;
                List<Map<String, Object>> recordList;

                if (rchUserType == RchUserType.MOTHER || rchUserType == RchUserType.CHILD) {
                    cellProcessorMapper = getKilkariProcessorMapping(origin);
                } else {
                    cellProcessorMapper = getRchAshaProcessorMapping();
                }
                recordList = mctsBeneficiaryImportReaderService.readCsv(bufferedReader, cellProcessorMapper);
                Long updatedRecords = bulkUpdate(recordList, rchUserType, origin);
                LOGGER.debug("File {} processed. {} records updated", rchImportFile.getName(), updatedRecords/2);

            }
        }
    }

    private Date getDateFromFileName(String fileName) throws ParseException {
        String[] names = fileName.split("_");
        String dateString = names[5].split(".csv")[0];
        Date date = new SimpleDateFormat(DATE_FORMAT_STRING_FOR_CSV_FILE).parse(dateString);
        return date;
    }

    private List<MultipartFile> findByStateIdAndRchUserType(Long stateId, RchUserType rchUserType, String origin) throws IOException {

        ArrayList<MultipartFile> csvFilesByStateIdAndRchUserType = new ArrayList<>();
        String locUpdateDir;
        if(MCTS.equalsIgnoreCase(origin)){
            locUpdateDir = mctsWebServiceFacade.getBeneficiaryLocationUpdateDirectory();
        } else {
            locUpdateDir = rchWebServiceFacade.getBeneficiaryLocationUpdateDirectory();
        }


        File file = new File(locUpdateDir);
        File[] files = file.listFiles();
        if (files != null) {
            for(File f: files){
                String[] fileNameSplitter =  f.getName().split("_");
                if(Objects.equals(fileNameSplitter[3], stateId.toString()) && fileNameSplitter[4].equalsIgnoreCase(rchUserType.toString())){
                    try {
                        FileInputStream input = new FileInputStream(f);
                        MultipartFile multipartFile = new MockMultipartFile("file",
                                f.getName(), "text/plain", IOUtils.toByteArray(input));
                        csvFilesByStateIdAndRchUserType.add(multipartFile);
                    }catch(IOException e) {
                        LOGGER.debug("IO Exception", e);
                    }

                }
            }
        }

        return csvFilesByStateIdAndRchUserType;
    }


    private Map<String, CellProcessor> getRchAshaProcessorMapping() {
        Map<String, CellProcessor> mapping = new HashMap<>();

        mapping.put(FlwConstants.ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(FlwConstants.STATE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));

        mapping.put(FlwConstants.DISTRICT_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(FlwConstants.DISTRICT_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));

        mapping.put(FlwConstants.TALUKA_ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        mapping.put(FlwConstants.TALUKA_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));

        mapping.put(FlwConstants.CENSUS_VILLAGE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(FlwConstants.VILLAGE_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));

        mapping.put(FlwConstants.PHC_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(FlwConstants.PHC_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));

        mapping.put(FlwConstants.HEALTH_BLOCK_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(FlwConstants.HEALTH_BLOCK_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));

        mapping.put(FlwConstants.SUB_CENTRE_ID, new org.supercsv.cellprocessor.Optional(new GetLong()));
        mapping.put(FlwConstants.SUB_CENTRE_NAME, new org.supercsv.cellprocessor.Optional(new GetString()));


        return mapping;
    }

    private Map<String, CellProcessor> getKilkariProcessorMapping(String origin) {
        Map<String, CellProcessor> mapping = new HashMap<>();

        MctsBeneficiaryUtils.getBeneficiaryLocationMapping(mapping);
        if(MCTS.equalsIgnoreCase(origin)){
            mapping.put(KilkariConstants.BENEFICIARY_ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        } else {
            mapping.put(KilkariConstants.RCH_ID, new org.supercsv.cellprocessor.Optional(new GetString()));
        }
        return mapping;
    }

    private Long rchBulkUpdateAsha(final List<Map<String, Object>> updateObjects, final RchUserType rchUserType, final String origin) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_front_line_workers (mctsFlwId, state_id_OID, district_id_OID, taluka_id_OID," +
                        " healthBlock_id_OID, healthFacility_id_OID, healthSubFacility_id_OID, village_id_OID," +
                        " modifiedBy, modificationDate)  " +
                        "values  " +
                        updateQuerySet(updateObjects, rchUserType, origin) +
                        " ON DUPLICATE KEY UPDATE " +
                        "state_id_OID = VALUES(state_id_OID), district_id_OID = VALUES(district_id_OID)," +
                        " taluka_id_OID = VALUES(taluka_id_OID), healthBlock_id_OID = VALUES(healthBlock_id_OID)," +
                        " healthFacility_id_OID = VALUES(healthFacility_id_OID), healthSubFacility_id_OID = VALUES(healthSubFacility_id_OID)," +
                        " village_id_OID = VALUES(village_id_OID), modifiedBy = VALUES(modifiedBy), modificationDate = VALUES(modificationDate)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(FrontLineWorker.class);
                return (Long) query.execute();
            }
        };

        Long updatedNo = frontLineWorkerDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }

    private Long mctsBulkUpdateAsha(final List<Map<String, Object>> updateObjects, final RchUserType rchUserType, final String origin) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_front_line_workers (flwId, state_id_OID, district_id_OID, taluka_id_OID," +
                        " healthBlock_id_OID, healthFacility_id_OID, healthSubFacility_id_OID, village_id_OID," +
                        " modifiedBy, modificationDate)  " +
                        "values  " +
                        updateQuerySet(updateObjects, rchUserType, origin) +
                        " ON DUPLICATE KEY UPDATE " +
                        "state_id_OID = VALUES(state_id_OID), district_id_OID = VALUES(district_id_OID)," +
                        " taluka_id_OID = VALUES(taluka_id_OID), healthBlock_id_OID = VALUES(healthBlock_id_OID)," +
                        " healthFacility_id_OID = VALUES(healthFacility_id_OID), healthSubFacility_id_OID = VALUES(healthSubFacility_id_OID)," +
                        " village_id_OID = VALUES(village_id_OID), modifiedBy = VALUES(modifiedBy), modificationDate = VALUES(modificationDate)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(FrontLineWorker.class);
                return (Long) query.execute();
            }
        };

        Long updatedNo = frontLineWorkerDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }


    private Long rchBulkUpdateChild(final List<Map<String, Object>> updateObjects, final RchUserType rchUserType, final String origin) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mcts_children (rchId, state_id_OID, district_id_OID, taluka_id_OID," +
                        " healthBlock_id_OID, primaryHealthCenter_id_OID, healthSubFacility_id_OID, village_id_OID," +
                        " modifiedBy, modificationDate)  " +
                        "values  " +
                        updateQuerySet(updateObjects, rchUserType, origin) +
                        " ON DUPLICATE KEY UPDATE " +
                        "state_id_OID = VALUES(state_id_OID), district_id_OID = VALUES(district_id_OID)," +
                        " taluka_id_OID = VALUES(taluka_id_OID), healthBlock_id_OID = VALUES(healthBlock_id_OID)," +
                        " primaryHealthCenter_id_OID = VALUES(primaryHealthCenter_id_OID), healthSubFacility_id_OID = VALUES(healthSubFacility_id_OID)," +
                        " village_id_OID = VALUES(village_id_OID), modifiedBy = VALUES(modifiedBy), modificationDate = VALUES(modificationDate)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(MctsChild.class);
                return (Long) query.execute();
            }
        };

        Long updatedNo = mctsChildDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }

    private Long mctsBulkUpdateChild(final List<Map<String, Object>> updateObjects, final RchUserType rchUserType, final String origin) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mcts_children (beneficiaryId, state_id_OID, district_id_OID, taluka_id_OID," +
                        " healthBlock_id_OID, primaryHealthCenter_id_OID, healthSubFacility_id_OID, village_id_OID," +
                        " modifiedBy, modificationDate)  " +
                        "values  " +
                        updateQuerySet(updateObjects, rchUserType, origin) +
                        " ON DUPLICATE KEY UPDATE " +
                        "state_id_OID = VALUES(state_id_OID), district_id_OID = VALUES(district_id_OID)," +
                        " taluka_id_OID = VALUES(taluka_id_OID), healthBlock_id_OID = VALUES(healthBlock_id_OID)," +
                        " primaryHealthCenter_id_OID = VALUES(primaryHealthCenter_id_OID), healthSubFacility_id_OID = VALUES(healthSubFacility_id_OID)," +
                        " village_id_OID = VALUES(village_id_OID), modifiedBy = VALUES(modifiedBy), modificationDate = VALUES(modificationDate)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(MctsChild.class);
                return (Long) query.execute();
            }
        };

        Long updatedNo = mctsChildDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }

    private Long rchBulkUpdateMother(final List<Map<String, Object>> updateObjects, final RchUserType rchUserType, final String origin) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mcts_mothers (rchId, state_id_OID, district_id_OID, taluka_id_OID," +
                        " healthBlock_id_OID, primaryHealthCenter_id_OID, healthSubFacility_id_OID, village_id_OID," +
                        " modifiedBy, modificationDate)  " +
                        "values  " +
                        updateQuerySet(updateObjects, rchUserType, origin) +
                        " ON DUPLICATE KEY UPDATE " +
                        "state_id_OID = VALUES(state_id_OID), district_id_OID = VALUES(district_id_OID)," +
                        " taluka_id_OID = VALUES(taluka_id_OID), healthBlock_id_OID = VALUES(healthBlock_id_OID)," +
                        " primaryHealthCenter_id_OID = VALUES(primaryHealthCenter_id_OID), healthSubFacility_id_OID = VALUES(healthSubFacility_id_OID)," +
                        " village_id_OID = VALUES(village_id_OID), modifiedBy = VALUES(modifiedBy), modificationDate = VALUES(modificationDate)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(MctsMother.class);
                return (Long) query.execute();
            }
        };

        Long updatedNo = mctsMotherDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }

    private Long mctsBulkUpdateMother(final List<Map<String, Object>> updateObjects, final RchUserType rchUserType, final String origin) {
        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                String query = "INSERT INTO nms_mcts_mothers (beneficiaryId, state_id_OID, district_id_OID, taluka_id_OID," +
                        " healthBlock_id_OID, primaryHealthCenter_id_OID, healthSubFacility_id_OID, village_id_OID," +
                        " modifiedBy, modificationDate)  " +
                        "values  " +
                        updateQuerySet(updateObjects, rchUserType, origin) +
                        " ON DUPLICATE KEY UPDATE " +
                        "state_id_OID = VALUES(state_id_OID), district_id_OID = VALUES(district_id_OID)," +
                        " taluka_id_OID = VALUES(taluka_id_OID), healthBlock_id_OID = VALUES(healthBlock_id_OID)," +
                        " primaryHealthCenter_id_OID = VALUES(primaryHealthCenter_id_OID), healthSubFacility_id_OID = VALUES(healthSubFacility_id_OID)," +
                        " village_id_OID = VALUES(village_id_OID), modifiedBy = VALUES(modifiedBy), modificationDate = VALUES(modificationDate)";

                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }

            @Override
            public Long execute(Query query) {

                query.setClass(MctsMother.class);
                return (Long) query.execute();
            }
        };

        Long updatedNo = mctsMotherDataService.executeSQLQuery(queryExecution);
        LOGGER.debug(CHILD_LOG_STRING, queryTimer.time());
        return updatedNo;
    }


    private Long bulkUpdate(List<Map<String, Object>> updateObjects, RchUserType rchUserType, String origin) {
        int count = 0;
        Long sqlCount = 0L;
        while (count < updateObjects.size()) {
            List<Map<String, Object>> updateObjectsPart = new ArrayList<>();
            while (updateObjectsPart.size() < PARTITION_SIZE && count < updateObjects.size()) {
                updateObjectsPart.add(updateObjects.get(count));
                count++;
            }

            if(rchUserType == RchUserType.MOTHER ){
                if (MCTS.equalsIgnoreCase(origin)){
                    sqlCount += mctsBulkUpdateMother(updateObjects, rchUserType, origin);
                } else {
                    sqlCount += rchBulkUpdateMother(updateObjects, rchUserType, origin);
                }
            } else if (rchUserType == RchUserType.CHILD){
                if (MCTS.equalsIgnoreCase(origin)){
                    sqlCount += mctsBulkUpdateChild(updateObjects, rchUserType, origin);
                } else {
                    sqlCount += rchBulkUpdateChild(updateObjects, rchUserType, origin);
                }
            } else{
                if (MCTS.equalsIgnoreCase(origin)){
                    sqlCount += mctsBulkUpdateAsha(updateObjects, rchUserType, origin);
                } else {
                    sqlCount += rchBulkUpdateAsha(updateObjects, rchUserType, origin);
                }
            }

            updateObjectsPart.clear();
        }
        return sqlCount;
    }

    private String updateQuerySet(List<Map<String, Object>> updateObjects, RchUserType rchUserType, String origin) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;

        for (Map<String, Object> object: updateObjects) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append("(");
            if (rchUserType == RchUserType.MOTHER || rchUserType == RchUserType.CHILD) {
                stringBuilder = addLocationsKilkari(stringBuilder, object, origin);
            } else {
                stringBuilder = addLocationsFlw(stringBuilder, object);
            }
            stringBuilder.append(")");
            i++;
        }
        return stringBuilder.toString();
    }

    private StringBuilder addLocationsFlw(StringBuilder stringBuilder, Map<String, Object> updateObject) {
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        stringBuilder.append(QUOTATION + updateObject.get(FlwConstants.ID) + QUOTATION_COMMA);
        stringBuilder.append(updateObject.get(FlwConstants.STATE_ID) + ", ");
        stringBuilder.append(updateObject.get(FlwConstants.DISTRICT_ID) + ", ");
        stringBuilder.append(updateObject.get(FlwConstants.TALUKA_ID) + ", ");
        stringBuilder.append(updateObject.get(FlwConstants.HEALTH_BLOCK_ID) + ", ");
        stringBuilder.append(updateObject.get(FlwConstants.PHC_ID) + ", ");
        stringBuilder.append(updateObject.get(FlwConstants.SUB_CENTRE_ID) + ", ");
        stringBuilder.append(updateObject.get(FlwConstants.CENSUS_VILLAGE_ID) + ", ");
        stringBuilder.append(MOTECH_STRING);
        stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
        return stringBuilder;
    }

    private StringBuilder addLocationsKilkari(StringBuilder stringBuilder, Map<String, Object> updateObject, String origin) {
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        if(MCTS.equalsIgnoreCase(origin)){
            stringBuilder.append(QUOTATION + updateObject.get(KilkariConstants.BENEFICIARY_ID) + QUOTATION_COMMA);
        } else {
            stringBuilder.append(QUOTATION + updateObject.get(KilkariConstants.RCH_ID) + QUOTATION_COMMA);
        }
        stringBuilder.append(updateObject.get(KilkariConstants.STATE_ID) + ", ");
        stringBuilder.append(updateObject.get(KilkariConstants.DISTRICT_ID) + ", ");
        stringBuilder.append(updateObject.get(KilkariConstants.TALUKA_ID) + ", ");
        stringBuilder.append(updateObject.get(KilkariConstants.HEALTH_BLOCK_ID) + ", ");
        stringBuilder.append(updateObject.get(KilkariConstants.PHC_ID) + ", ");
        stringBuilder.append(updateObject.get(KilkariConstants.SUB_CENTRE_ID) + ", ");
        stringBuilder.append(updateObject.get(KilkariConstants.CENSUS_VILLAGE_ID) + ", ");
        stringBuilder.append(MOTECH_STRING);
        stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
        return stringBuilder;
    }


}

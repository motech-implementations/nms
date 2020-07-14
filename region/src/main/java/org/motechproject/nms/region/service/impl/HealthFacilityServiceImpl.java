package org.motechproject.nms.region.service.impl;

import org.apache.commons.lang.StringEscapeUtils;
import org.datanucleus.store.rdbms.query.ForwardQueryResult;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.SqlQueryExecution;
import org.motechproject.metrics.service.Timer;
import org.motechproject.nms.region.domain.*;
import org.motechproject.nms.region.repository.HealthFacilityDataService;
import org.motechproject.nms.region.service.HealthFacilityService;
import org.motechproject.nms.region.utils.LocationConstants;
import org.motechproject.nms.rejectionhandler.domain.HealthFacilityImportRejection;
import org.motechproject.nms.rejectionhandler.service.HealthFacilityRejectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jdo.Query;
import javax.jdo.annotations.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service("healthFacilityService")
public class HealthFacilityServiceImpl implements HealthFacilityService {

    private static final String QUOTATION = "'";
    private static final String QUOTATION_COMMA = "', ";
    private static final String MOTECH_STRING = "'motech', ";
    private static final String SQL_QUERY_LOG = "SQL QUERY: {}";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss";

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthFacilityServiceImpl.class);


    @Autowired
    private HealthFacilityDataService dataService;

    @Autowired
    private HealthFacilityRejectionService healthFacilityRejectionService;

    @Override
    public HealthFacility findByHealthBlockAndCode(final HealthBlock healthBlock, final Long code) {
        if (healthBlock == null) { return null; }

        SqlQueryExecution<HealthFacility> queryExecution = new SqlQueryExecution<HealthFacility>() {

            @Override
            public String getSqlQuery() {
                return "select *  from nms_health_facilities where healthBlock_id_oid = ? and code = ?";
            }

            @Override
            public HealthFacility execute(Query query) {
                query.setClass(HealthFacility.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute(healthBlock.getId(), code);
                if (fqr.isEmpty()) {
                    return null;
                }
                if (fqr.size() == 1) {
                    return (HealthFacility) fqr.get(0);
                }
                throw new IllegalStateException("More than one row returned!");
            }
        };

        return dataService.executeSQLQuery(queryExecution);
    }

    @Override
    public HealthFacility create(HealthFacility healthFacility) {
        return dataService.create(healthFacility);
    }

    @Override
    public HealthFacility update(HealthFacility healthFacility) {
        return dataService.update(healthFacility);
    }

    @Override
    @Transactional
    public Long createUpdateHealthFacilities(final List<Map<String, Object>> healthFacilities, final Map<String, State> stateHashMap, final Map<String, District> districtHashMap, final Map<String, Taluka> talukaHashMap, final Map<String, HealthBlock> healthBlockHashMap) {
        LOGGER.debug("Inside createUpdateHealthFacilities");
        SqlQueryExecution<Long> queryExecution = new SqlQueryExecution<Long>() {

            @Override
            public String getSqlQuery() {
                LOGGER.debug("Inside getSqlQuery");

                String healthFacilityValues = healthFacilityQuerySet(healthFacilities, stateHashMap, districtHashMap, talukaHashMap, healthBlockHashMap);
                String query = "";
                if (!healthFacilityValues.isEmpty()) {
                    query = "INSERT into nms_health_facilities (`code`, `name`, `state_id_OID`, `district_id_OID`, `healthBlock_id_OID`, `taluka_id_oid`, " +
                            " `creator`, `modifiedBy`, `owner`, `creationDate`, `modificationDate`) VALUES " +
                            healthFacilityValues +
                            " ON DUPLICATE KEY UPDATE " +
                            "name = VALUES(name), district_id_OID = VALUES(district_id_OID), healthBlock_id_OID = VALUES(healthBlock_id_OID), taluka_id_oid = VALUES(taluka_id_oid), modificationDate = VALUES(modificationDate), modifiedBy = VALUES(modifiedBy) ";
                }
                LOGGER.debug(SQL_QUERY_LOG, query);
                return query;
            }



            @Override
            public Long execute(Query query) {
                query.setClass(HealthFacility.class);
                return (Long) query.execute();
            }
        };

        Long createdHealthFacilities = 0L;
        if (!talukaHashMap.isEmpty() && !healthBlockHashMap.isEmpty() && !queryExecution.getSqlQuery().isEmpty()) {
            createdHealthFacilities = dataService.executeSQLQuery(queryExecution);
        }

        return createdHealthFacilities;
    }


    @Override //NO CHECKSTYLE Cyclomatic Complexity
    public Map<String, HealthFacility> fillHealthFacilitiesFromTalukas(List<Map<String, Object>> recordList, final Map<String, Taluka> talukaHashMap) {
        final Set<String> healthFacilityKeys = new HashSet<>();
        for(Map<String, Object> record : recordList) {
            if (record.get(LocationConstants.CSV_STATE_ID) != null && record.get(LocationConstants.DISTRICT_ID) != null
                    && record.get(LocationConstants.TALUKA_ID) != null && record.get(LocationConstants.HEALTHFACILITY_ID) != null) {
                healthFacilityKeys.add(record.get(LocationConstants.CSV_STATE_ID).toString() + "_" + record.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        record.get(LocationConstants.TALUKA_ID).toString().trim() + "_" +
                        record.get(LocationConstants.HEALTHFACILITY_ID).toString());
            }
        }
        Map<String, HealthFacility> healthFacilityHashMap = new HashMap<>();

        Map<Long, String> talukaIdMap = new HashMap<>();
        for (String talukaKey : talukaHashMap.keySet()) {
            talukaIdMap.put(talukaHashMap.get(talukaKey).getId(), talukaKey);
        }

        Timer queryTimer = new Timer();

        @SuppressWarnings("unchecked")
        SqlQueryExecution<List<HealthFacility>> queryExecution = new SqlQueryExecution<List<HealthFacility>>() {

            @Override
            public String getSqlQuery() {
                String query = "SELECT * from nms_health_facilities where";
                int count = healthFacilityKeys.size();
                for (String healthFacilityString : healthFacilityKeys) {
                    String[] ids = healthFacilityString.split("_");
                    Taluka taluka = talukaHashMap.get(ids[0] + "_" + ids[1] + "_" + ids[2]);
                    if (taluka != null && taluka.getId() != null) {
                        if (count != healthFacilityKeys.size()) {
                            query += LocationConstants.OR_SQL_STRING;
                        }
                        query += LocationConstants.CODE_SQL_STRING + ids[3] + " and taluka_id_oid = " + taluka.getId() + ")";
                        count--;
                    }
                }

                LOGGER.debug("HEALTHFACILITY Query: {}", query);
                return query;
            }

            @Override
            public List<HealthFacility> execute(Query query) {
                query.setClass(HealthFacility.class);
                ForwardQueryResult fqr = (ForwardQueryResult) query.execute();
                List<HealthFacility> healthFacilities;
                if (fqr.isEmpty()) {
                    return null;
                }
                healthFacilities = (List<HealthFacility>) fqr;
                return healthFacilities;
            }
        };

        List<HealthFacility> healthFacilities = null;
        if (!talukaHashMap.isEmpty() && !healthFacilityKeys.isEmpty()) {
            healthFacilities = dataService.executeSQLQuery(queryExecution);
        }
        LOGGER.debug("HEALTHFACILITY Query time: {}", queryTimer.time());
        if(healthFacilities != null && !healthFacilities.isEmpty()) {
            for (HealthFacility healthFacility : healthFacilities) {
                String talukaKey = talukaIdMap.get(healthFacility.getTalukaIdOID());
                healthFacilityHashMap.put(talukaKey + "_" + healthFacility.getCode(), healthFacility);
            }
        }
        return healthFacilityHashMap;
    }

    private String healthFacilityQuerySet(List<Map<String, Object>> healthFacilities, Map<String, State> stateHashMap, Map<String, District> districtHashMap, Map<String, Taluka> talukaHashMap, Map<String, HealthBlock> healthBlockHashMap) { //NO CHECKSTYLE Cyclomatic Complexity
        LOGGER.debug("Inside healthFacilityQuerySet");

        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        DateTime dateTimeNow = new DateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT_STRING);
        for (Map<String, Object> healthFacility : healthFacilities) {
            if (healthFacility.get(LocationConstants.CSV_STATE_ID) != null && healthFacility.get(LocationConstants.DISTRICT_ID) != null &&
                    healthFacility.get(LocationConstants.TALUKA_ID) != null && !healthFacility.get(LocationConstants.TALUKA_ID).toString().trim().isEmpty() && healthFacility.get(LocationConstants.HEALTHBLOCK_ID) != null) {
                State state = stateHashMap.get(healthFacility.get(LocationConstants.CSV_STATE_ID).toString());
                District district = districtHashMap.get(healthFacility.get(LocationConstants.CSV_STATE_ID).toString() + "_" + healthFacility.get(LocationConstants.DISTRICT_ID).toString());
                Taluka taluka = talukaHashMap.get(healthFacility.get(LocationConstants.CSV_STATE_ID).toString() + "_" +
                        healthFacility.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        healthFacility.get(LocationConstants.TALUKA_ID).toString().trim());
                HealthBlock healthBlock = healthBlockHashMap.get(healthFacility.get(LocationConstants.CSV_STATE_ID).toString() + "_" +
                        healthFacility.get(LocationConstants.DISTRICT_ID).toString() + "_" +
                        healthFacility.get(LocationConstants.HEALTHBLOCK_ID).toString());
                Long healthFacilityCode = (Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID);
                String healthFacilityName = (String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME);
                LOGGER.debug("Entering Import/Rejection");

                if (taluka != null && healthBlock != null && healthFacilityCode != null) {
                    if (!((Long) (0L)).equals(healthFacilityCode) && (healthFacilityName != null && !healthFacilityName.trim().isEmpty()) && healthBlock.getDistrict().getId().equals(taluka.getDistrict().getId())) {
                        if (i != 0) {
                            stringBuilder.append(", ");
                        }
                        stringBuilder.append("(");
                        stringBuilder.append(healthFacilityCode + ", ");
                        stringBuilder.append(QUOTATION +
                                StringEscapeUtils.escapeSql(healthFacility.get(LocationConstants.HEALTHFACILITY_NAME) == null ?
                                        "" : healthFacility.get(LocationConstants.HEALTHFACILITY_NAME).toString()) + QUOTATION_COMMA);
                        stringBuilder.append(state.getId() + ", ");
                        stringBuilder.append(district.getId() + ", ");
                        stringBuilder.append(healthBlock.getId() + ", ");
                        stringBuilder.append(taluka.getId() + ", ");
                        stringBuilder.append(MOTECH_STRING);
                        stringBuilder.append(MOTECH_STRING);
                        stringBuilder.append(MOTECH_STRING);
                        stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION_COMMA);
                        stringBuilder.append(QUOTATION + dateTimeFormatter.print(dateTimeNow) + QUOTATION);
                        stringBuilder.append(")");

                        i++;
                    }
                    else if(((Long) (0L)).equals(healthFacilityCode)){
                        LOGGER.debug("rejection reason Location code zero");
                        HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.LOCATION_CODE_ZERO_IN_FILE.toString());
                        healthFacilityRejectionService.saveRejectedHealthFacility(healthFacilityImportRejection);
                    }
                    else if(!((Long) (0L)).equals(healthFacilityCode) && (healthFacilityName == null || healthFacilityName.trim().isEmpty())){
                        LOGGER.debug("rejection reason Location name not present");
                        HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.LOCATION_NAME_NOT_PRESENT_IN_FILE.toString());
                        healthFacilityRejectionService.saveRejectedHealthFacility(healthFacilityImportRejection);
                    }
                    else if(!((Long) (0L)).equals(healthFacilityCode) && healthFacilityName != null && !healthFacilityName.trim().isEmpty() && !(healthBlock.getDistrict().getId().equals(taluka.getDistrict().getId()))){
                        LOGGER.debug("rejection reason Location code zero");
                        HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.PARENT_TALUKA_AND_HEALTH_BLOCK_NOT_IN_SAME_DISTRICT.toString());
                        healthFacilityRejectionService.saveRejectedHealthFacility(healthFacilityImportRejection);
                    }

                }
                else if(healthFacilityCode == null){
                    LOGGER.debug("rejection reason Location code null");
                    HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.LOCATION_CODE_NOT_PRESENT_IN_FILE.toString());
                    healthFacilityRejectionService.createRejectedHealthFacility(healthFacilityImportRejection);
                }
                else if(taluka == null || healthBlock == null ) {
                    if(district!=null && taluka==null){
                        LOGGER.debug("taluka rejection reason Parent Location code null");
                        HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.TALUKA_ID_NOT_PRESENT_IN_DB.toString());
                        healthFacilityRejectionService.saveRejectedHealthFacility(healthFacilityImportRejection);
                    }
                    else {
                        LOGGER.debug("rejection reason Parent Location code null");
                        HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.PARENT_LOCATION_NOT_PRESENT_IN_DB.toString());
                        healthFacilityRejectionService.saveRejectedHealthFacility(healthFacilityImportRejection);
                    }

                }

            }
            else if(healthFacility.get(LocationConstants.CSV_STATE_ID) == null){

                HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.PARENT_LOCATION_ID_NOT_PRESENT_IN_FILE.toString());
                healthFacilityRejectionService.createRejectedHealthFacility(healthFacilityImportRejection);
            }
            else if(healthFacility.get(LocationConstants.TALUKA_ID) == null || healthFacility.get(LocationConstants.TALUKA_ID).toString().trim().isEmpty()){
                HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.TALUKA_ID_NOT_PRESENT_IN_FILE.toString());
                healthFacilityRejectionService.saveRejectedHealthFacility(healthFacilityImportRejection);
            }
            else {
                LOGGER.debug("rejection reason Parent Location code not in db");
                HealthFacilityImportRejection healthFacilityImportRejection = new HealthFacilityImportRejection((Long)healthFacility.get(LocationConstants.CSV_STATE_ID),(Long)healthFacility.get(LocationConstants.DISTRICT_ID),(String)healthFacility.get(LocationConstants.TALUKA_ID),(Long)healthFacility.get(LocationConstants.HEALTHBLOCK_ID),(Long) healthFacility.get(LocationConstants.HEALTHFACILITY_ID),(String) healthFacility.get(LocationConstants.HEALTHFACILITY_NAME),false,LocationRejectionReasons.PARENT_LOCATION_ID_NOT_PRESENT_IN_FILE.toString());
                healthFacilityRejectionService.saveRejectedHealthFacility(healthFacilityImportRejection);
            }
        }

        return stringBuilder.toString();
    }
}
